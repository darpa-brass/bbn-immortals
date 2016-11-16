package mil.darpa.immortals.core.das;

import java.util.HashSet;
import java.util.Set;

public class Dataflow {

	public Dataflow() {
		dataTypes = new HashSet<String>();
	}
	
	public void addDataType(String dataType) {
		dataTypes.add(dataType);
	}
	
	public String getDataflowURI() {
		return dataflowURI;
	}

	public void setDataflowURI(String dataflowURI) {
		this.dataflowURI = dataflowURI;
	}
	
	public boolean containsDataType(String dataType) {
		return dataTypes.contains(dataType);
	}

	private String dataflowURI;
	private Set<String> dataTypes;
	
}
