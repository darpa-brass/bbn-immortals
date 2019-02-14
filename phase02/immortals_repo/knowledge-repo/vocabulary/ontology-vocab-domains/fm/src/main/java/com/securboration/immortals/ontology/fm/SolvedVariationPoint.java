package com.securboration.immortals.ontology.fm;

import com.securboration.immortals.ontology.fm.feature.AbstractSoftwareFeature;
import com.securboration.immortals.ontology.fm.feature.FeatureSelectionPoint;

/**
 * Describes a solved point of variability in a software feature model. E.g., if
 * [A,B,C] or [D,E] or [F] could go in variation point V, the following are
 * the valid solutions: {[A,B,C], [D,E], or [F]}
 * 
 * @author jstaples
 *
 */
public class SolvedVariationPoint {
    
    /**
     * The point of variation in a Feature Model that was solved
     */
    private FeatureSelectionPoint solvedVariationPoint;
    
    /**
     * The selected features that satisfy the constraints of the original 
     * Feature Model
     */
    private AbstractSoftwareFeature[] solvedFeatures;
    
    

}
