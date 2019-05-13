package com.securboration.immortals.ontology.cp.jvm;

/**
 * The Android runtime.
 * 
 * @author Securboration
 *
 */
public class AndroidRuntimeEnvironment extends RuntimeEnvironment {
    
    /**
     * True iff the export controls on cryptographic strength are disabled
     */
    private boolean unlimitedCryptoStrengh;
    
    public AndroidRuntimeEnvironment(){
        super();
    }

    
    public Boolean getUnlimitedCryptoStrengh() {
        return unlimitedCryptoStrengh;
    }

    
    public void setUnlimitedCryptoStrengh(Boolean unlimitedCryptoStrengh) {
        this.unlimitedCryptoStrengh = unlimitedCryptoStrengh;
    }

    
    
    
}
