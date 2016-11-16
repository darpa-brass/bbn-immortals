//package com.securboration.immortals.example.instantiation;
//
//import com.securboration.immortals.ontology.annotations.triples.Literal;
//import com.securboration.immortals.ontology.annotations.triples.Triple;
//import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
//import com.securboration.immortals.ontology.constraint.PropertyImpactType;
//import com.securboration.immortals.ontology.constraint.ResourceImpactType;
//import com.securboration.immortals.ontology.functionality.dataproperties.Entropy;
//import com.securboration.immortals.ontology.functionality.dataproperties.MemoryFootprint;
//import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
//import com.securboration.immortals.ontology.property.impact.CauseEffectAssertion;
//import com.securboration.immortals.ontology.property.impact.ImpactStatement;
//import com.securboration.immortals.ontology.property.impact.AbstractPropertyCriterion;
//import com.securboration.immortals.ontology.property.impact.PropertyImpact;
//import com.securboration.immortals.ontology.property.impact.ResourceImpact;
//import com.securboration.immortals.ontology.resources.memory.PhysicalMemoryResource;
//import com.securboration.immortals.uris.Uris.rdfs;
//
//@ConceptInstance
//@Triple(
//    predicateUri=rdfs.comment$,
//    objectLiteral=@Literal(
//        "Describes the impact on memory and entropy of adding the " +
//        "'Compressed' property"
//        )
//    )
//public class ImpactOfCompressionOnResources extends CauseEffectAssertion{
//    
//    public ImpactOfCompressionOnResources(){
//        
//        this.setHumanReadableDescription(
//            "The addition of the Compressed property results in " +
//                    " reduced memory use and increased entropy"
//            );
//        
//        AbstractPropertyCriterion criterion = new AbstractPropertyCriterion();{
//            criterion.setHumanReadableDescription(
//                "the addition of the Compressed property"
//                );
//            criterion.setCriterion(PropertyCriterionType.WHEN_PROPERTY_ADDED);
//            criterion.setProperty(MemoryFootprint.class);
//        }this.setCriterion(criterion);
//        
//        ResourceImpact impact1 = new ResourceImpact();{
//            impact1.setHumanReadableDescription(
//                "decreases the consumption of memory"
//                );
//            impact1.setImpactOnResource(ResourceImpactType.DECREASES_CONSUMPTION_OF);
//            impact1.setImpactedResource(PhysicalMemoryResource.class);
//        }
//        
//        PropertyImpact impact2 = new PropertyImpact();{
//            impact2.setHumanReadableDescription(
//                "increases the entropy"
//                );
//            impact2.setImpactOnProperty(PropertyImpactType.INCREASES);
//            impact2.setImpactedProperty(Entropy.class);
//        }
//        
//        this.setImpact(new ImpactStatement[]{impact1,impact2});
//    }
//
//}
