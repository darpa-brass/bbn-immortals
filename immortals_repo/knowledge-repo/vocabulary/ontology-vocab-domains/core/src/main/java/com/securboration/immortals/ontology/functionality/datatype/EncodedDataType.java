package com.securboration.immortals.ontology.functionality.datatype;

/**
 * An encoded data type
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An encoded data type  @author jstaples ")
public class EncodedDataType extends DataType {
    
    /**
     * The encoding for the data type (e.g., base64)
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The encoding for the data type (e.g., base64)")
    private Encoding encoding;

    public Encoding getEncoding() {
        return encoding;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }
    
}
