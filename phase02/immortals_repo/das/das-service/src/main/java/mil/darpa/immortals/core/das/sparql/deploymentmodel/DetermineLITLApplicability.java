package mil.darpa.immortals.core.das.sparql.deploymentmodel;

import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

/**
 * Created by awellman@bbn.com on 7/5/18.
 */
public class DetermineLITLApplicability extends SparqlQuery {

    public static boolean select(DasAdaptationContext dac) {
        String query =
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
                        "prefix IMMoRTALS_mil_darpa_immortals_ontology: <http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#> " +
                        "prefix IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#>  " +
                        "prefix IMMoRTALS_resources: <http://darpa.mil/immortals/ontology/r2.0.0/resources#> " +
                        "prefix IMMoRTALS_java_project: <http://darpa.mil/immortals/ontology/r2.0.0/java/project#> " +
                        "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> " +
                        "SELECT ?artifactIdentifier ?hasAndroidUpgradeRequest " +
                        "WHERE { " +
                        "          GRAPH <" + dac.getKnowldgeUri() + "> { " +
                        "    ?aDeploymentModel a IMMoRTALS_gmei:DeploymentModel .  " +
                        "    ?aDeploymentModel IMMoRTALS:hasSessionIdentifier \"" + dac.getAdaptationIdentifer() + "\" .  " +
                        "    ?aResourceContainmentModel IMMoRTALS:hasResourceModel ?aConcreteResourceNode . " +
                        "    ?aConcreteResourceNode IMMoRTALS:hasResource ?targetApplication . " +
                        "    ?targetApplication IMMoRTALS_mil_darpa_immortals_ontology:hasArtifactIdentifier ?artifactIdentifier . " +
                        "    ?aDeploymentModel IMMoRTALS:hasResourceMigrationTargets ?resourceMigrationTargets . " +
                        "    ?resourceMigrationTargets IMMoRTALS:hasTargetResource ?libraryTarget . " +
                        "    ?libraryTarget a IMMoRTALS_resources:SoftwareLibrary . " +
                        "    BIND( " +
                        "      EXISTS { " +
                        "        ?libraryTarget IMMoRTALS_mil_darpa_immortals_ontology:hasDependencyCoordinates \"com.google:android-platform:23\" " +
                        "      } " +
                        "      as ?hasAndroidUpgradeRequest " +
                        "    ) " +
                        "  }  " +
                        "} ";

        ResultSet resultSet = getResultSet(query);

        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();

            return qs.getLiteral("hasAndroidUpgradeRequest").getBoolean();
        }
       
        return false;
    }
}
