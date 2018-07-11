package mil.darpa.immortals.core.api.ll.phase2.functionality;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP2;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP2
@Description("Common security standards")
public enum SecurityStandard {
    AES_128("AES encryption standard with 128bit+ key", "AES", 16, null);
//    AES_256("AES encryption standard with 256bit+ key", "AES", 32, null),
//    Blowfish_128("Blowfish encryption standard with 128bit+ key", "Blowfish", 16, null),
//    DESEDE_128("DESEDE encryption standard with 128bit+ key", "DESEDE", 16, null),
//    DES_56("DES encryption standard with 56bit+ key", "DES", 7, null),
//    GCM_128("GCM encryption standard with 128bit+ key", "GCM", 16, null);


    public final String description;
    public final String algorithm;
    public final Integer keySize;
    public final String cipherChainingMode;

    SecurityStandard(String description, String algorithm, Integer keySize, String cipherChainingMode) {
        this.description = description;
        this.algorithm = algorithm;
        this.keySize = keySize;
        this.cipherChainingMode = cipherChainingMode;
    }
}
