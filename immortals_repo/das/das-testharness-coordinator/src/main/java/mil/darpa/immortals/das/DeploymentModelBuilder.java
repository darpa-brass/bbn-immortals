package mil.darpa.immortals.das;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.RequirementsInterface;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModelInterface;
import mil.darpa.immortals.core.api.ll.phase2.UpgradableLibraryInterface;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.MartiRequirements;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.MartiSubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.ServerUpgradeLibrary;
import mil.darpa.immortals.ontology.BaselineFunctionalAspect;
import mil.darpa.immortals.ontology.BaselineFunctionalitySpec;
import mil.darpa.immortals.ontology.GetElevationFunctionalAspect;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by awellman@bbn.com on 4/2/18.
 */
public class DeploymentModelBuilder {

    private final SubmissionModel submissionModel;

    private final String CUSTOM_NS = ImmortalsConfig.getInstance().globals.getImmortalsOntologyUriRoot();
    private final String CUSTOM_PF = ImmortalsConfig.getInstance().globals.getImmortalsOntologyUriPrefix();

    private final Model model = ModelFactory.createDefaultModel();

    private final Property PROP_HAS_AVAILABLE_RESOURCES;
    private final Property PROP_HAS_HUMAN_READABLE_DESCRIPTION;
    private final Property PROP_HAS_RESOURCE_CONTAINMENT_MODEL;
    private final Property PROP_HAS_RESOURCE_MODEL;
    private final Property PROP_HAS_CONTAINED_NODE;
    private final Property PROP_HAS_RESOURCE;
    private final Property PROP_HAS_RESOURCE_MIGRATION_TARGETS;
    private final Property PROP_HAS_SESSION_IDENTIFIER;
    private final Property PROP_HAS_RATIONALE;
    private final Property PROP_HAS_ARTIFACT_ID;
    private final Property PROP_HAS_GROUP_ID;
    private final Property PROP_HAS_VERSION;
    private final Property PROP_HAS_TARGET_RESOURCE;
    private final Property PROP_HAS_FUNCTIONALITY_SPEC;


    //    private final Resource RES_CP3_DEPLOYMENT_MODEL;
    private final Resource RES_DEPLOYMENT_MODEL;
    private final Resource RES_MARTI_SERVER;
    private final Resource RES_ATAKLITE_CLIENT;
    private final Resource RES_SOFTWARE_LIBRARY;
    private final Resource RES_RESOURCE_CONTAINMENT_MODEL;
    private final Resource RES_CONCRETE_RESOURCE_NODE;
    private final Resource RES_RESOURCE_MIGRATION_TARGET;


    // Custom resources


    // Custom Properties and Resources
    private final Property PROP_HAS_DEPENDENCY_COORDINATES;
    private final Property PROP_HAS_FEATURE_REQUIREMENTS;
    private final Property PROP_HAS_ARTIFACT_IDENTIFIER;
    private final Property PROP_HAS_ORIGINAL_RESOURCE;
    private final Resource RES_FUNCTIONAL_ASPECT_BASELINE;
    private final Resource RES_FUNCTIONAL_ASPECT_ELEVATION;
    private final Resource RES_FUNCTIONALITY_SPEC_BASELINE;

