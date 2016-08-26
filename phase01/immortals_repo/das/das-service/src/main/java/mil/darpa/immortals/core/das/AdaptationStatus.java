package mil.darpa.immortals.core.das;

public class AdaptationStatus {
	
	public AdaptationStatus() {
		this.setAdaptationStatusValue(AdaptationStatusValue.PENDING);
	}
	
	public AdaptationStatus(AdaptationStatusValue adaptationStatusValue) {
		this.adaptationStatusValue = adaptationStatusValue;
	}
	
	public AdaptationStatus(AdaptationStatusValue adaptationStatusValue, String details) {
		this.adaptationStatusValue = adaptationStatusValue;
		this.details = details;
	}
	
	public AdaptationStatusValue getAdaptationStatusValue() {
		return adaptationStatusValue;
	}

	public void setAdaptationStatusValue(AdaptationStatusValue adaptationStatusValue) {
		this.adaptationStatusValue = adaptationStatusValue;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
	
	public String getSelectedDfu() {
		return selectedDfu;
	}

	public void setSelectedDfu(String selectedDfu) {
		this.selectedDfu = selectedDfu;
	}

	private AdaptationStatusValue adaptationStatusValue;
	private String details;
	private String selectedDfu;
}
