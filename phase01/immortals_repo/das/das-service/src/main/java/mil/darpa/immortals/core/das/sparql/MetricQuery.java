package mil.darpa.immortals.core.das.sparql;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import mil.darpa.immortals.core.das.AssertionCriterionValue;
import mil.darpa.immortals.core.das.Metric;

public class MetricQuery extends SparqlQuery {

	public static Metric select(String linkId, String deploymentGraphUri) {
		
		Metric result = null;
		
		String query =
				"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX cp: <http://darpa.mil/immortals/ontology/r2.0.0/cp#> " +

				"SELECT ?linkId ?value ?unit ?measurementTypeUri ?resourceType ?measurementType ?property ?assertionCriterion " +
				"WHERE { " + 
				"	GRAPH " + deploymentGraphUri + " {" +
				"  		?gme a cp:GmeInterchangeFormat . " +
				"   	?gme im:hasMissionSpec $missionSpec . " +
				"		?missionSpec im:hasRightValue ?metric . " +
				"		?missionSpec im:hasAssertionCriterion ?assertionCriterion ." +
				"		?metric im:hasApplicableResourceType ?resourceType . " +
				"		?metric im:hasUnit ?unit . " +
				"		?metric im:hasValue ?value . " +
				"		?metric im:hasMeasurementType ?measurementTypeUri . " +
				"		?metric im:hasLinkId ?linkId ." +
				"		?measurementTypeUri im:hasCorrespondingProperty ?property ." +
				"		?measurementTypeUri im:hasMeasurementType ?measurementType ." +
				"		FILTER (?linkId = '" + linkId + "') " +
				"	}" +
				"}";
				
        ResultSet resultSet = getResultSet(query);
        if (resultSet.hasNext()) {
        	QuerySolution t = resultSet.next();
        	result = new  mil.darpa.immortals.core.das.Metric(
        			getLiteral(t, "linkId"),
        			getLiteral(t, "value"),
        			getLiteral(t, "unit"),
        			getResource(t, "measurementTypeUri"),
        			"",
        			getResource(t, "resourceType"),
        			getLiteral(t, "measurementType"),
        			getResource(t, "property"),
        			AssertionCriterionValue.fromValue(getLiteral(t, "assertionCriterion"))
        	);
        }

        return result;
	}

}
