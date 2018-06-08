package mil.darpa.immortals.core.das.sparql.deploymentmodel;

import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.ContextManager;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 4/2/18.
 */
public class DeterminePartialLibraryUpgradeApplicability extends SparqlQuery {

    public static class PartialLibraryUpgradeCandidate {

        public final String applicationIdentifier;
        public final String applicationTargetConfiguration;
        public final String vulnerability;
        public final String vulnerableLibraryRepo;
        public final String vulnerableLibraryGroupId;
        public final String vulnerableLibraryArtifactId;
        public final String vulnerableLibraryVersion;
        public final String fixedLibraryRepo;
        public final String fixedLibraryGroupId;
        public final String fixedLibraryArtifactId;
        public final String fixedLibraryVersion;

        public PartialLibraryUpgradeCandidate(@Nonnull String applicationIdentifier, @Nonnull String applicationTargetConfiguration,
                                              @Nonnull String vulnerability, @Nonnull String vulnerableLibraryRepo, @Nonnull String vulnerableLibraryGroupId,
                                              @Nonnull String vulnerableLibraryArtifactId, @Nonnull String vulnerableLibraryVersion, @Nonnull String fixedLibraryRepo,
                                              @Nonnull String fixedLibraryGroupId, @Nonnull String fixedLibraryArtifactId, @Nonnull String fixedLibraryVersion) {
            this.applicationIdentifier = applicationIdentifier;
            this.applicationTargetConfiguration = applicationTargetConfiguration;
            this.vulnerability = vulnerability;
            this.vulnerableLibraryRepo = vulnerableLibraryRepo;
            this.vulnerableLibraryGroupId = vulnerableLibraryGroupId;
            this.vulnerableLibraryArtifactId = vulnerableLibraryArtifactId;
            this.vulnerableLibraryVersion = vulnerableLibraryVersion;
            this.fixedLibraryRepo = fixedLibraryRepo;
            this.fixedLibraryGroupId = fixedLibraryGroupId;
            this.fixedLibraryArtifactId = fixedLibraryArtifactId;
            this.fixedLibraryVersion = fixedLibraryVersion;
        }
        
        public String getVulnerableLibraryCoordinate() {
            return vulnerableLibraryGroupId + ":" + vulnerableLibraryArtifactId + ":" + vulnerableLibraryVersion;
        }
        
        public String getFixedLibraryCoordinate() {
            return fixedLibraryGroupId + ":" + fixedLibraryArtifactId + ":" + fixedLibraryVersion;
        }

    }

