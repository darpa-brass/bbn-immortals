package com.bbn.marti.dataservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataServiceConfiguration {

	Logger logger = LoggerFactory.getLogger(DataServiceConfiguration.class);
	
	public DataServiceConfiguration() {}
	
	public DataServiceConfiguration(String baseUrl) {
		logger.info("Data Service Configuration created with base url: "+baseUrl);
		setBaseUrl(baseUrl);
	}
	
	public String getBaseUrl() {
		return baseUrl;
	}


	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DataServiceConfiguration)) {
			return false;
		}
		DataServiceConfiguration other = (DataServiceConfiguration) obj;
		if (baseUrl == null) {
			if (other.baseUrl != null) {
				return false;
			}
		} else if (!baseUrl.equals(other.baseUrl)) {
			return false;
		}
		return true;
	}

	private String baseUrl;
}
