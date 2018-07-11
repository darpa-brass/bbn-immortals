package mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;
import mil.darpa.immortals.core.das.adaptationmodules.IAdaptationModule;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.core.das.sparql.adaptationtargets.DetermineCrossAppReplacementFiles;
import mil.darpa.immortals.core.das.sparql.deploymentmodel.DetermineCipherResources;
import mil.darpa.immortals.core.das.sparql.deploymentmodel.DetermineCrossAppApplicability;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by awellman@bbn.com on 6/22/18.
 */
public class CrossAppDependenciesAdapter implements IAdaptationModule {

    private static final Logger logger = LoggerFactory.getLogger(CrossAppDependenciesAdapter.class);

    private WebTarget repositoryService;

    private List<DetermineCrossAppApplicability.CrossAppApplicabilityDetails> partialSolutionData;

    @Override
    public boolean isApplicable(DasAdaptationContext context) throws Exception {
        List<DetermineCrossAppApplicability.CrossAppApplicabilityDetails> vals = DetermineCrossAppApplicability.select(context);
        if (vals == null || vals.isEmpty()) {
            return false;
        } else {
            partialSolutionData = vals;
            return true;
        }
    }

    @Override
    public void apply(DasAdaptationContext context) throws Exception {
        if (partialSolutionData == null) {
            isApplicable(context);
        }

        // TODO: No hard-coding!
        String clientIdentifier = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/client/ATAKLite/").toString();
        String serverIdentifier = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/server/Marti/").toString();

        // TODO: No hard-coding!
        String javaxDfuIdentifier = partialSolutionData.stream().filter(t -> t.dfuArtifactId.equals("JavaxCrypto")).map(t -> t.dfuInstance).findFirst().get();
        String bcDfuIdentifier = partialSolutionData.stream().filter(t -> t.dfuArtifactId.equals("BouncyCastleCipher")).map(t -> t.dfuInstance).findFirst().get();

        // Determine resources
        Map<String, DetermineCipherResources.CipherResourceDetails> cipherResourceDetails = DetermineCipherResources.select(context);
        DetermineCipherResources.CipherResourceDetails clientDetails = cipherResourceDetails.get(clientIdentifier);
        DetermineCipherResources.CipherResourceDetails serverDetails = cipherResourceDetails.get(serverIdentifier);

        // Construct a partial configuration
        CipherConfiguration partialConfiguration = new CipherConfiguration(
                serverDetails.hasAESNI, serverDetails.hasUCS, clientDetails.hasAESNI, clientDetails.hasUCS,
                partialSolutionData.get(0).algorithm, partialSolutionData.get(0).keyLength, partialSolutionData.get(0).paddingScheme, partialSolutionData.get(0).chainingMode);

        // Feed it to the DSL and get back complete candidate configurations
        DslAdapter da = DslAdapter.getInstance();
        Exception javaxException = null;
        Exception bcException = null;
        List<CipherConfiguration> configs = new LinkedList<>();
        try {
            List<CipherConfiguration> javaxConfigs = da.query(context.getAdaptationIdentifer(), partialConfiguration, true);
            configs.addAll(javaxConfigs);
        } catch (Exception e) {
            javaxException = e;
        }
        try {
            List<CipherConfiguration> bouncyCastleConfigs = da.query(context.getAdaptationIdentifer(), partialConfiguration, false);
            configs.addAll(bouncyCastleConfigs);
        } catch (Exception e) {
            bcException = e;
        }
        
        Random r = new Random(345312544354312L);
        Collections.shuffle(configs, r);
        
        // Then insert each Solution into a model
        Model m = ModelFactory.createDefaultModel();
        for (CipherConfiguration solution : configs) {
            AspectConfigureSolutionBuilder sb = new AspectConfigureSolutionBuilder(solution, m, javaxDfuIdentifier, bcDfuIdentifier);
            sb.buildSolution();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        m.write(out, "TURTLE");

        // Save that model for reference
        String graph = new String(out.toByteArray());
        Files.write(
                ImmortalsConfig.getInstance().globals.getAdaptationLogDirectory(
                        context.getAdaptationIdentifer()).resolve("cp2Solutions.ttl"),
                out.toByteArray());

        // Append it to the knowledge repo
        String graphIdentifier = context.getKnowldgeUri().replaceAll("http://localhost:3030/ds/data/", "");
        repositoryService = ClientBuilder.newClient(new ClientConfig()
                .register(JacksonFeature.class))
                .target(ImmortalsConfig.getInstance().knowledgeRepoService.getFullUrl().resolve("/krs/").toString());
        String newGraph = repositoryService.path("append/" + graphIdentifier).request().post(
                Entity.entity(graph, MediaType.TEXT_PLAIN), String.class);

        // Perform the adaptation with the new information
        String newGraphUri = repositoryService.path("adapt/" + newGraph).request().post(null, String.class);

        // Get a map of the projects and files and contents that must be replaced or added
        Map<String, DetermineCrossAppReplacementFiles.CrossAppReplacementData> replacementDataMap =
                DetermineCrossAppReplacementFiles.select(context);

        // And add them to the build instances
        for (Map.Entry<String, DetermineCrossAppReplacementFiles.CrossAppReplacementData> appEntry : replacementDataMap.entrySet()) {
            AdaptationTargetBuildInstance buildInstance = GradleKnowledgeBuilder.getBuildInstance(appEntry.getKey(), context.getAdaptationIdentifer());
            Path sourceRoot = buildInstance.getSourceRoot();
            DetermineCrossAppReplacementFiles.CrossAppReplacementData replacementData = appEntry.getValue();

            // EHACK: Not this. This should be handled by KR
            Set<String> keys = replacementData.getFilePathReplacementSource().keySet();
            for (String key : keys) {
                String value = replacementData.getFilePathReplacementSource().get(key);
                if (key.endsWith("Dispatcher.java")) {
                    String newValue = value.replaceAll("private SocketChannel socketChannel;",
                            "private WrapperSocketChannel socketChannel;");

                    if (!value.equals(newValue)) {
                        logger.warn("KRHACK: Removing Un-reclassed socketChannel!");
                    }
                    value = newValue;
                    replacementData.getFilePathReplacementSource().replace(key, value);

                } else if (key.endsWith("WrapperSocketChannel.java")) {
                    value = removeBadChunks(value);
                    replacementData.getFilePathReplacementSource().replace(key, value);

                }
                Files.write(sourceRoot.resolve(key), value.getBytes());
            }

            boolean buildSuccess = buildInstance.executeCleanAndBuild();
            DasOutcome outcome = buildSuccess ? DasOutcome.SUCCESS : DasOutcome.ERROR;


            AdaptationDetails update = new AdaptationDetails(
                    getClass().getName(),
                    outcome,
                    context.getAdaptationIdentifer());
            context.submitAdaptationStatus(update);

        }
    }

    private static String removeBadChunks(String value) {
        String[] chunks = {
                "    @Override\n    public SocketChannel bind(final SocketAddress socketAddress) throws IOException {\n        return ( this.socketchannel).bind(socketAddress);\n    }\n",
                "    @Override\n    public SocketChannel setOption(final SocketOption socketOption, final Object o) throws IOException {\n        return ( this.socketchannel).setOption(socketOption, o);\n    }\n",
                "    @Override\n    public SocketChannel shutdownInput() throws IOException {\n        return ( this.socketchannel).shutdownInput();\n    }\n",
                "    @Override\n    public SocketChannel shutdownOutput() throws IOException {\n        return ( this.socketchannel).shutdownOutput();\n    }\n",
                "    @Override\n    public SocketAddress getRemoteAddress() throws IOException {\n        return ( this.socketchannel).getRemoteAddress();\n    }\n",
                "    @Override\n    public SocketAddress getLocalAddress() throws IOException {\n        return ( this.socketchannel).getLocalAddress();\n    }\n"
        };

        for (String val : chunks) {
            String newValue = value.replace(val, "");
            if (!value.equals(newValue)) {
                logger.warn("KRHACK: Removing overridden methods that do not exist in Android 21!");
            }
            value = newValue;
        }
        return value;
    }
}
