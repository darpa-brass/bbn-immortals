package mil.darpa.immortals.core.das.adaptationmodules.hddrass;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.extensions.HddRassConfiguration;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.das.ImmortalsProcessBuilder;

import javax.annotation.Nonnull;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Created by awellman@bbn.com on 1/29/18.
 */
public class HddRassAdaptationModule {

    private final AdaptationTargetBuildInstance applicationBuildInstance;

    private final HddRassConfiguration configuration;

    private final HddRassInitializationObject initObject;

    public HddRassAdaptationModule(@Nonnull AdaptationTargetBuildInstance applicationBuildInstance,
                                   @Nonnull List<String> validators) {
        this.applicationBuildInstance = applicationBuildInstance;
        this.initObject = new HddRassInitializationObject(applicationBuildInstance, validators);
        this.configuration = ImmortalsConfig.getInstance().extensions.hddrass;
    }

    public Process execute() throws IOException {

        ImmortalsUtils.getGson().toJson(initObject);

        Path mutationScript = ImmortalsConfig.getInstance().globals.getAdaptationComponentWorkingDirectory(
                applicationBuildInstance.getAdaptationIdentifier(), configuration.getIdentifier())
                .resolve("mutationscript.json");

        FileWriter configFileWriter = new FileWriter(mutationScript.toFile());
        configFileWriter.write(ImmortalsUtils.getGson().toJson(initObject));
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
                        applicationBuildInstance.getAdaptationIdentifier(),
                        configuration.getIdentifier());

        Process p = processBuilder.command(cmd).start();
        return p;
    }

    public static void main(String[] args) {
        try {
            String timestamp = Long.toString(System.currentTimeMillis());
            String adaptationIdentifier = "adaptation" + timestamp.substring(0, timestamp.length() - 4);

            GradleKnowledgeBuilder gkb = new GradleKnowledgeBuilder();
            gkb.buildKnowledge(null);

            AdaptationTargetBuildInstance abi = gkb.getBuildInstance("Marti", adaptationIdentifier);

            HddRassAdaptationModule adaptationModule = new HddRassAdaptationModule(abi, Arrays.asList(
                    "com.bbn.marti.Tests.testSaTransmission",
                    "com.bbn.marti.Tests.testImageTransmission"
            ));

            Process p = adaptationModule.execute();
            p.waitFor();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
