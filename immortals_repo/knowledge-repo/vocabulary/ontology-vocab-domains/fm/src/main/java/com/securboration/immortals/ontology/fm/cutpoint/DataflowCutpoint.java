package com.securboration.immortals.ontology.fm.cutpoint;

/**
 * A cutpoint that intersects with a dataflow within an application
 * 
 * @author Securboration
 *
 */
public class DataflowCutpoint extends FeatureInjectionCutpoint {
	
	/**
	 * A unique ID that corresponds to a specific dataflow (which may, in turn,
	 * correspond to multiple places in code)
	 */
	private String dataflowId;

	public String getDataflowId() {
		return dataflowId;
	}

	public void setDataflowId(String dataflowId) {
		this.dataflowId = dataflowId;
	}

}
