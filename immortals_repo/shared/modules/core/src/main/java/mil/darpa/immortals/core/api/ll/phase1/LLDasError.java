package mil.darpa.immortals.core.api.ll.phase1;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
public enum LLDasError {
    TEST_DATA_FILE_ERROR("TEST_DATA_FILE_ERROR"),
    TEST_DATA_FORMAT_ERROR("TEST_DATA_FORMAT_ERROR"),
    DAS_LOG_FILE_ERROR("DAS_LOG_FILE_ERROR"),
    DAS_OTHER_ERROR("DAS_OTHER_ERROR");

    public final String tag;

    LLDasError(String tag) {
        this.tag = tag;
    }
}
