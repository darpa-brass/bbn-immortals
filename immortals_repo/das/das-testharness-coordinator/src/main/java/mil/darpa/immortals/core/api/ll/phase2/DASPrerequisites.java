package mil.darpa.immortals.core.api.ll.phase2;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Prerequisites;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Created by awellman@bbn.com on 5/10/18.
 */
@Prerequisites
@Description("Describes preconfiguration required prior to evaluating the DAS or SUT")
public class DASPrerequisites {

    @Prerequisites
    @Description("Defines the requirement that the described Android emulator be available for use by the DAS or SUT")
    public static class AndroidEmulatorRequirement {

        @Prerequisites
        @Description("The android version that the android emulator must be running")
        public final int androidVersion;

        @Prerequisites
        @Description("The upload limit that must be applied to the emulator to ensure consistent results")
        public final Integer uploadBandwidthLimitKilobitsPerSecond;

        @Prerequisites
        @Description("Urls that must be accessible by the emulator. Assume all subdomains are valid and all values with contain a port at the end")
        public final String[] externallyAccessibleUrls;

        @Prerequisites
        @Description("Whether or not the applications on the emulator must be granted the ability to use 'su'")
        public final boolean superuserAccess;

        public AndroidEmulatorRequirement(int androidVersion, @Nullable Integer uploadBandwidthLimitKilobitsPerSecond, boolean superuserAccess,
                                          @Nullable String[] externallyAccessibleUrls) {
            this.androidVersion = androidVersion;
            this.uploadBandwidthLimitKilobitsPerSecond = uploadBandwidthLimitKilobitsPerSecond;
            this.superuserAccess = superuserAccess;
            this.externallyAccessibleUrls = externallyAccessibleUrls == null ? new String[0] :
                    Arrays.copyOf(externallyAccessibleUrls, externallyAccessibleUrls.length);
        }
    }

    @Prerequisites
    @Description("Indicates the requirements for proper evaluation of a challenge problem")
    public static class ChallengeProblemRequirements {

        @Prerequisites
        @Description("The corresponding Test Adapter URL for convenience")
        public final String challengeProblemUrl;

        @Prerequisites
        @Description("A set of android emulators that must be available for use by the DAS or SUT")
        public final AndroidEmulatorRequirement[] androidEmulators;

        public ChallengeProblemRequirements(@Nonnull String challengeProblemUrl,
                                            @Nullable AndroidEmulatorRequirement[] androidEmulators) {
            this.challengeProblemUrl = challengeProblemUrl;
            this.androidEmulators = androidEmulators == null ? new AndroidEmulatorRequirement[0] :
                    Arrays.copyOf(androidEmulators, androidEmulators.length);
        }
    }

    @Prerequisites
    @Description("The prerequisites for evaluating Challenge Problem 1")
    public final ChallengeProblemRequirements cp1;

    @Prerequisites
    @Description("The prerequisites for evaluating Challenge Problem 2")
    public final ChallengeProblemRequirements cp2;

    @Prerequisites
    @Description("The prerequisites for evaluating Challenge Problem 3")
    public final ChallengeProblemRequirements cp3;

    public DASPrerequisites(ChallengeProblemRequirements cp1, ChallengeProblemRequirements cp2, ChallengeProblemRequirements cp3) {
        this.cp1 = cp1;
        this.cp2 = cp2;
        this.cp3 = cp3;
    }

}
