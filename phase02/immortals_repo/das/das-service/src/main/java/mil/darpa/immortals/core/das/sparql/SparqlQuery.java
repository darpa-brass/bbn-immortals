package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;

public abstract class SparqlQuery {

	protected static ResultSet getResultSet(String query) {
		
		ResultSet resultSet = null;
		
        try (QueryExecution qe = QueryExecutionFactory.sparqlService(FUSEKI_QUERY_ENDPOINT, query)) {
        	resultSet = ResultSetFactory.copyResults(qe.execSelect()) ;
        }
        
        return resultSet;
	}
	
	protected static String getResource(QuerySolution qs, String nodeName) {
		
		String result = null;
		
		if (qs.getResource(nodeName) != null) {
			result = "<" + qs.getResource(nodeName).getURI() + ">";
		}
		
		return result;
	}
	
	protected static List<String> getLiteralAsList(QuerySolution qs, String nodeName) {
		
		List<String> result = null;
		
		String value = qs.getLiteral(nodeName).getString();
		
		if (value != null && value.trim().length() > 0) {
			result = Arrays.asList(value.split(","));
		} else {
			result = new ArrayList<>();
		}
		
		return result;
	}
	
	protected static String getLiteral(QuerySolution qs, String nodeName) {
		return qs.getLiteral(nodeName).getString();
	}
	
	protected static Object getLiteralAsObject(QuerySolution qs, String nodeName) {
		return qs.getLiteral(nodeName).getValue();
	}

	protected static boolean getLiteralAsBoolean(QuerySolution qs, String nodeName) {
		return qs.getLiteral(nodeName).getBoolean();
	}
	
	protected static int getLiteralAsInt(QuerySolution qs, String nodeName) {
		return qs.getLiteral(nodeName).getInt();
	}
	
	public static final String FUSEKI_DATA_ENDPOINT = "http://localhost:3030/ds/data/";
	public static final String FUSEKI_QUERY_ENDPOINT = "http://localhost:3030/ds/query";

}
