package mil.darpa.immortals.core.das.knowledgebuilders.building;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildBase;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.adaptationtargets.building.BuildPlatform;
import mil.darpa.immortals.core.das.adaptationtargets.building.DeploymentTarget;
import mil.darpa.immortals.core.das.knowledgebuilders.IKnowledgeBuilder;
import org.apache.jena.rdf.model.Model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 1/29/18.
 */
public class GradleKnowledgeBuilder implements IKnowledgeBuilder {

    private final Map<String, AdaptationTargetBuildBase> adaptationBuildTargets = new HashMap<>();

    @Override
    public Model buildKnowledge(Map<String, Object> parameter) throws Exception {
        // TODO: Derive this information from the actual build
        Map<String, String> deploymentFileMap = new HashMap<>();
        deploymentFileMap.put(ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve(
                "harness/pymmortals/resources/applications/ataklite_baseline/sdcard/ataklite/ATAKLite-Config.json").toString(),
                "/sdcard/ataklite/ATAKLite-Config.json");
        deploymentFileMap.put(ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve(
                "harness/pymmortals/resources/applications/ataklite_baseline/sdcard/ataklite/sample_image.jpg").toString(),
                "/sdcard/ataklite/sample_image.jpg");
        deploymentFileMap.put(ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve(
                "harness/pymmortals/resources/applications/ataklite_baseline/sdcard/ataklite/env.json").toString(),
                "/sdcard/ataklite/env.json");

        // Add ATAKLite
        adaptationBuildTargets.put("ATAKLite", new AdaptationTargetBuildBase(
                "ATAKLite",
                2000,
                DeploymentTarget.ANDROID,
                "21",
                ImmortalsConfig.getInstance().globals.getImmortalsRoot().toString(),
                "applications/client/ATAKLite",
                "ATAKLite-debug.apk",
                "build.gradle",
                deploymentFileMap,
                "src",
                "com.bbn.ataklite",
                "com.bbn.ataklite.MainActivity",
                ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("gradlew").toString(),
                BuildPlatform.GRADLE,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        deploymentFileMap = new HashMap<>();
        deploymentFileMap.put("Marti-Config.json", "Marti-Config.json");

        // Add Marti
        adaptationBuildTargets.put("Marti", new AdaptationTargetBuildBase(
                "Marti",
                2000,
                DeploymentTarget.JAVA,
                "8",
                ImmortalsConfig.getInstance().globals.getImmortalsRoot().toString(),
                "applications/server/Marti",
                "Marti-immortals.jar",
                "build.gradle",
                deploymentFileMap,
                "src",
                "com.bbn.marti",
                "com.bbn.marti.service.MartiMain",
                ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("gradlew").toString(),
                BuildPlatform.GRADLE,
                "--daemon clean build -x test",
                "--daemon clean validate",
                "build/test-results/validate",
                null,
                null,
                null
        ));

        adaptationBuildTargets.put("TakServerDataManager", new AdaptationTargetBuildBase(
                "TakServerDataManager",
                0,
                DeploymentTarget.JAVA,
                "8",
                ImmortalsConfig.getInstance().globals.getImmortalsRoot().toString(),
                "shared/modules/dfus/TakServerDataManager/",
                null,
                "build.gradle",
                new HashMap<>(),
                "src/main/java",
                "mil.darpa.immortals.dfus",
                null,
                ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("gradlew").toString(),
                BuildPlatform.GRADLE,
                null,
                null,
                null,
                "mil.darpa.immortals.dfus",
                "TakServerDataManager",
                "2.0-LOCAL"
        ));

        return null;
    }

    @Nullable
    public AdaptationTargetBuildInstance getBuildInstance(@Nonnull String applicationIdentifier,
                                                          @Nonnull String adaptationIdentifier) {
        AdaptationTargetBuildBase abb = adaptationBuildTargets.get(applicationIdentifier);

        if (abb == null) {
            return null;
        }
        AdaptationTargetBuildInstance buildInstance = new AdaptationTargetBuildInstance(adaptationIdentifier, abb);
        buildInstance.getBuildRoot();

        return buildInstance;
    }

    @Nullable
    public AdaptationTargetBuildBase getBuildBase(@Nonnull String applicationIdentifier) {
        return adaptationBuildTargets.get(applicationIdentifier);
    }

    public static void main(String[] args) {
        try {
            // Init the knowledge builder
            GradleKnowledgeBuilder gkb = new GradleKnowledgeBuilder();
            gkb.buildKnowledge(null);

            // Request the build information for the artifact you are interested in
            AdaptationTargetBuildBase base = gkb.getBuildBase("TakServerDataManager");

            // Get the source root
            Path baseSourceRoot = base.getSourceRoot();
            System.out.println(baseSourceRoot.toString());

            // Create an aadaptation identifier
            String adaptationIdentifier = "adaptation" + Long.toString(System.currentTimeMillis()).substring(0, 10);

            // Get a build instance, which copies and creates the new artifact
            AdaptationTargetBuildInstance instance = gkb.getBuildInstance("TakServerDataManager", adaptationIdentifier);

            // Get the build instance source root
            Path adaptationSourceRoot = instance.getSourceRoot();
            System.out.println(adaptationSourceRoot.toString());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
