package mil.darpa.immortals.core.das.sparql.deploymentmodel;

import mil.darpa.immortals.core.das.adaptationmodules.hddrass.Hacks;
import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.ContextManager;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 4/2/18.
 */
public class DetermineLibraryUpgrades extends SparqlQuery {

    public static class LibraryUpgrade {
        public final String adaptationTarget;
        public final String originalResourceDependency;
        public final String replacementResourceDependency;

        public LibraryUpgrade(String adaptationTarget, String originalResourceDependency, String replacementResourceDependency) {
            this.adaptationTarget = adaptationTarget;
            this.originalResourceDependency = originalResourceDependency;
            this.replacementResourceDependency = replacementResourceDependency;
        }
    }

    public static List<LibraryUpgrade> select(DasAdaptationContext dac) {

        String query =
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
                        "prefix IMMoRTALS_mil_darpa_immortals_ontology: <http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#> " +
                        "prefix IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#>  " +
                        "prefix IMMoRTALS_resources: <http://darpa.mil/immortals/ontology/r2.0.0/resources#> " +
                        "SELECT ?targetResource ?originalDependencyCoordinates ?upgradedDependencyCoordinates " +
                        "WHERE { " +
                        "    GRAPH <" + dac.getKnowldgeUri() + "> { " +
                        "    ?aDeploymentModel a IMMoRTALS_gmei:DeploymentModel .  " +
                        "    ?aDeploymentModel IMMoRTALS:hasSessionIdentifier \"" + dac.getAdaptationIdentifer() + "\" .  " +
                        "    ?aDeploymentModel IMMoRTALS:hasResourceMigrationTargets ?resourceMigrationTargets . " +
                        "    ?resourceMigrationTargets IMMoRTALS:hasTargetResource ?libraryTarget . " +
                        "    ?libraryTarget a IMMoRTALS_resources:SoftwareLibrary . " +
                        "    ?libraryTarget IMMoRTALS_mil_darpa_immortals_ontology:hasDependencyCoordinates ?upgradedDependencyCoordinates . " +
                        "    ?resourceMigrationTargets IMMoRTALS_mil_darpa_immortals_ontology:hasOriginalResource ?originalResource . " +
                        "    ?originalResource a IMMoRTALS_resources:SoftwareLibrary . " +
                        "    ?originalResource IMMoRTALS_mil_darpa_immortals_ontology:hasDependencyCoordinates ?originalDependencyCoordinates . " +
                        "    ?aDeploymentModel IMMoRTALS:hasAvailableResources ?availableResources . " +
                        "    ?aDeploymentModel IMMoRTALS:hasResourceContainmentModel ?aResourceContainmentModel . " +
                        "    ?aResourceContainmentModel IMMoRTALS:hasResourceModel ?aConcreteResourceNode . " +
                        "    ?aConcreteResourceNode IMMoRTALS:hasContainedNode ?containedLibraryNode . " +
                        "    ?containedLibraryNode IMMoRTALS:hasResource ?containedLibraryResource . " +
                        "    ?containedLibraryResource  IMMoRTALS_mil_darpa_immortals_ontology:hasDependencyCoordinates ?originalDependencyCoordinates . " +
                        "    ?aConcreteResourceNode IMMoRTALS:hasResource ?targetResource . " +
                        "    ?containedLibraryResource a IMMoRTALS_resources:SoftwareLibrary . " +
                        "  }  " +
                        "} ";


        ResultSet resultSet = getResultSet(query);

        List<LibraryUpgrade> rval = new LinkedList<>();

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();

            LibraryUpgrade res = new LibraryUpgrade(
                    Hacks.deploymentModelIdentifierToAbsoluteIdentifier(qs.getResource("targetResource").toString()),
                    qs.getLiteral("originalDependencyCoordinates").getString(),
                    qs.getLiteral("upgradedDependencyCoordinates").getString()
            );
            rval.add(res);
        }

        return rval;
    }
}
