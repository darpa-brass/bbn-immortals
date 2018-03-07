package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import mil.darpa.immortals.core.das.exceptions.InvalidOrMissingParametersException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class SQLTransformer {

	public SQLTransformer() {}
	
	public String getParameterDistributionSQL(String originalParameterizedSQL) throws Exception {
		
		String result = null;
		
		Statement stmt = CCJSqlParserUtil.parse(originalParameterizedSQL);

		if (stmt instanceof Select) {
			Select selectStatement = (Select) stmt;

			StringBuilder buffer = new StringBuilder();

			ParameterRewritingExpressionDeParser expressionDeParser = new ParameterRewritingExpressionDeParser();
			ParameterRewritingSelectDeParser selectDeParser = new ParameterRewritingSelectDeParser(expressionDeParser, buffer);
			
			expressionDeParser.setBuffer(buffer);
			expressionDeParser.setSelectVisitor(selectDeParser);
			
			selectStatement.getSelectBody().accept(selectDeParser);
			
			if (expressionDeParser.getParameterColumns() == null || expressionDeParser.getParameterColumns().isEmpty()) {
				throw new Exception("SQL statement is not parameterized.");
			} else {
				StringBuilder params = new StringBuilder();
				for (int paramIndex = 0; paramIndex<expressionDeParser.getParameterColumns().size(); paramIndex++) {
					if (params.length() > 0) {
						params.append(", ");
					}
					params.append("p__" + paramIndex);
				}
				
				result = "select " + params + " from (" + buffer.toString() + ") as __t1 group by " + params + " order by count(*) desc";
			}
		} else {
			throw new Exception("SQL statement is not a SELECT statement.");
		}
		
		return result;
	}
	
	public ResolvedQuery getBoundQuery(DataLinkageMetadata dataLinkageMetadata, Connection conn) throws Exception {
		
		if (dataLinkageMetadata == null || conn == null) {
			throw new InvalidOrMissingParametersException();
		}
		
		ResolvedQuery result = new ResolvedQuery();
		
		result.setOriginalQuery(dataLinkageMetadata.getOriginalSql());
		
		if (dataLinkageMetadata.getSqlMetadata().isParameterized()) {
			String paramDistributionSQL = null;
			CachedRowSet rowset = null;

			paramDistributionSQL = getParameterDistributionSQL(dataLinkageMetadata.getOriginalSql());

			try (ResultSet rs = conn.prepareStatement(paramDistributionSQL).executeQuery()) {
				rowset = RowSetProvider.newFactory().createCachedRowSet();
				rowset.populate(rs);
			}

			Parameter p = null;
			
			if (rowset.first()) {
				if (dataLinkageMetadata.getSqlMetadata().getParameters().size() == rowset.getMetaData().getColumnCount()) {
					String resolvedQuery = dataLinkageMetadata.getOriginalSql();
					for (int columnIndex = 1; columnIndex <= rowset.getMetaData().getColumnCount(); columnIndex++) {
						p = new Parameter();
						p.setColumnName(rowset.getMetaData().getColumnName(columnIndex));
						p.setType(rowset.getMetaData().getColumnType(columnIndex));
						p.setValue(rowset.getObject(columnIndex));
						result.getParameters().add(p);
						resolvedQuery = resolvedQuery.replaceFirst("\\?", Parameter.formatToString(p));
					}
					result.setResolvedQuery(resolvedQuery);
				} else {
					throw new Exception("Number of query parameters in parameterized query distribution does not match original query.");
				}
			} else {
				throw new Exception("Unable to resolve query parameter values.");
			}
		} else {
			result.setResolvedQuery(dataLinkageMetadata.getOriginalSql());
		}
		
		return result;
	}

	public String getComplementQuery(String originalQuery) throws Exception {
		
		if (originalQuery == null || originalQuery.trim().length() == 0) {
			throw new InvalidOrMissingParametersException();
		}
		
		String result = null;

		Statement stmt = CCJSqlParserUtil.parse(originalQuery);

		if (stmt instanceof Select) {
			Select selectStatement = (Select) stmt;

			ExpressionDeParser expressionVisitor = new ExpressionDeParser();
			StringBuilder buffer = new StringBuilder();

			SelectComplementDeParser complementDeParser = new SelectComplementDeParser(expressionVisitor, buffer);
			
			expressionVisitor.setBuffer(buffer);
			expressionVisitor.setSelectVisitor(complementDeParser);

			selectStatement.getSelectBody().accept(complementDeParser);
			
			result = complementDeParser.getBuffer().toString();
		} else {
			throw new Exception("SQL statement is not a SELECT statement.");
		}
			

		return result;
	}

	public String createTableForSQL(String sql, Connection conn) throws SQLException {
		
		if (sql == null || sql.trim().length() == 0) {
			throw new InvalidOrMissingParametersException();
		}

		String newTableName = "das.temp_" + UUID.randomUUID().toString().replaceAll("-", "");
		String createDdl = "create table " + newTableName + " as (" + sql + ")";
		
		try (java.sql.Statement stmt = conn.createStatement()) {
			stmt.execute(createDdl);
		}
		
		return newTableName;
	}
	
	public String getStableSampleSQL(DataLinkageMetadata dataLinkageMetadata, String sql, int sampleSize) {
		
		String result = null;
		
		//String ordinalSequence = IntStream.rangeClosed(1, dataLinkageMetadata.getSqlMetadata()
		//		.getProjectedIdentifiers().size())
		//		.mapToObj(n -> String.valueOf(n))
		//		.collect(Collectors.joining(","));
		
		result = "select * from (" + sql + ") as _t1 order by random() limit " + sampleSize;
		
		return result;
	}

}
