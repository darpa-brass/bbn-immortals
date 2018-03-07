package mil.darpa.immortals.analytics;

/**
 * Created by awellman@bbn.com on 10/20/16.
 */
public class AnalysisSnapshot {
    public final AnalysisDataFormat dataFormat;
    public final String value;

    public AnalysisSnapshot(AnalysisDataFormat dataFormat, String value) {
        this.dataFormat = dataFormat;
        this.value = value;
    }
}
