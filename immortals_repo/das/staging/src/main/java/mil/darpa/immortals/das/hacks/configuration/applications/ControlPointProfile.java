package mil.darpa.immortals.das.hacks.configuration.applications;

import mil.darpa.immortals.das.sourcecomposer.configuration.AugmentationType;
import mil.darpa.immortals.das.sourcecomposer.configuration.paradigms.ConfigurationContainer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Defines the information currently necessary to define a control point within an application
 * <p>
 * Created by awellman@bbn.com on 5/4/17.
 */
@Deprecated
public class ControlPointProfile {
    public final String controlPointUuid;
    public final AugmentationType augmentationType;
    public final LinkedList<String> synthesisTargetFiles;
    public final HashMap<String, String> fileCopyMap;
    public final ConfigurationContainer originalDfu;

    public ControlPointProfile(String augmentationIdentifier, AugmentationType augmentationType,
                               LinkedList<String> synthesisTargetFiles, HashMap<String, String> fileCopyMap,
                               @Nullable ConfigurationContainer originalDfu) {
        this.controlPointUuid = augmentationIdentifier;
        this.augmentationType = augmentationType;
        this.synthesisTargetFiles = synthesisTargetFiles;
        this.fileCopyMap = fileCopyMap;
        this.originalDfu = originalDfu;
    }
}
