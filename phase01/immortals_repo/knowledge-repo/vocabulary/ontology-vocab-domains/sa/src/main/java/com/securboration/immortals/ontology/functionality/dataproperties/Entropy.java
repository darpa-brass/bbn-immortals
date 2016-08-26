package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.uris.Uris.rdfs;

@ConceptInstance
@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal("the Shannon entropy of data")
    )
public class Entropy extends DataProperty {
    
}