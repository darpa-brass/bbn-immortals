package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.ll.phase2.RequirementsInterface;
import mil.darpa.immortals.core.api.ll.phase2.UpgradableLibraryInterface;
import mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.AndroidPlatformVersion;
import mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.ClientPartialUpgradeLibrary;
import mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.ClientUpgradeLibrary;

/**
 * Created by awellman@bbn.com on 9/13/17.
 */
@P2CP3
@Description("A requirement specification for an ATAKLite instance")
public class AtakliteRequirements implements RequirementsInterface {

    @P2CP3
    @Description("Which version of the Android platform the clients must be deployed on")
    public AndroidPlatformVersion deploymentPlatformVersion;
    @P2CP3
    @Description("A library upgrade that will trigger a partial library upgrade")
    public ClientPartialUpgradeLibrary partialLibraryUpgrade;
    @P2CP3
    @Description("A library upgrade that will trigger a mutation")
    public ClientUpgradeLibrary libraryUpgrade;

    public AtakliteRequirements() {
    }

    public AtakliteRequirements(AndroidPlatformVersion deploymentPlatformVersion,
                                ClientPartialUpgradeLibrary partialLibraryUpgrade,
                                ClientUpgradeLibrary libraryUpgrade) {
        this.deploymentPlatformVersion = deploymentPlatformVersion;
        this.partialLibraryUpgrade = partialLibraryUpgrade;
        this.libraryUpgrade = libraryUpgrade;
    }

    @Override
    public UpgradableLibraryInterface getPartialLibraryUpgrade() {
        return partialLibraryUpgrade;
    }

    @Override
    public UpgradableLibraryInterface getUpgradeLibrary() {
        return libraryUpgrade;
    }
}
