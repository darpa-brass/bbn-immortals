package com.securboration.immortals.ontology.fm;

import com.securboration.immortals.ontology.core.HumanReadable;
import com.securboration.immortals.ontology.fm.feature.FeatureSelectionPoint;

/**
 * A feature model specifies the valid configurations of a software system. Its
 * core abstraction is the SoftwareVariationPoint which may be satisfied by one
 * or more Features selected from an enumerated list of possibilities.
 * 
 * @author jstaples
 *
 */
public class FeatureModel implements HumanReadable {
	
	private String humanReadableDesc;
    
    /**
     * Describes the variation of the Feature Model
     */
    private FeatureSelectionPoint[] variationPoint;

	public FeatureSelectionPoint[] getVariationPoints() {
		return variationPoint;
	}

	public void setVariationPoints(FeatureSelectionPoint[] variationPoints) {
		this.variationPoint = variationPoints;
	}

	@Override
	public String getHumanReadableDesc() {
		return humanReadableDesc;
	}
	public void setHumanReadableDesc(String humanReadableDesc) {
		this.humanReadableDesc = humanReadableDesc;
	}
    

}
