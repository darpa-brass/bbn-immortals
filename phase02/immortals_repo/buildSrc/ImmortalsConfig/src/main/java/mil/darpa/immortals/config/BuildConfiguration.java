package mil.darpa.immortals.config;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by awellman@bbn.com on 11/1/17.
 */
public class BuildConfiguration {

    public final DasConfiguration das = new DasConfiguration();
    public final AdaptationsConfiguration augmentations = new AdaptationsConfiguration();

    public BuildConfiguration() {
    }

    private static class DasConfiguration {
        private String rootGroup = "mil.darpa.immortals";
        private String publishVersion = "2.0-LOCAL";
        private String javaVersionCompatibility = "1.8";
        private String slf4jVersion = "1.7.21";
        private String gradleVersion = "3.4.1";

        public DasConfiguration() {
        }

        public String getRootGroup() {
            return rootGroup;
        }

        public String getPublishVersion() {
            return publishVersion;
        }

        public String getJavaVersionCompatibility() {
            return javaVersionCompatibility;
        }

        public String getSlf4jVersion() {
            return slf4jVersion;
        }

        public String getGradleVersion() {
            return gradleVersion;
        }
    }


    public static class AdaptationsConfiguration {

        private String publishVersion = "2.0-LOCAL";
        private String javaVersionCompatibility = "1.7";
        private String androidBuildToolsVersion = "25.0.2";
        private String androidGradleToolsVersion = "2.3.3";
        private int androidCompileSdkVersion = 21;
        private int androidMinSdkVersion = 21;
        private int androidTargetSdkVersion = 21;
        private String javaHome = System.getenv("JAVA_HOME");
        private String androidSdkRoot = System.getenv("ANDROID_HOME");
        private String mavenPublishRepo = GlobalsConfig.staticImmortalsRepo.toString();

        public AdaptationsConfiguration() {
        }

        public String getPublishVersion() {
            return publishVersion;
        }

        public Path getJavaHome() {
            return Paths.get(javaHome).toAbsolutePath();
        }

        public String getJavaExecutablePath() {
            return Paths.get(javaHome).resolve("bin/java").toString();
        }

        public String getJavaVersionCompatibility() {
            return javaVersionCompatibility;
        }

        public String getAndroidBuildToolsVersion() {
            return androidBuildToolsVersion;
        }

        public String getAndroidGradleToolsVersion() {
            return androidGradleToolsVersion;
        }

        public int getAndroidCompileSdkVersion() {
            return androidCompileSdkVersion;
        }

        public int getAndroidMinSdkVersion() {
            return androidMinSdkVersion;
        }

        public int getAndroidTargetSdkVersion() {
            return androidTargetSdkVersion;
        }

        public Path getAndroidSdkRoot() {
            return Paths.get(androidSdkRoot).toAbsolutePath();
        }

        public String getAndroidSdkJarPath() {
            return Paths.get(androidSdkRoot).resolve(
                    "platforms/android-" + this.androidCompileSdkVersion + "/android.jar").toAbsolutePath().toString();
        }

        public String getAndroidAdbPath() {
            return getAndroidSdkRoot().resolve("platform-tools/adb").toString();
        }

        public String getAndroidEmulatorCommand() {
            return getAndroidSdkRoot().resolve("emulator/emulator").toString();
        }

        public String getAndroidAvdmanagerPath() {
            return getAndroidSdkRoot().resolve("tools/bin/avdmanager").toString();
        }

        public URI getMavenPublishRepo() {
            return Paths.get(mavenPublishRepo).toAbsolutePath().toUri();
        }
    }
}
