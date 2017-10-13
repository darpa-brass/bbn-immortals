package com.securboration.immortals.ontology.fm;

/**
 * A variant is a valid instantiation that meets the requirements of some
 * FeatureModel
 * 
 * @author jstaples
 *
 */
public class ProgramVariant {
    
    /**
     * The FeatureModel from which this variant was derived
     */
    private FeatureModel originalFeatureModel;
    
    /**
     * For each variation point, specifies one or more SoftwareFeatures to use
     */
    private SolvedVariationPoint[] solvedVariationPoint;
}
