package mil.darpa.immortals.das.deploymentmodel;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.das.KRTemp;
import mil.darpa.immortals.ontology.BaselineFunctionalAspect;
import mil.darpa.immortals.ontology.BaselineFunctionalitySpec;
import mil.darpa.immortals.ontology.GetElevationFunctionalAspect;
import mil.darpa.immortals.ontology.VulnerabilityDropboxFunctionalitySpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import javax.annotation.Nonnull;
import java.util.UUID;

import static mil.darpa.immortals.das.deploymentmodel.Props.CUSTOM_NS;

/**
 * Created by awellman@bbn.com on 5/22/18.
 */
public class Resources {

    private final Model model;
    private final Props props;


    public Resources(@Nonnull Model model, @Nonnull Props props) {
        this.model = model;
        this.props = props;
    }

    private Resource DEPLOYMENT_MODEL;
    private Resource DEPLOYMENT_MODEL_CP3;
    private Resource MARTI_SERVER;
    private Resource ATAKLITE_CLIENT;
    private Resource SOFTWARE_LIBRARY;
    private Resource RESOURCE_CONTAINMENT_MODEL;
    private Resource CONCRETE_RESOURCE_NODE;
    private Resource RESOURCE_MIGRATION_TARGET;

    // Custom Resources
    private Resource BBN_FUNCTIONAL_ASPECT_BASELINE;
    private Resource BBN_FUNCTIONAL_ASPECT_ELEVATION;
    private Resource BBN_FUNCTIONALITY_SPEC_BASELINE;
    private Resource BBN_FUNCTIONALITY_SPEC_VULNERABILITY_DROPBOX_78;
    private Resource BBN_EXAMPLE_APP_JAVA;
    private Resource BBN_EXAMPLE_APP_ANDROID;

    public synchronized Resource deploymentModelCP3() {
        if (DEPLOYMENT_MODEL == null) {
            DEPLOYMENT_MODEL = model.createResource(KRTemp.immortals_scratchpad.DeploymentModel$);
        }
        if (DEPLOYMENT_MODEL_CP3 == null) {
            DEPLOYMENT_MODEL_CP3 = model.createResource(KRTemp.immortals_scratchpad.DeploymentModel$ + "CP3", DEPLOYMENT_MODEL);
        }
        return DEPLOYMENT_MODEL_CP3;
    }

