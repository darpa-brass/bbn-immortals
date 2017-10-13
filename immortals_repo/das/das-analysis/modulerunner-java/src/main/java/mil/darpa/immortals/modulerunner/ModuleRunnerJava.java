package mil.darpa.immortals.modulerunner;

import mil.darpa.immortals.modulerunner.configuration.AnalysisComparisonConfig;
import mil.darpa.immortals.modulerunner.configuration.GsonHelper;
import mil.darpa.immortals.modulerunner.reporting.ExecutionReport;

import java.io.IOException;

/**
 * Created by awellman@bbn.com on 6/1/16.
 */
public class ModuleRunnerJava {

    public static void runEncryptionStreamAnalysis() throws IOException, ReflectiveOperationException {
        ModuleConfiguration moduleConfiguration = GsonHelper.getInstance().fromFile("configurations/EncryptionStreamModuleConfig.json", ModuleConfiguration.class);
        AnalysisComparisonConfig analysisComparisonConfig = GsonHelper.getInstance().fromFile("configurations/EncryptionStreamAnalysisConfig.json", AnalysisComparisonConfig.class);

        AnalysisRunner acr = new AnalysisRunner(moduleConfiguration, analysisComparisonConfig);

        ExecutionReport er = acr.executeAllAnalyses();
        er.writeReportToJsonFile("EncryptionStreamTestReport.json");
        er.generateCSVReport("EncryptionStreamTestReport.csv");
    }

    public static void runDecryptionStreamAnalysis() throws IOException, ReflectiveOperationException {
        ModuleConfiguration moduleConfiguration = GsonHelper.getInstance().fromFile("configurations/DecryptionStreamModuleConfig.json", ModuleConfiguration.class);
        AnalysisComparisonConfig analysisComparisonConfig = GsonHelper.getInstance().fromFile("configurations/DecryptionStreamAnalysisConfig.json", AnalysisComparisonConfig.class);

        AnalysisRunner acr = new AnalysisRunner(moduleConfiguration, analysisComparisonConfig);

        ExecutionReport er = acr.executeAllAnalyses();
        er.writeReportToJsonFile("DecryptionStreamTestReport.json");
        er.generateCSVReport("DecryptionStreamTestReport.csv");
    }

    public static void runEncryptionDiscreteBytesAnalysis() throws IOException, ReflectiveOperationException {
        ModuleConfiguration moduleConfiguration = GsonHelper.getInstance().fromFile("configurations/EncryptionDiscreteBytesModuleConfig.json", ModuleConfiguration.class);
        AnalysisComparisonConfig analysisComparisonConfig = GsonHelper.getInstance().fromFile("configurations/EncryptionDiscreteBytesAnalysisConfig.json", AnalysisComparisonConfig.class);

        AnalysisRunner acr = new AnalysisRunner(moduleConfiguration, analysisComparisonConfig);

        ExecutionReport er = acr.executeAllAnalyses();
        er.writeReportToJsonFile("EncryptionDiscreteBytesTestReport.json");
        er.generateCSVReport("EncryptionDiscreteBytesTestReport.csv");
    }

//    public static void runDecryptionDiscreteBytesAnalysis() throws IOException, ReflectiveOperationException {
//        ModuleConfiguration moduleConfiguration = GsonHelper.getInstance().fromFile("configurations/DecryptionDiscreteBytesModuleConfig.json", ModuleConfiguration.class);
//        AnalysisComparisonConfig analysisComparisonConfig = GsonHelper.getInstance().fromFile("configurations/DecryptionDiscreteBytesAnalysisConfig.json", AnalysisComparisonConfig.class);
//
//        AnalysisRunner acr = new AnalysisRunner(moduleConfiguration, analysisComparisonConfig);
//
//        ExecutionReport er = acr.executeAllAnalyses();
//        er.writeReportToJsonFile("DecryptionDiscreteBytesTestReport.json");
//        er.generateCSVReport("DecryptionDiscreteBytesTestReport.csv");
//    }

    public static void main(String[] args) {

        try {
            runEncryptionStreamAnalysis();
            runDecryptionStreamAnalysis();
            runEncryptionDiscreteBytesAnalysis();
//            runDecryptionDiscreteBytesAnalysis();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
