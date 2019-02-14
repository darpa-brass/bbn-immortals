package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import java.util.ArrayList;
import java.util.List;

public class ResolvedQuery {

	public ResolvedQuery() {}
	
	public String getOriginalQuery() {
		return originalQuery;
	}

	public void setOriginalQuery(String originalQuery) {
		this.originalQuery = originalQuery;
	}

	public String getResolvedQuery() {
		return resolvedQuery;
	}

	public void setResolvedQuery(String resolvedQuery) {
		this.resolvedQuery = resolvedQuery;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	private String originalQuery;
	private String resolvedQuery;
	private List<Parameter> parameters = new ArrayList<>();

}
