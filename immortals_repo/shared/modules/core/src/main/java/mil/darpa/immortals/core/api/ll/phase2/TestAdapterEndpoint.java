package mil.darpa.immortals.core.api.ll.phase2;

import mil.darpa.immortals.core.api.ll.phase2.result.TestAdapterState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by awellman@bbn.com on 9/28/17.
 */
public enum TestAdapterEndpoint {
    CP1(RestType.POST, "/action/databaseSchemaPerturbation", SubmissionModel.class, TestAdapterState.class),
    CP2(RestType.POST, "/action/crossApplicationDependencies", SubmissionModel.class, TestAdapterState.class),
    CP3(RestType.POST, "/action/libraryEvolution", SubmissionModel.class, TestAdapterState.class),
    ALIVE(RestType.GET, "/alive", null, null),
    QUERY(RestType.GET, "/query", null, TestAdapterState.class),
    ENABLED(RestType.POST, "/enabled", EnableDas.class, null);

    public final RestType restType;
    public final String path;
    public final Class submitDatatype;
    public final Class ackDatatype;

    TestAdapterEndpoint(@Nonnull RestType restType, @Nonnull String path, @Nullable Class submitDatatype, @Nullable Class ackDatatype) {
        this.restType = restType;
        this.path = path;
        this.submitDatatype = submitDatatype;
        this.ackDatatype = ackDatatype;
    }
}
