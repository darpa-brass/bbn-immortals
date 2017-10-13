package com.securboration.immortals.test;

import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherCleanup;
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

/**
 * A cipher DFU
 * 
 * @author Securboration
 *
 */
@DfuAnnotation(
        functionalityBeingPerformed = Cipher.class,
        resourceDependencies={
                Cpu.class,
                PhysicalMemoryResource.class
                },
        properties={} //Note: for now, embedded properties don't work.  
                      //       I know how to make them work, I just haven't 
                      //       implemented it yet.  To add a property to the 
                      //       DFU, add it to the class instead
        )
@BlockBased//see note above.  This property is applicable to the entire DFU
           //could also be @Streaming
public class Example7_BlockCipher {
    
    /**
     * Initialize the cipher with a secret key
     * @param secretKey
     */
    @FunctionalAspectAnnotation(
        aspect=AspectCipherInitialize.class,
        aspectSpecificResourceDependencies={},
        properties={}//Note: again, embedded aspect-specific properties don't 
                     //       work here, add them to the method instead for now
        )
    //properties could go here
    public void initialize(
            @EncryptionKey
            byte[] secretKey
            ){}
    
    /**
     * Decrypt using the secret key provided during initialization
     * 
     * @param data
     * @return
     */
    @FunctionalAspectAnnotation(
        aspect=AspectCipherDecrypt.class,
        aspectSpecificResourceDependencies={}
        )
    public byte[] decrypt(
            @BinaryData//indicates that decrypt operates on binary data
            byte[] data
            ){return null;}
    
    @FunctionalAspectAnnotation(
        aspect=AspectCipherEncrypt.class,
        aspectSpecificResourceDependencies={}
        )
    public byte[] encrypt(
            @BinaryData//indicates that encrypt operates on binary data
            byte[] data
            ){return null;}
    
    @FunctionalAspectAnnotation(
        aspect=AspectCipherCleanup.class,
        aspectSpecificResourceDependencies={}
        )
    public void cleanup(){}
    
}
