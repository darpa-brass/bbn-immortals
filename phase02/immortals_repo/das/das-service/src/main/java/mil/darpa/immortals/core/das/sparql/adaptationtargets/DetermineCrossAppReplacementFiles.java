package mil.darpa.immortals.core.das.sparql.adaptationtargets;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.ContextManager;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 7/6/18.
 */
public class DetermineCrossAppReplacementFiles extends SparqlQuery {

    public static class CrossAppReplacementData {
        private final String appIdentifier;
        private final Map<String, String> filePathReplacementSource;

        private CrossAppReplacementData(@Nonnull String appIdentifier) {
            this.appIdentifier = appIdentifier;
            this.filePathReplacementSource = new HashMap<>();
        }

        private synchronized void addFilePathReplacementSource(@Nonnull String filepath, @Nonnull String newSource) {
            filePathReplacementSource.put(filepath, newSource);
        }

        public String getAppIdentifier() {
            return appIdentifier;
        }

        public Map<String, String> getFilePathReplacementSource() {
            return filePathReplacementSource;
        }
    }

    /**
     * @param dac
     * @return A mapping of base application paths to {@link CrossAppReplacementData} objects
     */
    public static Map<String, CrossAppReplacementData> select(DasAdaptationContext dac) throws Exception {

        String query =
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
                        "PREFIX IMMoRTALS_functionality: <http://darpa.mil/immortals/ontology/r2.0.0/functionality#> " +
                        "PREFIX IMMoRTALS_cp_jvm: <http://darpa.mil/immortals/ontology/r2.0.0/cp/jvm#> " +
                        "PREFIX IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#> " +
                        "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX owl:   <http://www.w3.org/2002/07/owl#> " +
                        "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> " +
                        "PREFIX IMMoRTALS_cp2: <http://darpa.mil/immortals/ontology/r2.0.0/cp2#> " +
                        "PREFIX IMMoRTALS_functionality_datatype: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/datatype#> " +
                        "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX IMMoRTALS_com_securboration_dontcommit: <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/dontcommit#> " +
                        "PREFIX IMMoRTALS_resources_compute: <http://darpa.mil/immortals/ontology/r2.0.0/resources/compute#> " +
                        "PREFIX IMMoRTALS_property_impact: <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#> " +
                        "PREFIX IMMoRTALS_functionality_aspects: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/aspects#> " +
                        "PREFIX IMMoRTALS_mil_darpa_immortals_ontology: <http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#> " +
                        "SELECT ?targetIdentifier ?sourceFilepath ?source " +
                        "WHERE { " +
                        "  GRAPH<" + dac.getKnowldgeUri() + "> { " +
                        "    ?deploymentModel a IMMoRTALS_gmei:DeploymentModel . " +
                        "    ?aDeploymentModel IMMoRTALS:hasSessionIdentifier \"" + dac.getAdaptationIdentifer() + "\" .  " +
                        "    { " +
                        "      { " +
                        "        ?deploymentModel IMMoRTALS:hasAvailableResources " +
                        "              IMMoRTALS_cp2:ClientServerEnvironment.MartiServer  . " +
                        "        IMMoRTALS_cp2:ClientServerEnvironment.MartiServer  " +
                        "          IMMoRTALS_mil_darpa_immortals_ontology:hasArtifactIdentifier ?targetIdentifier . " +
                        "        ?sourceFiles IMMoRTALS:hasFullyQualifiedName " +
                        "              \"com.bbn.marti.immortals.net.tcp.TcpSocketServer\" ; " +
                        "          IMMoRTALS:hasFullyQualifiedName ?classPathName ; " +
                        "          IMMoRTALS:hasFileName ?fileName ; " +
                        "          IMMoRTALS:hasSource ?source . " +
                        "        BIND(CONCAT (REPLACE(?classPathName, \"\\\\.\", \"/\"), \".java\") as ?sourceFilepath) " +
                        "      } UNION { " +
                        "        ?deploymentModel IMMoRTALS:hasAvailableResources " +
                        "              IMMoRTALS_cp2:ClientServerEnvironment.ClientDevice1 . " +
                        "        IMMoRTALS_cp2:ClientServerEnvironment.ClientDevice1 " +
                        "          IMMoRTALS_mil_darpa_immortals_ontology:hasArtifactIdentifier ?targetIdentifier . " +
                        "        ?sourceFiles  " +
                        "          IMMoRTALS:hasFullyQualifiedName \"com.bbn.ataklite.net.Dispatcher\" ; " +
                        "          IMMoRTALS:hasFullyQualifiedName ?classPathName ; " +
                        "          IMMoRTALS:hasSource ?source . " +
                        "        BIND(CONCAT (REPLACE(?classPathName, \"\\\\.\", \"/\"), \".java\") as ?sourceFilepath) " +
                        "      } " +
                        "    } " +
                        "  } " +
                        "} ";


        ResultSet resultSet = getResultSet(query);

        Map<String, CrossAppReplacementData> dataMap = new HashMap<>();

        Path krwd = Paths.get(ImmortalsConfig.getInstance().knowledgeRepoService.getWorkingDirectory());

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            String targetIdentifier = qs.get("targetIdentifier").toString();
            String sourceFilepath = qs.get("sourceFilepath").toString();
            String source = qs.get("source").toString().replaceAll("\\\\\"", "\"");

            CrossAppReplacementData data = dataMap.get(targetIdentifier);
            if (data == null) {
                data = new CrossAppReplacementData(targetIdentifier);
                dataMap.put(targetIdentifier, data);

                // KRHACK: This hard coding should not be necessary and should be properly resolvable via a SPARQL query!
                if (targetIdentifier.endsWith("Marti")) {
                    Path wrapperSocket = krwd.resolve("sootOutput/WrapperSocket.java");
                    byte[] bytes = Files.readAllBytes(wrapperSocket);
                    String src = new String(bytes);
                    src = "package com.bbn.marti.immortals.net.tcp;\n\n" + src;
                    data.addFilePathReplacementSource("com/bbn/marti/immortals/net/tcp/WrapperSocket.java", src);
                } else if (targetIdentifier.endsWith("ATAKLite")) {
                    Path wrapperSocketChannel = krwd.resolve("sootOutput/WrapperSocketChannel.java");
                    byte[] bytes = Files.readAllBytes(wrapperSocketChannel);
                    String src = new String(bytes);
                    src = "package com.bbn.ataklite.net;\n\n" + src;
                    data.addFilePathReplacementSource("com/bbn/ataklite/net/WrapperSocketChannel.java", src);

                }
            }
            data.addFilePathReplacementSource(sourceFilepath, source);
        }

        return dataMap;
    }

    public static void main(String[] args) {
        try {
            DasAdaptationContext dac = ContextManager.getContext(
                    "CP2Challenge",
                    "http://localhost:3030/ds/data/d3002bda-44e0-4f5a-9b14-c9bc5d965c0e-IMMoRTALS-r2.0.0",
                    "http://localhost:3030/ds/data/d3002bda-44e0-4f5a-9b14-c9bc5d965c0e-IMMoRTALS-r2.0.0"
            );

            Map<String, CrossAppReplacementData> val = DetermineCrossAppReplacementFiles.select(dac);
            System.out.println("MEH");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
