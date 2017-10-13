package mil.darpa.immortals.core.das.sparql;

import org.apache.jena.query.ResultSet;

import mil.darpa.immortals.core.das.Dataflow;

public class DataflowQuery extends SparqlQuery {

	public static Dataflow select(String bootstrapUri, String dataflowGraphUri) {
		
		Dataflow result = new Dataflow();
		
		result.setDataflowURI(dataflowGraphUri);
		
		String query = 
				"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX cp: <http://darpa.mil/immortals/ontology/r2.0.0/cp#> " +
				"PREFIX IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#> " +
				"PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
				"PREFIX IMMoRTALS_cp2: <http://darpa.mil/immortals/ontology/r2.0.0/cp2#> " +
	
				"SELECT DISTINCT ?data " +
				"WHERE { " +
	    		"	 GRAPH <" + bootstrapUri + "> { " +
				"    	?p a IMMoRTALS_analysis:MethodInvocationDataflowNode . " +
				"    	?c a IMMoRTALS_analysis:MethodInvocationDataflowNode . " +
				"    	?e a IMMoRTALS_analysis:DataflowEdge . " +
				"    	?e IMMoRTALS:hasProducer ?p . " +
				"    	?e IMMoRTALS:hasDataTypeCommunicated ?data . " +
				"    	?e IMMoRTALS:hasConsumer ?c . " +
				"    	?graph IMMoRTALS:hasEdges ?e . " +
				"  		FILTER (?graph = " + dataflowGraphUri + ") " +
				"	} " +
				"}";

		ResultSet resultSet = getResultSet(query);
	    resultSet.forEachRemaining(t -> result.addDataType(getResource(t, "data")));

		return result;
		
	}

}
