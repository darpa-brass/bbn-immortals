package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ResultSet;

public class AbstractResources extends SparqlQuery {

	public static List<String> select(String deploymentGraphUri) {
		
		List<String> results = new ArrayList<String>();
		
		String query =
			"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX cp: <http://darpa.mil/immortals/ontology/r2.0.0/cp#> " +

			"SELECT ?resourceClass " +
			"WHERE { " +
			"	GRAPH " + deploymentGraphUri + " {" +
			"  		{ " +
			"   	?gme a cp:GmeInterchangeFormat . " +
			"  		?gme im:hasAvailableResources ?resource . " +
			"   	?resource a ?resourceClass . " +
			"  		} " +
			"  	UNION " +
			"   	{ " +
			"   	?gme a cp:GmeInterchangeFormat . " +
			"   	?gme im:hasAvailableResources ?resource . " +
			"   	?resource im:hasPlatformResources ?platformResource . " +
			"   	?platformResource a ?resourceClass . " +
			"   	} " +
			"	}" +
			"}";
		
		ResultSet rs = getResultSet(query);
		
        rs.forEachRemaining(t -> results.add(getResource(t, "resourceClass")));

        return results;

	}
}
