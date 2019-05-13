package com.securboration.immortals.ontology.cp.jvm;

/**
 * A Java Virtual Machine.
 * 
 * @author Securboration
 *
 */
public class JavaRuntimeEnvironment extends RuntimeEnvironment {
    
    /**
     * The absolute path to the JAVA_HOME directory
     */
    private String javaHomePath;
    
    /**
     * The version of java installed at the specified directory
     */
    private String javaVersion;
    
    /**
     * True iff the export controls on cryptographic strength are disabled
     */
    private boolean unlimitedCryptoStrengh;
    
    public JavaRuntimeEnvironment(){
        super();
    }

    
    public String getJavaHomePath() {
        return javaHomePath;
    }

    
    public void setJavaHomePath(String javaHomePath) {
        this.javaHomePath = javaHomePath;
    }

    
    public String getJavaVersion() {
        return javaVersion;
    }

    
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    
    public Boolean getUnlimitedCryptoStrengh() {
        return unlimitedCryptoStrengh;
    }

    
    public void setUnlimitedCryptoStrengh(Boolean unlimitedCryptoStrengh) {
        this.unlimitedCryptoStrengh = unlimitedCryptoStrengh;
    }

    
    
    
}
