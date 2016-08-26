package mil.darpa.immortals.modulerunner.dfuexecuters;

import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.BinaryData;
import mil.darpa.immortals.core.Semantics;
import mil.darpa.immortals.modulerunner.ClassReflectionHelper;
import mil.darpa.immortals.modulerunner.DFUJavaInterface;
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
public class ByteArrayDfuExecuter extends AbstractDfuExecuter {

    private final ModuleConfiguration moduleConfiguration;
    private final AnalysisConfig analysisConfig;

    public ByteArrayDfuExecuter(@Nonnull ModuleConfiguration moduleConfiguration, @Nonnull AnalysisConfig analysisConfig) {
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
        boolean fillEachTime = (executionParams.containsKey(BinaryData.class.getName()) && executionParams.get(BinaryData.class.getName()).equals(ClassReflectionHelper.FILL_IN_VALUE_KEY));

        for (int j = 0; j < analysisConfig.dataTransferCount; j++) {
            if (fillEachTime) {
                byte[] bytes = (byte[])dataSource.generate();
                executionParams.put(BinaryData.class.getName(), dataSource.generate());

                if (rawOutputTarget != null) {
                    rawOutputTarget.write(bytes);
                }
            }
            time0 = System.currentTimeMillis();
            byte[] received = (byte[]) moduleConfiguration.execute(constructedDfu, executionParams);
            executionData.addTimeDifference(time0, System.currentTimeMillis());
            if (!moduleConfiguration.hasMatchForOneTimeUseParameter(Semantics.Datatype_SerializableDataTargetStream) && processedOutputTarget != null && received != null && received.length > 0) {
                processedOutputTarget.write(received);
            }
        }
    }
}
