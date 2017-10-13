package com.securboration.immortals.ontology.lang;

/**
 * A discrete compiled code unit
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A discrete compiled code unit  @author jstaples ")
public class DiscreteCompiledCodeUnit extends CompiledCodeUnit {
    
    /**
     * The source code compiled into this artifact
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The source code compiled into this artifact")
    private SourceFile source;
    
    /**
     * The compiled form of the artifact
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The compiled form of the artifact")
    private byte[] compiledForm;
    
    public DiscreteCompiledCodeUnit(){}
    
    public SourceFile getSource() {
        return source;
    }

    public void setSource(SourceFile source) {
        this.source = source;
    }

    public byte[] getCompiledForm() {
        return compiledForm;
    }

    public void setCompiledForm(byte[] compiledForm) {
        this.compiledForm = compiledForm;
    }

}
