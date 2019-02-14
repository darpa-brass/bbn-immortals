package mil.darpa.immortals.core.das.adaptationmodules.schemaevolution;

import java.util.List;

import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.DataLinkageMetadata;

public class LearnedQuery {

	public String getLearnedSql() {
		return learnedSql;
	}
	
	public void setLearnedSql(String learnedSql) {
		this.learnedSql = learnedSql;
	}
	
	public List<Integer> getParameterOrder() {
		return parameterOrder;
	}

	public void setParameterOrder(List<Integer> parameterOrder) {
		this.parameterOrder = parameterOrder;
	}

	public DataLinkageMetadata getDataLinkageMetadata() {
		return dataLinkageMetadata;
	}

	public void setDataLinkageMetadata(DataLinkageMetadata dataLinkageMetadata) {
		this.dataLinkageMetadata = dataLinkageMetadata;
	}

	private String learnedSql;
	private List<Integer> parameterOrder;
	private DataLinkageMetadata dataLinkageMetadata;
}
