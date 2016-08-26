package mil.darpa.immortals.modulerunner.dfuexecuters;

import mil.darpa.immortals.modulerunner.ModuleConfiguration;
import mil.darpa.immortals.modulerunner.configuration.AnalysisConfig;
import mil.darpa.immortals.modulerunner.generators.DataSource;
import mil.darpa.immortals.modulerunner.reporting.ExecutionData;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 6/8/16.
 */
public class OutputStreamDfuExecuter extends AbstractDfuExecuter {

    private final ModuleConfiguration moduleConfiguration;
    private final AnalysisConfig analysisConfig;

    public OutputStreamDfuExecuter(@Nonnull ModuleConfiguration moduleConfiguration, @Nonnull AnalysisConfig analysisConfig) {
        super(moduleConfiguration, analysisConfig);
        this.moduleConfiguration = moduleConfiguration;
        this.analysisConfig = analysisConfig;

    }

    protected void executeIteration(
            Object constructedDfu,
            DataSource dataSource,
            Map<String, Object> executionParams,
            OutputStream rawOutputTarget,
            OutputStream processedOutputTarget,
            ExecutionData executionData
    ) throws IOException, ReflectiveOperationException {

        long time0;
        byte[] byteBuffer;

        OutputStream streamUnderTest;
        if (constructedDfu instanceof OutputStream) {
            streamUnderTest = (OutputStream)constructedDfu;
        } else {
            streamUnderTest = (OutputStream) moduleConfiguration.execute(constructedDfu, executionParams);
        }

        for (int j = 0; j < analysisConfig.dataTransferCount; j++) {
            byteBuffer = (byte[]) dataSource.generate();
            time0 = System.currentTimeMillis();
            streamUnderTest.write(byteBuffer);
            executionData.addTimeDifference(time0, System.currentTimeMillis());
            if (rawOutputTarget != null) {
                rawOutputTarget.write(byteBuffer);
            }
        }

        streamUnderTest.flush();
        streamUnderTest.close();
    }
}
