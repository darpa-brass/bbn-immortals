package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ResultSet;

import mil.darpa.immortals.core.das.ControlPoint;
import mil.darpa.immortals.core.das.FunctionalitySpecification;

public class ControlPoints extends SparqlQuery {

	public static List<ControlPoint> select(String bootstrapUri, FunctionalitySpecification functionalitySpecification) {
		
		List<ControlPoint> results = new ArrayList<ControlPoint>();
		
		String query = 
			"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
			"PREFIX cp: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/cp#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
			"SELECT ?cp_uri ?uuid ?class_name ?class_url ?dataflowEntryPoint " +
			"WHERE { " +
        	"GRAPH <" + bootstrapUri + "> { " +
			"	 ?cp_uri  a cp:ControlPoint . " +
			"    ?cp_uri im:hasControlPointUuid ?uuid. " +
			"    ?cp_uri im:hasOwnerClass ?class . " +
			"    ?class im:hasClassName ?class_name . " +
			"    ?class im:hasClassUrl ?class_url . " + 
			"	 ?cp_uri im:hasFunctionalityPerformed ?functionalityUri . " +
			"	 ?cp_uri im:dataflowEntryPoint ?dataflowEntryPoint . " +	
			"  } " +
			"FILTER (?functionalityUri = " + functionalitySpecification.getFunctionalityUri() + ") " +
			"} ";
		
        ResultSet resultSet = getResultSet(query);
        
        resultSet.forEachRemaining(t -> results.add(
        		new ControlPoint(getResource(t, "cp_uri"), 
        						getLiteral(t, "uuid"),
        						getLiteral(t, "class_name"),
        						getLiteral(t, "class_url"),
        						getResource(t, "dataflowEntryPoint"))));

        return results;
	}

}
