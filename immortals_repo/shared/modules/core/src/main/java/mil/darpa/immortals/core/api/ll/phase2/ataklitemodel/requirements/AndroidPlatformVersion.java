package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;

/**
 * Created by awellman@bbn.com on 9/15/17.
 */
@P2CP3
@Description("Possible Android platforms to deploy on")
public enum AndroidPlatformVersion {
    @P2CP3
    Android21("Baseline Android API version 21"),
    @P2CP3
    Android23("Newer Android API version 23 which requires runtime permission requests");

    public final String description;

    AndroidPlatformVersion(String description) {
        this.description = description;
    }
}