    public DeploymentModelBuilder(@Nonnull SubmissionModel submissionModel) {
        this.submissionModel = submissionModel;

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
        model.setNsPrefix(CUSTOM_PF, CUSTOM_NS);

        PROP_HAS_AVAILABLE_RESOURCES = model.createProperty(KRTemp.immortals_cp.hasAvailableResources$);
        PROP_HAS_HUMAN_READABLE_DESCRIPTION = model.createProperty(KRTemp.immortals_cp.hasHumanReadableDescription$);
        PROP_HAS_RESOURCE_CONTAINMENT_MODEL = model.createProperty(KRTemp.immortals_scratchpad.hasResourceContainmentModel$);
        PROP_HAS_RESOURCE_MODEL = model.createProperty(KRTemp.immortals_cp.hasResourceModel$);
        PROP_HAS_CONTAINED_NODE = model.createProperty(KRTemp.immortals_cp.hasContainedNode$);
        PROP_HAS_RESOURCE = model.createProperty(KRTemp.immortals_cp.hasResource$);
        PROP_HAS_RESOURCE_MIGRATION_TARGETS = model.createProperty(KRTemp.immortals_scratchpad.hasResourceMigrationTargets$);
        PROP_HAS_SESSION_IDENTIFIER = model.createProperty(KRTemp.immortals_cp.hasSessionIdentifier$);
        PROP_HAS_RATIONALE = model.createProperty(KRTemp.immortals_core.hasRationale$);
        PROP_HAS_ARTIFACT_ID = model.createProperty(KRTemp.immortals_bytecode.hasArtifactId$);
        PROP_HAS_GROUP_ID = model.createProperty(KRTemp.immortals_bytecode.hasGroupId$);
        PROP_HAS_VERSION = model.createProperty(KRTemp.immortals_bytecode.hasVersion$);
        PROP_HAS_TARGET_RESOURCE = model.createProperty(KRTemp.immortals_core.hasTargetResource$);
        PROP_HAS_FUNCTIONALITY_SPEC = model.createProperty(KRTemp.immortals_cp.hasFunctionalitySpec$);

        PROP_HAS_DEPENDENCY_COORDINATES = model.createProperty(CUSTOM_NS + "hasDependencyCoordinates");
        PROP_HAS_FEATURE_REQUIREMENTS = model.createProperty(CUSTOM_NS + "hasFeatureRequirements");
        PROP_HAS_ARTIFACT_IDENTIFIER = model.createProperty(CUSTOM_NS + "hasArtifactIdentifier");
        PROP_HAS_ORIGINAL_RESOURCE = model.createProperty(CUSTOM_NS + "hasOriginalResource");

        RES_DEPLOYMENT_MODEL = model.createResource(KRTemp.immortals_scratchpad.DeploymentModel$);
        RES_MARTI_SERVER = model.createResource("http://darpa.mil/immortals/ontology/r2.0.0/cp2#ClientServerEnvironment.MartiServer");
        RES_MARTI_SERVER.addLiteral(PROP_HAS_ARTIFACT_IDENTIFIER, "mil.darpa.immortals:Marti:2.0-LOCAL");
        RES_ATAKLITE_CLIENT = model.createResource("com.securboration.immortals.ontology.cp2.ClientServerEnvironment$ClientDevice1");
        RES_ATAKLITE_CLIENT.addLiteral(PROP_HAS_ARTIFACT_IDENTIFIER, "mil.darpa.immortals:ATAKLite:2.0-LOCAL");
        RES_RESOURCE_CONTAINMENT_MODEL = model.createResource(KRTemp.immortals_cp.ResourceContainmentModel$);
        RES_CONCRETE_RESOURCE_NODE = model.createResource(KRTemp.immortals_cp.ConcreteResourceNode$);
        RES_RESOURCE_MIGRATION_TARGET = model.createResource(KRTemp.immortals_scratchpad.ResourceMigrationTarget$);
        RES_SOFTWARE_LIBRARY = model.createResource(KRTemp.immortals_core.SoftwareLibrary$);


        RES_FUNCTIONAL_ASPECT_BASELINE = model.createResource(CUSTOM_NS + BaselineFunctionalAspect.class.getSimpleName());
        RES_FUNCTIONAL_ASPECT_ELEVATION = model.createResource(CUSTOM_NS + GetElevationFunctionalAspect.class.getSimpleName());
        RES_FUNCTIONALITY_SPEC_BASELINE = model.createResource(CUSTOM_NS + BaselineFunctionalitySpec.class.getSimpleName());
    }


