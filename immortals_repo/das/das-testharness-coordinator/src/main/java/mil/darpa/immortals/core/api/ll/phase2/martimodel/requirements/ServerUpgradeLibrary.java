package mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@Description("A server upgrade library that will cause a mutation")
@P2CP3
public enum ServerUpgradeLibrary {
    ImageSaverLibrary_2("Newer image saver library");

    public final String description;

    ServerUpgradeLibrary(String description) {
        this.description = description;
    }
}
