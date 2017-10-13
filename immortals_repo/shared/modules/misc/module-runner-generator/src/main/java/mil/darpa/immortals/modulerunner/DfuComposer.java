package mil.darpa.immortals.modulerunner;

import mil.darpa.immortals.modulerunner.applicationgenerators.BasicGenerator;
import mil.darpa.immortals.modulerunner.applicationgenerators.InputPipelineGenerator;
import mil.darpa.immortals.modulerunner.applicationgenerators.OutputPipelineGenerator;
import mil.darpa.immortals.modulerunner.configuration.*;
import mil.darpa.immortals.modulerunner.generators.ControlPointFormat;
import mil.darpa.immortals.modulerunner.generators.DeploymentPlatform;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 8/11/16.
 */
public class DfuComposer {

//    private final String sourceProjectPath;
//    private final String targetProductsPath;
//    private final String fileWithinSourceProjectPathToModify;
//    private final DeploymentPlatform platform;

    private final LinkedList<AnalysisModuleConfiguration> moduleConfigurations = new LinkedList<>();

    public DfuComposer(@Nonnull DfuCompositionConfiguration configuration, @Nonnull String dfuTargetPath) {

    }
//
//    public DfuComposer(@Nonnull DeploymentPlatform platform, @Nonnull String sourceProjectPath, @Nonnull String targetProductsPath, @Nonnull String fileWithinSourceProjectPathToModify, @Nullable AnalysisGeneratorConfiguration generatorConfiguration) {
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

//    public void clearConfigurations() {
//        moduleConfigurations.clear();
//    }

    public void constructDfu() {

    }

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
//            BasicGenerator bg = new BasicGenerator(moduleConfigurations, BasicMainActivity.class, null);
//            bg.regenerateFile(productPath + "/" + fileWithinSourceProjectPathToModify);
//        } else if (moduleConfigurations.get(0).controlPointFormat == ControlPointFormat.InputPipe) {
//            InputPipelineGenerator ipg = new InputPipelineGenerator(moduleConfigurations, BasicMainActivity.class, null);
//            ipg.regenerateFile(productPath + "/" + fileWithinSourceProjectPathToModify);
//        } else if (moduleConfigurations.get(0).controlPointFormat == ControlPointFormat.OutputPipe) {
//            OutputPipelineGenerator opg = new OutputPipelineGenerator(moduleConfigurations, BasicMainActivity.class, null);
//            opg.regenerateFile(productPath + "/" + fileWithinSourceProjectPathToModify);
//        }
//    }
}
