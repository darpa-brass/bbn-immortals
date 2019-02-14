package mil.darpa.immortals.core.das.sparql;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import mil.darpa.immortals.core.das.DFU;
import mil.darpa.immortals.core.das.DependencyCoordinate;

public class DFUByClassName extends SparqlQuery {

	public static DFU select(String bootstrapUri, String className) {
		
		DFU result = null;
		
		String query =
			"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
			"PREFIX dfu: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> " +
			"PREFIX lp_func: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#> " +
			"PREFIX bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +

			"SELECT DISTINCT ?dfu ?className ?groupId ?artifactId ?version ?functionalityUri " +
			"WHERE { " +
    		"	GRAPH <" + bootstrapUri + "> { " +
			"		?dfu a dfu:DfuInstance . " +
			"		?dfu im:hasFunctionalityAbstraction ?functionalityUri . " +
			"		?dfu im:hasClassPointer ?classPointer . " +
			"		?class a bytecode:AClass . " +
			"		?class im:hasBytecodePointer ?classPointer . " +
			"		?class im:hasClassName ?className . " +
			"		?jar a bytecode:JarArtifact . " +
			"		?jar im:hasJarContents ?content . " +
			"		?content a bytecode:ClassArtifact . " +
			"		?content im:hasClassModel ?class . " +
			"		?jar im:hasCoordinate ?coordinate . " +
			"		?coordinate im:hasVersion ?version . " +
			"		?coordinate im:hasArtifactId ?artifactId . " +
			"		?coordinate im:hasGroupId ?groupId . " +
			"		FILTER (?className = '" + className + "') " +
			"	} " +
			"}";

        ResultSet resultSet = getResultSet(query);
        
        if (resultSet.hasNext()) {
        	QuerySolution qs = resultSet.next();
        	result = new DFU(getResource(qs, "dfu"), 
        		getLiteral(qs, "className"),
        		new DependencyCoordinate(getLiteral(qs, "groupId"),
        			getLiteral(qs, "artifactId"),
        			getLiteral(qs, "version")),
        			getResource(qs, "functionalityUri"));
        }

		
		return result;
	}

}
