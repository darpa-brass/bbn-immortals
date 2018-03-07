package mil.darpa.immortals.config;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by awellman@bbn.com on 9/29/17.
 */
public class ImmortalsConfig {

    private static transient ImmortalsConfig configuration;

    public final GlobalsConfig globals = new GlobalsConfig();
    public final DasServiceConfiguration dasService = new DasServiceConfiguration();
    public final TestHarnessConfiguration testHarness = new TestHarnessConfiguration();
    public final TestAdapterConfiguration testAdapter = new TestAdapterConfiguration();
    public final FusekiConfiguration fuseki = new FusekiConfiguration();
    public final KnowledgeRepoConfiguration knowledgeRepoService = new KnowledgeRepoConfiguration();
    public final BuildConfiguration build = new BuildConfiguration();
    public final DebugConfiguration debug = new DebugConfiguration();
    public final ExtensionsConfiguration extensions = new ExtensionsConfiguration();
    public final DeploymentEnvironmentConfiguration deploymentEnvironment = new DeploymentEnvironmentConfiguration();

    ImmortalsConfig() {
    }

    public static synchronized ImmortalsConfig getInstance() {
        if (configuration == null) {
            // Attempt to load an override file if the environment variable points ot one
            String override_path = System.getenv("IMMORTALS_OVERRIDE_FILE");
            if (override_path == null) {
                configuration = new ImmortalsConfig();
            } else {
                try {
                    File f = new File(override_path);

                    JsonObject configJsonObject = StaticHelper.gson.fromJson(new FileReader(f), JsonObject.class);
                    configuration = StaticHelper.gson.fromJson(configJsonObject, ImmortalsConfig.class);

                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return configuration;
    }
    
    public static synchronized String getInstanceAsJsonString() {
        return StaticHelper.gson.toJson(getInstance());
    }

    public static void main(String[] argsv) {
        try {
            ImmortalsConfig configuration = ImmortalsConfig.getInstance();
            System.out.println("DIRo: " + configuration.globals.getImmortalsRoot());
            System.out.println("IRo: " + GlobalsConfig.staticImmortalsRoot);
            System.out.println("DIRe: " + configuration.globals.getImmortalsRepo());
            System.out.println("IRe: " + GlobalsConfig.staticImmortalsRepo);

            System.out.println("APR: " + configuration.build.augmentations.getMavenPublishRepo());
            System.out.println("ASR: " + configuration.build.augmentations.getAndroidSdkRoot());
            System.out.println("ASJ: " + configuration.build.augmentations.getAndroidSdkJarPath());
            System.out.println("TAP: " + configuration.testAdapter.getPort());

            System.out.println("TTLID: " + configuration.globals.getTtlIngestionDirectory());
            System.out.println("TTLAD: " + configuration.extensions.getProducedTtlOutputDirectory());
            System.out.println("KRGPTTLD: " + configuration.extensions.krgp.getTtlTargetDirectory());

            Files.write(Paths.get("produced_config.json"), ImmortalsConfig.getInstanceAsJsonString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
