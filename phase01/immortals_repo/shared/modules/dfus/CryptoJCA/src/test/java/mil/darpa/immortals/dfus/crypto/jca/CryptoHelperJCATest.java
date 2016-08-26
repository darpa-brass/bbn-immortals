package mil.darpa.immortals.dfus.crypto.jca;

import mil.darpa.immortals.core.synthesis.adapters.InputStreamToPipe;
import mil.darpa.immortals.core.synthesis.adapters.OutputStreamToPipe;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Created by awellman@bbn.com on 6/15/16.
 */
public class CryptoHelperJCATest {

    private static final String originalString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

    private static final String[] newStringArray = {originalString, "YesNo", "\n", "\n", "This i", "s Lati", "n.", " I don", "'t know La", "tin.", ".\n\n"};

    private static final String SCRATCH_FILE = "scratchFile.dat";

    private static final String CRYPTO_KEY = "fGPgAeiHCivZqzJnESrasQ==";

    public static void CryptoBlockTest(final String algorithm) {
        try {
            CryptoHelperJca cryptoHelperJca = new CryptoHelperJca(CRYPTO_KEY) {
                @Override
                protected String getCipherAlgorithm() {
                    return algorithm;
                }
            };

            for (String originalString : newStringArray) {
                byte[] encryptedBytes = cryptoHelperJca.encryptAll(originalString.getBytes());
                String encryptedString = new String(encryptedBytes);
                byte[] decryptedBytes = cryptoHelperJca.decryptAll(encryptedBytes);
                String decryptedString = new String(decryptedBytes);

                System.out.println("Original Block String:\n" + originalString);
                System.out.println("Encrypted Block String:\n" + encryptedString);
                System.out.println("Decrypted Block String:\n" + decryptedString);

                Assert.assertTrue(originalString.equals(decryptedString));
                Assert.assertFalse(originalString.equals(encryptedString));
            }

        } catch (GeneralSecurityException e) {
            Assert.fail(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void CryptoPipeTest(String algorithm) {
        try {
            String originalString = "";
            String encryptedString;
            String decryptedString;

            JcaEncryptor encryptor = new JcaEncryptor(CRYPTO_KEY, algorithm,
                            new FileOutputStream(new File(SCRATCH_FILE))
            );

            for (String str : newStringArray) {
                originalString += str;
                encryptor.consume(str.getBytes());
            }
            encryptor.flush();
            encryptor.close();

            // Get the encrypted Data
            FileInputStream unecryptedFileReader = new FileInputStream(new File(SCRATCH_FILE));

            byte[] byteBuffer = new byte[8192];
            int readCount;
            int idx = 0;
            while ((readCount = unecryptedFileReader.read(byteBuffer, idx, 4096 - idx)) >= 0) {
                idx += readCount;
            }
            unecryptedFileReader.close();

            encryptedString = new String(Base64.decodeBase64(Arrays.copyOf(byteBuffer, idx)));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JcaDecryptor decryptor = new JcaDecryptor(CRYPTO_KEY, algorithm,
                            new FileInputStream(new File(SCRATCH_FILE))
            );

            byte[] produced;
            while ((produced = decryptor.produce()) != null) {
                baos.write(produced);
            }
            decryptor.close();

            decryptedString = baos.toString();

            System.out.println("Original String:\n" + originalString);
            System.out.println("Encrypted String:\n" + encryptedString);
            System.out.println("Decrypted String:\n" + decryptedString);
            Assert.assertTrue(originalString.equals(decryptedString));
            Assert.assertFalse(originalString.equals(encryptedString));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void AESTest() {
        CryptoPipeTest(JcaAes.cipherAlgorithm);
        CryptoBlockTest(JcaAes.cipherAlgorithm);
    }

    @Test
    public void BlowfishTest() {
        CryptoPipeTest(JcaBlowfish.cipherAlgorithm);
        CryptoBlockTest(JcaBlowfish.cipherAlgorithm);
    }

}
