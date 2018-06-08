package mil.darpa.immortals.das.deploymentmodel;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.RequirementsInterface;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModelInterface;
import mil.darpa.immortals.core.api.ll.phase2.UpgradableLibraryInterface;
import mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.ATAKLiteSubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.AtakliteRequirements;
import mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.ClientPartialUpgradeLibrary;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.MartiRequirements;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.MartiSubmissionModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 4/2/18.
 */
public class DeploymentModelBuilder {

    private static final HashMap<String, Path> resourceFilesystemLocationMap = new HashMap<>();

    static {
        Path ir = ImmortalsConfig.getInstance().globals.getImmortalsRoot();
        resourceFilesystemLocationMap.put("Marti", ir.resolve("applications/server/Marti"));
        resourceFilesystemLocationMap.put("ATAKLite", ir.resolve("applications/client/"));
        resourceFilesystemLocationMap.put("ExampleAppJava", ir.resolve("applications/examples/ThirdPartyLibAnalysisJavaApp"));
        resourceFilesystemLocationMap.put("ExampleAppAndroid", ir.resolve("applications/examples/ThirdPartyLibAnalysisAndroidApp"));
    }

    private final SubmissionModel submissionModel;
    private final Model model;
    private final Props props;
    private final Resources res;

    public DeploymentModelBuilder(@Nonnull SubmissionModel submissionModel) {
        this.submissionModel = submissionModel;
        this.model = ModelFactory.createDefaultModel();
        this.props = new Props(model);
        this.res = new Resources(model, props);


        model.setNsPrefix("IMMoRTALS_gmei", "http://darpa.mil/immortals/ontology/r2.0.0/gmei#");
        model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNsPrefix("IMMoRTALS_functionality_datatype", "http://darpa.mil/immortals/ontology/r2.0.0/functionality/datatype#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        model.setNsPrefix("IMMoRTALS_property_impact", "http://darpa.mil/immortals/ontology/r2.0.0/property/impact#");
        model.setNsPrefix("IMMoRTALS_functionality_dataproperties", "http://darpa.mil/immortals/ontology/r2.0.0/functionality/dataproperties#");
        model.setNsPrefix("IMMoRTALS_resources_logical", "http://darpa.mil/immortals/ontology/r2.0.0/resources/logical#");
        model.setNsPrefix("IMMoRTALS_functionality", "http://darpa.mil/immortals/ontology/r2.0.0/functionality#");
        model.setNsPrefix("IMMoRTALS", "http://darpa.mil/immortals/ontology/r2.0.0#");
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("IMMoRTALS_resources", "http://darpa.mil/immortals/ontology/r2.0.0/resources#");
        model.setNsPrefix("IMMoRTALS_cp2", "http://darpa.mil/immortals/ontology/r2.0.0/cp2#");
        model.setNsPrefix("IMMoRTALS_resource_containment", "http://darpa.mil/immortals/ontology/r2.0.0/resource/containment#");
        model.setNsPrefix(props.CUSTOM_PF, props.CUSTOM_NS);
    }

    public Model build() {
        // Create the deployment model
        Resource deploymentModel = res.deploymentModelCP3();

        // Add the baseline feature requirements
        deploymentModel.addProperty(
                props.HAS_FUNCTIONALITY_SPEC,
                res.functionalitySpecBaseline()
        );

        // Set the adaptation identifier
        deploymentModel.addLiteral(props.HAS_SESSION_IDENTIFIER, submissionModel.sessionIdentifier);

        // Add the base resource model
        Resource resourceContainmentModel = res.resourceContainmentModel("");
        deploymentModel.addProperty(props.HAS_RESOURCE_CONTAINMENT_MODEL, resourceContainmentModel);

        Map<Resource, SubmissionModelInterface> resourceSubmissionModels = new HashMap<>();

        if (submissionModel.martiServerModel != null) {
            if (submissionModel.martiServerModel.requirements != null &&
                    submissionModel.martiServerModel.requirements.partialLibraryUpgrade != null) {

                resourceSubmissionModels.put(res.exampleAppJava(), new MartiSubmissionModel(
                        new MartiRequirements(
                                submissionModel.martiServerModel.requirements.partialLibraryUpgrade,
                                null,
                                null)
                ));
            } else {
                resourceSubmissionModels.put(res.martiServer(), submissionModel.martiServerModel);
            }
        }

        // If ATAKLite is defined, add it to the map of SubmissionModels and map of resource identifiers
        if (submissionModel.atakLiteClientModel != null) {
            if (submissionModel.atakLiteClientModel.requirements != null &&
                    submissionModel.atakLiteClientModel.requirements.partialLibraryUpgrade != null) {
                resourceSubmissionModels.put(res.exampleAppAndroid(), new ATAKLiteSubmissionModel(
                        new AtakliteRequirements(
                                null,
                                submissionModel.atakLiteClientModel.requirements.partialLibraryUpgrade,
                                null)
                ));
            } else {
                resourceSubmissionModels.put(res.atakliteClient(), submissionModel.atakLiteClientModel);
            }
        }

        for (Resource environmentResource : resourceSubmissionModels.keySet()) {
            SubmissionModelInterface submissionModel = resourceSubmissionModels.get(environmentResource);

            // Add the global resource
            deploymentModel.addProperty(props.HAS_AVAILABLE_RESOURCES, environmentResource);

            // Add the resource model
            Resource instanceResourceNode = res.concreteResourceNode(submissionModel.getIdentifier());

            resourceContainmentModel.addProperty(props.HAS_RESOURCE_MODEL, instanceResourceNode);
            instanceResourceNode.addProperty(props.HAS_RESOURCE, environmentResource);

            if (submissionModel.getRequirements() != null) {
                RequirementsInterface lui = submissionModel.getRequirements();

                List<UpgradableLibraryInterface> libraryUpgrades = new LinkedList<>();
                if (lui.getUpgradeLibrary() != null) {
                    libraryUpgrades.add(lui.getUpgradeLibrary());
                }
                if (lui.getPartialLibraryUpgrade() != null) {
                    libraryUpgrades.add(lui.getPartialLibraryUpgrade());
                }

                for (UpgradableLibraryInterface ul : libraryUpgrades) {

                    String coordinates = ul.getOldDependencyCoordinates();
                    String[] splitCoordinates = coordinates.split(":");
                    String groupId = splitCoordinates[0];
                    String artifactId = splitCoordinates[1];
                    String version = splitCoordinates[2];
                    Resource oldLib = res.softwareLibrary(submissionModel.getIdentifier() + "-oldLibrary")
                            .addLiteral(props.BBN_HAS_DEPENDENCY_COORDINATES, ul.getOldDependencyCoordinates())
                            .addLiteral(props.HAS_GROUP_ID, groupId)
                            .addLiteral(props.HAS_ARTIFACT_ID, artifactId)
                            .addLiteral(props.HAS_VERSION, version)
                            .addLiteral(props.BBN_HAS_MAVEN_REPOSITORY, ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve(ul.getRepositoryUrl()).toString());

                    if (ul.getVulnerabilityIdentifiers() != null && ul.getVulnerabilityIdentifiers().length > 0) {
                        for (String vulnerabilityIdentifier : ul.getVulnerabilityIdentifiers()) {
                            oldLib.addLiteral(props.BBN_HAS_KNOWN_VULNERABILITY, vulnerabilityIdentifier);
                            if (vulnerabilityIdentifier.equals("https://github.com/dropbox/dropbox-sdk-java/issues/78")) {
                                deploymentModel.addProperty(
                                        props.HAS_FUNCTIONALITY_SPEC,
                                        res.functionalitySpecVulnerabilityDropbox78()
                                );

                            } else {
                                throw new RuntimeException("Unable to hack a validator into place for vulnerability '" + vulnerabilityIdentifier + "'!");
                            }
                        }
                    }


                    deploymentModel.addProperty(props.BBN_HAS_DEPENDENCY_COORDINATES, oldLib);

                    Resource libraryNode = res.concreteResourceNode(submissionModel.getIdentifier() + "-currentLib");
                    libraryNode.addProperty(props.HAS_RESOURCE, oldLib);
                    instanceResourceNode.addProperty(props.HAS_CONTAINED_NODE, libraryNode);

                    coordinates = ul.getNewDependencyCoordinates();
                    splitCoordinates = coordinates.split(":");
                    groupId = splitCoordinates[0];
                    artifactId = splitCoordinates[1];
                    version = splitCoordinates[2];
                    Resource newLib = res.softwareLibrary(submissionModel.getIdentifier() + "-newLibrary")
                            .addLiteral(props.BBN_HAS_DEPENDENCY_COORDINATES, ul.getNewDependencyCoordinates())
                            .addLiteral(props.HAS_GROUP_ID, groupId)
                            .addLiteral(props.HAS_ARTIFACT_ID, artifactId)
                            .addLiteral(props.HAS_VERSION, version)
                            .addLiteral(props.BBN_HAS_MAVEN_REPOSITORY, ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve(ul.getRepositoryUrl()).toString());

                    deploymentModel.addProperty(props.BBN_HAS_DEPENDENCY_COORDINATES, newLib);

                    Resource resourceMigrationTarget = res.resourceMigrationTarget("libraryUpgradeTarget");
                    deploymentModel.addProperty(props.HAS_RESOURCE_MIGRATION_TARGETS, resourceMigrationTarget);
                    resourceMigrationTarget.addProperty(props.HAS_TARGET_RESOURCE, newLib);
                    resourceMigrationTarget.addProperty(props.BBN_HAS_ORIGINAL_RESOURCE, oldLib);
                }
            }
        }
        return model;
    }

    public static void main(String args[]) {
        try {
            SubmissionModel submissionModel = new SubmissionModel(
                    "1337Model",
//                    new MartiSubmissionModel(
//                            new MartiRequirements(
//                                    null,
//                                    ServerUpgradeLibrary.ElevationApi_2,
//                                    null
//                            )
//                    ),
//                    null,


                    null,
                    new ATAKLiteSubmissionModel(
                            new AtakliteRequirements(
                                    null,
                                    ClientPartialUpgradeLibrary.Dropbox_3_0_6,
                                    null
                            )
                    ),


                    null
            );

            String jsonValue = ImmortalsUtils.nonHtmlEscapingGson.toJson(submissionModel);
            System.out.println(jsonValue);

            DeploymentModelBuilder dmb = new DeploymentModelBuilder(submissionModel);
            Model model = dmb.build();


            ByteArrayOutputStream out = new ByteArrayOutputStream();
            model.write(out, "TURTLE");

            String rval = new String(out.toByteArray());
            System.out.println(rval);
//            try {
//                Files.write(ImmortalsConfig.getInstance().globals.getTtlIngestionDirectory().resolve("deployment_model.ttl"), rval.getBytes());
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
