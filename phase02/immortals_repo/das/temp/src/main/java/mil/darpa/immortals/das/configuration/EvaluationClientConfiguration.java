package mil.darpa.immortals.das.configuration;

/**
 * Created by awellman@bbn.com on 10/17/16.
 */
public class EvaluationClientConfiguration {

    public final int latestSABroadcastIntervalMS;
    public final int imagePixelCount = 10000000;
    public final int imageBroadcastIntervalMS;

    public EvaluationClientConfiguration(int latestSABroadcastIntervalMS, int imageBroadcastIntervalMS) {
        this.latestSABroadcastIntervalMS = latestSABroadcastIntervalMS;
        this.imageBroadcastIntervalMS = imageBroadcastIntervalMS;
    }
}
