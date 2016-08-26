package com.securboration.immortals.test;

import com.securboration.immortals.ontology.algorithm.Algorithm;
import com.securboration.immortals.ontology.algorithm.purpose.AlgorithmPurpose;
import com.securboration.immortals.ontology.functionality.alg.encryption.aes.AES_128;

import mil.darpa.immortals.annotation.dsl.ontology.algorithm.AlgorithmStandardProperty;
import mil.darpa.immortals.annotation.dsl.ontology.algorithm.purpose.AlgorithmPurposeProperty;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.properties.BlockBased;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.properties.KeyLength;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.properties.Symmetric;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties.Encrypted;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.Image;

public class Example1_encryption {

    /**
     * Converts an encrypted image into a non encrypted image (ie decrypts the
     * image).
     * 
     * The cipher and its properties are not specified. As a result,
     * reusability/swappability are minimal.
     * 
     * A lint-like annotation checking tool should pick up the missing 
     * properties after scanning this class and issue a warning (not yet 
     * implemented).
     * 
     * @param image
     * @return
     */
    public 
        @Image
        byte[] 
            decryptImage1(
                @Image
                @Encrypted
                byte[] image
                ){return null;}
    
    /**
     * Converts an encrypted image into a non encrypted image (ie decrypts the 
     * image).  
     * 
     * The cipher is an existing standard: AES_128.  It has sufficient 
     * properties associated with it to swap out alternate implementations.
     * 
     * @param image
     * @return
     */
    public 
        @Image
        byte[] 
            decryptImage2(
                @Image
                @Encrypted(encryptionAlgorithm=AES_128.class)
                byte[] image
                ){return null;}
    
    /**
     * Converts an encrypted image into a non-encrypted image (ie decrypts the 
     * image).  
     * 
     * The cipher is AES_1024, a hypothetical standard that emerges in the 
     * future.
     * 
     * @param image
     * @return
     */
    public 
        @Image
        byte[] 
            decryptImage3(
                @Image
                @Encrypted(encryptionAlgorithm=AES_1024.class)
                byte[] image
                ){return null;}
    
    /**
     * An embedded definition of a new encryption standard that operates on
     * 1024-bit blocks with a 1024-bit key size.
     * 
     */
    @AlgorithmPurposeProperty(
            purpose = AlgorithmPurpose.ENCRYPTION
            )
    @AlgorithmStandardProperty(
            ownerOrganization = "nist", 
            standardName = "fips-500_FUTURE", 
            url = "http://csrc.nist.gov/publications/fips/fips197/fips-500_FUTURE.pdf"
            )
    @BlockBased(blockSize=1024)
    @KeyLength(keyLength=1024)
    @Symmetric
    private static class AES_1024 extends Algorithm {}
    
    
}
