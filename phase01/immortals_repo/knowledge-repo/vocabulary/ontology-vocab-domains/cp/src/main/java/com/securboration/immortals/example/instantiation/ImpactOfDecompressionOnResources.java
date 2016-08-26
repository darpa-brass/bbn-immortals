package com.securboration.immortals.example.instantiation;

import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
import com.securboration.immortals.ontology.functionality.dataproperties.Compressed;
import com.securboration.immortals.ontology.functionality.dataproperties.Entropy;
import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.impact.ImpactOnProperty;
import com.securboration.immortals.ontology.property.impact.ImpactOnResource;
import com.securboration.immortals.ontology.property.impact.ImpactSpecification;
import com.securboration.immortals.ontology.property.impact.PropertyImpact;
import com.securboration.immortals.ontology.resources.memory.PhysicalMemoryResource;

@ConceptInstance
public class ImpactOfDecompressionOnResources extends PropertyImpact{
    
    public ImpactOfDecompressionOnResources(){
        
        this.setHumanReadableDescription(
            "The removal of the compression property results in " +
            " increased memory use and reduced entropy"
            );
        this.setCriterionForImpact(
            PropertyCriterionType.WHEN_PROPERTY_REMOVED
            );
        this.setProperty(
            Compressed.class
            );
        
        ImpactOnResource decreasesMemory = new ImpactOnResource();
        {
            decreasesMemory.setImpact(ImpactType.INCREASES);
            decreasesMemory.setImpactedResource(PhysicalMemoryResource.class);
        }
        
        ImpactOnProperty increasesEntropy = new ImpactOnProperty();
        {
            increasesEntropy.setImpact(ImpactType.DECREASES);
            increasesEntropy.setImpactedProperty(Entropy.class);
        }
        
        this.setImpact(new ImpactSpecification[]{
                decreasesMemory,
                increasesEntropy
        });
    }

}
