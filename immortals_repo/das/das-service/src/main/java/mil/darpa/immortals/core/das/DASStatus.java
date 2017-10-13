package mil.darpa.immortals.core.das;

import java.io.Serializable;

public class DASStatus implements Serializable {

	public DASStatus() {}
	
	public DASStatus(DASStatusValue dasStatusValue) {
		this.setStatus(dasStatusValue);
	}
	
	public DASStatus(String description, DASStatusValue dasStatusValue) {
		this.description = description;
		this.setStatus(dasStatusValue);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DASStatusValue getStatus() {
		return status;
	}

	public void setStatus(DASStatusValue status) {
		this.status = status;
	}
	
	private static final long serialVersionUID = -8275989059342033765L;
	private String description;
	private DASStatusValue status;
}
