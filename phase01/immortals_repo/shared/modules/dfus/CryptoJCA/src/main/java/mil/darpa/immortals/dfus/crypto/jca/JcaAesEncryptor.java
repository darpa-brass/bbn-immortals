package mil.darpa.immortals.dfus.crypto.jca;

import mil.darpa.immortals.annotation.dsl.ontology.functionality.Output;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.EncryptionKey;
import mil.darpa.immortals.core.synthesis.adapters.OutputStreamPipe;
import mil.darpa.immortals.core.synthesis.interfaces.WriteableObjectPipeInterface;

import java.io.OutputStream;

/**
 * Created by awellman@bbn.com on 7/15/16.
 */
public class JcaAesEncryptor extends JcaEncryptor {

    public JcaAesEncryptor(@EncryptionKey String encryptionKey, @Output WriteableObjectPipeInterface<byte[]> consumer) {
        super(encryptionKey, JcaAes.cipherAlgorithm, consumer);
    }

    public JcaAesEncryptor(@EncryptionKey String encryptionKey, @Output OutputStream consumer) {
        super(encryptionKey, JcaAes.cipherAlgorithm, consumer);
    }

    public JcaAesEncryptor(@EncryptionKey String encryptionKey, @Output OutputStreamPipe consumer) {
        super(encryptionKey, JcaAes.cipherAlgorithm, consumer);
    }

}
