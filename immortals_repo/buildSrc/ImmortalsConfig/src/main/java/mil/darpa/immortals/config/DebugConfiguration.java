package mil.darpa.immortals.config;

/**
 * Created by awellman@bbn.com on 11/2/17.
 */
public class DebugConfiguration {
    private Boolean useMockFuseki = false;
    private Boolean useMockKnowledgeRepository = false;
    private Boolean useMockDas = false;
    private Boolean useMockApplicationDeployment = false;
    private Boolean useMockTestHarness = false;
    private Boolean useMockTestAdapter = false;
    private Boolean useMockAqlBrass = true;
    private int shutdownGracePeriodMS = 10000;
    public String loggingConfigDirectory = GlobalsConfig.staticImmortalsRoot.resolve("das/das-launcher/logging_scripts").toAbsolutePath().toString() + "/";
    


    DebugConfiguration() {
    }

    public boolean isUseMockFuseki() {
        return useMockFuseki;
    }

    public boolean isUseMockKnowledgeRepository() {
        return useMockKnowledgeRepository;
    }

    public boolean isUseMockDas() {
        return useMockDas;
    }

    public boolean isUseMockApplicationDeployment() {
        return useMockApplicationDeployment;
    }

    public boolean isUseMockTestHarness() {
        return useMockTestHarness;
    }

    public boolean isUseMockTestAdapter() {
        return useMockTestAdapter;
    }
    
    public boolean isUseMockAqlBrass() {
        return useMockAqlBrass;
    }

    public int getShutdownGracePeriodMS() {
        return shutdownGracePeriodMS;
    }

    /**
     * Gets the directory that contains logging configuration files (logging.properties, logback.xml, etc)
     * This is used by the DasLauncher to enable logging through the CLI.
     *
     * @return The directory
     */
    public String getLoggingConfigDirectory() {
        return loggingConfigDirectory;
    }
}
