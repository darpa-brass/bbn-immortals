package mil.darpa.immortals.dfus.crypto.jca;

import mil.darpa.immortals.annotation.dsl.ontology.functionality.Input;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.EncryptionKey;
import mil.darpa.immortals.core.synthesis.adapters.InputStreamPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ProducingPipe;

import java.io.InputStream;

/**
 * Created by awellman@bbn.com on 7/15/16.
 */
public class JcaBlowfishDecryptor extends JcaDecryptor {

    public JcaBlowfishDecryptor(@EncryptionKey String encryptionKey, @Input ProducingPipe<byte[]> producer) {
        super(encryptionKey, JcaAes.cipherAlgorithm, producer);
    }

    public JcaBlowfishDecryptor(@EncryptionKey String encryptionKey, @Input InputStream producer) {
        super(encryptionKey, JcaAes.cipherAlgorithm, producer);
    }

    public JcaBlowfishDecryptor(@EncryptionKey String encryptionKey, @Input InputStreamPipe producer) {
        super(encryptionKey, JcaAes.cipherAlgorithm, producer);
    }
}
