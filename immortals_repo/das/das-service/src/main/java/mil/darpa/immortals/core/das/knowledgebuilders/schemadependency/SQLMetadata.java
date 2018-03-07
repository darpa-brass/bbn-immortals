package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import java.util.ArrayList;
import java.util.List;

public class SQLMetadata {

	public SQLMetadata() {}
	
	public boolean isDisjunctiveFilter() {
		return disjunctiveFilter;
	}

	public void setDisjunctiveFilter(boolean disjunctiveFilter) {
		this.disjunctiveFilter = disjunctiveFilter;
	}

	public boolean isParameterized() {
		return !this.parameters.isEmpty();
	}
	
	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public List<String> getProjectedIdentifiers() {
		return projectedIdentifiers;
	}

	public void setProjectedIdentifiers(List<String> projectedIdentifiers) {
		this.projectedIdentifiers = projectedIdentifiers;
	}

	private boolean disjunctiveFilter;
	private List<Parameter> parameters = new ArrayList<Parameter>();
	private List<String> projectedIdentifiers = new ArrayList<String>();
}