    public static List<PartialLibraryUpgradeCandidate> select(DasAdaptationContext dac) {

        String query =
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
                        "prefix IMMoRTALS_mil_darpa_immortals_ontology: <http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#> " +
                        "prefix IMMoRTALS_java_project: <http://darpa.mil/immortals/ontology/r2.0.0/java/project#> " +
                        "prefix IMMoRTALS_gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#>  " +
                        "prefix IMMoRTALS_resources: <http://darpa.mil/immortals/ontology/r2.0.0/resources#> " +
                        "prefix IMMoRTALS_java_testing_instance: <http://darpa.mil/immortals/ontology/r2.0.0/java/testing/instance#> " +
                        "PREFIX IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> " +
                        "SELECT ?applicationIdentifier ?vulnerability ?vulnerableLibraryRepo ?vulnerableLibraryGroupId ?vulnerableLibraryArtifactId ?vulnerableLibraryVersion ?classpathName ?fixedLibraryRepo ?fixedLibraryGroupId ?fixedLibraryArtifactId ?fixedLibraryVersion " +
                        "WHERE { " +
                        "      GRAPH <" + dac.getKnowldgeUri() + "> { " +
//                        "    # Determine all deployment model target applications. " +
                        "    ?aDeploymentModel a IMMoRTALS_gmei:DeploymentModel .  " +
                        "    ?aDeploymentModel IMMoRTALS:hasSessionIdentifier \"" + dac.getAdaptationIdentifer() + "\" . " +
                        "    ?aDeploymentModel IMMoRTALS:hasAvailableResources ?availableResources . " +
                        "    ?availableResources IMMoRTALS_mil_darpa_immortals_ontology:hasArtifactIdentifier ?applicationIdentifier . " +
//                        "    # Determine the software libraries with vulnerabilities " +
                        "    ?aDeploymentModel IMMoRTALS:hasResourceMigrationTargets ?resourceMigrationTargets . " +
                        "    ?resourceMigrationTargets IMMoRTALS_mil_darpa_immortals_ontology:hasOriginalResource ?originalResource . " +
                        "    ?originalResource a IMMoRTALS_resources:SoftwareLibrary . " +
                        "    ?originalResource IMMoRTALS_mil_darpa_immortals_ontology:hasKnownVulnerability ?vulnerability . " +
                        "    ?originalResource IMMoRTALS_mil_darpa_immortals_ontology:hasMavenRepository ?vulnerableLibraryRepo . " +
                        "    ?originalResource IMMoRTALS:hasArtifactId ?vulnerableLibraryArtifactId . " +
                        "    ?originalResource IMMoRTALS:hasGroupId ?vulnerableLibraryGroupId . " +
                        "    ?originalResource IMMoRTALS:hasVersion ?vulnerableLibraryVersion . " +
//                        "    # Determine the fixed library details " +
                        "    ?resourceMigrationTargets IMMoRTALS:hasTargetResource ?targetResource . " +
                        "    ?targetResource a IMMoRTALS_resources:SoftwareLibrary . " +
                        "    ?targetResource IMMoRTALS_mil_darpa_immortals_ontology:hasMavenRepository ?fixedLibraryRepo . " +
                        "    ?targetResource IMMoRTALS:hasArtifactId ?fixedLibraryArtifactId . " +
                        "    ?targetResource IMMoRTALS:hasGroupId ?fixedLibraryGroupId . " +
                        "    ?targetResource IMMoRTALS:hasVersion ?fixedLibraryVersion . " +
//                        "    # Determine the JavaProject based on the applications in the deployment model " +
                        "    ?aProject a IMMoRTALS_java_project:JavaProject . " +
                        "    ?aProject IMMoRTALS:hasCoordinate ?projectCoordinate . " +
                        "    ?aProject IMMoRTALS:hasVcsCoordinate ?aVcsCoordinate . " +
                        "    ?aVcsCoordinate IMMoRTALS:hasVersionControlUrl ?applicationIdentifier . " +
//                        "    # Then determine the libraries used by those applications that use vulnerable libraries " +
                        "    ?aProject IMMoRTALS:hasClasspaths ?classpaths . " +
                        "    { " +
                        "        { " +
                        "            ?classpaths IMMoRTALS:hasClasspathName \"compile\" . " +
                        "        } UNION { " +
                        "            ?classpaths IMMoRTALS:hasClasspathName \"testCompile\" . " +
                        "        } UNION { " +
                        "            ?classpaths IMMoRTALS:hasClasspathName \"androidTestCompile\" . " +
                        "        } " +
                        "    } " +
                        "    ?classpaths IMMoRTALS:hasClasspathName ?classpathName . " +
                        "    ?classpaths IMMoRTALS:hasElementHashValues ?classpathsHash . " +
                        "    ?jarArtifact IMMoRTALS:hasHash ?classpathsHash . " +
                        "    ?jarArtifact IMMoRTALS:hasCoordinate ?jarCoordinate . " +
                        "    ?jarCoordinate a IMMoRTALS_bytecode:BytecodeArtifactCoordinate . " +
                        "    ?jarCoordinate IMMoRTALS:hasArtifactId ?vulnerableLibraryArtifactId . " +
                        "    ?jarCoordinate IMMoRTALS:hasGroupId ?vulnerableLibraryGroupId . " +
                        "    ?jarCoordinate IMMoRTALS:hasVersion ?vulnerableLibraryVersion " +
                        "} " +
                        "} ";


        ResultSet resultSet = getResultSet(query);

        List<PartialLibraryUpgradeCandidate> upgradeCandidates = new LinkedList<>();

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();

            String applicationIdentifier = qs.getLiteral("applicationIdentifier").toString();
            String applicationTargetConfiguration = qs.getLiteral("classpathName").toString();
            String vulnerability = qs.getLiteral("vulnerability").toString();
            String vulnerableLibraryRepo = qs.getLiteral("vulnerableLibraryRepo").toString();
            String vulnerableLibraryGroupId = qs.getLiteral("vulnerableLibraryGroupId").toString();
            String vulnerableLibraryArtifactId = qs.getLiteral("vulnerableLibraryArtifactId").toString();
            String vulnerableLibraryVersion = qs.getLiteral("vulnerableLibraryVersion").toString();
            String fixedLibraryRepo = qs.getLiteral("fixedLibraryRepo").toString();
            String fixedLibraryGroupId = qs.getLiteral("fixedLibraryGroupId").toString();
            String fixedLibraryArtifactId = qs.getLiteral("fixedLibraryArtifactId").toString();
            String fixedLibraryVersion = qs.getLiteral("fixedLibraryVersion").toString();

            PartialLibraryUpgradeCandidate pluc = new PartialLibraryUpgradeCandidate(applicationIdentifier,
                    applicationTargetConfiguration, vulnerability, vulnerableLibraryRepo, vulnerableLibraryGroupId,
                    vulnerableLibraryArtifactId, vulnerableLibraryVersion, fixedLibraryRepo, fixedLibraryGroupId,
                    fixedLibraryArtifactId, fixedLibraryVersion);

            upgradeCandidates.add(pluc);
        }
        return upgradeCandidates;
    }
}
