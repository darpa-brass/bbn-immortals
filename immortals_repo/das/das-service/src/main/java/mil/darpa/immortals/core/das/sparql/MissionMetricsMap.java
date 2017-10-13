package mil.darpa.immortals.core.das.sparql;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.ResultSet;

import mil.darpa.immortals.core.das.AssertionCriterionValue;
import mil.darpa.immortals.core.das.Metric;

public class MissionMetricsMap extends SparqlQuery {

	public static Map<String, Metric> select(String deploymentGraphUri) {
		
		Map<String, Metric> results = new HashMap<String, Metric>();

		String eol = System.lineSeparator();
		String query =
				"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " + eol +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " + eol +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + eol +
				"PREFIX cp: <http://darpa.mil/immortals/ontology/r2.0.0/cp#> " + eol +

				"SELECT DISTINCT ?linkId ?value ?unit ?measurementTypeUri ?resourceType ?measurementType ?property ?assertionCriterion " + eol +
				"WHERE { " + eol +
				"	GRAPH " + deploymentGraphUri + " {" + eol +
				"  		?gme a cp:GmeInterchangeFormat . " + eol +
				"  		?gme im:hasMissionSpec $missionSpec . " + eol +
				"		?missionSpec im:hasRightValue ?metric . " + eol +
				"		?missionSpec im:hasAssertionCriterion ?assertionCriterion ." + eol +
				"		?metric im:hasApplicableResourceType ?resourceType . " + eol +
				"		?metric im:hasUnit ?unit . " + eol +
				"		?metric im:hasValue ?value . " + eol +
				"		?metric im:hasMeasurementType ?measurementTypeUri . " + eol +
				"		?metric im:hasLinkId ?linkId ." + eol +
				"		?measurementTypeUri im:hasCorrespondingProperty ?property ." + eol +
				"		?measurementTypeUri im:hasMeasurementType ?measurementType ." + eol +
				"	}" +
				"}";
				
        ResultSet resultSet = getResultSet(query);
        if (resultSet.hasNext()) {
	        resultSet.forEachRemaining(t -> results.put(getLiteral(t, "linkId"), 
	        		new Metric(getLiteral(t, "linkId"),
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
