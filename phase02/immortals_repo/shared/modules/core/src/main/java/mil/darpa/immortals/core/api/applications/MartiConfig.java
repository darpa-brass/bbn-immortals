package mil.darpa.immortals.core.api.applications;

/**
 * Created by awellman@bbn.com on 9/8/17.
 */
public class MartiConfig {

    public AnalyticsConfig analyticsConfig;
    public String storageDirectory;
    public PostGreSqlConfig postGreSqlConfig;


    public MartiConfig(AnalyticsConfig analyticsConfig, String storageDirectory, PostGreSqlConfig postGreSqlConfig) {
        this.analyticsConfig = analyticsConfig;
        this.storageDirectory = storageDirectory;
        this.postGreSqlConfig = postGreSqlConfig;
    }

}

