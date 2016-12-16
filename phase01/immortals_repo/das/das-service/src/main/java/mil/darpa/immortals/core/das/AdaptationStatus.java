package mil.darpa.immortals.core.das;

import java.util.ArrayList;
import java.util.List;

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
	
	public void addAudit(String audit) {
		audits.add(audit);
	}
	
	public String getSessionIdentifier() {
		return sessionIdentifier;
	}

	public void setSessionIdentifier(String sessionIdentifier) {
		this.sessionIdentifier = sessionIdentifier;
	}

	public List<String> getAudits() {
		return audits;
	}
	
	public String getAuditsAsString() {
		
		StringBuilder sb = new StringBuilder();
		
		for (String s : audits) {
			sb.append(s + System.lineSeparator());
		}
		
		return sb.toString();
	}

	private AdaptationStatusValue adaptationStatusValue;
	private String details;
	private String selectedDfu;
	private List<String> audits = new ArrayList<String>();
	private String sessionIdentifier;
}
