package mil.darpa.immortals.dfus.crypto.jca;

import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherDecrypt;
import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherEncrypt;
import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherInitialize;
import com.securboration.immortals.ontology.functionality.alg.encryption.Cipher;
import com.securboration.immortals.ontology.resources.compute.Cpu;
import com.securboration.immortals.ontology.resources.memory.PhysicalMemoryResource;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.EncryptionKey;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.properties.BlockBased;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.BinaryData;

import java.security.GeneralSecurityException;

/**
 * Created by awellman@bbn.com on 7/5/16.
 */
@DfuAnnotation(
        functionalityBeingPerformed = Cipher.class,
        resourceDependencies = {
                Cpu.class,
                PhysicalMemoryResource.class
        }
)
public class JcaAes extends CryptoHelperJca {

    public static final String cipherAlgorithm = "AES/ECB/PKCS5Padding";

    @FunctionalAspectAnnotation(
            aspect = AspectCipherInitialize.class
    )
    public JcaAes(@EncryptionKey String key) throws GeneralSecurityException {
        super(key);
    }

    @Override
    protected String getCipherAlgorithm() {
        return cipherAlgorithm;
    }

    @FunctionalAspectAnnotation(
            aspect = AspectCipherEncrypt.class
    )
    @BlockBased
    public byte[] encryptBytes(@BinaryData byte[] bytes) {
        return encryptAll(bytes);
    }

    @FunctionalAspectAnnotation(
            aspect = AspectCipherDecrypt.class
    )
    @BlockBased
    public byte[] decryptBytes(@BinaryData byte[] bytes) {
        return decryptAll(bytes);
    }
}
