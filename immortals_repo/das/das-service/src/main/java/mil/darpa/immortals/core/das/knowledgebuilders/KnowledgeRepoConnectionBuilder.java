package mil.darpa.immortals.core.das.knowledgebuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mil.darpa.immortals.analysis.adaptationtargets.DeploymentTarget;
import mil.darpa.immortals.analysis.adaptationtargets.ImmortalsGradleProjectData;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies.Prefix;
import mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies.Res;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.ARQ;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by awellman@bbn.com on 7/2/18.
 */
public class KnowledgeRepoConnectionBuilder implements IKnowledgeBuilder {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public KnowledgeRepoConnectionBuilder() {

    }

    private static String getDfuResourceUri(Path sourceFilepath) throws IOException {
        Model m = ModelFactory.createDefaultModel();
        InputStream is = new FileInputStream(sourceFilepath.toFile());
        RDFDataMgr.read(m, is, Lang.TTL);

        List<Resource> resources = m.listResourcesWithProperty(RDF.type, m.getResource(Res.DFU_INSTANCE.uri)).toList();

        if (resources.size() != 1) {
            throw new RuntimeException("There should only be one DfuInstance in the JavaX Dfu file!");
        }

        return resources.get(0).getURI();
    }


    @Override
    public Model buildKnowledge(Map<String, Object> parameters) throws Exception {
        // KRHACK: This should be handled by KR analysis somehow... 
        Path krgpAnalysisPath = ImmortalsConfig.getInstance().extensions.krgp.getTtlTargetDirectory();
        Path ttlIngestionDirectory = ImmortalsConfig.getInstance().globals.getTtlIngestionDirectory();

        // Set the DfuInstance identifiers
        String javaxResourceUri = getDfuResourceUri(
                krgpAnalysisPath.resolve("JavaxCrypto/structures/dfus/CipherImplJavaxCrypto.class-DFU.ttl"))
                .replaceAll(Prefix.IMMoRTALS_dfu_instance.uri, Prefix.IMMoRTALS_dfu_instance.name() + ":");


        String bcResourceUri = getDfuResourceUri(
                krgpAnalysisPath.resolve("BouncyCastleCipher/structures/dfus/CipherImplBouncyCrypto.class-DFU.ttl"))
                .replaceAll(Prefix.IMMoRTALS_dfu_instance.uri, Prefix.IMMoRTALS_dfu_instance.name() + ":");

        String noopResourceUri = getDfuResourceUri(
                krgpAnalysisPath.resolve("NoOpCipher/structures/dfus/CipherImplNoop.class-DFU.ttl"))
                .replaceAll(Prefix.IMMoRTALS_dfu_instance.uri, Prefix.IMMoRTALS_dfu_instance.name() + ":");

        InputStream is = this.getClass().getResourceAsStream("/cipherUsageParadigms.ttl");
        List<String> fileLines = IOUtils.readLines(is);

        List<String> outputLines = new ArrayList<>(fileLines.size());

        for (String str : fileLines) {
            str = str.replaceAll("<\\?\\?\\?NOOP_DFU\\?\\?\\?>", noopResourceUri);
            str = str.replaceAll("<\\?\\?\\?BC_DFU\\?\\?\\?>", bcResourceUri);
            str = str.replaceAll("<\\?\\?\\?JAVAX_DFU\\?\\?\\?>", javaxResourceUri);
            outputLines.add(str);
        }

        InputStream atakmartiDuplexInputStream = this.getClass().getResourceAsStream("/atakMartiDuplex.ttl");
        List<String> atakMartiDuplexLines = IOUtils.readLines(atakmartiDuplexInputStream);
        Path atakmartiDuplexTargetPath = ttlIngestionDirectory.resolve("atakMartiDuplex.ttl");
        if (Files.exists(atakmartiDuplexTargetPath)) {
            Files.delete(atakmartiDuplexTargetPath);
        }

        Files.write(atakmartiDuplexTargetPath, atakMartiDuplexLines);

        // Remove all the missleading DfuInstance identifiers
        Files.write(ImmortalsConfig.getInstance().globals.getTtlIngestionDirectory().resolve("cipherUsageParadigms.ttl"), outputLines);

        Path[] filesToDelete = {
                krgpAnalysisPath.resolve("Marti/structures/dfus/JavaxCrypto-2.0-LOCAL.jarDFU.ttl"),
                krgpAnalysisPath.resolve("Marti/structures/dfus/BouncyCastleCipher-2.0-LOCAL.jarDFU.ttl"),
                krgpAnalysisPath.resolve("Marti/structures/dfus/BogoCipher-2.0-LOCAL.jarDFU.ttl"),
                krgpAnalysisPath.resolve("Marti/structures/dfus/NoOpCipher-2.0-LOCAL.jarDFU.ttl")
        };

        for (Path p : filesToDelete) {
            if (Files.exists(p)) {
                Files.delete(p);
            }
        }

        Path dataFile = ImmortalsConfig.getInstance().extensions.immortalizer.getProducedDataTargetFile();
        JsonObject jsonData = gson.fromJson(new FileReader(dataFile.toFile()), JsonObject.class);
        
        for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
            ImmortalsGradleProjectData projectData = gson.fromJson(entry.getValue(), ImmortalsGradleProjectData.class);
            if (projectData.getDeploymentTarget() == DeploymentTarget.ANDROID) {
                Path filePath = ImmortalsConfig.getInstance().extensions.krgp.getTtlTargetDirectory().
                        resolve(projectData.getTargetName()).resolve("structures/" + projectData.getTargetName() + "-projectOutput.ttl");
                if (Files.exists(filePath)) {
                    ARQ.init();
                    FileInputStream fis = new FileInputStream(filePath.toFile());
                    Model m = ModelFactory.createDefaultModel();
                    RDFDataMgr.read(m, fis, Lang.TTL);
                    fis.close();

                    Resource javaProject = m.getResource("http://darpa.mil/immortals/ontology/r2.0.0/java/project#JavaProject");
                    ResIterator ri = m.listResourcesWithProperty(RDF.type, javaProject);
                    Resource javaProjectResource = ri.next();
                    Property hasAndroidApp = m.getProperty("http://darpa.mil/immortals/ontology/r2.0.0#hasAndroidApp");
                    if (!javaProjectResource.hasProperty(hasAndroidApp)) {
                        Resource androidApp = m.getResource("http://darpa.mil/immortals/ontology/r2.0.0/java/android#AndroidApp");
                        Property hasPathToUberJar = m.getProperty("http://darpa.mil/immortals/ontology/r2.0.0#hasPathToUberJar");
                        Resource androidAppInstance = m.getResource("http://darpa.mil/immortals/ontology/r2.0.0/java/android#AndroidApp-" + UUID.randomUUID().toString());
                        androidAppInstance.addProperty(RDF.type, androidApp);
                        androidAppInstance.addProperty(hasPathToUberJar, ImmortalsConfig.getInstance().build.augmentations.getAndroidSdkJarPath());
                        javaProjectResource.addProperty(hasAndroidApp, androidAppInstance);
                        
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        m.write(baos, "TURTLE");
                        Files.write(filePath, baos.toByteArray());
                    }
                }
            }
        }

        return null;
    }

    public static void main(String[] args) {
        try {
            KnowledgeRepoConnectionBuilder x = new KnowledgeRepoConnectionBuilder();
            x.buildKnowledge(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