    public synchronized Resource martiServer() {
        if (MARTI_SERVER == null) {
            MARTI_SERVER = model.createResource("http://darpa.mil/immortals/ontology/r2.0.0/cp2#ClientServerEnvironment.MartiServer");
            MARTI_SERVER.addLiteral(props.BBN_HAS_ARTIFACT_IDENTIFIER,
                    ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/server/Marti").toString());
        }
        return MARTI_SERVER;
    }

    public synchronized Resource atakliteClient() {
        if (ATAKLITE_CLIENT == null) {
            ATAKLITE_CLIENT = model.createResource("com.securboration.immortals.ontology.cp2.ClientServerEnvironment$ClientDevice1");
            ATAKLITE_CLIENT.addLiteral(props.BBN_HAS_ARTIFACT_IDENTIFIER,
                    ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/client/ATAKLite").toString());
        }
        return ATAKLITE_CLIENT;
    }

    public synchronized Resource exampleAppJava() {
        if (BBN_EXAMPLE_APP_JAVA == null) {
            BBN_EXAMPLE_APP_JAVA = model.createResource(CUSTOM_NS + "ExampleAppJava");
            BBN_EXAMPLE_APP_JAVA.addLiteral(props.BBN_HAS_ARTIFACT_IDENTIFIER,
                    ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/examples/ThirdPartyLibAnalysisJavaApp").toString());
        }
        return BBN_EXAMPLE_APP_JAVA;
    }

    public synchronized Resource exampleAppAndroid() {
        if (BBN_EXAMPLE_APP_ANDROID == null) {
            BBN_EXAMPLE_APP_ANDROID = model.createResource(CUSTOM_NS + "ExampleAppAndroid");
            BBN_EXAMPLE_APP_ANDROID.addLiteral(props.BBN_HAS_ARTIFACT_IDENTIFIER,
                    ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/examples/ThirdPartyLibAnalysisAndroidApp").toString());

        }
        return BBN_EXAMPLE_APP_ANDROID;
    }

    public synchronized Resource resourceContainmentModel(@Nonnull String tag) {
        if (RESOURCE_CONTAINMENT_MODEL == null) {
            RESOURCE_CONTAINMENT_MODEL = model.createResource(KRTemp.immortals_cp.ResourceContainmentModel$);
        }
        return model.createResource(
                RESOURCE_CONTAINMENT_MODEL.getURI() + "-" + tag + "-" + UUID.randomUUID().toString(),
                RESOURCE_CONTAINMENT_MODEL);
    }

    public synchronized Resource concreteResourceNode(@Nonnull String tag) {
        if (CONCRETE_RESOURCE_NODE == null) {
            CONCRETE_RESOURCE_NODE = model.createResource(KRTemp.immortals_cp.ConcreteResourceNode$);
        }
        Resource r = model.createResource(
                CONCRETE_RESOURCE_NODE.getURI() + "-" + tag + "-" + UUID.randomUUID().toString(),
                CONCRETE_RESOURCE_NODE);

        if (tag.equals("Marti")) {
            r.addProperty(props.BBN_HAS_FEATURE_REQUIREMENTS, functionalAspectElevation());
            r.addProperty(props.BBN_HAS_FEATURE_REQUIREMENTS, functionalAspectBaseline());
        }

        return r;
    }

    public synchronized Resource resourceMigrationTarget(@Nonnull String tag) {
        if (RESOURCE_MIGRATION_TARGET == null) {
            RESOURCE_MIGRATION_TARGET = model.createResource(KRTemp.immortals_scratchpad.ResourceMigrationTarget$);
        }
        return model.createResource(
                RESOURCE_MIGRATION_TARGET.getURI() + "-" + tag + "-" + UUID.randomUUID().toString(),
                RESOURCE_MIGRATION_TARGET);
    }

    public synchronized Resource softwareLibrary(@Nonnull String tag) {
        if (SOFTWARE_LIBRARY == null) {
            SOFTWARE_LIBRARY = model.createResource(KRTemp.immortals_core.SoftwareLibrary$);
        }
        return model.createResource(
                SOFTWARE_LIBRARY.getURI() + "-" + tag + "-" + UUID.randomUUID().toString(),
                SOFTWARE_LIBRARY);
    }


    public synchronized Resource functionalAspectBaseline() {
        if (BBN_FUNCTIONAL_ASPECT_BASELINE == null) {
            BBN_FUNCTIONAL_ASPECT_BASELINE = model.createResource(CUSTOM_NS + BaselineFunctionalAspect.class.getSimpleName());
        }
        return BBN_FUNCTIONAL_ASPECT_BASELINE;
    }

    public synchronized Resource functionalAspectElevation() {
        if (BBN_FUNCTIONAL_ASPECT_ELEVATION == null) {
            BBN_FUNCTIONAL_ASPECT_ELEVATION = model.createResource(CUSTOM_NS + GetElevationFunctionalAspect.class.getSimpleName());
        }
        return BBN_FUNCTIONAL_ASPECT_ELEVATION;
    }

    public synchronized Resource functionalitySpecBaseline() {
        if (BBN_FUNCTIONALITY_SPEC_BASELINE == null) {
            BBN_FUNCTIONALITY_SPEC_BASELINE = model.createResource(CUSTOM_NS + BaselineFunctionalitySpec.class.getSimpleName());
        }
        return BBN_FUNCTIONALITY_SPEC_BASELINE;
    }

    public synchronized Resource functionalitySpecVulnerabilityDropbox78() {
        if (BBN_FUNCTIONALITY_SPEC_VULNERABILITY_DROPBOX_78 == null) {
            BBN_FUNCTIONALITY_SPEC_VULNERABILITY_DROPBOX_78 = model.createResource(CUSTOM_NS + VulnerabilityDropboxFunctionalitySpec.class.getSimpleName());
        }
        return BBN_FUNCTIONALITY_SPEC_VULNERABILITY_DROPBOX_78;
    }
}
