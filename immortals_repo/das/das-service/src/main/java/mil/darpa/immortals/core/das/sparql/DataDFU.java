package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ResultSet;
import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.DataLinkageMetadata;
import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.Parameter;

public class DataDFU extends SparqlQuery {

	public static List<DataLinkageMetadata> select(String bootstrapUri) {
		
		List<DataLinkageMetadata> results = new ArrayList<>();
		
		String query = 
			"PREFIX im: <http://darpa.mil/immortals/ontology/2.0-LOCAL#> " +
			"PREFIX kb: <http://darpa.mil/immortals/ontology/2.0-LOCAL/KnowledgeBuilders#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +

			"SELECT ?dataLinkage ?className ?containsDisjunctiveFilter " +
				"?startLineNumber ?endLineNumber ?sqlVariableName ?sql ?trainingDataTableName "
				+ "?negativeDataTableName ?projection " +
			"WHERE { " +
			"   GRAPH <" + bootstrapUri + "> { " +
			"	 	?dataLinkage a kb:DataLinkage . " +
			"    	?dataLinkage im:hasClassName ?className . " +
			"    	?dataLinkage kb:containDisjunctiveFilter ?containsDisjunctiveFilter . " +
			"    	?dataLinkage kb:startLineNumber ?startLineNumber . " +
			"    	?dataLinkage kb:endLineNumber ?endLineNumber . " +
			"		?dataLinkage kb:hasSQLVariableName ?sqlVariableName . " +
			"		?dataLinkage kb:hasSQL ?sql . " +
			"		?dataLinkage kb:trainingDataTable ?trainingDataTableName . " +
			"		?dataLinkage kb:negativeDataTable ?negativeDataTableName . " +
			"		?dataLinkage kb:hasProjection ?projection . " +
			"	} " +
			"}";
		
        ResultSet resultSet = getResultSet(query);

        if (resultSet.hasNext()) {
	        resultSet.forEachRemaining(t -> results.add(new DataLinkageMetadata(
	        	getResource(t, "dataLinkage"),
        		getLiteral(t, "className"),
	        	getLiteralAsBoolean(t, "containsDisjunctiveFilter"),
	        	getLiteralAsInt(t, "startLineNumber"),
	        	getLiteralAsInt(t, "endLineNumber"),
	        	getLiteral(t, "sqlVariableName"),
	        	getLiteral(t, "sql"),
	        	getLiteral(t, "trainingDataTableName"),
	        	getLiteral(t, "negativeDataTableName"),
	        	getLiteralAsList(t, "projection"))));
        }
        
        for (DataLinkageMetadata dl : results) {
        	List<Parameter> parameters = DataDFUParameters.select(bootstrapUri, dl.getUri());
        	dl.getSqlMetadata().setParameters(parameters);
        }
        
        return results;
	}
	

}
