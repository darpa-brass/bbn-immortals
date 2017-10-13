package mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@Description("A server upgrade library that will cause a mutation")
@P2CP3
public enum ServerUpgradeLibrary {
    ImageSaverLibrary("A library used for saving images", "2");

    public final String description;
    public final String latestVersion;

    ServerUpgradeLibrary(String description, String latestVersion) {
        this.description = description;
        this.latestVersion = latestVersion;
    }
}
