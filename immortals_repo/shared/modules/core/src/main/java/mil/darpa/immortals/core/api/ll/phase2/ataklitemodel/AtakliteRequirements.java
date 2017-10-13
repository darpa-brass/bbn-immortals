package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.AndroidPlatformVersion;
import mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.ClientLibraryUpgradeRequirements;
import mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.ClientPartialLibraryUpgradeRequirements;

/**
 * Created by awellman@bbn.com on 9/13/17.
 */
@P2CP3
@Description("A requirement specification for an ATAKLite instance")
public class AtakliteRequirements {

    //    public final LinkedList<AtakliteGeneralResource> generalResources;
//    public final LocationProduction locationProduction;
    @P2CP3
    @Description("Which version of the Android platform the clients must be deployed on")
    public AndroidPlatformVersion deploymentPlatformVersion;
    @P2CP3
    @Description("A library upgrade that will trigger a partial library upgrade")
    public ClientPartialLibraryUpgradeRequirements partialLibraryUpgrade;
    @P2CP3
    @Description("A library upgrade that will trigger a mutation")
    public ClientLibraryUpgradeRequirements libraryUpgrade;

    public AtakliteRequirements() {
    }

    public AtakliteRequirements(AndroidPlatformVersion deploymentPlatformVersion,
                                ClientPartialLibraryUpgradeRequirements partialLibraryUpgrade,
                                ClientLibraryUpgradeRequirements libraryUpgrade) {
        this.deploymentPlatformVersion = deploymentPlatformVersion;
        this.partialLibraryUpgrade = partialLibraryUpgrade;
        this.libraryUpgrade = libraryUpgrade;
    }
}
