package mil.darpa.immortals.das.deploymentmodel;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.ATAKLiteSubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.AndroidResource;
import mil.darpa.immortals.core.api.ll.phase2.functionality.DataInTransit;
import mil.darpa.immortals.core.api.ll.phase2.functionality.SecurityStandard;
import mil.darpa.immortals.core.api.ll.phase2.globalmodel.GlobalRequirements;
import mil.darpa.immortals.core.api.ll.phase2.globalmodel.GlobalSubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.JavaResource;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.MartiSubmissionModel;
import mil.darpa.immortals.das.KRTemp;
import mil.darpa.immortals.ontology.BaselineFunctionalitySpec;
import mil.darpa.immortals.ontology.CryptoFunctionalitySpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Created by awellman@bbn.com on 6/21/18.
 */
public class CP2DeploymentModelBuilder {

    private final Map<String, Resource> algorithmMap = new HashMap<>();
    private final Map<String, Resource> chainingModeMap = new HashMap<>();
    private final Map<String, Resource> keysizeMap = new HashMap<>();
    private Model m;
    private Props props;
    private SubmissionModel submissionModel;

    public CP2DeploymentModelBuilder(Model m, Props props, SubmissionModel submissionModel) {
        this.m = m;
        this.props = props;
        this.submissionModel = submissionModel;

    }

    private Resource getPropertyByType(Resource parentResource, Property property, Resource type) {
        return DeploymentModelBuilder.getPropertyByType(m, parentResource, property, type);
    }

    private Resource getResource(Res res) {
        return m.createResource(res.uri);
    }

    private Resource resourceInstance(Res res) {
        Resource r = m.createResource(res.uri + "-" + UUID.randomUUID().toString());
        r.addProperty(RDF.type, getResource(res));
        return r;
    }

    private Resource algBinding(String algorithm) {

        Resource r = algorithmMap.get(algorithm);
        if (r == null) {
            r = resourceInstance(Res.CONFIGURATION_BINDING);
            r.addProperty(props.HAS_BINDING, algorithm);
            r.addProperty(props.HAS_SEMANTIC_TYPE, getResource(Res.CIPHER_ALGORITHM));
            algorithmMap.put(algorithm, r);
        }
        return r;
    }

    private Resource ksBinding(int keysize) {
        String ks = Integer.toString(keysize);

        Resource r = keysizeMap.get(ks);
        if (r == null) {
            r = resourceInstance(Res.CONFIGURATION_BINDING);
            r.addProperty(props.HAS_BINDING, ks);
            r.addProperty(props.HAS_SEMANTIC_TYPE, getResource(Res.CIPHER_KEY_LENGTH));
            keysizeMap.put(ks, r);
        }
        return r;
    }

    private Resource cmBinding(String chainingMode) {
        Resource r = chainingModeMap.get(chainingMode);
        if (r == null) {
            r = resourceInstance(Res.CONFIGURATION_BINDING);
            r.addProperty(props.HAS_BINDING, chainingMode);
            r.addProperty(props.HAS_SEMANTIC_TYPE, getResource(Res.CIPHER_CHAINING_MODE));
            chainingModeMap.put(chainingMode, r);
        }
        return r;
    }

    public void build() {
        buildCp2DeploymentModel();
        buildCp2MinimalEncryptionRequirement();
    }

    private void buildCp2MinimalEncryptionRequirement() {
        Resource cipher = getResource(Res.CIPHER);
        cipher.addLiteral(props.HAS_POJO_PROVENANCE, "com.securboration.immortals.ontology.functionality.alg.encryption.Cipher");

        SecurityStandard ss = submissionModel.globalModel.requirements.dataInTransit.securityStandard;
        String alg = ss.algorithm;
        Integer ks = ss.keySize;
        String cm = ss.cipherChainingMode;

        Resource abpc = resourceInstance(Res.ASPECT_BEST_PRACTICE_CONFIGURATION);
        abpc.addProperty(props.HAS_BOUND_FUNCTIONALITY, cipher);

        if (alg != null) {
            abpc.addProperty(props.HAS_CONFIGURATION_BINDINGS, algBinding(alg));
        }
        if (ks != null) {
            abpc.addProperty(props.HAS_CONFIGURATION_BINDINGS, ksBinding(ks));
        }
        if (cm != null) {
            abpc.addProperty(props.HAS_CONFIGURATION_BINDINGS, cmBinding(cm));
        }
    }

