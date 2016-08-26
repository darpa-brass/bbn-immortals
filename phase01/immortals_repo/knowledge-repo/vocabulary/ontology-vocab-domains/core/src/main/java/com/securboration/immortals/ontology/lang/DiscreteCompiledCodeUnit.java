package com.securboration.immortals.ontology.lang;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

/**
 * A discrete compiled code unit
 * 
 * @author Securboration
 *
 */
@Triple(
        predicateUri=rdfs.comment$,
        objectLiteral=@Literal("a discrete compiled code unit")
        )
public class DiscreteCompiledCodeUnit extends CompiledCodeUnit {
    
    /**
     * The source code compiled into this artifact
     */
    private SourceFile source;
    
    /**
     * The compiled form of the artifact
     */
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
