package mil.darpa.immortals.core.das;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdaptationStatus {

	static final Logger logger = LoggerFactory.getLogger(AdaptationStatus.class);

	public AdaptationStatus() {

		try {
			lp = new LoggerProxy();
		} catch (Exception e) {
			logger.error("Unable to initialize logger proxy:" + e.getMessage());
			if (lp != null) {
				lp.close();
			}
		}
		
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
		if (lp != null) {
			lp.sendLogEntry(audit);
		}
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
	
	public void close() {
		if (lp != null) {
			lp.close();
		}
	}

	private AdaptationStatusValue adaptationStatusValue;
	private String details;
	private String selectedDfu;
	private List<String> audits = new ArrayList<String>();
	private String sessionIdentifier;
	private LoggerProxy lp;
}
