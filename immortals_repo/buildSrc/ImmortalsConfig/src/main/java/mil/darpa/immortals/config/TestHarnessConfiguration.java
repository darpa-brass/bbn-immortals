package mil.darpa.immortals.config;

/**
 * Created by awellman@bbn.com on 11/1/17.
 */
public class TestHarnessConfiguration {
    private String protocol = "http";
    private String url = "brass-th";
    private int port = 80;

    TestHarnessConfiguration() {
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUrl() {
        return url;
    }

    public int getPort() {
        return port;
    }
}
