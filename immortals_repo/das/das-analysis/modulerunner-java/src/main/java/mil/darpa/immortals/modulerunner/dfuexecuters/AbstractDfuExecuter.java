package mil.darpa.immortals.modulerunner.dfuexecuters;

import mil.darpa.immortals.annotation.dsl.ontology.functionality.Input;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.Output;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.BinaryData;
import mil.darpa.immortals.modulerunner.ClassReflectionHelper;
import mil.darpa.immortals.modulerunner.DFUJavaInterface;
import mil.darpa.immortals.modulerunner.ModuleConfiguration;
import mil.darpa.immortals.modulerunner.configuration.AnalysisConfig;
import mil.darpa.immortals.modulerunner.generators.DataSource;
import mil.darpa.immortals.modulerunner.reporting.ExecutionData;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 6/8/16.
 */
abstract class AbstractDfuExecuter {

    private final ModuleConfiguration moduleConfiguration;
    private final AnalysisConfig analysisConfig;

    AbstractDfuExecuter(@Nonnull ModuleConfiguration moduleConfiguration, @Nonnull AnalysisConfig analysisConfig) {
        this.moduleConfiguration = moduleConfiguration;
        this.analysisConfig = analysisConfig;

    }

    public final ExecutionData execute() throws IOException, ReflectiveOperationException {
        ExecutionData executionData = new ExecutionData(analysisConfig);

        OutputStream rawOutputTarget = null;
        OutputStream processedOutputTarget = null;

        for (int i = 0; i < analysisConfig.testIterations; i++) {

            HashMap<String, Object> oneTimeUseParameters = new HashMap<>();

            if (analysisConfig.unprocessedDataTargetFilepath != null) {
                rawOutputTarget = new FileOutputStream(analysisConfig.unprocessedDataTargetFilepath, (i != 0));
            }

            if (analysisConfig.processedDataTargetFilepath != null) {
                processedOutputTarget = new FileOutputStream(analysisConfig.processedDataTargetFilepath, (i != 0));
                oneTimeUseParameters.put(Output.class.getName(), processedOutputTarget);
            }

            DataSource dataSource = new DataSource(moduleConfiguration, analysisConfig);
            if (analysisConfig.dfuJavaInterface == DFUJavaInterface.InputStream) {
                oneTimeUseParameters.put(Input.class.getName(), dataSource.generate());
            } else if (analysisConfig.dfuJavaInterface == DFUJavaInterface.DiscreteBytes) {
                oneTimeUseParameters.put(BinaryData.class.getName(), ClassReflectionHelper.FILL_IN_VALUE_KEY);
            }

            Object dfuUnderTest = moduleConfiguration.createInstance(oneTimeUseParameters);
            moduleConfiguration.init(dfuUnderTest, oneTimeUseParameters);

            executeIteration(dfuUnderTest, dataSource, oneTimeUseParameters, rawOutputTarget, processedOutputTarget, executionData);

            if (rawOutputTarget != null) {
                rawOutputTarget.flush();
                rawOutputTarget.close();
            }

            if (processedOutputTarget != null) {
                processedOutputTarget.flush();
                processedOutputTarget.close();
            }

            if (analysisConfig.unprocessedDataTargetFilepath != null &&
                    analysisConfig.processedDataTargetFilepath != null) {
                executionData.setFileSizeDifference(
                        FileUtils.sizeOf(new File(analysisConfig.unprocessedDataTargetFilepath)),
                        FileUtils.sizeOf(new File(analysisConfig.processedDataTargetFilepath)));
            } else if (analysisConfig.generatorSourceDataFilepath != null &&
                    analysisConfig.processedDataTargetFilepath != null) {
                executionData.setFileSizeDifference(
                        FileUtils.sizeOf(new File(analysisConfig.generatorSourceDataFilepath)),
                        FileUtils.sizeOf(new File(analysisConfig.processedDataTargetFilepath)));
            }

            if (analysisConfig.processedDataTargetFilepath != null &&
                    analysisConfig.processedDataTargetValidationFilepath != null) {
                boolean fileEquals = FileUtils.contentEquals(
                        new File(analysisConfig.processedDataTargetFilepath),
                        new File(analysisConfig.processedDataTargetValidationFilepath)
                );
                executionData.setFilesAreEqual(fileEquals);
            }
            moduleConfiguration.cleanup(dfuUnderTest);
        }
        return executionData;
    }

    protected abstract void executeIteration(
            Object constructedDfu,
            DataSource dataSource,
            Map<String, Object> executionParams,
            OutputStream rawOutputTarget,
            OutputStream processedOutputTarget,
            ExecutionData executionData
    ) throws IOException, ReflectiveOperationException;
}
