package mil.darpa.immortals.dfus.crypto.jca;

import mil.darpa.immortals.core.synthesis.adapters.InputStreamPipe;
import mil.darpa.immortals.core.synthesis.adapters.PipeToInputStream;
import mil.darpa.immortals.core.synthesis.interfaces.ReadableObjectPipeInterface;

import java.io.InputStream;
import java.security.GeneralSecurityException;

/**
 * Created by awellman@bbn.com on 7/15/16.
 */
public class JcaDecryptor extends InputStreamPipe {
    private InputStream cryptoStream;

    public JcaDecryptor(String encryptionKey, final String cipherAlgorithm, ReadableObjectPipeInterface<byte[]> producer) {
        this(encryptionKey, cipherAlgorithm, (InputStream) new PipeToInputStream(producer));
    }

    public JcaDecryptor(String encryptionKey, final String cipherAlgorithm, InputStreamPipe producer) {
        this(encryptionKey, cipherAlgorithm, (InputStream) producer);
    }


    public JcaDecryptor(String encryptionKey, final String cipherAlgorithm, InputStream producer) {
        try {
            CryptoHelperJca cryptoHelperJca = new CryptoHelperJca(encryptionKey) {
                @Override
                protected String getCipherAlgorithm() {
                    return cipherAlgorithm;
                }
            };

            this.cryptoStream = cryptoHelperJca.attachCipherDecryptionStream(producer);
            delayedInit(cryptoStream, cryptoHelperJca.blockSize);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}