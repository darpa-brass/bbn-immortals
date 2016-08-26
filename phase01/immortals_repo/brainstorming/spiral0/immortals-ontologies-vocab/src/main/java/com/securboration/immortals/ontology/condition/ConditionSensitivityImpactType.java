package com.securboration.immortals.ontology.condition;

/**
 * Describes the performance impact of a sensitivity on a resource's performance
 * 
 * @author Securboration
 *
 */
public enum ConditionSensitivityImpactType {

    Optimizes,
    
    ImprovesSignificantly,
    Improves,
    
    DoesNotAffect,
    
    Degrades,
    DegradesSignificantly,
    
    Breaks
    ;

}
