package mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 6/30/18.
 */
public enum Res {
    CONFIGURATION_BINDING(Prefix.IMMoRTALS_functionality, "ConfigurationBinding"),
    CIPHER_ALGORITHM(Prefix.IMMoRTALS_functionality_alg_encryption, "CipherAlgorithm"),
    CIPHER_KEY_LENGTH(Prefix.IMMoRTALS_functionality_alg_encryption, "CipherKeyLength"),
    CIPHER_BLOCK_SIZE(Prefix.IMMoRTALS_functionality_alg_encryption, "CipherBlockSize"),
    PADDING_SCHEME(Prefix.IMMoRTALS_functionality_alg_encryption, "PaddingScheme"),
    CIPHER_CHAINING_MODE(Prefix.IMMoRTALS_functionality_alg_encryption, "CipherChainingMode"),
    ASPECT_CONFIGURE_SOLUTION(Prefix.IMMoRTALS_functionality_aspects, "AspectConfigureSolution"),
    ASPECT_CONFIGURE_REQUEST(Prefix.IMMoRTALS_functionality_aspects, "AspectConfigureRequest"),
    DFU_INSTANCE(Prefix.IMMoRTALS_dfu_instance, "DfuInstance"),
    DFU_INSTANCE_JAVAX(Prefix.IMMoRTALS_dfu_instance, "JavaxCipher"),
    DFU_INSTANCE_BC(Prefix.IMMoRTALS_dfu_instance, "BouncyCastle");


    public final String uri;

    Res(@Nonnull Prefix prefix, @Nonnull String uri) {
        this.uri = prefix.toString() + uri;
    }

    public String toString() {
        return this.uri;
    }
}
