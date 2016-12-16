//package com.securboration.immortals.example.instantiation;
//
//import com.securboration.immortals.ontology.annotations.triples.Literal;
//import com.securboration.immortals.ontology.annotations.triples.Triple;
//import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
//import com.securboration.immortals.ontology.constraint.ResourceImpactType;
//import com.securboration.immortals.ontology.functionality.dataproperties.MemoryFootprint;
//import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
//import com.securboration.immortals.ontology.property.impact.CauseEffectAssertion;
//import com.securboration.immortals.ontology.property.impact.ImpactStatement;
//import com.securboration.immortals.ontology.property.impact.AbstractPropertyCriterion;
//import com.securboration.immortals.ontology.property.impact.ResourceImpact;
//import com.securboration.immortals.ontology.resources.network.NetworkBandwidth;
//import com.securboration.immortals.uris.Uris.rdfs;
//
//@ConceptInstance
/**
 * Describes the impact on network bandwidth of a larger memory footprint
 * 
 * @author Securboration
 */
//public class ImpactOfMemoryOnBandwidth extends CauseEffectAssertion{
//    
//    public ImpactOfMemoryOnBandwidth(){
//        
//        this.setHumanReadableDescription(
//            "A larger memory footprint is associated with more bandwidth " +
//            "being consumed"
//            );
//        
//        AbstractPropertyCriterion criterion = new AbstractPropertyCriterion();{
//            criterion.setHumanReadableDescription(
//                "an increase in the memory footprint"
//                );
//            criterion.setCriterion(PropertyCriterionType.WHEN_PROPERTY_INCREASES);
//            criterion.setProperty(MemoryFootprint.class);
//        }this.setCriterion(criterion);
//        
//        ResourceImpact impact = new ResourceImpact();{
//            impact.setHumanReadableDescription(
//                "an increase in the consumption of network bandwidth"
//                );
//            impact.setImpactOnResource(ResourceImpactType.INCREASES_CONSUMPTION_OF);
//            impact.setImpactedResource(NetworkBandwidth.class);
//        }this.setImpact(new ImpactStatement[]{impact});
//    }
//
//}
