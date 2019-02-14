package mil.darpa.immortals.core.das;

public class ResourceProfile {

	public ResourceProfile() {}
	
	public ResourceProfile(String resourceProfileUri, String targetResourceTypeUri, String formula, String constrainingMetricLinkID, String unit) {
		this.setResourceProfileUri(resourceProfileUri);
		this.setTargetResourceTypeUri(targetResourceTypeUri);
		this.setFormula(formula);
		this.setConstrainingMetricLinkID(constrainingMetricLinkID);
		this.setUnit(unit);
	}
	
	public String getResourceProfileUri() {
		return resourceProfileUri;
	}

	public void setResourceProfileUri(String resourceProfileUri) {
		this.resourceProfileUri = resourceProfileUri;
	}

	public String getTargetResourceTypeUri() {
		return targetResourceTypeUri;
	}

	public void setTargetResourceTypeUri(String targetResourceTypeUri) {
		this.targetResourceTypeUri = targetResourceTypeUri;
	}

	public String getFormula() {
		return formula;
	}
	
	public void setFormula(String formula) {
		this.formula = formula;
	}
	
	public String getConstrainingMetricLinkID() {
		return constrainingMetricLinkID;
	}

	public void setConstrainingMetricLinkID(String constrainingMetricLinkID) {
		this.constrainingMetricLinkID = constrainingMetricLinkID;
	}
	
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	private String resourceProfileUri;
	private String targetResourceTypeUri;
	private String formula;
	private String constrainingMetricLinkID;
	private String unit;
}
