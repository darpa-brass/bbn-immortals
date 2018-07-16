package mil.darpa.immortals.core.api.ll.phase2.functionality;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP2;
import mil.darpa.immortals.core.api.annotations.Unstable;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP2
@Description("Common security standards")
public enum SecurityStandard {
    AES_128("AES encryption algorithm with 128 bit key", "AES", 16, null),
    AES_256("AES encryption algorithm with 256 bit key", "AES", 32, null),
//    Blowfish_128("Blowfish encryption algorithm with 128 bit key", "Blowfish", 16, null),
    DES_56("DES encryption algorithm with 56 bit key", "DES", 8, null),
//    ARIA("ARIA encryption algorithm", "ARIA", null, null),
//    TWOFISH("Twofish encryption algorithm", "Twofish", null, null),
//    CAST6_192("CAST6 encryption algorithm with 192 bit key", "CAST6", 24, null),
//    IDEA("IDEA encryption algorithm", "IDEA", null, null),
//    Noekeon("Noekeon encryption algorithm", "Noekeon", null, null),
    Rijndael("Rijndael encryption algorithm", "Rijndael", null, null);
//    DESEDE_128("DESEDE encryption algorithm with 128bit+ key", "DESede", 16, null),
//    GCM_128("GCM encryption algorithm with 128bit+ key", "GCM", 16, null),
//    XTEA("XTEA encryption algorithm", "XTEA", null, null);


    public final String description;
    @Unstable
    public final String algorithm;
    @Unstable
    public final Integer keySize;
    @Unstable
    public final String cipherChainingMode;

    SecurityStandard(String description, String algorithm, Integer keySize, String cipherChainingMode) {
        this.description = description;
        this.algorithm = algorithm;
        this.keySize = keySize;
        this.cipherChainingMode = cipherChainingMode;
    }
}
