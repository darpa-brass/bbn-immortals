package mil.darpa.immortals.das.deploymentmodel;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.das.KRTemp;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 5/22/18.
 */
public class Props {

    public static final String CUSTOM_NS = ImmortalsConfig.getInstance().globals.getImmortalsOntologyUriRoot();
    public static final String CUSTOM_PF = ImmortalsConfig.getInstance().globals.getImmortalsOntologyUriPrefix();

    public final Property HAS_AVAILABLE_RESOURCES;
    public final Property HAS_HUMAN_READABLE_DESCRIPTION;
    public final Property HAS_RESOURCE_CONTAINMENT_MODEL;
    public final Property HAS_RESOURCE_MODEL;
    public final Property HAS_CONTAINED_NODE;
    public final Property HAS_RESOURCE;
    public final Property HAS_RESOURCE_MIGRATION_TARGETS;
    public final Property HAS_SESSION_IDENTIFIER;
    public final Property HAS_RATIONALE;
    public final Property HAS_ARTIFACT_ID;
    public final Property HAS_GROUP_ID;
    public final Property HAS_VERSION;
    public final Property HAS_TARGET_RESOURCE;
    public final Property HAS_FUNCTIONALITY_SPEC;

    // Custom Properties;
    public final Property BBN_HAS_DEPENDENCY_COORDINATES;
    public final Property BBN_HAS_FEATURE_REQUIREMENTS;
    public final Property BBN_HAS_ARTIFACT_IDENTIFIER;
    public final Property BBN_HAS_ORIGINAL_RESOURCE;
    public final Property BBN_HAS_KNOWN_VULNERABILITY;
    public final Property BBN_HAS_FILESYSTEM_LOCATION;
    public final Property BBN_HAS_MAVEN_REPOSITORY;

    public Props(@Nonnull Model model) {

        HAS_AVAILABLE_RESOURCES = model.createProperty(KRTemp.immortals_cp.hasAvailableResources$);
        HAS_HUMAN_READABLE_DESCRIPTION = model.createProperty(KRTemp.immortals_cp.hasHumanReadableDescription$);
        HAS_RESOURCE_CONTAINMENT_MODEL = model.createProperty(KRTemp.immortals_scratchpad.hasResourceContainmentModel$);
        HAS_RESOURCE_MODEL = model.createProperty(KRTemp.immortals_cp.hasResourceModel$);
        HAS_CONTAINED_NODE = model.createProperty(KRTemp.immortals_cp.hasContainedNode$);
        HAS_RESOURCE = model.createProperty(KRTemp.immortals_cp.hasResource$);
        HAS_RESOURCE_MIGRATION_TARGETS = model.createProperty(KRTemp.immortals_scratchpad.hasResourceMigrationTargets$);
        HAS_SESSION_IDENTIFIER = model.createProperty(KRTemp.immortals_cp.hasSessionIdentifier$);
        HAS_RATIONALE = model.createProperty(KRTemp.immortals_core.hasRationale$);
        HAS_ARTIFACT_ID = model.createProperty(KRTemp.immortals_bytecode.hasArtifactId$);
        HAS_GROUP_ID = model.createProperty(KRTemp.immortals_bytecode.hasGroupId$);
        HAS_VERSION = model.createProperty(KRTemp.immortals_bytecode.hasVersion$);
        HAS_TARGET_RESOURCE = model.createProperty(KRTemp.immortals_core.hasTargetResource$);
        HAS_FUNCTIONALITY_SPEC = model.createProperty(KRTemp.immortals_cp.hasFunctionalitySpec$);

        BBN_HAS_DEPENDENCY_COORDINATES = model.createProperty(CUSTOM_NS + "hasDependencyCoordinates");
        BBN_HAS_FEATURE_REQUIREMENTS = model.createProperty(CUSTOM_NS + "hasFeatureRequirements");
        BBN_HAS_ARTIFACT_IDENTIFIER = model.createProperty(CUSTOM_NS + "hasArtifactIdentifier");
        BBN_HAS_ORIGINAL_RESOURCE = model.createProperty(CUSTOM_NS + "hasOriginalResource");
        BBN_HAS_KNOWN_VULNERABILITY = model.createProperty(CUSTOM_NS + "hasKnownVulnerability");
        BBN_HAS_FILESYSTEM_LOCATION = model.createProperty(CUSTOM_NS + "hasFilesystemLocation");
        BBN_HAS_MAVEN_REPOSITORY = model.createProperty(CUSTOM_NS + "hasMavenRepository");
    }
}
