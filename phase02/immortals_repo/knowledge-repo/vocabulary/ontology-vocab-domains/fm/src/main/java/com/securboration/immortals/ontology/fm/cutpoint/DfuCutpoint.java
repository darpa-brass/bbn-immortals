package com.securboration.immortals.ontology.fm.cutpoint;

import com.securboration.immortals.ontology.dfu.instance.DfuInstance;

/**
 * A cutpoint that intersects with a specific annotated region of code
 * 
 * @author Securboration
 *
 */
public class DfuCutpoint extends FeatureInjectionCutpoint {
	
	/**
	 * The DFU instance associated with this cutpoint
	 */
	private DfuInstance dfu;

	public DfuInstance getDfu() {
		return dfu;
	}

	public void setDfu(DfuInstance dfu) {
		this.dfu = dfu;
	}

}