    public Model build() {
        // Create the deployment model
        Resource deploymentModel = model.createResource(KRTemp.immortals_scratchpad.DeploymentModel$ + "CP3", RES_DEPLOYMENT_MODEL);


        // Add the baseline feature requirements
        deploymentModel.addProperty(PROP_HAS_FUNCTIONALITY_SPEC, RES_FUNCTIONALITY_SPEC_BASELINE);

        // Set the adaptation identifier
        deploymentModel.addProperty(PROP_HAS_SESSION_IDENTIFIER, submissionModel.sessionIdentifier);

        // Add the base resource model
        Resource resourceContainmentModel = model.createResource(RES_RESOURCE_CONTAINMENT_MODEL + "-" + UUID.randomUUID().toString(), RES_RESOURCE_CONTAINMENT_MODEL);
        deploymentModel.addProperty(PROP_HAS_RESOURCE_CONTAINMENT_MODEL, resourceContainmentModel);

        // If Marti is defined, add it to the map of SubmissionModels and map of resource identifiers
        Map<String, SubmissionModelInterface> submissionModels = new HashMap<>();
        Map<String, Resource> platformResources = new HashMap<>();
        if (submissionModel.martiServerModel != null) {
            submissionModels.put("Marti", submissionModel.martiServerModel);

            platformResources.put("Marti", RES_MARTI_SERVER);
        }

        // If ATAKLite is defined, add it to the map of SubmissionModels and map of resource identifiers
        if (submissionModel.atakLiteClientModel != null) {
            submissionModels.put("ATAKLite", submissionModel.atakLiteClientModel);
            platformResources.put("ATAKLite", RES_ATAKLITE_CLIENT);
        }

        // Then, for each submission model, add it
        for (String identifier : submissionModels.keySet()) {
            SubmissionModelInterface submissionModel = submissionModels.get(identifier);
            Resource environmentResource = platformResources.get(identifier);

            // Add the global resource
            deploymentModel.addProperty(PROP_HAS_AVAILABLE_RESOURCES, environmentResource);

            // Add the resource model
            Resource serverResourceNode = model.createResource(RES_CONCRETE_RESOURCE_NODE + "-" + identifier + "-" + UUID.randomUUID().toString(), RES_CONCRETE_RESOURCE_NODE);
            resourceContainmentModel.addProperty(PROP_HAS_RESOURCE_MODEL, serverResourceNode);
            serverResourceNode.addProperty(PROP_HAS_RESOURCE, environmentResource);


            if (identifier.equals("Marti")) {
                // Add the default server deployment requirements
                serverResourceNode.addProperty(PROP_HAS_FEATURE_REQUIREMENTS, RES_FUNCTIONAL_ASPECT_BASELINE);
                serverResourceNode.addProperty(PROP_HAS_FEATURE_REQUIREMENTS, RES_FUNCTIONAL_ASPECT_ELEVATION);
            }

            if (submissionModel.getRequirements() != null) {


                RequirementsInterface lui = submissionModel.getRequirements();

                // And if it has requirements, add them
                if (lui.getUpgradeLibrary() != null) {
                    // Add the library requirements
                    UpgradableLibraryInterface ul = lui.getUpgradeLibrary();

                    String coordinates = ul.getOldDependencyCoordinates();
                    String[] splitCoordinates = coordinates.split(":");
                    String groupId = splitCoordinates[0];
                    String artifactId = splitCoordinates[1];
                    String version = splitCoordinates[2];
                    Resource oldLib =
                            model.createResource(RES_SOFTWARE_LIBRARY + "-" + identifier + "-oldLibrary-" + UUID.randomUUID().toString(), RES_SOFTWARE_LIBRARY)
                                    .addLiteral(PROP_HAS_DEPENDENCY_COORDINATES, ul.getOldDependencyCoordinates())
                                    .addLiteral(PROP_HAS_GROUP_ID, groupId)
                                    .addLiteral(PROP_HAS_ARTIFACT_ID, artifactId)
                                    .addLiteral(PROP_HAS_VERSION, version);


                    deploymentModel.addProperty(PROP_HAS_DEPENDENCY_COORDINATES, oldLib);

                    Resource libraryNode = model.createResource(RES_CONCRETE_RESOURCE_NODE + "-" + identifier + "-currentLib-" + UUID.randomUUID().toString(), RES_CONCRETE_RESOURCE_NODE);
                    libraryNode.addProperty(PROP_HAS_RESOURCE, oldLib);
                    serverResourceNode.addProperty(PROP_HAS_CONTAINED_NODE, libraryNode);

                    coordinates = ul.getNewDependencyCoordinates();
                    splitCoordinates = coordinates.split(":");
                    groupId = splitCoordinates[0];
                    artifactId = splitCoordinates[1];
                    version = splitCoordinates[2];
                    Resource newLib =
                            model.createResource(RES_SOFTWARE_LIBRARY + "-newLibrary-" + UUID.randomUUID().toString(), RES_SOFTWARE_LIBRARY)
                                    .addLiteral(PROP_HAS_DEPENDENCY_COORDINATES, ul.getNewDependencyCoordinates())
                                    .addLiteral(PROP_HAS_GROUP_ID, groupId)
                                    .addLiteral(PROP_HAS_ARTIFACT_ID, artifactId)
                                    .addLiteral(PROP_HAS_VERSION, version);

                    deploymentModel.addProperty(PROP_HAS_DEPENDENCY_COORDINATES, newLib);

                    Resource resourceMigrationTarget =
                            model.createResource(RES_RESOURCE_MIGRATION_TARGET + "-libraryUpgradeTarget-" + UUID.randomUUID().toString(), RES_RESOURCE_MIGRATION_TARGET);
                    deploymentModel.addProperty(PROP_HAS_RESOURCE_MIGRATION_TARGETS, resourceMigrationTarget);
                    resourceMigrationTarget.addProperty(PROP_HAS_TARGET_RESOURCE, newLib);
                    resourceMigrationTarget.addProperty(PROP_HAS_ORIGINAL_RESOURCE, oldLib);
                }
            }
        }

        // TODO: add client

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, "TURTLE");

        String rval = new String(out.toByteArray());
        System.out.println(rval);
        return model;
    }

    public static void main(String args[]) {
        try {
            SubmissionModel submissionModel = new SubmissionModel(
                    "1337Model",
                    new MartiSubmissionModel(
                            new MartiRequirements(
                                    null,
                                    ServerUpgradeLibrary.ElevationApi_2,
                                    null
                            )
                    ),
                    null,
                    null
            );

            System.out.println(ImmortalsUtils.nonHtmlEscapingGson.toJson(submissionModel));
            
            DeploymentModelBuilder dmb = new DeploymentModelBuilder(submissionModel);
            dmb.build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
