package mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@Description("A server library upgrade that will cause a partial upgrade")
@P2CP3
public enum ServerPartialUpgradeLibrary {
    Dom4jCot("Dom4J library used for decoding incoming XML data into CoT objects", "2");

    public final String description;
    public final String latestVersion;

    ServerPartialUpgradeLibrary(String description, String latestVersion) {
        this.description = description;
        this.latestVersion = latestVersion;
    }
}
