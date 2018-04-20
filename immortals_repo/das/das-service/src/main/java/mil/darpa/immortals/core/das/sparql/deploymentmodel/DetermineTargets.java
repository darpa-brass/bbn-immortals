package mil.darpa.immortals.core.das.sparql.deploymentmodel;

import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.ContextManager;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 4/2/18.
 */
public class DetermineTargets extends SparqlQuery {
    public static Set<String> select(DasAdaptationContext dac) {
        
        String query = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
                "prefix IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#>  " +
                "prefix IMMoRTALS_mil_darpa_immortals_ontology:  <http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#> " +
                "SELECT ?artifactIdentifiers " +
                "WHERE { " +
                "  GRAPH <" + dac.getKnowldgeUri() + "> { " +
                "    ?aDeploymentModel a IMMoRTALS_gmei:DeploymentModel .  " +
                "    ?aDeploymentModel IMMoRTALS:hasSessionIdentifier \"" + dac.getAdaptationIdentifer() + "\" .  " +
                "    ?aDeploymentModel IMMoRTALS:hasResourceContainmentModel ?aResourceContainmentModel . " +
                "    ?aResourceContainmentModel IMMoRTALS:hasResourceModel ?aResourceModel . " +
                "    ?aResourceModel IMMoRTALS:hasResource ?containedResources . " +
                "    ?containedResources IMMoRTALS_mil_darpa_immortals_ontology:hasArtifactIdentifier ?artifactIdentifiers  ; " +
                "  } " +
                "} ";


        ResultSet resultSet = getResultSet(query);

        Set<String> targets = new HashSet<>();

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            targets.add(qs.getLiteral("artifactIdentifiers").toString());
        }
        return targets;
    }
}
