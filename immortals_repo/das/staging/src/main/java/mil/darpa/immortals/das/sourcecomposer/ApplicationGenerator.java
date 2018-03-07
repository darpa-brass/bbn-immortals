//package mil.darpa.immortals.sourcecomposer;
//
//import mil.darpa.immortals.modulerunner.configuration.*;
//import mil.darpa.immortals.modulerunner.generators.ControlPointFormat;
//import mil.darpa.immortals.modulerunner.generators.DeploymentPlatform;
//import GsonHelper;
//import org.apache.commons.io.FileUtils;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Set;
//
///**
// * Created by awellman@bbn.com on 8/11/16.
// */
//public class ApplicationGenerator {
//
//    private final String sourceProjectPath;
//    private final String targetProductsPath;
//    private final String fileWithinSourceProjectPathToModify;
//    private final DeploymentPlatform platform;
//
//    private final LinkedList<AnalysisModuleConfiguration> moduleConfigurations = new LinkedList<>();
//
//    public ApplicationGenerator(@Nonnull DeploymentPlatform platform, @Nonnull String sourceProjectPath, @Nonnull String targetProductsPath, @Nonnull String fileWithinSourceProjectPathToModify, @Nullable AnalysisGeneratorConfiguration generatorConfiguration) {
//        this.platform = platform;
//        this.sourceProjectPath = sourceProjectPath;
//        this.targetProductsPath = targetProductsPath;
//        this.fileWithinSourceProjectPathToModify = fileWithinSourceProjectPathToModify;
//    }
//
////    public void addConfiguration(@Nonnull String packagePath, @Nonnull String aspect) {
////        // Get the configuration
////        ConfigurationManager cm = ConfigurationManager.getInstance();
////        AnalysisModuleConfiguration moduleConfiguration = cm.getModuleConfiguration(packagePath, aspect);
////        moduleConfigurations.add(moduleConfiguration);
////    }
//
//    public void addConfiguration(@Nonnull AnalysisModuleConfiguration moduleConfiguration) {
//        moduleConfigurations.add(moduleConfiguration);
//    }
//
////    public void clearConfigurations() {
////        moduleConfigurations.clear();
////    }
//
//    public void createApplication(@Nonnull String applicationName) throws IOException, ReflectiveOperationException {
//        // Get the path info
//        Path productPath = Paths.get(targetProductsPath, applicationName);
//        File productFile = productPath.toFile();
//
//        // If it exists, move it to a  backup file
//        if (productFile.exists()) {
//            FileUtils.moveDirectory(productFile, new File(Paths.get(productPath.toString()) + "-" + System.currentTimeMillis()));
//        }
//
//        // Make a copy of the project
//        FileUtils.copyDirectory(new File(sourceProjectPath), productPath.toFile());
//
//        if (moduleConfigurations.get(0).controlPointFormat == ControlPointFormat.Basic) {
//            BasicGenerator bg = new BasicGenerator(moduleConfigurations, mil.darpa.immortals.modulerunner.BasicMainActivity.class, null);
//            bg.regenerateFile(productPath + "/" + fileWithinSourceProjectPathToModify);
//        } else if (moduleConfigurations.get(0).controlPointFormat == ControlPointFormat.InputPipe) {
//            InputPipelineGenerator ipg = new InputPipelineGenerator(moduleConfigurations, mil.darpa.immortals.modulerunner.BasicMainActivity.class, null);
//            ipg.regenerateFile(productPath + "/" + fileWithinSourceProjectPathToModify);
//        } else if (moduleConfigurations.get(0).controlPointFormat == ControlPointFormat.OutputPipe) {
//            OutputPipelineGenerator opg = new OutputPipelineGenerator(moduleConfigurations, mil.darpa.immortals.modulerunner.BasicMainActivity.class, null);
//            opg.regenerateFile(productPath + "/" + fileWithinSourceProjectPathToModify);
//        }
//    }
//
//    public static void main(String[] args) {
//        try {
//
//            if (args.length > 0) {
//                if (!Files.exists(Paths.get(args[0]))) {
//                    System.err.println("The file '" + args[0] + "' does not exist!\nExiting.");
//                    System.exit(1);
//                }
//
//                ModuleCompositionConfiguration compositionConfiguration = GsonHelper.getInstance().fromFilepath(args[0], ModuleCompositionConfiguration.class);
//
//                ConfigurationManager cm = ConfigurationManager.getInstance();
//                List<AnalysisModuleConfiguration> configurationSequence = new ArrayList<>(compositionConfiguration.compositionSequence.size());
//
//                int idx = 0;
//                for (AnalysisModuleConfiguration configurationElement : compositionConfiguration.compositionSequence) {
//                    Set<AnalysisModuleConfiguration> matchingConfigurations = cm.getModuleConfigurations(compositionConfiguration, configurationElement);
//                    if (matchingConfigurations.size() == 0) {
//                        System.err.println("Could not find a matching configuration for composition sequence item at index " + Integer.toString(idx) + "!");
//                        System.exit(1);
//                    } else if (matchingConfigurations.size() < 1) {
//                        System.err.println("Found multiple configurations for composition sequence item at index " + Integer.toString(idx) + "!");
//                    } else {
//                        configurationSequence.add(matchingConfigurations.iterator().next());
//                    }
//                    idx++;
//                }
//
//                ApplicationGenerator ag = new ApplicationGenerator(
//                        compositionConfiguration.deploymentPlatform,
//                        "/Users/austin/Documents/workspaces/primary/immortals/repo/immortals/shared/modules/misc/AndroidRunner",
//                        "/Users/austin/Documents/workspaces/primary/immortals/repo/immortals/das/das-analytics/generated/",
//                        "src/main/java/mil/darpa/immortals/modulerunner/BasicMainActivity.java",
//                        null
//                );
//
//                for (AnalysisModuleConfiguration loopConfiguration : configurationSequence) {
//                    ag.addConfiguration(loopConfiguration);
//                }
//                ag.createApplication(compositionConfiguration.compositionIdentifier);
//
//
////            } else {
////
////                ConfigurationManager cm = ConfigurationManager.getInstance();
////
////                ApplicationGenerator ag = new ApplicationGenerator(
////                        DeploymentPlatform.ANDROID,
////                        "/Users/austin/Documents/workspaces/primary/immortals/repo/immortals/das/das-analytics/applications/AndroidRunner",
////                        "/Users/austin/Documents/workspaces/primary/immortals/repo/immortals/das/das-analytics/generated/",
////                        "src/main/java/mil/darpa/immortals/modulerunner/BasicMainActivity.java",
////                        null
////                );
////
////                AnalysisModuleConfiguration writerModuleConfiguration = cm.getModuleConfiguration(
////                        "mil.darpa.immortals.dfus.images.BitmapWriter",
////                        "com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorProcessImage"
////                );
////
////                AnalysisModuleConfiguration downsizerModuleConfiguration = cm.getModuleConfiguration(
////                        "mil.darpa.immortals.dfus.images.BitmapDownsizer",
////                        "com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorProcessImage"
////                );
////
////                AnalysisModuleConfiguration readerModuleConfiguration = cm.getModuleConfiguration(
////                        "mil.darpa.immortals.dfus.images.BitmapReader",
////                        "com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorProcessImage"
////                );
////
////                ag.addConfiguration(writerModuleConfiguration);
////                ag.addConfiguration(downsizerModuleConfiguration);
////                ag.addConfiguration(readerModuleConfiguration);
////                ag.createApplication("InputPipeResizingSampleApp");
////
////
////                readerModuleConfiguration.setControlPointFormat(ControlPointFormat.OutputPipe);
////                readerModuleConfiguration.constructorParameterMap.put(Output.class.getName(),
////                        readerModuleConfiguration.constructorParameterMap.get(Input.class.getName()));
////                readerModuleConfiguration.constructorParameterMap.remove(Input.class.getName());
////
////                downsizerModuleConfiguration.setControlPointFormat(ControlPointFormat.OutputPipe);
////                downsizerModuleConfiguration.constructorParameterMap.put(Output.class.getName(),
////                        downsizerModuleConfiguration.constructorParameterMap.get(Input.class.getName()));
////                downsizerModuleConfiguration.constructorParameterMap.remove(Input.class.getName());
////
////                writerModuleConfiguration.setControlPointFormat(ControlPointFormat.OutputPipe);
////                writerModuleConfiguration.constructorParameterMap.put(Output.class.getName(),
////                        writerModuleConfiguration.constructorParameterMap.get(Input.class.getName()));
////                writerModuleConfiguration.constructorParameterMap.remove(Input.class.getName());
////
////                ag.clearConfigurations();
////                ag.addConfiguration(readerModuleConfiguration);
////                ag.addConfiguration(downsizerModuleConfiguration);
////                ag.addConfiguration(writerModuleConfiguration);
////                ag.createApplication("OutputPipeResizingSampleApp");
//            }
//
//        } catch (IOException | ReflectiveOperationException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//}
