package mil.darpa.immortals.core.das.sparql;

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
	
	protected static String getLiteral(QuerySolution qs, String nodeName) {
		return qs.getLiteral(nodeName).getString();
	}

	
	public static final String FUSEKI_DATA_ENDPOINT = "http://localhost:3030/ds/data/";
	public static final String FUSEKI_QUERY_ENDPOINT = "http://localhost:3030/ds/query";

}
