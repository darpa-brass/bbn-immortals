package mil.darpa.immortals.core.das.adaptationmodules.hddrass;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.extensions.HddRassConfiguration;
import mil.darpa.immortals.das.ImmortalsProcessBuilder;
import mil.darpa.immortals.das.context.ContextManager;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;

import javax.annotation.Nonnull;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by awellman@bbn.com on 1/29/18.
 */
public class HddRassExecuter {

    private final HddRassConfiguration configuration;

    private final HddRassInitializationObject initObject;

    private final String adaptationIdentifier;

    public HddRassExecuter(@Nonnull DasAdaptationContext dac, @Nonnull HddRassInitializationObject initObject) {
        this.initObject = initObject;
        this.adaptationIdentifier = dac.getAdaptationIdentifer();
        configuration = ImmortalsConfig.getInstance().extensions.hddrass;
    }

    public Process execute() throws IOException {
        if (ImmortalsConfig.getInstance().debug.isUseMockExtensionHddRass()) {
            return mockExecute();
        }

        Path mutationScript = configuration.getExecutionWorkingDirectory(adaptationIdentifier).resolve("mutatescript.json");
        FileWriter configFileWriter = new FileWriter(mutationScript.toFile());
        configFileWriter.write(ImmortalsUtils.nonHtmlEscapingGson.toJson(initObject));
        configFileWriter.flush();
        configFileWriter.close();


        String[] cmd = {
                ImmortalsConfig.getInstance().build.augmentations.getJavaExecutablePath(),
                "-jar",
                configuration.getExePath(),
                "-jsonFile",
                mutationScript.toAbsolutePath().toString()
        };

        ImmortalsProcessBuilder processBuilder =
                new ImmortalsProcessBuilder(
                        adaptationIdentifier,
                        configuration.getIdentifier());

        Process p = processBuilder.command(cmd).start();
        return p;
    }

    private Process mockExecute() throws IOException {

        ImmortalsErrorHandler.reportFatalError("Mock hddRASS being done! This should not happen unless you are debugging!");
        
        String[] cmd = {
                "sed",
                "-i",
                "-e",
                "s/cot\\.setElevationData(elevationApi\\.getElevation(cot\\.getLon(), cot\\.getLat()));//g",
                initObject.getApplicationPath().resolve("src/com/bbn/marti/immortals/pipes/CotByteBufferPipe.java").toString()
        };

        ImmortalsProcessBuilder processBuilder =
                new ImmortalsProcessBuilder(adaptationIdentifier, configuration.getIdentifier());

        Process p = processBuilder.command(cmd).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return p;
    }
}
