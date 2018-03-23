package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.apache.commons.collections4.iterators.PermutationIterator;
import org.apache.commons.math3.util.Combinations;

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
	
	public String getStableSampleSQL(String sql, int sampleSize) {
		
		String result = null;
		
		//String ordinalSequence = IntStream.rangeClosed(1, dataLinkageMetadata.getSqlMetadata()
		//		.getProjectedIdentifiers().size())
		//		.mapToObj(n -> String.valueOf(n))
		//		.collect(Collectors.joining(","));
		
		result = "select * from (" + sql + ") as _t1 order by random() limit " + sampleSize;
		
		return result;
	}
	
	public String parameterizeSql(String sql, int parameterMask) throws Exception {
		
		String result = null;
		
		Statement stmt = CCJSqlParserUtil.parse(sql);

		if (stmt instanceof Select) {
			Select selectStatement = (Select) stmt;

			StringBuilder buffer = new StringBuilder();

			ParameterizingExpressionDeParser expressionDeParser = new ParameterizingExpressionDeParser();
			expressionDeParser.setParameterMask(parameterMask);
			ParameterizingSelectDeParser selectDeParser = new ParameterizingSelectDeParser(expressionDeParser, buffer);
			
			expressionDeParser.setBuffer(buffer);
			expressionDeParser.setSelectVisitor(selectDeParser);
			
			selectStatement.getSelectBody().accept(selectDeParser);
			
			result = buffer.toString();
		} else {
			throw new Exception("SQL statement is not a SELECT statement.");
		}
		
		return result;
	}

	public List<String> generateParameterizedCombinations(int numberParameters,
			String sql, int numberLiterals) throws Exception {
		
		if (numberLiterals < numberParameters) {
			throw new Exception("Number of literals must be greater than or equal to number of parameters.");
		}
		
		List<String> results = new ArrayList<>();
		
		Combinations c = new Combinations(numberLiterals, numberParameters);
		SQLTransformer st = new SQLTransformer();
		
		Iterator<int[]> i = c.iterator();
		while (i.hasNext()) {
			int[] selectedLiterals = i.next();
			int pmask = 0;
			for (int x = 0; x < selectedLiterals.length; x++) {
				pmask = pmask | (1 << selectedLiterals[x]);
			}
			results.add(st.parameterizeSql(sql, pmask));
		}
		
		return results;
	}
	
	public Set<List<Integer>> generatePermutations(int n) {
		
		Set<List<Integer>> results = new HashSet<>();
		
		List<Integer> elements = IntStream.rangeClosed(1, n)
					.mapToObj(i -> Integer.valueOf(i))
					.collect(Collectors.toList());

		PermutationIterator<Integer> pelements = new PermutationIterator<>(elements);
		
		pelements.forEachRemaining(t -> results.add(t));
		
		return results;
	}
	
	public String resolveParameters(DataLinkageMetadata originalQuery, List<Integer> order) 
			throws Exception {
		
		if (originalQuery == null || order == null) {
			throw new InvalidOrMissingParametersException();
		}
		
		if (originalQuery.getSqlMetadata().getParameters().size() != order.size()) {
			throw new Exception("Number of query parameters does not match size of permutation.");
		}
		
		String result = null;
		
		String sql = originalQuery.getOriginalSql();
		
		for (Integer i : order) {
			sql = sql.replaceFirst("\\?", 
					Parameter.formatToString(originalQuery.getSqlMetadata().getParameters().get(i.intValue())));
		}
		
		result = sql;
		
		return result;
		
	}
	
	public String resolveParameters(String perturbedSql, DataLinkageMetadata originalQuery, 
			List<Integer> parameterOrder) throws Exception {
		
		if (originalQuery == null || parameterOrder == null || perturbedSql == null || 
				perturbedSql.trim().length() == 0) {
			throw new InvalidOrMissingParametersException();
		}
		
		if (originalQuery.getSqlMetadata().getParameters().size() != parameterOrder.size()) {
			throw new Exception("Number of query parameters does not match size of permutation.");
		}
		
		String result = null;
		
		String sql = perturbedSql;
		
		for (Integer i : parameterOrder) {
			sql = sql.replaceFirst("\\?", 
					Parameter.formatToString(originalQuery.getSqlMetadata().getParameters().get(i.intValue()-1)));
		}
		
		result = sql;
		
		return result;

	}
	
	@SuppressWarnings("unused")
	private String replaceOccurrence(String stringToSearch, String stringToSearchFor, int occurrence, String replacement) {

		String result = null;
		int position = -1;
		
		int x = 0;
		for (; x <= occurrence; x++) {
			position = stringToSearch.indexOf(stringToSearchFor, position+1);
			if (position == -1) {
				break;
			}
		}
		
		if (x == occurrence + 1 && position > -1) {
			StringBuilder sb = new StringBuilder(stringToSearch);
			result = sb.replace(position, position+1, replacement).toString();
		}
		
		return result;
	}

}
