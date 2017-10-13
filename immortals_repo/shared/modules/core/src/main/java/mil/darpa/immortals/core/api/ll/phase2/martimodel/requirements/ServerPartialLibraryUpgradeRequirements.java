package mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@Description("A library upgrade that will trigger a partial library upgrade to allow the library to be compatible")
@P2CP3
public class ServerPartialLibraryUpgradeRequirements {

    @P2CP3
    @Description("A library used within the application")
    public ServerPartialUpgradeLibrary libraryIdentifier;
    @P2CP3
    @Description("The required version of the library")
    public String libraryVersion;

    public ServerPartialLibraryUpgradeRequirements() {
    }

    public ServerPartialLibraryUpgradeRequirements(ServerPartialUpgradeLibrary libraryIdentifier, String libraryVersion) {
        this.libraryIdentifier = libraryIdentifier;
        this.libraryVersion = libraryVersion;
    }

}
