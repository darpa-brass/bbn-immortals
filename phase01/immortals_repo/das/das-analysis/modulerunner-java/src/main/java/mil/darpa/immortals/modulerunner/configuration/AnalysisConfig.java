package mil.darpa.immortals.modulerunner.configuration;

import mil.darpa.immortals.modulerunner.DFUJavaInterface;
import mil.darpa.immortals.modulerunner.generators.DataSource;
import mil.darpa.immortals.modulerunner.generators.SemanticTypeMapping;

/**
 * Created by awellman@bbn.com on 6/8/16.
 */
public class AnalysisConfig {
    public DFUJavaInterface dfuJavaInterface;
    public DataSource.GeneratorJavaType dataTransferJavaType;
    public DataSource.DataTransferUnit dataTransferUnit;
    public Integer dataTransferUnitBurstCount;
    public Integer dataTransferCount;
    public Integer testIterations;
    public SemanticTypeMapping generatorSemanticTypeOverride;
    public String processedDataTargetFilepath;
    public String processedDataTargetValidationFilepath;
    public String unprocessedDataTargetFilepath;
    public String generatorSourceDataFilepath;

    public AnalysisConfig() {
    }

    public synchronized AnalysisConfig clone() {
        AnalysisConfig runConfig = new AnalysisConfig();
        runConfig.dfuJavaInterface = this.dfuJavaInterface;
        runConfig.dataTransferJavaType = this.dataTransferJavaType;
        runConfig.dataTransferUnit = this.dataTransferUnit;
        runConfig.dataTransferUnitBurstCount = this.dataTransferUnitBurstCount;
        runConfig.dataTransferCount = this.dataTransferCount;
        runConfig.testIterations = this.testIterations;
        runConfig.generatorSemanticTypeOverride = this.generatorSemanticTypeOverride;
        runConfig.processedDataTargetFilepath = this.processedDataTargetFilepath;
        runConfig.processedDataTargetValidationFilepath = this.processedDataTargetValidationFilepath;
        runConfig.unprocessedDataTargetFilepath = this.unprocessedDataTargetFilepath;
        runConfig.generatorSourceDataFilepath = this.generatorSourceDataFilepath;
        return runConfig;
    }
}
