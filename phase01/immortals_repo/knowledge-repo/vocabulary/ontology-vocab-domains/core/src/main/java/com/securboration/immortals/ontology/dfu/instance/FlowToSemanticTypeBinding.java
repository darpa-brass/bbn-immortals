package com.securboration.immortals.ontology.dfu.instance;

import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.property.Property;

/**
 * Models the binding of a functional aspect dataflow to bytecode
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Models the binding of a functional aspect dataflow to bytecode " +
    " @author jstaples ")
public class FlowToSemanticTypeBinding {
    
    /**
     * The index of the argument.  For instance methods, 0 refers to the 
     * implicit "this" argument, 1 refers to the first argument, 2 to the
     * second, etc.  For static methods, 0 is an invalid index, 1 refers to the 
     * first argument, 2 to the second, etc.
     * 
     * A value of -1 implies an unknown mapping
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The index of the argument.  For instance methods, 0 refers to the " +
        " implicit \"this\" argument, 1 refers to the first argument, 2 to" +
        " the second, etc.  For static methods, 0 is an invalid index, 1" +
        " refers to the  first argument, 2 to the second, etc.  A value of" +
        " -1 implies an unknown mapping")
    private int argIndex;
    
    /**
     * The semantic type of the data flow
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The semantic type of the data flow")
    private Class<? extends DataType> semanticType;
    
    /**
     * Properties of the flow
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Properties of the flow")
    private Property[] properties;
    
    /**
     * A comment about the mapping.  For human use only.  Format/content may
     * change without warning.
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A comment about the mapping.  For human use only.  Format/content" +
        " may change without warning.")
    private String comment;

    
    public int getArgIndex() {
        return argIndex;
    }

    
    public void setArgIndex(int argIndex) {
        this.argIndex = argIndex;
    }

    
    public Class<? extends DataType> getSemanticType() {
        return semanticType;
    }

    
    public void setSemanticType(Class<? extends DataType> semanticType) {
        this.semanticType = semanticType;
    }

    
    public Property[] getProperties() {
        return properties;
    }

    
    public void setProperties(Property[] properties) {
        this.properties = properties;
    }


    
    public String getComment() {
        return comment;
    }


    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    
    
    
}
