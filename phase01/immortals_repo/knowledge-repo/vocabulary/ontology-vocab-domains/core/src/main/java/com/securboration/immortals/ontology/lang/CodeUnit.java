package com.securboration.immortals.ontology.lang;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

/**
 * A fundamental compilable unit of code (e.g., a C or Java source file)
 * 
 * @author Securboration
 *
 */
@Triple(
    predicateUri = rdfs.comment$,
    objectLiteral = @Literal("a fundamental compilable unit of code, " +
            "e.g., a C or Java source file"))
public class CodeUnit {

}
