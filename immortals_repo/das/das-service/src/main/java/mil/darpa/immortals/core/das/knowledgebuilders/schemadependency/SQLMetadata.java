package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Select;

public class SQLMetadata {

	public SQLMetadata() {}
	
	public static SQLMetadata buildSqlMetadata(String sql) throws JSQLParserException {
		
		SQLMetadata metadata = new SQLMetadata();

		CCJSqlParserManager pm = new CCJSqlParserManager();
		
		net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));
		
		GenericSQLVisitor visitor = null;
		
		if (statement instanceof Select) {
			Select selectStatement = (Select) statement;
			visitor = new GenericSQLVisitor();

			selectStatement.getSelectBody().accept(visitor);
		}
		
		metadata.setDisjunctiveFilter(visitor.isDisjunctiveFilter());
		metadata.setParameters(visitor.getParameters());
		metadata.setProjectedIdentifiers(visitor.getProjection());
		metadata.setLiteralReferencesInFilter(visitor.getLiteralsInFilter());
		
		return metadata;
	}

	
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
	
	public List<LiteralReference> getLiteralReferencesInFilter() {
		return literalReferencesInFilter;
	}

	public void setLiteralReferencesInFilter(List<LiteralReference> literalReferencesInFilter) {
		this.literalReferencesInFilter = literalReferencesInFilter;
	}

	private boolean disjunctiveFilter;
	private List<Parameter> parameters = new ArrayList<Parameter>();
	private List<String> projectedIdentifiers = new ArrayList<String>();
	private List<LiteralReference> literalReferencesInFilter = new ArrayList<>();
}
