package mil.darpa.immortals.analysis.adaptationtargets;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * Created by awellman@bbn.com on 4/11/18.
 */
public class ImmortalsGradleExecutionData {

    private final int executionStartSettleTimeMS;
    private final String executableFilename;
    private final HashMap<String, String> executionFileMap;
    private final String executionPackageIdentifier;
    private final String executionMainMethodClasspath;

    public ImmortalsGradleExecutionData(int executionStartSettleTimeMS, @Nonnull String executableFilename, @Nonnull HashMap<String, String> executionFileMap,
                                        @Nonnull String executionPackageIdentifier, @Nonnull String executionMainMethodClasspath) {
        this.executionStartSettleTimeMS = executionStartSettleTimeMS;
        this.executableFilename = executableFilename;
        this.executionFileMap = executionFileMap == null ? new HashMap<String,String>() : executionFileMap;
        this.executionPackageIdentifier = executionPackageIdentifier;
        this.executionMainMethodClasspath = executionMainMethodClasspath;
    }

    public int getExecutionStartSettleTimeMS() {
        return executionStartSettleTimeMS;
    }

    public String getExecutableFilename() {
        return executableFilename;
    }

    public HashMap<String, String> getExecutionFileMap() {
        return new HashMap<>(executionFileMap);
   }

    public String getExecutionPackageIdentifier() {
        return executionPackageIdentifier;
    }

    public String getExecutionMainMethodClasspath() {
        return executionMainMethodClasspath;
    }
}
