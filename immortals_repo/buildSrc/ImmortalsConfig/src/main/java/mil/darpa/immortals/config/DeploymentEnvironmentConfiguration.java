package mil.darpa.immortals.config;

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

    public static class AndroidEnivronmentConfiguration {
        private int adbPort;
        private String adbUrl;
        private String adbIdentifier;
        private int androidVersion;

        public int getAdbPort() {
            return adbPort;
        }

        public String getAdbUrl() {
            return adbUrl;
        }

        public String getAdbIdentifier() {
            return adbIdentifier;
        }

        public int getAndroidVersion() {
            return androidVersion;
        }
    }

}
