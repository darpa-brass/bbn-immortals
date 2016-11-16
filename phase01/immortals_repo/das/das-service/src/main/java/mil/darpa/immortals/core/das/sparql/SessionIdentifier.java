package mil.darpa.immortals.core.das.sparql;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class SessionIdentifier extends SparqlQuery {

	public static String select(String deploymentGraphUri) {
		
		String result = null;
		
		String query =
				"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  " +
				"PREFIX cp: <http://darpa.mil/immortals/ontology/r2.0.0/cp#> " +

				"SELECT ?sessionIdentifier " +
				"WHERE { " +
				"	GRAPH " + deploymentGraphUri + " {" +
				"  		?gme a cp:GmeInterchangeFormat . " +
				"   	?gme im:hasSessionIdentifier ?sessionIdentifier . " +
				"	}" +
				"}";
		
        ResultSet resultSet = getResultSet(query);
        if (resultSet.hasNext()) {
        	QuerySolution qs = resultSet.next();
        	result = getLiteral(qs, "sessionIdentifier");
        }
        
        return result;
	}

}
