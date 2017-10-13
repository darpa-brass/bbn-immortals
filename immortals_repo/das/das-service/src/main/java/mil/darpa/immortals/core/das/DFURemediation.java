package mil.darpa.immortals.core.das;

public class DFURemediation {

	public DFURemediation() {}
	
	public DFURemediation(String functionalAspectUri, String applicableDataType, String action, String property, String strategyUri) {
		setFunctionalAspectUri(functionalAspectUri);
		setApplicableDataType(applicableDataType);
		setAction(action);
		setProperty(property);
		setStrategyUri(strategyUri);
	}
	
	public String getFunctionalAspectUri() {
		return functionalAspectUri;
	}

	public void setFunctionalAspectUri(String functionalAspectUri) {
		this.functionalAspectUri = functionalAspectUri;
	}

	public String getApplicableDataType() {
		return applicableDataType;
	}

	public void setApplicableDataType(String applicableDataType) {
		this.applicableDataType = applicableDataType;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}
	
	public String getStrategyUri() {
		return strategyUri;
	}

	public void setStrategyUri(String strategyUri) {
		this.strategyUri = strategyUri;
	}

	private String functionalAspectUri;
	private String applicableDataType;
	private String action;
	private String property;
	private String strategyUri;
}
