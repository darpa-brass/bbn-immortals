package com.securboration.immortals.ontology.core.org;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

/**
 * A standard for some domain or process
 * 
 * @author Securboration
 *
 */
@Triple(
    predicateUri = rdfs.comment$,
    objectLiteral = @Literal("A standard for describing some domain " +
            "or process.  E.g., FIPS 46-3 defines the Data Encryption " +
            "Standard (DES)."))
public class Standard {
//    String s = Uris.dcelements.creator$
}
