package com.securboration.miniatakapp;

import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherDecrypt;
import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherEncrypt;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.BinaryData;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;


@DfuAnnotation(
        functionalityBeingPerformed = com.securboration.immortals.ontology.functionality.alg.encryption.Cipher.class
)
public class JavaxCryptoWrapper {

    private static byte[] key = "MZygpewJsCpRrfOr".getBytes(StandardCharsets.UTF_8);

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

 
    /**
     * Encrypts the given plain text
     *
     * @param plainText The plain text to encrypt
     */
    public static byte[] encrypt(byte[] plainText)
    {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            return cipher.doFinal(plainText);
        }catch (Exception exc) {
            exc.printStackTrace();
        }
        
        return null;
    }

    /**
     * Decrypts the given byte array
     *
     * @param cipherText The data to decrypt
     */
    public static byte[] decrypt(byte[] cipherText)
    {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(cipherText);
            
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return null;
    }
    
    public static void main(String[] args) {
        
        try {
            byte[] cipherText = JavaxCryptoWrapper.encrypt("secretMessage".getBytes(StandardCharsets.UTF_8));
            byte[] decryptedCipherText = JavaxCryptoWrapper.decrypt(cipherText);

            System.out.println(new String("secretMessage"));
            System.out.println(new String(cipherText));
            System.out.println(new String(decryptedCipherText));
        } catch(Exception exc) {
            exc.printStackTrace();
        }
    }
    
}
