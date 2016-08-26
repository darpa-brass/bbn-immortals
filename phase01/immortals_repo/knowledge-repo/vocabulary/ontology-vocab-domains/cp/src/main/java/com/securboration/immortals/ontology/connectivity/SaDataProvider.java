package com.securboration.immortals.ontology.connectivity;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "TODO"
        )
    )
@ConceptInstance
public class SaDataProvider extends Functionality {
    
    public SaDataProvider(){
        
    }

}
