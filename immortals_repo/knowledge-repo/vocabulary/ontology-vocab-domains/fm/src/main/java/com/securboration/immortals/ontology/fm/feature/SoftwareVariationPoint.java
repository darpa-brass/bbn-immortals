package com.securboration.immortals.ontology.fm.feature;

import com.securboration.immortals.ontology.tmp.ConfigurationSpec;

public class SoftwareVariationPoint extends AbstractSoftwareFeature {
    
    /**
     * The possible features that can be used to compose an application
     */
    private AbstractSoftwareFeature[] variationPointFeatures;
    
    /**
     * Specifies whether all or any of the possible features are required
     */
    private FeatureVariantOperator variationPointFeatureOperator;
    
    /**
     * Uniquely identifies the corresponding ControlPoint annotation
     */
    private String controlPointId;
    
    /**
     * Specifies the configuration of any DFU to be substituted here
     */
    private ConfigurationSpec configurationSpec;

}
