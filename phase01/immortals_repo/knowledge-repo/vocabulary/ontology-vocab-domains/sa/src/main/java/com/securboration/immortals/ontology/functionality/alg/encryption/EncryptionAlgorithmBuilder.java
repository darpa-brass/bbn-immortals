package com.securboration.immortals.ontology.functionality.alg.encryption;

import com.securboration.immortals.ontology.algorithm.AlgorithmConfigurationProperty;
import com.securboration.immortals.ontology.algorithm.AlgorithmProperty;
import com.securboration.immortals.ontology.algorithm.AlgorithmSpecificationProperty;
import com.securboration.immortals.ontology.algorithm.AlgorithmStandardProperty;
import com.securboration.immortals.ontology.algorithm.purpose.AlgorithmPurpose;
import com.securboration.immortals.ontology.algorithm.purpose.AlgorithmPurposeProperty;
import com.securboration.immortals.ontology.functionality.alg.encryption.properties.Asymmetric;
import com.securboration.immortals.ontology.functionality.alg.encryption.properties.BlockBased;
import com.securboration.immortals.ontology.functionality.alg.encryption.properties.KeyLength;
import com.securboration.immortals.ontology.functionality.alg.encryption.properties.Symmetric;
import com.securboration.immortals.ontology.pojos.markup.Ignore;

@Ignore
public class EncryptionAlgorithmBuilder {
    
    public static AlgorithmConfigurationProperty getBlockSizeProperty(int size){
        BlockBased p = new BlockBased();
        p.setBlockSize(size);
        
        return p;
    }
    
    private static final Asymmetric asymmetricProperty = new Asymmetric();
    private static final Symmetric symmetricProperty = new Symmetric();
    
    
    public static KeyLength getKeyLengthProperty(int size){
        KeyLength k = new KeyLength();
        
        k.setKeyLength(size);
        
        return k;
    }
    
    public static AlgorithmSpecificationProperty getPurpose(AlgorithmPurpose p){
        AlgorithmPurposeProperty property = new AlgorithmPurposeProperty();
        property.setPurpose(p);
        return property;
    }
    
    public static AlgorithmStandardProperty getStandard(
            final String org,
            final String standardName,
            final String standardUrl
            ){
        AlgorithmStandardProperty p = new AlgorithmStandardProperty();
        
        p.setOwnerOrganization(org);
        p.setStandardName(standardName);
        p.setUrl(standardUrl);
        
        return p;
    }
    
    public static AlgorithmProperty[] getSymmetricBlockEncryptionAlgorithmProperties(
            final String standardOrg,
            final String standardUrl,
            final String standardName,
            final int blockSize, 
            final int keyLength
            ){
        return new AlgorithmProperty[]{
                getStandard(standardOrg,standardName,standardUrl),
                getPurpose(AlgorithmPurpose.ENCRYPTION),
                symmetricProperty,
                getKeyLengthProperty(keyLength),
                getBlockSizeProperty(blockSize)
        };
    }

}
