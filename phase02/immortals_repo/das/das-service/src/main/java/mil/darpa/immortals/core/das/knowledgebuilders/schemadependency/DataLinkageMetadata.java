package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import java.util.List;

public class DataLinkageMetadata {

	public DataLinkageMetadata() {}
	
	public DataLinkageMetadata(String uri, String className, boolean containsDisjunctiveFilter,
			int sqlLineNumberStart, int sqlLineNumberEnd, String sqlVariableName, String originalSql,
			String positiveTrainingDataTableName, String negativeTrainingDataTableName, String validationDataTableName,
			List<String> projection) {
		
		this.uri = uri;
		this.className = className;
		this.getSqlMetadata().setDisjunctiveFilter(containsDisjunctiveFilter);
		this.getSqlMetadata().setProjectedIdentifiers(projection);
		this.sqlLineNumberStart = sqlLineNumberStart;
		this.sqlLineNumberEnd = sqlLineNumberEnd;
		this.sqlVariableName = sqlVariableName;
		this.originalSql = originalSql;
		this.positiveTrainingDataTableName = positiveTrainingDataTableName;
		this.negativeTrainingDataTableName = negativeTrainingDataTableName;
		this.validationDataTableName = validationDataTableName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getSqlVariableName() {
		return sqlVariableName;
	}

	public void setSqlVariableName(String sqlVariableName) {
		this.sqlVariableName = sqlVariableName;
	}
	
	public int getSqlLineNumberStart() {
		return sqlLineNumberStart;
	}

	public void setSqlLineNumberStart(int sqlLineNumberStart) {
		this.sqlLineNumberStart = sqlLineNumberStart;
	}

	public int getSqlLineNumberEnd() {
		return sqlLineNumberEnd;
	}

	public void setSqlLineNumberEnd(int sqlLineNumberEnd) {
		this.sqlLineNumberEnd = sqlLineNumberEnd;
	}

	public SQLMetadata getSqlMetadata() {
		return sqlMetadata;
	}

	public void setSqlMetadata(SQLMetadata sqlMetadata) {
		this.sqlMetadata = sqlMetadata;
	}

	public String getOriginalSql() {
		return originalSql;
	}

	public void setOriginalSql(String originalSql) {
		this.originalSql = originalSql;
	}

	public String getResolvedQuery() {
		
		String result = this.getOriginalSql();
		
		if (this.getSqlMetadata().isParameterized()) {
			for (Parameter p : this.getSqlMetadata().getParameters()) {
				result = result.replaceFirst("\\?", Parameter.formatToString(p));
			}
		}
		
		return result;
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getPositiveTrainingDataTableName() {
		return positiveTrainingDataTableName;
	}

	public void setPositiveTrainingDataTableName(String positiveTrainingDataTableName) {
		this.positiveTrainingDataTableName = positiveTrainingDataTableName;
	}

	public String getNegativeTrainingDataTableName() {
		return negativeTrainingDataTableName;
	}

	public void setNegativeTrainingDataTableName(String negativeTrainingDataTableName) {
		this.negativeTrainingDataTableName = negativeTrainingDataTableName;
	}
	
	public String getValidationDataTableName() {
		return validationDataTableName;
	}

	public void setValidationDataTableName(String validationDataTableName) {
		this.validationDataTableName = validationDataTableName;
	}

	private int sqlLineNumberStart = -1;
	private int sqlLineNumberEnd = -1;
	private String sqlVariableName = null;
	private String originalSql = null;
	private SQLMetadata sqlMetadata = new SQLMetadata();
	private String className = null;
	private String uri = null;
	private String positiveTrainingDataTableName = null;
	private String negativeTrainingDataTableName = null;
	private String validationDataTableName = null;

}
