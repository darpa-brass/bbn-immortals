package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ResultSet;

public class SchemaDFUDependencies extends SparqlQuery {

	public static List<String> select(String bootstrapUri, String schemaUri, String schemaVersion) {
		
		List<String> results = new ArrayList<String>();
		
		String query = 
				"prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
				"prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#> " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> " +
				"prefix IMMoRTALS_dfu_instance: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> " +
				 
				"select ?className where { " +
				"    ?AClass a IMMoRTALS_bytecode:AClass " +
				"    ; IMMoRTALS:hasClassName ?className " +
				"    ; IMMoRTALS:hasBytecodePointer ?classPointer . " +

				"    ?DfuInstance a IMMoRTALS_dfu_instance:DfuInstance " +
				"    ; IMMoRTALS:hasClassPointer ?classPointer " +
				"    ; IMMoRTALS:hasConcreteResourceDependencies " + schemaUri + " . " +
				 
				"    <???SCHEMA_URI???> IMMoRTALS:hasVersion " + schemaVersion + " . " +
				"}";
		
        ResultSet resultSet = getResultSet(query);
        
        resultSet.forEachRemaining(t -> results.add(getResource(t, "className")));

        return results;
	}

}