    private void buildCp2DeploymentModel() {
        InputStream is = this.getClass().getResourceAsStream("/cp2_deployment_model.ttl");
        RDFDataMgr.read(m, is, Lang.TTL);

        Resource deploymentModel = getResource(Res.DEPLOYMENT_MODEL_CP2);

        // Add the baseline and crypto feature requirements
        deploymentModel.addProperty(
                props.HAS_FUNCTIONALITY_SPEC,
                getResource(Res.FUNCTIONALITY_SPEC_BASELINE));

        deploymentModel.addProperty(
                props.HAS_FUNCTIONALITY_SPEC,
                getResource(Res.FUNCTIONALITY_SPEC_CRYPTO));


        // Set the adaptation identifier
        deploymentModel.addLiteral(props.HAS_SESSION_IDENTIFIER, submissionModel.sessionIdentifier);

        Resource martiServer =
                m.listStatements(deploymentModel, props.HAS_AVAILABLE_RESOURCES, getResource(Res.MARTI_SERVER)).nextStatement().getObject().asResource();
        martiServer.addLiteral(props.BBN_HAS_ARTIFACT_IDENTIFIER, ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/server/Marti").toString());

        Resource atakClient = m.listStatements(deploymentModel, props.HAS_AVAILABLE_RESOURCES, getResource(Res.ATAKLITE_CLIENT)).nextStatement().getObject().asResource();
        atakClient.addLiteral(props.BBN_HAS_ARTIFACT_IDENTIFIER, ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/client/ATAKLite").toString());

        Resource martiResourceContainmentModel = resourceInstance(Res.RESOURCE_CONTAINMENT_MODEL);
        deploymentModel.addProperty(props.HAS_RESOURCE_CONTAINMENT_MODEL, martiResourceContainmentModel);
        Resource martiConcreteResourceNode = resourceInstance(Res.CONCRETE_RESOURCE_NODE);
        martiResourceContainmentModel.addProperty(props.HAS_RESOURCE_MODEL, martiConcreteResourceNode);
        martiConcreteResourceNode.addProperty(props.HAS_RESOURCE, martiServer);

        Resource atakResourceContainmentModel = resourceInstance(Res.RESOURCE_CONTAINMENT_MODEL);
        deploymentModel.addProperty(props.HAS_RESOURCE_CONTAINMENT_MODEL, atakResourceContainmentModel);
        Resource atakConcreteResourceNode = resourceInstance(Res.CONCRETE_RESOURCE_NODE);
        atakResourceContainmentModel.addProperty(props.HAS_RESOURCE_MODEL, atakConcreteResourceNode);
        atakConcreteResourceNode.addProperty(props.HAS_RESOURCE, atakClient);

        if (submissionModel.martiServerModel != null && submissionModel.martiServerModel.resources != null) {
            Resource martiJavaEnvironment = getPropertyByType(martiServer, props.HAS_RESOURCES, getResource(Res.JAVA_RUNTIME_ENVIRONMENT));
            List<JavaResource> javaResources = Arrays.asList(submissionModel.martiServerModel.resources);
            martiJavaEnvironment.addLiteral(props.HAS_UNLIMITED_CRYPTO_STRENGTH, javaResources.contains(JavaResource.STRONG_CRYPTO));

            if (javaResources.contains(JavaResource.HARWARE_AES)) {
                Resource martiCpu = getPropertyByType(martiServer, props.HAS_RESOURCES, getResource(Res.CPU));
                martiCpu.addProperty(props.HAS_INSTRUCTION_SET_ARCHITECTURE_SUPPORT, getResource(Res.INSTRUCTION_SET_AES_NI));
            }
        }


        if (submissionModel.atakLiteClientModel != null &&
                submissionModel.atakLiteClientModel.resources != null) {
            Resource atakAndroidEnvironment = getPropertyByType(atakClient, props.HAS_RESOURCES, getResource(Res.ANDROID_RUNTIME_ENVIRONMENT));
            List<AndroidResource> androidResources = Arrays.asList(submissionModel.atakLiteClientModel.resources);
            atakAndroidEnvironment.addLiteral(props.HAS_UNLIMITED_CRYPTO_STRENGTH, androidResources.contains(AndroidResource.STRONG_CRYPTO));
        }
    }

    private enum Res {
        ASPECT_BEST_PRACTICE_CONFIGURATION("http://darpa.mil/immortals/ontology/r2.0.0/functionality/aspects#AspectBestPracticeConfiguration"),
        CONFIGURATION_BINDING("http://darpa.mil/immortals/ontology/r2.0.0/functionality#ConfigurationBinding"),
        CIPHER_ALGORITHM("http://darpa.mil/immortals/ontology/r2.0.0/functionality/alg/encryption#CipherAlgorithm"),
        CIPHER_CHAINING_MODE("http://darpa.mil/immortals/ontology/r2.0.0/functionality/alg/encryption#CipherChainingMode"),
        CIPHER("http://darpa.mil/immortals/ontology/r2.0.0/functionality/alg/encryption#Cipher"),
        CIPHER_KEY_LENGTH("http://darpa.mil/immortals/ontology/r2.0.0/functionality/alg/encryption#CipherKeyLength"),
        JAVA_RUNTIME_ENVIRONMENT(KRTemp.immortals_cp.JavaRuntimeEnvironment$),
        ANDROID_RUNTIME_ENVIRONMENT(KRTemp.immortals_cp.AndroidRuntimeEnvironment$),
        CPU(KRTemp.immortals_core.Cpu$),
        INSTRUCTION_SET_AES_NI("http://darpa.mil/immortals/ontology/r2.0.0/resources/compute#InstructionSets.AES_NI"),
        DEPLOYMENT_MODEL_CP2("http://darpa.mil/immortals/ontology/r2.0.0/gmei#DeploymentModelCP2"),
        MARTI_SERVER("http://darpa.mil/immortals/ontology/r2.0.0/cp2#ClientServerEnvironment.MartiServer"),
        ATAKLITE_CLIENT("http://darpa.mil/immortals/ontology/r2.0.0/cp2#ClientServerEnvironment.ClientDevice1"),
        FUNCTIONALITY_SPEC_BASELINE(Props.CUSTOM_NS + BaselineFunctionalitySpec.class.getSimpleName()),
        FUNCTIONALITY_SPEC_CRYPTO(Props.CUSTOM_NS + CryptoFunctionalitySpec.class.getSimpleName()),
        RESOURCE_CONTAINMENT_MODEL("http://darpa.mil/immortals/ontology/r2.0.0/resource/containment#ResourceContainmentModel"),
        CONCRETE_RESOURCE_NODE("http://darpa.mil/immortals/ontology/r2.0.0/resource/containment#ConcreteResourceNode");

        public final String uri;

        Res(String uri) {
            this.uri = uri;
        }
    }

    public static void main(String[] args) {
        SubmissionModel sm = new SubmissionModel(
                "1337Model",
                new MartiSubmissionModel(
                        null,
                        new JavaResource[]{
                                JavaResource.HARWARE_AES
                        }
                ),
                new ATAKLiteSubmissionModel(
                        null,
                        new AndroidResource[]{
                                AndroidResource.STRONG_CRYPTO
                        }
                ),
                new GlobalSubmissionModel(
                        new GlobalRequirements(
                                new DataInTransit(
                                        SecurityStandard.AES_128

                                )

                        )
                )
        );

        DeploymentModelBuilder dmb = new DeploymentModelBuilder(sm);
        Model model = dmb.build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, "TURTLE");

        String rval = new String(out.toByteArray());
        System.out.println(rval);
    }
}
