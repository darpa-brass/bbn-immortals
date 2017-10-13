package com.securboration.immortals.ontology.functionality.datatype;

/**
 * An encoded data type
 * 
 * @author Securboration
 *
 */
public class EncodedDataType extends DataType {
    
    /**
     * The encoding for the data type (e.g., base64)
     */
    private Encoding encoding;

    public Encoding getEncoding() {
        return encoding;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }
    
}
