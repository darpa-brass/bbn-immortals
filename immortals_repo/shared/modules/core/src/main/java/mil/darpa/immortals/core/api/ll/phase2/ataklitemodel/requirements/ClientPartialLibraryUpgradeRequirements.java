package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP3
@Description("A library upgrade that will trigger a partial library upgrade to allow the library to be compatible")
public class ClientPartialLibraryUpgradeRequirements {
    @P2CP3
    @Description("The identifier for the library")
    public ClientPartialUpgradeLibrary libraryIdentifier;
    @P2CP3
    @Description("The required version of the library")
    public String libraryVersion;

    public ClientPartialLibraryUpgradeRequirements() {
    }

    public ClientPartialLibraryUpgradeRequirements(ClientPartialUpgradeLibrary libraryIdentifier, String libraryVersion) {
        this.libraryIdentifier = libraryIdentifier;
        this.libraryVersion = libraryVersion;
    }

}
