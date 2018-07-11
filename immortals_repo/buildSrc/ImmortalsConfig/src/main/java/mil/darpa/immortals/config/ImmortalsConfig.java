package mil.darpa.immortals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by awellman@bbn.com on 9/29/17.
 */
public class ImmortalsConfig {
    
    private static transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

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

    private String[] targetApplicationUris = new String[]{
            GlobalsConfig.staticImmortalsRoot.resolve("shared/modules/core").toString(),
            GlobalsConfig.staticImmortalsRoot.resolve("shared/modules/dfus/ElevationApi-1").toString(),
            GlobalsConfig.staticImmortalsRoot.resolve("shared/modules/dfus/ElevationApi-2").toString(),
            GlobalsConfig.staticImmortalsRoot.resolve("shared/modules/dfus/TakServerDataManager").toString(),
            GlobalsConfig.staticImmortalsRoot.resolve("shared/modules/dfus/JavaxCrypto").toString(),
            GlobalsConfig.staticImmortalsRoot.resolve("shared/modules/dfus/BogoCipher").toString(),
            GlobalsConfig.staticImmortalsRoot.resolve("shared/modules/dfus/BouncyCastleCipher").toString(),
            GlobalsConfig.staticImmortalsRoot.resolve("shared/modules/dfus/NoOpCipher").toString(),
            GlobalsConfig.staticImmortalsRoot.resolve("applications/server/Marti").toString(),
            GlobalsConfig.staticImmortalsRoot.resolve("applications/client/ATAKLite").toString(),
            GlobalsConfig.staticImmortalsRoot.resolve("applications/examples/ThirdPartyLibAnalysisAndroidApp").toString(),
            GlobalsConfig.staticImmortalsRoot.resolve("applications/examples/ThirdPartyLibAnalysisJavaApp").toString()
    };

    public String[] getTargetApplicationUris() {
        return Arrays.copyOf(targetApplicationUris, targetApplicationUris.length);
    }

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

                    JsonObject configJsonObject = gson.fromJson(new FileReader(f), JsonObject.class);
                    configuration = gson.fromJson(configJsonObject, ImmortalsConfig.class);

                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return configuration;
    }

    public static synchronized String getInstanceAsJsonString() {
        return gson.toJson(getInstance());
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
