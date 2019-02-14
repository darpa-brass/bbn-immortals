package mil.darpa.immortals.core.das.sparql;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class SchemaMigrationTarget extends SparqlQuery {

	public static String select(String deploymentGraphUri) {
		
		String result = null;

		String query = 
				"PREFIX im_res_log: <http://darpa.mil/immortals/ontology/r2.0.0/resources/logical#> " +
				"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
				"PREFIX gmei: <http://darpa.mil/immortals/ontology/r2.0.0/gmei#> " +
				"PREFIX owl:   <http://www.w3.org/2002/07/owl#> " +
				"PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX IMMoRTALS_resources: <http://darpa.mil/immortals/ontology/r2.0.0/resources#> " +
				"PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>  " +
				"PREFIX IMMoRTALS_cp2: <http://darpa.mil/immortals/ontology/r2.0.0/cp2#>  " +
				"PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX IMMoRTALS_resource_containment: <http://darpa.mil/immortals/ontology/r2.0.0/resource/containment#>  " +

				"SELECT ?migrationTarget " +
				"WHERE { " +
				"	GRAPH <" + deploymentGraphUri + "> {" +
				"	 	?sub a gmei:DeploymentModel . " +
				"    	?sub im:hasResourceMigrationTargets ?migrationTarget . " +
				"    	?migrationTarget im:hasTargetResource ?resource . " +
				"    	?resource a im_res_log:DBSchema . " +
				"	}" +
				"}";
		
        ResultSet resultSet = getResultSet(query);
        if (resultSet.hasNext()) {
        	QuerySolution qs = resultSet.next();
        	result = getResource(qs, "migrationTarget");
        }
        
        return result;
	}

}
