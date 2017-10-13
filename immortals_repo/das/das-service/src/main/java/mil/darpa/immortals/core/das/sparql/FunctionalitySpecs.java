package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import mil.darpa.immortals.core.das.FunctionalitySpecification;

public class FunctionalitySpecs extends SparqlQuery {

	public static List<FunctionalitySpecification> select(String deploymentGraphUri) {
		
		List<FunctionalitySpecification> result = new ArrayList<FunctionalitySpecification>();

		String query =
			"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  " +
			"PREFIX cp: <http://darpa.mil/immortals/ontology/r2.0.0/cp#> " +

			"SELECT DISTINCT (?fs AS ?functionalitySpec) " +
			"(?fp AS ?functionalityPerformed) " +
			"(group_concat(distinct ?pt;separator=',') AS ?propertyRollup) " +
			"WHERE { " +
			"	GRAPH " + deploymentGraphUri + " {" +
			"  		?gme a cp:GmeInterchangeFormat . " +
			"   	?gme im:hasFunctionalitySpec $fs . " +
			"   	?fs im:hasFunctionalityPerformed ?fp .  " +
			"optional{?fs im:hasPropertyConstraint ?pc . " +
			"  		?pc im:hasConstrainedProperty ?p . " +
			"		?p a ?pt . } " +
			"	}" +
			"}" +
			"GROUP BY ?fs ?fp";

		ResultSet resultSet = getResultSet(query);
        QuerySolution qs = null;
        FunctionalitySpecification fs = null;

        while (resultSet.hasNext()) {
        	qs = resultSet.next();
        	fs = new FunctionalitySpecification(getResource(qs, "functionalitySpec"),
        			getResource(qs, "functionalityPerformed"));
        	
        	if (qs.getLiteral("propertyRollup") != null) {
	        	String propertyRollup = getLiteral(qs, "propertyRollup");
	        	String[] propertiesSplit = propertyRollup.split(",");
	        	for (int x = 0; x < propertiesSplit.length; x++) {
	        		fs.addPropertyUri("<" + propertiesSplit[x] + ">");
	        	}
        	}
        	
        	result.add(fs);
        }
        
		return result;
	}

}
