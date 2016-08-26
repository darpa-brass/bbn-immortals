package mil.darpa.immortals.modulerunner;

import mil.darpa.immortals.modulerunner.configuration.AnalysisComparisonConfig;
import mil.darpa.immortals.modulerunner.configuration.AnalysisConfig;
import mil.darpa.immortals.modulerunner.dfuexecuters.ByteArrayDfuExecuter;
import mil.darpa.immortals.modulerunner.dfuexecuters.InputStreamDfuExecuter;
import mil.darpa.immortals.modulerunner.dfuexecuters.OutputStreamDfuExecuter;
import mil.darpa.immortals.modulerunner.reporting.ExecutionData;
import mil.darpa.immortals.modulerunner.reporting.ExecutionReport;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 6/7/16.
 */
public class AnalysisRunner {

    private final ModuleConfiguration moduleConfiguration;
    private final AnalysisComparisonConfig analysisComparisonConfig;

    private final Map<Map<String, String>, AnalysisConfig> analysisConfigurationMap;

    public AnalysisRunner(@Nonnull ModuleConfiguration moduleConfiguration, @Nonnull AnalysisComparisonConfig analysisComparisonConfig) throws IllegalAccessException, NoSuchFieldException {
        this.moduleConfiguration = moduleConfiguration;
        this.analysisComparisonConfig = analysisComparisonConfig;
        this.analysisConfigurationMap = analysisComparisonConfig.getModifiedPermutations();
    }


    public ExecutionReport executeAllAnalyses() throws IOException, ReflectiveOperationException {
        ExecutionReport er = new ExecutionReport(moduleConfiguration, analysisComparisonConfig);

        for (Map<String, String> configurationVarianceMap : analysisConfigurationMap.keySet()) {
            AnalysisConfig configToRun = analysisConfigurationMap.get(configurationVarianceMap);
            ExecutionData executionData = executeAnalysis(configToRun);
            er.addExecutionData(configurationVarianceMap, executionData);
        }
        return er;
    }

    public ExecutionData executeAnalysis(@Nonnull AnalysisConfig analysisConfig) throws IOException, ReflectiveOperationException {

        if (analysisConfig.dfuJavaInterface == DFUJavaInterface.OutputStream) {
            OutputStreamDfuExecuter dfuExecuter = new OutputStreamDfuExecuter(moduleConfiguration, analysisConfig);
            return dfuExecuter.execute();
        } else if (analysisConfig.dfuJavaInterface == DFUJavaInterface.InputStream) {
            InputStreamDfuExecuter dfuExecuter = new InputStreamDfuExecuter(moduleConfiguration, analysisConfig);
            return dfuExecuter.execute();
        } else if (analysisConfig.dfuJavaInterface == DFUJavaInterface.DiscreteBytes) {
            ByteArrayDfuExecuter dfuExecuter = new ByteArrayDfuExecuter(moduleConfiguration, analysisConfig);
            return dfuExecuter.execute();
        } else {
            throw new RuntimeException("Unexpected DFU interface '" + analysisConfig.dfuJavaInterface + "'!");
        }
    }

}
