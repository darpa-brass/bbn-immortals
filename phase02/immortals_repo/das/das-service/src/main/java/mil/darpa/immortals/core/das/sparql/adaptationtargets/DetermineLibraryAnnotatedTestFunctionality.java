package mil.darpa.immortals.core.das.sparql.adaptationtargets;

import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DetermineLibraryAnnotatedTestFunctionality extends SparqlQuery {

    /**
     * @return A map of target identifiers to a map of their test cases to a set of their annotated functionality
     */
    public static Map<String, Map<String, Set<String>>> select(@Nonnull DasAdaptationContext dac) {
        String query = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
                "prefix IMMoRTALS_java_project: <http://darpa.mil/immortals/ontology/r2.0.0/java/project#> " +
                "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> " +
                "prefix IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#>  " +
                "prefix IMMoRTALS_java_testing_instance: <http://darpa.mil/immortals/ontology/r2.0.0/java/testing/instance#> " +
                "prefix IMMoRTALS_mil_darpa_immortals_ontology: <http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#> " +
                "SELECT ?artifactIdentifier ?testIdentifier ?featureRequirement " +
                "WHERE { " +
                "      GRAPH <" + dac.getKnowldgeUri() + "> { " +
                "    ?aDeploymentModel a IMMoRTALS_gmei:DeploymentModel . " +
                "    ?aDeploymentModel IMMoRTALS:hasAvailableResources ?availableResources . " +
                "    ?availableResources IMMoRTALS_mil_darpa_immortals_ontology:hasArtifactIdentifier ?artifactIdentifier . " +
                "    ?aProject a IMMoRTALS_java_project:JavaProject . " +
                "    ?aProject IMMoRTALS:hasVcsCoordinate ?aVcsCoordinate . " +
                "    ?aVcsCoordinate IMMoRTALS:hasVersionControlUrl ?artifactIdentifier . " +
                "    ?aProject IMMoRTALS:hasCompiledSourceHash ?compiledSourceHashes . " +
                "    ?compiledSourceFiles IMMoRTALS:hasHash ?compiledSourceHashes . " +
                "    ?compiledSourceFiles IMMoRTALS:hasCorrespondingClass ?classArtifacts . " +
                "    ?classArtifacts IMMoRTALS:hasClassModel ?aClassModel . " +
                "    ?aClassModel IMMoRTALS:hasMethods ?theMethods . " +
                "    ?theMethods IMMoRTALS:hasBytecodePointer ?methodPointers . " +
                "    ?validationInstance a IMMoRTALS_java_testing_instance:ProvidedFunctionalityValidationInstance . " +
                "    ?validationInstance IMMoRTALS:hasAspectsValidated ?featureRequirement . " +
                "    ?validationInstance IMMoRTALS:hasMethodPointer ?methodPointers . " +
                "    ?aClassModel IMMoRTALS:hasClassName ?theClassName . " +
                "    ?theMethods IMMoRTALS:hasMethodName ?methodName . " +
                "    BIND(REPLACE(CONCAT(?theClassName, \"/\", ?methodName), \"/\", \".\") as ?testIdentifier) " +
                "  }  " +
                "} ";

        ResultSet resultSet = getResultSet(query);

        Map<String, Map<String, Set<String>>> artifactMap = new HashMap<>();

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            String targetIdentifier = qs.get("artifactIdentifier").toString();
            String testIdentifier = qs.get("testIdentifier").toString();
            String featureRequirement = qs.get("featureRequirement").toString();

            Map<String, Set<String>> targetTests = artifactMap.computeIfAbsent(targetIdentifier, k -> new HashMap<>());

            Set<String> functionalityValidated = targetTests.computeIfAbsent(testIdentifier, k -> new HashSet<>());

            functionalityValidated.add(featureRequirement);
        }
        return artifactMap;
    }
}

