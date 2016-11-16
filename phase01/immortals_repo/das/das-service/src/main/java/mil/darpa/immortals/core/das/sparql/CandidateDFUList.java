package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.query.ResultSet;

import mil.darpa.immortals.core.das.DFU;
import mil.darpa.immortals.core.das.DependencyCoordinate;

public class CandidateDFUList extends SparqlQuery {

	public static List<DFU> select(String bootstrapUri, String functionalityUri, List<String> resourceUris, List<String> propertyUris) {
		
		List<DFU> result = new ArrayList<DFU>();
		String resources = resourceUris.stream().collect(Collectors.joining(","));

		String query =
				"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
				"PREFIX dfu: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> " +
				"PREFIX lp_func: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#> " +
				"PREFIX bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +

				"SELECT DISTINCT ?dfu ?className ?groupId ?artifactId ?version ?functionalityUri " +
				"WHERE { " +
	    		"	  	GRAPH <" + bootstrapUri + "> { " +
				"			?dfu a dfu:DfuInstance . " +
				"			?dfu im:hasFunctionalityAbstraction ?functionalityUri . " +
				"			?dfu im:hasClassPointer ?classPointer . " +
				"			?class a bytecode:AClass . " +
				"			?class im:hasBytecodePointer ?classPointer . " +
				"			?class im:hasClassName ?className . " +
				"			?jar a bytecode:JarArtifact . " +
				"			?jar im:hasJarContents ?content . " +
				"			?content a bytecode:ClassArtifact . " +
				"			?content im:hasClassModel ?class . " +
				"			?jar im:hasCoordinate ?coordinate . " +
				"			?coordinate im:hasVersion ?version . " +
				"			?coordinate im:hasArtifactId ?artifactId . " +
				"			?coordinate im:hasGroupId ?groupId . " +
				"			filter (?functionalityUri = " + functionalityUri + ") ";

				for (String property : propertyUris) {
					query = query.concat(
							"?dfu im:hasDfuProperties ?propertyInstance . " +
							"?propertyInstance a " + property + ". ");
				}

			    query = query.concat(
			    		"MINUS { " +
			    		"SELECT DISTINCT ?dfu " +
			    		"WHERE { " +
			    		"GRAPH <" + bootstrapUri + "> { " +
			            "?dfu a dfu:DfuInstance . " +
			            "?dfu im:hasFunctionalityAbstraction ?functionalityUri . " +
			            "?dfu im:hasResourceDependencies ?resource . " +
			            "FILTER(?resource NOT IN ( " + resources +
			            "  ) " +
			            ") " +
			          "} " +
			        "} " +
			      "}");
	
			query = query.concat("}}");

        ResultSet resultSet = getResultSet(query);
        
        resultSet.forEachRemaining(t -> result.add(
        		new DFU(getResource(t, "dfu"), 
        				getLiteral(t, "className"),
        				new DependencyCoordinate(getLiteral(t, "groupId"),
        					getLiteral(t, "artifactId"),
        					getLiteral(t, "version")),
        					getResource(t, "functionalityUri"))));
        
        return result;
	}

	public static List<DFU> selectByFunctionalAspect(String bootstrapUri, String functionalAspectUri, List<String> resourceUris, List<String> propertyUris) {
		
		List<DFU> result = new ArrayList<DFU>();
		String resources = resourceUris.stream().collect(Collectors.joining(","));

		String query =
				"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
				"PREFIX dfu: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> " +
				"PREFIX lp_func: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#> " +
				"PREFIX bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +

				"SELECT DISTINCT ?dfu ?className ?groupId ?artifactId ?version ?functionalityUri " +
				"WHERE { " +
	    		"	  	GRAPH <" + bootstrapUri + "> { " +
				"			?dfu a dfu:DfuInstance . " +
				"			?dfu im:hasFunctionalityAbstraction ?functionalityUri . " +
				"			?functionalityUri im:hasFunctionalAspects ?functionalAspectUri . " +
				"			?dfu im:hasClassPointer ?classPointer . " +
				"			?class a bytecode:AClass . " +
				"			?class im:hasBytecodePointer ?classPointer . " +
				"			?class im:hasClassName ?className . " +
				"			?jar a bytecode:JarArtifact . " +
				"			?jar im:hasJarContents ?content . " +
				"			?content a bytecode:ClassArtifact . " +
				"			?content im:hasClassModel ?class . " +
				"			?jar im:hasCoordinate ?coordinate . " +
				"			?coordinate im:hasVersion ?version . " +
				"			?coordinate im:hasArtifactId ?artifactId . " +
				"			?coordinate im:hasGroupId ?groupId . " +
				"			filter (?functionalAspectUri = " + functionalAspectUri + ") ";

				for (String property : propertyUris) {
					query = query.concat(
							"?dfu im:hasDfuProperties ?propertyInstance . " +
							"?propertyInstance a " + property + ". ");
				}

			    query = query.concat(
			    		"MINUS { " +
			    		"SELECT DISTINCT ?dfu " +
			    		"WHERE { " +
			    		"GRAPH <" + bootstrapUri + "> { " +
			            "?dfu a dfu:DfuInstance . " +
			            "?dfu im:hasFunctionalityAbstraction ?functionalityUri . " +
			            "?functionalityUri im:hasFunctionalAspects ?functionalAspectUri . " +
			            "?dfu im:hasResourceDependencies ?resource . " +
			            "FILTER(?resource NOT IN ( " + resources +
			            "  ) " +
			            ") " +
			          "} " +
			        "} " +
			      "}");
	
			query = query.concat("}}");

        ResultSet resultSet = getResultSet(query);
        
        resultSet.forEachRemaining(t -> result.add(
        		new DFU(getResource(t, "dfu"), 
        				getLiteral(t, "className"),
        				new DependencyCoordinate(getLiteral(t, "groupId"),
        					getLiteral(t, "artifactId"),
        					getLiteral(t, "version")),
        					getResource(t, "functionalityUri"))));
        
        return result;
	}

}
