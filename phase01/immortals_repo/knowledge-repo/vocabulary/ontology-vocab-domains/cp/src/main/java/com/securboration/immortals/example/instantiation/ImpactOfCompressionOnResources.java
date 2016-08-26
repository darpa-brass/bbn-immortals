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
public class ImpactOfCompressionOnResources extends PropertyImpact{
    
    public ImpactOfCompressionOnResources(){
        
        this.setHumanReadableDescription(
            "The addition of a compression property results in " +
            " decreased file size and increased entropy"
            );
        this.setCriterionForImpact(
            PropertyCriterionType.WHEN_PROPERTY_ADDED
            );
        this.setProperty(
            Compressed.class
            );
        
        ImpactOnResource decreasesMemory = new ImpactOnResource();
        {
            decreasesMemory.setImpact(ImpactType.DECREASES);
            decreasesMemory.setImpactedResource(PhysicalMemoryResource.class);
        }
        
        ImpactOnProperty increasesEntropy = new ImpactOnProperty();
        {
            increasesEntropy.setImpact(ImpactType.INCREASES);
            increasesEntropy.setImpactedProperty(Entropy.class);
        }
        
        this.setImpact(new ImpactSpecification[]{
                decreasesMemory,
                increasesEntropy
        });
    }

}
