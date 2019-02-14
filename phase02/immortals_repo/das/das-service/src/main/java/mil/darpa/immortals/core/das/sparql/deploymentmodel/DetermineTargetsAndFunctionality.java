package mil.darpa.immortals.core.das.sparql.deploymentmodel;

import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.ContextManager;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 4/2/18.
 */
public class DetermineTargetsAndFunctionality extends SparqlQuery {
    public static HashMap<String, Set<String>> select(DasAdaptationContext dac) {
        HashMap<String, Set<String>> targetFunctionalityMap = new HashMap<>();
        

        String query =
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
                        "prefix IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#>  " +
                        "prefix IMMoRTALS_mil_darpa_immortals_ontology:  <http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#> " +
                        "SELECT ?artifactIdentifiers ?functionalityProvided " +
                        "WHERE { " +
                        "    GRAPH <" + dac.getKnowldgeUri() + "> { " +
                        "    ?aDeploymentModel a IMMoRTALS_gmei:DeploymentModel .  " +
                        "    ?aDeploymentModel IMMoRTALS:hasSessionIdentifier \"" + dac.getAdaptationIdentifer() + "\" .  " +
                        "    ?aDeploymentModel IMMoRTALS:hasResourceContainmentModel ?aResourceContainmentModel . " +
                        "    ?aResourceContainmentModel IMMoRTALS:hasResourceModel ?aResourceModel . " +
                        "    ?aResourceModel IMMoRTALS:hasResource ?containedResources . " +
                        "    ?containedResources IMMoRTALS_mil_darpa_immortals_ontology:hasArtifactIdentifier ?artifactIdentifiers  . " +
                        "    ?aDeploymentModel IMMoRTALS:hasFunctionalitySpec  ?aFunctionalitySpec . " +
                        "    ?aFunctionalitySpec IMMoRTALS:hasFunctionalityProvided ?functionalityProvided . " +
                        "  } " +
                        "} ";



//        "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
//                "prefix IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#>  " +
//                "prefix IMMoRTALS_mil_darpa_immortals_ontology:  <http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#> " +
//                "SELECT ?artifactIdentifiers " +
//                "WHERE { " +
//                "  GRAPH <" + dac.getKnowldgeUri() + "> { " +
//                "    ?aDeploymentModel a IMMoRTALS_gmei:DeploymentModel .  " +
//                "    ?aDeploymentModel IMMoRTALS:hasSessionIdentifier \"" + dac.getAdaptationIdentifer() + "\" .  " +
//                "    ?aDeploymentModel IMMoRTALS:hasResourceContainmentModel ?aResourceContainmentModel . " +
//                "    ?aResourceContainmentModel IMMoRTALS:hasResourceModel ?aResourceModel . " +
//                "    ?aResourceModel IMMoRTALS:hasResource ?containedResources . " +
//                "    ?containedResources IMMoRTALS_mil_darpa_immortals_ontology:hasArtifactIdentifier ?artifactIdentifiers  ; " +
//                "  } " +
//                "} ";
        ResultSet resultSet = getResultSet(query);

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            
            String target = qs.getLiteral("artifactIdentifiers").toString();
            
            Set<String> functionality = targetFunctionalityMap.get(target);
            
            if (functionality == null) {
                functionality = new HashSet<>();
                targetFunctionalityMap.put(target, functionality);
            }
            functionality.add(qs.getResource("functionalityProvided").toString());
        }
        return targetFunctionalityMap;
    }
    
    public static void main(String[] args) {
        DasAdaptationContext dac = ContextManager.getContext(
                "I1529010259647",
                "http://localhost:3030/ds/data/3c3c71a6-4823-4c62-ad19-1a10af036690-IMMoRTALS-r2.0.0",
                "http://localhost:3030/ds/data/3c3c71a6-4823-4c62-ad19-1a10af036690-IMMoRTALS-r2.0.0"
        );
        Map<String, Set<String>> val = DetermineTargetsAndFunctionality.select(dac);
        System.out.println("MEH");
    }
}
