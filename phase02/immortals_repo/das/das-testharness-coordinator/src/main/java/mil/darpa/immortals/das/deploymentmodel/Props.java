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

    final Property HAS_AVAILABLE_RESOURCES;
    final Property HAS_HUMAN_READABLE_DESCRIPTION;
    final Property HAS_RESOURCE_CONTAINMENT_MODEL;
    final Property HAS_RESOURCE_MODEL;
    final Property HAS_CONTAINED_NODE;
    final Property HAS_RESOURCE;
    final Property HAS_RESOURCE_MIGRATION_TARGETS;
    final Property HAS_SESSION_IDENTIFIER;
    final Property HAS_RATIONALE;
    final Property HAS_ARTIFACT_ID;
    final Property HAS_GROUP_ID;
    final Property HAS_VERSION;
    final Property HAS_TARGET_RESOURCE;
    final Property HAS_FUNCTIONALITY_SPEC;

    final Property HAS_CAUSE_EFFECT_ASSERTIONS;
    final Property HAS_INSTRUCTION_SET_ARCHITECTURE_SUPPORT;
    final Property HAS_UNLIMITED_CRYPTO_STRENGTH;
    final Property HAS_RESOURCES;
    final Property HAS_BOUND_FUNCTIONALITY;
    final Property HAS_CONFIGURATION_BINDINGS;
    final Property HAS_BINDING;
    final Property HAS_SEMANTIC_TYPE;
    final Property HAS_POJO_PROVENANCE;

    // Custom Properties;
    final Property BBN_HAS_DEPENDENCY_COORDINATES;
    final Property BBN_HAS_FEATURE_REQUIREMENTS;
    final Property BBN_HAS_ARTIFACT_IDENTIFIER;
    final Property BBN_HAS_ORIGINAL_RESOURCE;
    final Property BBN_HAS_KNOWN_VULNERABILITY;
    final Property BBN_HAS_FILESYSTEM_LOCATION;
    final Property BBN_HAS_MAVEN_REPOSITORY;

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

        HAS_CAUSE_EFFECT_ASSERTIONS = model.createProperty(KRTemp.immortals_scratchpad.hasCauseEffectAssertions$);
        HAS_INSTRUCTION_SET_ARCHITECTURE_SUPPORT = model.createProperty(KRTemp.immortals_core.hasInstructionSetArchitectureSupport$);
        HAS_UNLIMITED_CRYPTO_STRENGTH = model.createProperty(KRTemp.immortals_cp.hasUnlimitedCryptoStrengh$);
        HAS_RESOURCES = model.createProperty(KRTemp.immortals_core.hasResources$);
        HAS_BOUND_FUNCTIONALITY = model.createProperty("http://darpa.mil/immortals/ontology/r2.0.0#hasBoundFunctionality");
        HAS_CONFIGURATION_BINDINGS = model.createProperty("http://darpa.mil/immortals/ontology/r2.0.0#hasConfigurationBindings");
        HAS_BINDING = model.createProperty("http://darpa.mil/immortals/ontology/r2.0.0#hasBinding");
        HAS_SEMANTIC_TYPE = model.createProperty("http://darpa.mil/immortals/ontology/r2.0.0#hasSemanticType");
        HAS_POJO_PROVENANCE = model.createProperty("http://darpa.mil/immortals/ontology/r2.0.0#hasPojoProvenance");

        BBN_HAS_DEPENDENCY_COORDINATES = model.createProperty(CUSTOM_NS + "hasDependencyCoordinates");
        BBN_HAS_FEATURE_REQUIREMENTS = model.createProperty(CUSTOM_NS + "hasFeatureRequirements");
        BBN_HAS_ARTIFACT_IDENTIFIER = model.createProperty(CUSTOM_NS + "hasArtifactIdentifier");
        BBN_HAS_ORIGINAL_RESOURCE = model.createProperty(CUSTOM_NS + "hasOriginalResource");
        BBN_HAS_KNOWN_VULNERABILITY = model.createProperty(CUSTOM_NS + "hasKnownVulnerability");
        BBN_HAS_FILESYSTEM_LOCATION = model.createProperty(CUSTOM_NS + "hasFilesystemLocation");
        BBN_HAS_MAVEN_REPOSITORY = model.createProperty(CUSTOM_NS + "hasMavenRepository");
    }
}
