package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ResultSet;

public class ConfigurationAspects extends SparqlQuery {

	public static List<String> select(String bootstrapUri, String strategyUri) {
		
		List<String> results = new ArrayList<String>();
		
		String query = 
				"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#>  " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +

				"SELECT DISTINCT ?configurationAspectUri " +
				"  WHERE { " +
	        	"		GRAPH <" + bootstrapUri + "> { " +
				"      		?strategy a <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#InvocationCriterion> . " +
				"      		?strategy im:hasConfigurationAspect ?configurationAspectUri . " +
				"		} " +
				"  }";
		
        ResultSet resultSet = getResultSet(query);
        
        resultSet.forEachRemaining(t -> results.add(getResource(t, "configurationAspectUri")));

        return results;
	}

}
