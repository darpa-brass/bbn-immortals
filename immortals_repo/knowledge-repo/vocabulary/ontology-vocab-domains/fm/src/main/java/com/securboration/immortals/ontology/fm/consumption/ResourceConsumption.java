package com.securboration.immortals.ontology.fm.consumption;

import com.securboration.immortals.ontology.core.Resource;

/**
 * Simple model of the consumption of a resource
 * 
 * @author Securboration
 *
 */
public class ResourceConsumption {

	/**
	 * The amount of the resource consumed
	 */
	private String consumedQuantityExpression;
	
	/**
	 * The unit of resource consumed.  E.g., MB
	 */
	private String consumedQuantityUnit;
	
	/**
	 * The type of resource consumed
	 */
	private Class<? extends Resource> consumedResourceType;
	
	/**
	 * The qualifier of the consumption
	 */
	private ConsumptionQualifier consumptionQualifier;
	
	/**
	 * The scope of the consumption
	 */
	private ConsumptionScope consumptionScope;

	public String getConsumedQuantityExpression() {
		return consumedQuantityExpression;
	}

	public void setConsumedQuantityExpression(String consumedQuantityExpression) {
		this.consumedQuantityExpression = consumedQuantityExpression;
	}

	public String getConsumedQuantityUnit() {
		return consumedQuantityUnit;
	}

	public void setConsumedQuantityUnit(String consumedQuantityUnit) {
		this.consumedQuantityUnit = consumedQuantityUnit;
	}

	public Class<? extends Resource> getConsumedResourceType() {
		return consumedResourceType;
	}

	public void setConsumedResourceType(Class<? extends Resource> consumedResourceType) {
		this.consumedResourceType = consumedResourceType;
	}

	public ConsumptionQualifier getConsumptionQualifier() {
		return consumptionQualifier;
	}

	public void setConsumptionQualifier(ConsumptionQualifier consumptionQualifier) {
		this.consumptionQualifier = consumptionQualifier;
	}

	public ConsumptionScope getConsumptionScope() {
		return consumptionScope;
	}

	public void setConsumptionScope(ConsumptionScope consumptionScope) {
		this.consumptionScope = consumptionScope;
	}

}
