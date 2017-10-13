package mil.darpa.immortals.modulerunner.dfuexecuters;

import mil.darpa.immortals.core.Semantics;
import mil.darpa.immortals.modulerunner.ModuleConfiguration;
import mil.darpa.immortals.modulerunner.configuration.AnalysisConfig;
import mil.darpa.immortals.modulerunner.generators.DataSource;
import mil.darpa.immortals.modulerunner.reporting.ExecutionData;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 6/8/16.
 */
public class InputStreamDfuExecuter extends AbstractDfuExecuter {

    private final ModuleConfiguration moduleConfiguration;
    private final AnalysisConfig analysisConfig;

    public InputStreamDfuExecuter(@Nonnull ModuleConfiguration moduleConfiguration, @Nonnull AnalysisConfig analysisConfig) {
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

        int readCount;
        long time0;
        byte[] byteBuffer = new byte[analysisConfig.dataTransferUnitBurstCount];


        InputStream streamUnderTest;
        if (constructedDfu instanceof InputStream) {
            streamUnderTest = (InputStream) constructedDfu;
        } else {
            streamUnderTest = (InputStream) moduleConfiguration.execute(constructedDfu, executionParams);
        }

        for (int j = 0; j < analysisConfig.dataTransferCount; j++) {
            time0 = System.currentTimeMillis();
            readCount = streamUnderTest.read(byteBuffer);
            executionData.addTimeDifference(time0, System.currentTimeMillis());
            if (!moduleConfiguration.hasMatchForOneTimeUseParameter(Semantics.Datatype_SerializableDataTargetStream) && processedOutputTarget != null && readCount >= 0) {
                processedOutputTarget.write(byteBuffer, 0, readCount);
            }
        }
    }
}
