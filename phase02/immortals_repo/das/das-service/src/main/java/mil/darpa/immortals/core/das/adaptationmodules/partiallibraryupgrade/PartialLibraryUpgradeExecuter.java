package mil.darpa.immortals.core.das.adaptationmodules.partiallibraryupgrade;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.extensions.PartialLibraryUpdatesConfiguration;
import mil.darpa.immortals.das.ImmortalsProcessBuilder;
import mil.darpa.immortals.das.context.ContextManager;
import mil.darpa.immortals.das.context.DasAdaptationContext;

import javax.annotation.Nonnull;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by awellman@bbn.com on 1/29/18.
 */
public class PartialLibraryUpgradeExecuter {

    private final PartialLibraryUpdatesConfiguration configuration;

    private final PartialLibraryUpgradeInitializationObject initObject;

    private final String adaptationIdentifier;

    public PartialLibraryUpgradeExecuter(@Nonnull DasAdaptationContext dac, @Nonnull PartialLibraryUpgradeInitializationObject initObject) {
        this.initObject = initObject;
        this.adaptationIdentifier = dac.getAdaptationIdentifer();
        configuration = ImmortalsConfig.getInstance().extensions.partiallibraryupgrade;
    }

    public Process execute() throws IOException {
        Path workingDir = configuration.getExecutionWorkingDirectory(adaptationIdentifier);

        Path templateFolder = configuration.getWorkingDirectoryTemplateFolder();
        if (templateFolder != null) {
            ImmortalsUtils.Copy.copyDir(templateFolder, workingDir);
//            Files.createDirectory(workingDir.resolve("output"));
        }


        Path configScript = workingDir.resolve("adaptation_configuration.json");
        FileWriter configFileWriter = new FileWriter(configScript.toFile());
        configFileWriter.write(ImmortalsUtils.nonHtmlEscapingGson.toJson(initObject));
        configFileWriter.flush();
        configFileWriter.close();

        String exePath = configuration.getExePath().replace(configuration.getWorkingDirectoryTemplateFolder().toString(), workingDir.toString());

        String[] cmd = {
                "python",
                exePath,
                "--configFile",
                configScript.toAbsolutePath().toString()
        };

        ImmortalsProcessBuilder processBuilder =
                new ImmortalsProcessBuilder(
                        adaptationIdentifier,
                        configuration.getIdentifier());
        processBuilder.directory(workingDir.toFile());

        Process p = processBuilder.command(cmd).start();
        return p;
    }
}
