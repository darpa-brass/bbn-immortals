package mil.darpa.immortals.core.api.ll.phase2.martimodel;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP2;
import mil.darpa.immortals.core.api.ll.phase2.ResourceInterface;

/**
 * Created by awellman@bbn.com on 6/8/18.
 */
@P2CP2
@Description("Resources available to Marti Server")
public enum JavaResource implements ResourceInterface {
    @P2CP2
    HARWARE_AES("Hardware accelerated AES cryptography"),
    @P2CP2
    STRONG_CRYPTO("Support for strong cryptography");

    public final String description;

    JavaResource(String description) {
        this.description = description;
    }
}
