package mil.darpa.immortals.core.api.ll.phase2.globalmodel;

import mil.darpa.immortals.core.api.ll.phase2.ResourceInterface;

/**
 * Created by awellman@bbn.com on 6/8/18.
 */
public enum GlobalResource implements ResourceInterface {
    ToBeDetermined_X_X("Resources to be determined");

    public final String description;

    GlobalResource(String description) {
        this.description = description;
    }
}
