package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ResultSet;

public class Resources extends SparqlQuery {

	public static List<String> getResources(String deploymentGraphUri) {
		
		List<String> result = new ArrayList<String>();

		String query =
			"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX cp: <http://darpa.mil/immortals/ontology/r2.0.0/cp#> " +

			"SELECT DISTINCT ?resource " +
			"WHERE { " + 
			"	graph " + deploymentGraphUri + " {" +
			"  		?gme a cp:GmeInterchangeFormat . " +
			"   	?gme im:hasAvailableResources $resource . " +
			"	}" +
			"}";

        ResultSet resultSet = getResultSet(query);
        resultSet.forEachRemaining(t -> result.add(getResource(t, "resource")));

		return result;
		
	}

}
