package mil.darpa.immortals.das.sourcecomposer;

import mil.darpa.immortals.das.configuration.ControlPointAugmenter;
import mil.darpa.immortals.das.configuration.DfuCompositionConfiguration;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Created by awellman@bbn.com on 9/27/16.
 */
public class Main {
    public static void main(String args[]) {
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


            SourceComposer.ApplicationInstance applicationInstance = sourceComposer.initializeApplicationInstance(EnvironmentConfiguration.CompositionTarget.Client_ATAKLite);

            // Get the path to the SACommunicationService file and modify it for CP1
            Path saCommunicationServicePath = applicationInstance.getApplicationPath().resolve("src/com/bbn/ataklite/service/SACommunicationService.java");

            // Add the new dependencies from CP1 to the application instance
            applicationInstance.addDependency("mil.darpa.immortals.dfus:LocationProviderAndroidGpsBuiltIn:+");
            applicationInstance.addDependency("mil.darpa.immortals.dfus:LocationProviderManualSimulated:+");

            // Construct the composition configuration (you will generate it from the ontology/dsl/deployment model)
            ArrayList<String> parameters = new ArrayList<>();
            parameters.add("0.5");
            DfuCompositionConfiguration compositionConfiguration = ControlPointAugmenter.constructCP2ControlPointComposition(
                    "mil.darpa.immortals.dfus:ImageUtilsAndroid:+",
                    "mil.darpa.immortals.dfus.images.BitmapReader",
                    "mil.darpa.immortals.dfus:ImageUtilsAndroid:+",
                    "mil.darpa.immortals.dfus.images.BitmapScaler",
                    parameters,
                    sessionIdentifier
            );
            sourceComposer.executeCP2Composition(applicationInstance, compositionConfiguration);

            applicationInstance.build();


//            BuildBridge.getInstance().buildApplication(applicationInstance.getApplicationPath());

//            SourceComposer.ApplicationInstance applicationInstance = sourceComposer.initializeApplicationInstance(EnvironmentConfiguration.CompositionTarget.Client_ConsumingPipeRunner, sessionIdentifier);
//            applicationInstance.addDependency("mil.darpa.immortals.dfus:LocationProviderAndroidGpsBuiltIn:+");
//            applicationInstance.addDependency("mil.darpa.immortals.dfus:LocationProviderManualSimulated:+");
//            DfuCompositionAnalysisConfiguration compositionAnalysisConfiguration = GsonHelper.getInstance().fromFile("CompositionAnalysisExample.json", DfuCompositionAnalysisConfiguration.class);
//            sourceComposer.executeAnalysis(applicationInstance, compositionAnalysisConfiguration);


            // Build the application


        } catch (CompositionException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
