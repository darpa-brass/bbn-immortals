//package mil.darpa.immortals.modulerunner.configuration;
//
//import mil.darpa.immortals.modulerunner.generators.ControlPointFormat;
//import mil.darpa.immortals.modulerunner.generators.DeploymentPlatform;
//
//import javax.annotation.Nonnull;
//import java.util.ArrayList;
//
///**
// * Created by awellman@bbn.com on 9/14/16.
// */
//public class ModuleCompositionConfiguration extends RuntimeEnvironmentConfiguration {
//
//    public final String compositionIdentifier;
//    public final ArrayList<AnalysisModuleConfiguration> compositionSequence;
//
//    public ModuleCompositionConfiguration(
//            @Nonnull ControlPointFormat controlPointFormat,
//            @Nonnull DeploymentPlatform deploymentPlatform,
//            @Nonnull String compositionIdentifier,
//            @Nonnull ArrayList<AnalysisModuleConfiguration> compositionSequence) {
//        super(controlPointFormat, deploymentPlatform);
//        this.compositionIdentifier = compositionIdentifier;
//        this.compositionSequence = new ArrayList(compositionSequence);
//    }
//}
