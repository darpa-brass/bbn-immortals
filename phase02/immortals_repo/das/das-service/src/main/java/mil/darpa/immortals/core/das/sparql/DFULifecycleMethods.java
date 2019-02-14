package mil.darpa.immortals.core.das.sparql;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.ResultSet;

public class DFULifecycleMethods extends SparqlQuery {

	public static Map<String, String> select(String bootstrapUri, String dfuUri) {

		HashMap<String, String> result = new HashMap<String, String>();

		String query = 
			"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
			"PREFIX dfu: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#>" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +

			"SELECT DISTINCT ?dfu ?abstractAspect ?methodName " +
			"WHERE { " +
    		"	  	GRAPH <" + bootstrapUri + "> { " +
			"			?dfu a dfu:DfuInstance . " +
			"  			?dfu im:hasFunctionalAspects ?functionalAspectInstance . " +
			"  			?functionalAspectInstance im:hasAbstractAspect ?abstractAspect . " +
			"  			?functionalAspectInstance im:hasMethodPointer ?methodPointer . " +
			"  			?method im:hasBytecodePointer ?methodPointer . " +
			"  			?method im:hasMethodName ?methodName . " +
			"			} " +
			"FILTER (?dfu = " + dfuUri + ")" + 
			"}";
        
        ResultSet resultSet = getResultSet(query);
        resultSet.forEachRemaining(t -> result.put(getResource(t, "abstractAspect"), 
        				getLiteral(t, "methodName")));
		
		return result;
	}

}
