package com.securboration.immortals.ontology.lang;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

/**
 * The result of compiling source code
 * 
 * @author Securboration
 *
 */
@Triple(
    predicateUri = rdfs.comment$,
    objectLiteral = @Literal("a compiled code unit, which includes " +
            "references to source file[s] and their compiled form"))
public abstract class CompiledCodeUnit {

    public CompiledCodeUnit() {
    }

}
