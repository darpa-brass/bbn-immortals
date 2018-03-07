package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ResultSet;

import mil.darpa.immortals.core.das.ApiSpecification;

public class LibraryUsagePattern extends SparqlQuery {

	public static ApiSpecification getApiSpec(String deploymentGraphUri, String libraryCoordinates, String functionalityAspect) {
		
		ApiSpecification specification = null;
		
		List<String> result = new ArrayList<String>();

		String query =
			"PREFIX res: <http://darpa.mil/immortals/ontology/r2.0.0/resources#> " +
			"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
			"PREFIX spec: <http://darpa.mil/immortals/ontology/r2.0.0/spec#>" +
			"PREFIX pattern: <http://darpa.mil/immortals/ontology/r2.0.0/pattern/spec#>" +

			"SELECT  ?api_spec ?spec_component ?usage_paradigm ?specification ?abstract_component ?ordering ?multiplicity ?abstract_component " +
			"WHERE {  " +
			"	GRAPH " + deploymentGraphUri + " {" +
			"  ?api_spec a pattern:LibraryFunctionalAspectSpec . " +
			"  ?api_spec im:hasComponent ?spec_component . " +
			"  ?api_spec im:hasLibraryCoordinateTag " + libraryCoordinates + "." +
			"  ?api_spec im:hasUsageParadigm ?usage_paradigm . " +
			"  ?api_spec im:hasAspect " + functionalityAspect + " . " +
			"  ?spec_component im:hasSpec ?specification . " +
			"  ?spec_component im:hasAbstractComponentLinkage ?abstract_component . " +
			"  ?abstract_component im:hasOrdering ?ordering . " +
			"  ?abstract_component im:hasMultiplicityOperator ?multiplicity . " +
			"  ?abstract_component im:hasDurableId ?abstract_component_durable_id . " +
			"	" +
			"} " +
			"order by asc(?ordering) ";
		
        ResultSet resultSet = getResultSet(query);
        
        resultSet.forEachRemaining(t -> result.add(getResource(t, "resource")));

		return specification;
		
	}

}
