package mil.darpa.immortals.core.api.ll.phase1;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
public enum LLDasStatus {
    PERTURBATION_DETECTED("PERTURBATION_DETECTED"),
    MISSION_SUSPENDED("MISSION_SUSPENDED"),
    MISSION_RESUMED("MISSION_RESUMED"),
    MISSION_HALTED("MISSION_HALTED"),
    MISSION_ABORTED("MISSION_ABORTED"),
    ADAPTING("ADAPTING"),
    ADAPTATION_COMPLETED("ADAPTATION_COMPLETED"),
    TEST_ERROR("TEST_ERROR");

    public final String tag;

    LLDasStatus(String tag) {
        this.tag = tag;
    }
}
