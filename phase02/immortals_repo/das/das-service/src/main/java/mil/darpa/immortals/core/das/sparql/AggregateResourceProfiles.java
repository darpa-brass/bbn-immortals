package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ResultSet;

import mil.darpa.immortals.core.das.ResourceProfile;

public class AggregateResourceProfiles extends SparqlQuery {

	public static List<ResourceProfile> select(String bootstrapUri, String controlPointUri) {
		
		List<ResourceProfile> results = new ArrayList<ResourceProfile>();
		
		String query = 
			"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
			"PREFIX cp: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/cp#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
			"SELECT ?controlPointUri ?resourceProfileUri ?targetResourceTypeUri ?formula ?constrainingMetricLinkID ?unit " +
			"WHERE { " +
        	"GRAPH <" + bootstrapUri + "> { " +
			"	 ?controlPointUri  a cp:ControlPoint . " +
			"    ?controlPointUri im:hasAggregateResourceProfile ?resourceProfileUri . " +
			"    ?resourceProfileUri im:hasApplicableResourceType ?targetResourceTypeUri . " +
			"    ?resourceProfileUri im:formula ?formula . " +
			"	 ?resourceProfileUri im:constrainingMetricLinkID ?constrainingMetricLinkID ." +
			"	 ?resourceProfileUri im:hasUnit ?unit ." + 
			"  } " +
			"FILTER (?controlPointUri = " + controlPointUri + ") " +
			"} ";
		
		ResultSet rs = getResultSet(query);
		
        rs.forEachRemaining(t -> results.add(new ResourceProfile( 
        		getResource(t, "resourceProfileUri"),
        		getResource(t, "targetResourceTypeUri"),
        		getLiteral(t, "formula"),
        		getLiteral(t, "constrainingMetricLinkID"),
        		getLiteral(t, "unit"))));
        
        return results;
	}

}
