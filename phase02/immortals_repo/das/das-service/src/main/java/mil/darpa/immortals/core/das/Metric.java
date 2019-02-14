package mil.darpa.immortals.core.das;

public class Metric {

	public Metric() {}
	
	public Metric(String linkId, String value, String unit, String measurementType, String resourceUri, String resourceTypeUri,
			String type, String property, AssertionCriterionValue assertionCriterionValue) {

		setLinkId(linkId);
		setValue(value);
		setUnit(unit);
		setMeasurementType(measurementType);
		setResourceUri(resourceUri);
		setResourceTypeUri(resourceTypeUri);
		setType(type);
		setProperty(property);
		setAssertionCriterion(assertionCriterionValue);
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getUnit() {
		return unit;
	}
	
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public String getMeasurementType() {
		return measurementType;
	}
	
	public void setMeasurementType(String measurementType) {
		this.measurementType = measurementType;
	}
	
	public String getResourceUri() {
		return resourceUri;
	}
	
	public void setResourceUri(String resourceUri) {
		this.resourceUri = resourceUri;
	}
	
	public String getResourceTypeUri() {
		return resourceTypeUri;
	}
	
	public void setResourceTypeUri(String resourceTypeUri) {
		this.resourceTypeUri = resourceTypeUri;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}
	
	public AssertionCriterionValue getAssertionCriterion() {
		return assertionCriterion;
	}

	public void setAssertionCriterion(AssertionCriterionValue assertionCriterion) {
		this.assertionCriterion = assertionCriterion;
	}

	private String linkId;
	private String value;
	private String unit;
	private String measurementType;
	private String resourceUri;
	private String resourceTypeUri;
	private String property;
	private String type;
	private AssertionCriterionValue assertionCriterion;
}
