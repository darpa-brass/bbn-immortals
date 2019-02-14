package mil.darpa.immortals.core.das.sparql;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class PropertyImpact extends SparqlQuery {
	
	public static Map<String, String> select(String bootstrapUri, String actionToTake, String onProperty) {
		
		Map<String, String> results = null;
		
		String query = "PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#>  " +
				"PREFIX dfu: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#>  " +
				"PREFIX lp_func: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#>  " +
				"PREFIX bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +

				"SELECT DISTINCT ?strategy ?actionToTake ?onProperty ?invokedAspect ?applicableDataType " +
				"  WHERE { " +
	        	"		GRAPH <" + bootstrapUri + "> { " +
				"      		?strategy a <http://darpa.mil/immortals/ontology/r2.0.0/property/impact#PredictiveCauseEffectAssertion> . " +
	        	"			OPTIONAL { ?strategy im:hasApplicableDataType ?applicableDataType . } " +
				"      		?strategy <http://darpa.mil/immortals/ontology/r2.0.0#hasImpact> ?impact . " +
				"      		?impact <http://darpa.mil/immortals/ontology/r2.0.0#hasImpactOnProperty> '" + actionToTake + "' . " +
				"      		?impact <http://darpa.mil/immortals/ontology/r2.0.0#hasImpactOnProperty> ?impactOnProperty . " +
				"      		?impact <http://darpa.mil/immortals/ontology/r2.0.0#hasImpactedProperty> " + onProperty + " . " +
				"      		?impact <http://darpa.mil/immortals/ontology/r2.0.0#hasImpactedProperty> ?impactedProperty . " +
				"      		?strategy <http://darpa.mil/immortals/ontology/r2.0.0#hasCriterion> ?criterion . " +
				"      		?criterion <http://darpa.mil/immortals/ontology/r2.0.0#hasCriterion> ?actionToTake . " +
				"      		OPTIONAL { ?criterion <http://darpa.mil/immortals/ontology/r2.0.0#hasProperty> ?onProperty . } " +
				"      		OPTIONAL { ?criterion <http://darpa.mil/immortals/ontology/r2.0.0#hasInvokedAspect> ?invokedAspect . } " +
				"		} " +
				"  }";
		
		
		ResultSet resultSet = getResultSet(query);
		
		if (resultSet.hasNext()) {
			results = new HashMap<String, String>();
			QuerySolution qs = resultSet.next();
			if (getLiteral(qs, "actionToTake") != null) {
				results.put("actionToTake", getLiteral(qs, "actionToTake"));
			}
			
			if (getResource(qs, "onProperty") != null) {
				results.put("onProperty", getResource(qs, "onProperty"));
			}
			
			if (getResource(qs, "invokedAspect") != null) {
				results.put("invokedAspect", getResource(qs, "invokedAspect"));
				
				if (getResource(qs, "applicableDataType") != null) {
					results.put("applicableDataType", getResource(qs, "applicableDataType"));
				}
				
				if (getResource(qs, "strategy") != null) {
					results.put("strategy", getResource(qs, "strategy"));
				}				
			}
		}
		
        return results;
	}
 

}
