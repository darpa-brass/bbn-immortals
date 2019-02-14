package com.securboration.immortals.ontology.fm.feature;

import com.securboration.immortals.ontology.fm.consumption.ResourceConsumption;
import com.securboration.immortals.ontology.fm.cutpoint.FeatureInjectionCutpoint;
import com.securboration.immortals.ontology.vp.FeatureProvides;
import com.securboration.immortals.ontology.vp.FeatureRequirement;

/**
 * A software feature
 * 
 * @author jstaples
 *
 */
public abstract class AbstractSoftwareFeature {
	
	/**
	 * Links an abstract feature to a place in code. Typically, this is done
	 * using a control point identifier, which is conveyed via annotation of the
	 * source code.
	 */
	private FeatureInjectionCutpoint correspondingCodeCutpoint;
    
    /**
     * The capabilities provided by the feature
     * TODO
     */
    private FeatureProvides[] featureProvides;
    
    /**
     * The requirements of the feature that must be met before the feature can
     * be selected
     */
    private FeatureRequirement[] featureRequirement;
    
    /**
     * Describes the resources consumed upon selection of this feature
     */
    private ResourceConsumption[] featureResourceConsumption;

	public FeatureProvides[] getFeatureProvides() {
		return featureProvides;
	}

	public void setFeatureProvides(FeatureProvides[] featureProvides) {
		this.featureProvides = featureProvides;
	}

	public FeatureRequirement[] getFeatureRequirement() {
		return featureRequirement;
	}

	public void setFeatureRequirement(FeatureRequirement[] featureRequirement) {
		this.featureRequirement = featureRequirement;
	}

	public FeatureInjectionCutpoint getCorrespondingCodeCutpoint() {
		return correspondingCodeCutpoint;
	}

	public void setCorrespondingCodeCutpoint(FeatureInjectionCutpoint correspondingApplicationCutpoint) {
		this.correspondingCodeCutpoint = correspondingApplicationCutpoint;
	}

	public ResourceConsumption[] getFeatureResourceConsumption() {
		return featureResourceConsumption;
	}

	public void setFeatureResourceConsumption(ResourceConsumption[] featureResourceConsumption) {
		this.featureResourceConsumption = featureResourceConsumption;
	}

}
