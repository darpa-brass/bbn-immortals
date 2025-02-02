package mil.darpa.immortals.config;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 1/26/18.
 */
public class DeploymentEnvironmentConfiguration {

    private LinkedList<AndroidEnivronmentConfiguration> androidEnvironments = new LinkedList<>();

    private String martiAddress;

    public List<AndroidEnivronmentConfiguration> getAndroidEnvironments() {
        return androidEnvironments;
    }

    public String getMartiAddress() {
        return martiAddress;
    }

    public static class AndroidEmulatorRequirement {
        public int getAndroidVersion() {
            return androidVersion;
        }

        public Integer getUploadBandwidthLimitKilobitsPerSecond() {
            return uploadBandwidthLimitKilobitsPerSecond;
        }

        public String[] getExternallyAccessibleUrls() {
            return Arrays.copyOf(externallyAccessibleUrls, externallyAccessibleUrls.length);
        }

        private final int androidVersion;
        private final Integer uploadBandwidthLimitKilobitsPerSecond;
        private final String[] externallyAccessibleUrls;

        public AndroidEmulatorRequirement(int androidVersion, @Nullable Integer uploadBandwidthLimitKilobitsPerSecond,
                                          @Nullable String[] externallyAccessibleUrls) {
            this.androidVersion = androidVersion;
            this.uploadBandwidthLimitKilobitsPerSecond = uploadBandwidthLimitKilobitsPerSecond;
            this.externallyAccessibleUrls = externallyAccessibleUrls == null ? new String[0] :
                    Arrays.copyOf(externallyAccessibleUrls, externallyAccessibleUrls.length);
        }
    }

    public static class AndroidEnivronmentConfiguration {
        private int adbPort;
        private String adbUrl;
        private String adbIdentifier;
        private AndroidEmulatorRequirement environmentDetails;

        public int getAdbPort() {
            return adbPort;
        }

        public String getAdbUrl() {
            return adbUrl;
        }

        public String getAdbIdentifier() {
            return adbIdentifier;
        }

        public AndroidEmulatorRequirement getEnvironmentDetails() {
            return environmentDetails;
        }
    }
}
