package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.List;
import org.apache.jena.query.ResultSet;

import mil.darpa.immortals.core.das.AssertionCriterionValue;
import mil.darpa.immortals.core.das.Metric;

public class MissionMetrics extends SparqlQuery {

	public static List<Metric> select(String deploymentGraphUri) {
		
		List<Metric> results = new ArrayList<Metric>();
		
		String query =
				"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX cp: <http://darpa.mil/immortals/ontology/r2.0.0/cp#> " +

				"SELECT DISTINCT ?linkId ?value ?unit ?measurementTypeUri ?resourceType ?measurementType ?property ?assertionCriterion " +
				"WHERE { " + 
				"	GRAPH " + deploymentGraphUri + " {" +
				"  		?gme a cp:GmeInterchangeFormat . " +
				"  		?gme im:hasMissionSpec $missionSpec . " +
				"		?missionSpec im:hasRightValue ?metric . " +
				"		?missionSpec im:hasAssertionCriterion ?assertionCriterion ." +
				"		?metric im:hasApplicableResourceType ?resourceType . " +
				"		?metric im:hasUnit ?unit . " +
				"		?metric im:hasValue ?value . " +
				"		?metric im:hasMeasurementType ?measurementTypeUri . " +
				"		?metric im:hasLinkId ?linkId ." +
				"		?measurementTypeUri im:hasCorrespondingProperty ?property ." +
				"		?measurementTypeUri im:hasMeasurementType ?measurementType ." +
				"	}" +
				"}";
				
        ResultSet resultSet = getResultSet(query);
        if (resultSet.hasNext()) {
	        resultSet.forEachRemaining(t -> results.add(new Metric(
	        	getLiteral(t, "linkId"),
        		getLiteral(t, "value"),
	        	getLiteral(t, "unit"),
	        	getResource(t, "measurementTypeUri"),
	        	"",
	        	getResource(t, "resourceType"),
	        	getLiteral(t, "measurementType"),
	        	getResource(t, "property"),
	        	AssertionCriterionValue.fromValue(t.getLiteral("assertionCriterion").toString()))));
        }

        return results;
	}

}
