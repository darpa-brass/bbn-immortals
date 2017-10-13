package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP3
@Description("A client library upgrade that will cause a partial upgrade")
public enum ClientPartialUpgradeLibrary {
    ToBeDetermined("A yet-to-be determined library", "1.3.3.7");

    @P2CP3
    public final String description;
    @P2CP3
    public final String latestVersion;

    ClientPartialUpgradeLibrary(String description, String latestVersion) {
        this.description = description;
        this.latestVersion = latestVersion;
    }
}
