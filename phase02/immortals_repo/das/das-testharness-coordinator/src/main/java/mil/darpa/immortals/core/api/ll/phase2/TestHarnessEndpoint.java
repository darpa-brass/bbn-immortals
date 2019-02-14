package mil.darpa.immortals.core.api.ll.phase2;

import mil.darpa.immortals.core.api.ll.phase2.result.TestAdapterState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by awellman@bbn.com on 9/28/17.
 */
public enum TestHarnessEndpoint {
    READY(RestType.POST, "/ready", null, null),
    ERROR(RestType.POST, "/error", String.class, null),
    STATUS(RestType.POST, "/status", TestAdapterState.class, null),
    DONE(RestType.POST, "/done", TestAdapterState.class, null);

    public final RestType restType;
    public final String path;
    public final Class submitDatatype;
    public final Class ackDatatype;

    TestHarnessEndpoint(@Nonnull RestType restType, @Nonnull String path, @Nullable Class submitDatatype, @Nullable Class ackDatatype) {
        this.restType = restType;
        this.path = path;
        this.submitDatatype = submitDatatype;
        this.ackDatatype = ackDatatype;
    }
}
