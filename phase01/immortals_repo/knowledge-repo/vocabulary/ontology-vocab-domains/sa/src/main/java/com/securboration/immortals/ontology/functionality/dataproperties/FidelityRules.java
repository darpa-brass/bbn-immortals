package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.resources.memory.PhysicalMemoryResource;
import com.securboration.immortals.uris.Uris.rdfs;

@Ignore//note: ignore only applies to outer class
public class FidelityRules {
    
    @ConceptInstance
    @Triple(
        predicateUri=rdfs.comment$,
        objectLiteral=@Literal("describes the impact of increasing # of pixels on memory use")
        )
    public static class IncreasePixelsVsMemory extends FidelityResourceRelationship {
        
        public IncreasePixelsVsMemory(){
            this.setDrivingCondition(ImpactType.INCREASES);
            this.setDrivenFidelity(PixelFidelity.class);
            //has the result of
            this.setImpactOnResource(ImpactType.INCREASES);
            this.setImpactedResource(PhysicalMemoryResource.class);
        }
        
    }
    
    @ConceptInstance
    @Triple(
        predicateUri=rdfs.comment$,
        objectLiteral=@Literal("describes the impact of decreasing # of pixels on memory use")
        )
    public static class DecreasePixelsVsMemory extends FidelityResourceRelationship {
        
        public DecreasePixelsVsMemory(){
            this.setDrivingCondition(ImpactType.DECREASES);
            this.setDrivenFidelity(PixelFidelity.class);
            //has the result of
            this.setImpactOnResource(ImpactType.DECREASES);
            this.setImpactedResource(PhysicalMemoryResource.class);
        }
        
    }

}
