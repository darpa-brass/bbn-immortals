package mil.darpa.immortals.core.das.adaptationmodules.hddrass;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.extensions.HddRassConfiguration;
import mil.darpa.immortals.das.ImmortalsProcessBuilder;
import mil.darpa.immortals.das.context.DasAdaptationContext;

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


        ImmortalsUtils.getGson().toJson(initObject);

        Path mutationScript = ImmortalsConfig.getInstance().extensions.hddrass.getExecutionWorkingDirectory(adaptationIdentifier).resolve("mutatescript.json");
        FileWriter configFileWriter = new FileWriter(mutationScript.toFile());
        configFileWriter.write(ImmortalsUtils.getNonHtmlEscapingGson().toJson(initObject));
        configFileWriter.flush();
        configFileWriter.close();


        String[] cmd = {
                ImmortalsConfig.getInstance().build.augmentations.getJavaExecutablePath(),
                "-jar",
                configuration.getJarPath().toString(),
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
}
