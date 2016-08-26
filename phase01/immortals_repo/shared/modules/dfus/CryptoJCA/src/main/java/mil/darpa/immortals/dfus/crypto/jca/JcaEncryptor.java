package mil.darpa.immortals.dfus.crypto.jca;

import mil.darpa.immortals.core.synthesis.adapters.OutputStreamPipe;
import mil.darpa.immortals.core.synthesis.adapters.PipeToOutputStream;
import mil.darpa.immortals.core.synthesis.interfaces.WriteableObjectPipeInterface;

import java.io.OutputStream;
import java.security.GeneralSecurityException;

/**
 * Created by awellman@bbn.com on 7/15/16.
 */
public class JcaEncryptor extends OutputStreamPipe {
    private OutputStream outputStream;
    private WriteableObjectPipeInterface<byte[]> pipe;

    private OutputStream cryptoStream;

    public JcaEncryptor(String encryptionKey, final String cipherAlgorithm, WriteableObjectPipeInterface<byte[]> consumer) {
        this(encryptionKey, cipherAlgorithm, (OutputStream) new PipeToOutputStream(consumer));
    }

    public JcaEncryptor(String encryptionKey, final String cipherAlgorithm, OutputStreamPipe consumer) {
        this(encryptionKey, cipherAlgorithm, (OutputStream) consumer);
    }

    public JcaEncryptor(String encryptionKey, final String cipherAlgorithm, OutputStream consumer) {
        try {
            CryptoHelperJca cryptoHelperJca = new CryptoHelperJca(encryptionKey) {
                @Override
                protected String getCipherAlgorithm() {
                    return cipherAlgorithm;
                }
            };

            this.cryptoStream = cryptoHelperJca.attachCipherEncryptionStream(consumer);
            delayedInit(cryptoStream);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}