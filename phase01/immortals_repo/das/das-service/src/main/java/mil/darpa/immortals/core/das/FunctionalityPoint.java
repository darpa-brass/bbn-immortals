package mil.darpa.immortals.core.das;

import java.util.List;

public class FunctionalityPoint {

	public FunctionalityPoint() {}
	
	public FunctionalityPoint(String missionFunctionalityUri, String controlPointUuid, List<String> propertyUris) {
		this.missionFunctionalityUri = missionFunctionalityUri;
		this.controlPointUuid = controlPointUuid;
		this.propertyUris = propertyUris;
	}
	
	public String getMissionFunctionalityUri() {
		return missionFunctionalityUri;
	}

	public void setMissionFunctionalityUri(String missionFunctionalityUri) {
		this.missionFunctionalityUri = missionFunctionalityUri;
	}

	public String getControlPointUuid() {
		return controlPointUuid;
	}

	public void setControlPointUuid(String controlPointUuid) {
		this.controlPointUuid = controlPointUuid;
	}
	
	public List<String> getPropertyUris() {
		return propertyUris;
	}

	public void setPropertyUris(List<String> propertyUris) {
		this.propertyUris = propertyUris;
	}

	private String missionFunctionalityUri;
	private String controlPointUuid;
	private List<String> propertyUris;
}
