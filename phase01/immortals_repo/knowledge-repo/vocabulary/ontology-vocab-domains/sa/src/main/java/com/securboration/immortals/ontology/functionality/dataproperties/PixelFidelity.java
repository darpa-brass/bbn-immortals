package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal("describes the fidelity of an image in terms of the number of visible pixels")
    )
public class PixelFidelity extends Fidelity {
    
}