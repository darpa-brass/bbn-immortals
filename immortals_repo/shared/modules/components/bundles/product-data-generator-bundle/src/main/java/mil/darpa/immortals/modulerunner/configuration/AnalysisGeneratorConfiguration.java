package mil.darpa.immortals.modulerunner.configuration;

import javax.annotation.Nullable;

/**
 * Created by awellman@bbn.com on 8/5/16.
 */
public class AnalysisGeneratorConfiguration {


//    public final ControlPointFormat controlPointFormat;
//    public final JavaClassType javaType;
//    public final SemanticType semanticType;
    public final int dataTransferUnitsPerBurst;
    public final int dataTransferBurstCount;
    public final int dataTransferIntervalMS;
    public final int testIterations;

    public final String unprocessedDataTargetFilepath;
    public final String processedDataTargetFilepath;
    public final String generatorSourceDataFilepath;
    public final String processedDataTargetValidationFilepath;

    public AnalysisGeneratorConfiguration(
                                          int dataTransferUnitsPerBurst,
                                          int dataTransferBurstCount,
                                          int dataTransferIntervalMS,
                                          int testIterations,
                                          @Nullable String unprocessedDataTargetFilepath,
                                          @Nullable String processedDataTargetFilepath,
                                          @Nullable String generatorSourceDataFilepath,
                                          @Nullable String processedDataTargetValidationFilepath) {
        this.dataTransferUnitsPerBurst = dataTransferUnitsPerBurst;
        this.dataTransferBurstCount = dataTransferBurstCount;
        this.dataTransferIntervalMS = dataTransferIntervalMS;
        this.testIterations = testIterations;
        this.unprocessedDataTargetFilepath = unprocessedDataTargetFilepath;
        this.processedDataTargetFilepath = processedDataTargetFilepath;
        this.generatorSourceDataFilepath = generatorSourceDataFilepath;
        this.processedDataTargetValidationFilepath = processedDataTargetValidationFilepath;
    }

    public AnalysisGeneratorConfiguration clone() {
        return new AnalysisGeneratorConfiguration(
                dataTransferUnitsPerBurst,
                dataTransferBurstCount,
                dataTransferIntervalMS,
                testIterations,
                unprocessedDataTargetFilepath,
                processedDataTargetFilepath,
                generatorSourceDataFilepath,
                processedDataTargetValidationFilepath);

    }

    public String AnalysisGeneratorConfigurationDeclarationClone() {
        return "new " + AnalysisGeneratorConfiguration.class.getName() + "(" +
//        return "new AnalysisGeneratorConfiguration(" +
                dataTransferUnitsPerBurst + "," +
                dataTransferBurstCount + "," +
                dataTransferIntervalMS + "," +
                testIterations + "," +
                unprocessedDataTargetFilepath + "," +
                processedDataTargetFilepath + "," +
                generatorSourceDataFilepath + "," +
                processedDataTargetValidationFilepath + ")";
    }
}
