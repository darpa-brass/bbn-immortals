package mil.darpa.immortals.modulerunner.reporting;

import mil.darpa.immortals.modulerunner.ModuleConfiguration;
import mil.darpa.immortals.modulerunner.configuration.AnalysisComparisonConfig;
import mil.darpa.immortals.modulerunner.configuration.GsonHelper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import javax.annotation.Nonnull;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by awellman@bbn.com on 6/3/16.
 */
public class ExecutionReport {

    private final ModuleConfiguration moduleConfiguration;

    private final GsonHelper.ExecutionDataset executionDataset = new GsonHelper.ExecutionDataset();

    private final LinkedList<String> monitoredValues = new LinkedList();


    public ExecutionReport(@Nonnull ModuleConfiguration moduleConfiguration, @Nonnull AnalysisComparisonConfig analysisConfig) {
        this.moduleConfiguration = moduleConfiguration;
        this.monitoredValues.addAll(analysisConfig.monitoredValues);
    }

    public synchronized void addExecutionData(@Nonnull Map<String, String> configurationMap, @Nonnull ExecutionData executionData) {
        executionDataset.put(new HashMap<>(configurationMap), executionData);

    }

    public void writeReportToJsonFile(@Nonnull String filepath) throws IOException {
        GsonHelper.getInstance().toFile(this, filepath);
    }

    public void generateCSVReport(@Nonnull String filepath) throws IllegalAccessException, NoSuchFieldException, IOException {

        CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
        FileWriter fw = new FileWriter(filepath);
        CSVPrinter csvPrinter = new CSVPrinter(fw, csvFormat);

        List<String> variableHeaders = null;
        LinkedList<String> loopList = new LinkedList<>();

        for (Map<String, String> varianceMap : executionDataset.keySet()) {
            if (variableHeaders == null) {

                Set<String> varianceSet = varianceMap.keySet();
                variableHeaders = new ArrayList<>(varianceSet.size());
                for (String varianceHeaderValue : varianceSet) {
                    variableHeaders.add(varianceHeaderValue);
                }

                List<String> headers = new LinkedList<>(variableHeaders);
                headers.addAll(monitoredValues);
                csvPrinter.printRecord(headers);
            }

            for (String variable : variableHeaders) {
                loopList.add(varianceMap.get(variable));
            }

            for (String monitoredValue : monitoredValues) {
                ExecutionData executionData = executionDataset.get(varianceMap);
                Field monitoredField = ExecutionData.class.getDeclaredField(monitoredValue);

                if (monitoredField.getType() == Integer.class) {
                    loopList.add(Integer.toString((Integer) monitoredField.get(executionData)));
                } else if (monitoredField.getType() == String.class) {
                    loopList.add((String) monitoredField.get(executionData));

                } else if (monitoredField.getType() == Long.class) {
                    loopList.add(Long.toString((Long) monitoredField.get(executionData)));

                } else if (monitoredField.getType() == Boolean.class) {
                    loopList.add(Boolean.toString((Boolean) monitoredField.get(executionData)));
                } else {
                    throw new RuntimeException("Cannot convert specified type '" + monitoredField.getType() + "' to a CSV table element!");
                }
            }
            csvPrinter.printRecord(loopList);
            loopList.clear();
        }

        csvPrinter.flush();
        fw.flush();
        fw.close();
        csvPrinter.close();
    }

}
