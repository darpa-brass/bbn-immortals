package mil.darpa.immortals.das.sourcecomposer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.airlift.airline.*;
import mil.darpa.immortals.das.hacks.configuration.applications.CompositionTarget;
import mil.darpa.immortals.das.configuration.GsonHelper;
import mil.darpa.immortals.das.buildtools.GradleHelper;
import mil.darpa.immortals.das.hacks.Phase1Hacks;
import mil.darpa.immortals.das.sourcecomposer.configuration.DfuCompositionConfiguration;
import mil.darpa.immortals.das.sourcecomposer.configuration.DfuSubstitutionInstance;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 9/27/16.
 */
public class Main {


    @Command(name = "dasinterfacedemo", description = "Executes a demo of the DAS interface to the source composer")
    public static class DasInterface implements Runnable {
        public void run() {
            try {
                // Load the default environment configuration stored as a resource within the BuildBridge.  This will then
                // be returned whenever EnvironmentConfiguration.getInstance() is called. This should only be done once!
                // The only difficult aspect of it is determining the immortals root directory. For that uses the following list of prioritized locations:
                // 1.  The value passed to the loadDefaultEnvironmentConfiguration()
                // 2.  The value defined in the environment configuration (blank by default)
                // 3.  The first parent directory of the jar that contains a number of expected files (applications, das, dsl, knowledge-repo, shared, build.gradle, settings.gradle)
                //
                // I recommend passing a known value, but this _should_ work fine
                EnvironmentConfiguration.initializeDefaultEnvironmentConfiguration();

                String sessionIdentifier = "S" + Long.toString(System.currentTimeMillis());


                // Load the SourceComposer with the initialized environment configuration
                SourceComposer sourceComposer = new SourceComposer(sessionIdentifier);


                ApplicationInstance applicationInstance = sourceComposer.initializeApplicationInstance(CompositionTarget.Client_ATAKLite);

                // Get the path to the SACommunicationService file and modify it for CP1
                Path saCommunicationServicePath = applicationInstance.getApplicationPath().resolve("src/com/bbn/ataklite/service/SACommunicationService.java");

                // Add the new dependencies from CP1 to the application instance
                applicationInstance.addDependency("mil.darpa.immortals.dfus:LocationProviderAndroidGpsBuiltIn:+");
                applicationInstance.addDependency("mil.darpa.immortals.dfus:LocationProviderManualSimulated:+");

                // Construct the composition configuration (you will generate it from the ontology/dsl/deployment model)
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("http://darpa.mil/immortals/ontology/r2.0.0/functionality/imagescaling#ImageScalingFactor", "0.5");
                DfuCompositionConfiguration compositionConfiguration = Phase1Hacks.constructCP2ControlPointComposition(
                        "mil.darpa.immortals.dfus:ImageUtilsAndroid:+",
                        "mil.darpa.immortals.dfus.images.BitmapReader",
                        "mil.darpa.immortals.dfus:ImageUtilsAndroid:+",
                        "mil.darpa.immortals.dfus.images.BitmapScaler",
                        parameters,
                        sessionIdentifier
                );
                sourceComposer.executeCP2Composition(applicationInstance, compositionConfiguration);

                applicationInstance.build();

                GradleHelper.getInstance().buildApplication(applicationInstance.getApplicationPath());

                // Build the application


            } catch (CompositionException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String args[]) {
        Cli.CliBuilder<Runnable> builder = Cli.<Runnable>builder("substitution")
                .withDescription("IMMoRTALS Substitution builder")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class, GenLPR.class, GenBRR.class, DasInterface.class);

        Cli<Runnable> parser = builder.build();
        parser.parse(args).run();
    }

    private static void executeComposition(DfuCompositionConfiguration configuration) throws CompositionException, IOException {
        SourceComposer sourceComposer = new SourceComposer(configuration.sessionIdentifier);
        
        SourceComposer.DfuInstance dfuInstance = sourceComposer.constructAndPublishComposedDfu(configuration);

        ApplicationInstance app = sourceComposer.initializeApplicationInstance(
                CompositionTarget.Client_ConsumingPipeRunner);

        app.executeAugmentation(configuration, dfuInstance);
        app.build();
    }

    @Command(name = "genlpr", description = "Generates Location Provider Runner android apks.")
    public static class GenLPR implements Runnable {
        public void run() {
            try {
                EnvironmentConfiguration.initializeDefaultEnvironmentConfiguration();

                List<DfuSubstitutionInstance> configurations = GsonHelper.getInstance().listFromResourceIdentifier(
                        "/LocationProviderRunnerConfigurations.json", DfuSubstitutionInstance.class);

                for (DfuSubstitutionInstance configuration : configurations) {
                    generateSubstitutionRunner(configuration);
                }

            } catch (CompositionException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void generateSubstitutionRunner(DfuSubstitutionInstance configuration) throws CompositionException, IOException {
        SourceComposer sourceComposer = new SourceComposer(configuration.sessionIdentifier);
        ApplicationInstance app = sourceComposer.initializeApplicationInstance(
                CompositionTarget.Client_SubstitutionRunner);
        app.executeAugmentation(configuration);
        app.build();
    }


    @Command(name = "genbrr", description = "Generates bitmap resizer analysis apks.")
    public static class GenBRR implements Runnable {
        public void run() {
            try {
                EnvironmentConfiguration.initializeDefaultEnvironmentConfiguration();

                DfuCompositionConfiguration configuration = GsonHelper.getInstance().fromResourceIdentifier(
                        "/ImageScalerAnalysisConfiguration.json", DfuCompositionConfiguration.ShallowDfuCompositionConfiguration.class).toDfuCompositionConfiguration();

                Set<DfuCompositionConfiguration> configs = configuration.produceAnalysisConfigurations();
                
                JsonArray configurationArray = new JsonArray();
                
                for (DfuCompositionConfiguration config : configs) {
                    executeComposition(config);
                    configurationArray.add(GsonHelper.getInstance().toJsonElement(config));
                }

                JsonObject details = new JsonObject();
                details.add("compositionConfigurations", configurationArray);
                GsonHelper.getInstance().toFile(details, EnvironmentConfiguration.getInstance()
                        .getSynthesisRootPath("").resolve("analysis.json").toAbsolutePath().toString());

            } catch (CompositionException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}