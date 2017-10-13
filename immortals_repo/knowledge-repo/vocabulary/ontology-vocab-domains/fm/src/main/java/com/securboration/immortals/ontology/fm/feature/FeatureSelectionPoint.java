package com.securboration.immortals.ontology.fm.feature;

/**
 * A FeatureGroup is a recursive abstraction of a point of variation within a
 * feature model. For example, at a given variation point the software may use
 * an X or a Y or a Z or a (P and a Q).
 * 
 * @author jstaples
 *
 */
public class FeatureSelectionPoint extends AbstractSoftwareFeature {

	/**
	 * An enumeration of the child features in the group
	 */
	private AbstractSoftwareFeature[] featureInGroup;

	/**
	 * The criterion for selecting child features from this group. E.g., some
	 * feature groups require that all elements in the group be selected whereas
	 * others require that exactly one be selected whereas others require that 0
	 * or more be selected.
	 */
	private FeatureSelectionCriterion childSelectionCriterion;

	public FeatureSelectionCriterion getChildSelectionCriterion() {
		return childSelectionCriterion;
	}

	public void setChildSelectionCriterion(FeatureSelectionCriterion childSelectionCriterion) {
		this.childSelectionCriterion = childSelectionCriterion;
	}

	public AbstractSoftwareFeature[] getFeatureInGroup() {
		return featureInGroup;
	}

	public void setFeatureInGroup(AbstractSoftwareFeature[] featureInGroup) {
		this.featureInGroup = featureInGroup;
	}
	
	


}
