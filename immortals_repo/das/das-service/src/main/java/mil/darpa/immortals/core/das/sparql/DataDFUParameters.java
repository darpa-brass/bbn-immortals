package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ResultSet;
import mil.darpa.immortals.core.das.knowledgebuilders.schemadependency.Parameter;

public class DataDFUParameters extends SparqlQuery {

	public static List<Parameter> select(String bootstrapUri, String dataLinkageUri) {
		
		List<Parameter> results = new ArrayList<>();
		
        String query = 
				"PREFIX im: <http://darpa.mil/immortals/ontology/2.0-LOCAL#> " +
				"PREFIX kb: <http://darpa.mil/immortals/ontology/2.0-LOCAL/KnowledgeBuilders#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +

				"SELECT ?parameterUri ?parameterValue ?sqlType ?ordinalPosition ?columnName " +
				"WHERE { " +
				"   GRAPH <" + bootstrapUri + "> { " +
						dataLinkageUri + " a kb:DataLinkage . " +
						dataLinkageUri + " kb:hasParameter ?parameterUri . " +
				"		?parameterUri kb:hasOrdinalPosition ?ordinalPosition ." +
				"		?parameterUri kb:hasParameterValue ?parameterValue . " +
				"		?parameterUri kb:hasSQLType ?sqlType . " +
				"		?parameterUri kb:hasColumnName ?columnName . " +
				"	} " +
				"} order by ?ordinalPosition";
		
        ResultSet resultSet = getResultSet(query);

        if (resultSet.hasNext()) {
	        resultSet.forEachRemaining(t -> results.add(new Parameter(
	        		getLiteral(t, "columnName"),
	        		getLiteralAsInt(t, "sqlType"),
	        		getLiteralAsObject(t, "parameterValue"),
	        		getLiteralAsInt(t, "ordinalPosition"),
	        		getResource(t, "parameterUri"))));
        }

        return results;
	}
}
