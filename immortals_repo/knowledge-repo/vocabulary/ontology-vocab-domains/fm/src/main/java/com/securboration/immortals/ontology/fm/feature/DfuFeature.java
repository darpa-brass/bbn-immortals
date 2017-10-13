package com.securboration.immortals.ontology.fm.feature;

import com.securboration.immortals.ontology.dfu.instance.DfuInstance;
import com.securboration.immortals.ontology.tmp.ConfigurationSpec;

/**
 * A cleanly separable feature that can be implemented by a single DFU. E.g., an
 * encoding library that converts binary data into a Base64 String
 * 
 * @author jstaples
 *
 */
public class DfuFeature extends AbstractSoftwareFeature {
	
	/**
     * Specifies the configuration of any DFU to be substituted here
     */
    private ConfigurationSpec configurationSpec;
    
    /**
     * The functionality performed by this feature
     */
    private DfuInstance dfuOfFeature;

	public ConfigurationSpec getConfigurationSpec() {
		return configurationSpec;
	}

	public void setConfigurationSpec(ConfigurationSpec configurationSpec) {
		this.configurationSpec = configurationSpec;
	}

	public DfuInstance getDfuOfFeature() {
		return dfuOfFeature;
	}

	public void setDfuOfFeature(DfuInstance dfuOfFeature) {
		this.dfuOfFeature = dfuOfFeature;
	}

}
