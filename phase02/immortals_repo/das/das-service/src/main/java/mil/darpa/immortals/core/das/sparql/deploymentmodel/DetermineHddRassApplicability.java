package mil.darpa.immortals.core.das.sparql.deploymentmodel;

import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.ContextManager;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by awellman@bbn.com on 4/2/18.
 */
public class DetermineHddRassApplicability extends SparqlQuery {

    public static class HddRassApplicabilityDetails {
        final String adaptationTargetUid;
        final HashMap<String, String> libraryUpgrades;
        HashSet<String> requiredTargetFunctionality;

        public HddRassApplicabilityDetails(@Nonnull String adaptationTargetUid, @Nonnull HashMap<String, String> libraryUpgrades, @Nonnull Set<String> requiredTargetFunctionality) {
            this.adaptationTargetUid = adaptationTargetUid;
            this.libraryUpgrades = libraryUpgrades;
            this.requiredTargetFunctionality = new HashSet(requiredTargetFunctionality);
        }

        public String getAdaptationTarget() {
            return adaptationTargetUid;
        }

        public HashMap<String, String> getLibraryUpgradeMap() {
            return new HashMap<>(libraryUpgrades);
        }

        public Set<String> getRequiredTargetFunctionality() {
            return new HashSet<>(requiredTargetFunctionality);
        }
    }

    public static List<HddRassApplicabilityDetails> select(DasAdaptationContext dac) {

        String query =
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
                        "prefix IMMoRTALS_mil_darpa_immortals_ontology: <http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#> " +
                        "prefix IMMoRTALS_java_project: <http://darpa.mil/immortals/ontology/r2.0.0/java/project#> " +
                        "prefix IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#>  " +
                        "prefix IMMoRTALS_resources: <http://darpa.mil/immortals/ontology/r2.0.0/resources#> " +
                        "prefix IMMoRTALS_java_testing_instance: <http://darpa.mil/immortals/ontology/r2.0.0/java/testing/instance#> " +
                        "prefix IMMoRTALS_mil_darpa_immortals_ontology: <http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#> " +
                        "SELECT ?artifactIdentifier ?requiredFunctionality ?originalDependencyCoordinates ?upgradedDependencyCoordinates " +
                        "WHERE { " +
                        "    GRAPH <" + dac.getKnowldgeUri() + "> { " +
                        "    ?aDeploymentModel a IMMoRTALS_gmei:DeploymentModel .  " +
                        "    ?aDeploymentModel IMMoRTALS:hasSessionIdentifier \"" + dac.getAdaptationIdentifer() + "\" .  " +
                        "    ?aDeploymentModel IMMoRTALS:hasResourceMigrationTargets ?resourceMigrationTargets . " +
                        "    ?aDeploymentModel IMMoRTALS:hasFunctionalitySpec ?aFunctionalitySpec . " +
                        "    ?aFunctionalitySpec IMMoRTALS:hasFunctionalityProvided ?requiredFunctionality . " +
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
                        "    ?targetResource IMMoRTALS_mil_darpa_immortals_ontology:hasArtifactIdentifier ?artifactIdentifier . " +
                        "  }  " +
                        "} ";


        ResultSet resultSet = getResultSet(query);

        HashMap<String, HddRassApplicabilityDetails> applicabilityDetailsMap = new HashMap<>();

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();

            String targetResource = qs.getLiteral("artifactIdentifier").toString();
            String requiredFunctionality = qs.getResource("requiredFunctionality").toString();
            String originalDependencyCoordinates = qs.getLiteral("originalDependencyCoordinates").getString();
            String upgradedDependencyCoordinates = qs.getLiteral("upgradedDependencyCoordinates").getString();

            HddRassApplicabilityDetails details = applicabilityDetailsMap.get(targetResource);
            if (details == null) {
                HashMap<String, String> libraryUpgrades = new HashMap<>();
                libraryUpgrades.put(originalDependencyCoordinates, upgradedDependencyCoordinates);
                Set<String> requiredFunctionalitySet = new HashSet<>();
                requiredFunctionalitySet.add(requiredFunctionality);
                details = new HddRassApplicabilityDetails(
                        targetResource,
                        libraryUpgrades,
                        requiredFunctionalitySet
                );
                applicabilityDetailsMap.put(targetResource, details);
            } else {
                details.requiredTargetFunctionality.add(requiredFunctionality);
                details.libraryUpgrades.put(originalDependencyCoordinates, upgradedDependencyCoordinates);
            }
        }
        return new LinkedList<>(applicabilityDetailsMap.values());
    }
}
