package mil.darpa.immortals.core;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by awellman@bbn.com on 9/29/17.
 */
public class Configuration {

    private static Configuration configuration;

    public static synchronized Configuration getInstance() {
        if (configuration == null) {

            String override_path = System.getenv("IMMORTALS_OVERRIDE_FILE");
            if (override_path == null) {
                configuration = new Configuration();
            } else {
                try {
                    File f = new File(override_path);
                    Gson gson = new Gson();
                    configuration = gson.fromJson(new FileReader(f), Configuration.class);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return configuration;
    }

    public DasServiceConfiguration dasService = new DasServiceConfiguration();
    public TestHarnessConfiguration testHarness = new TestHarnessConfiguration();
    public TestAdapterConfiguration testAdapter = new TestAdapterConfiguration();

    public static class DasServiceConfiguration {
        public boolean enabled = true;
        public String protocol = "http://";
        public String url = "127.0.0.1";
        public int port = 8080;
        public int websocketPort = 7878;
        
    }

    public static class TestHarnessConfiguration {
        public boolean enabled = true;
        public String protocol = "http://";
        public String url = "brass-th";
        public int port = 80;
    }

    public static class TestAdapterConfiguration {
        public boolean enabled = true;
        public String protocol = "http://";
        public String url = "brass-ta";
        public int port = 80;
    }

    Configuration() {
    }
}
