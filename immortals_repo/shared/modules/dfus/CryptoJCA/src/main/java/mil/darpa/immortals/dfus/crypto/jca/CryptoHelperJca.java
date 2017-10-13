package mil.darpa.immortals.dfus.crypto.jca;

import org.apache.commons.codec.binary.Base64;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;


/**
 * Base encryption DFU
 * <p>
 * Created by awellman@bbn.com on 5/18/16.
 */
abstract class CryptoHelperJca {

    protected abstract String getCipherAlgorithm();

    private final Key encryptionKey;
    protected final int blockSize;
    protected Cipher _decryptionCipher;
    protected Cipher _encryptionCipher;

    private Cipher getDecryptionCipher() throws GeneralSecurityException {
        if (_decryptionCipher == null) {
            _decryptionCipher = Cipher.getInstance(getCipherAlgorithm());
            _decryptionCipher.init(Cipher.DECRYPT_MODE, encryptionKey);
        }
        return _decryptionCipher;
    }

    private Cipher getEncryptionCipher() throws GeneralSecurityException {
        if (_encryptionCipher == null) {
            _encryptionCipher = Cipher.getInstance(getCipherAlgorithm());
            _encryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        }
        return _encryptionCipher;
    }


    public CryptoHelperJca(String encryptionKey) throws GeneralSecurityException {
        String cipherAlgorithm = getCipherAlgorithm();
        this.encryptionKey = loadKey(cipherAlgorithm.split("/")[0], encryptionKey);
        this.blockSize = Cipher.getInstance(getCipherAlgorithm()).getBlockSize();
    }

    private SecretKey loadKey(@Nonnull String cipherAlgorithm, @Nonnull String key) throws GeneralSecurityException {
        byte[] keyBytes = Base64.decodeBase64(key.getBytes());
        SecretKeySpec sks = new SecretKeySpec(keyBytes, cipherAlgorithm);
        return sks;
    }

    protected CipherOutputStream attachCipherEncryptionStream(OutputStream targetStream) {
        try {
            Cipher c = Cipher.getInstance(getCipherAlgorithm());
            c.init(Cipher.ENCRYPT_MODE, encryptionKey);
            return new CipherOutputStream(targetStream, c);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    protected CipherInputStream attachCipherDecryptionStream(InputStream sourceStream) {
        try {
            Cipher c = Cipher.getInstance(getCipherAlgorithm());
            c.init(Cipher.DECRYPT_MODE, encryptionKey);
            return new CipherInputStream(sourceStream, c);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

    }

    protected byte[] encryptAll(byte[] bytes) {
        synchronized (encryptionKey) {
            try {
                return getEncryptionCipher().doFinal(bytes);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected byte[] decryptAll(byte[] bytes) {
        synchronized (encryptionKey) {
            try {
                return getDecryptionCipher().doFinal(bytes);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
