package mil.darpa.immortals.core.das.sparql;

import org.apache.jena.query.ResultSet;

import mil.darpa.immortals.core.das.CodeMetricMetaData;
import mil.darpa.immortals.core.das.CodeMetricMetaData.Variable;
import mil.darpa.immortals.core.das.CodeMetricMetaData.VariableTypeValue;

public class CodeMetricMetaDataQuery extends SparqlQuery {

	public static CodeMetricMetaData select(String bootstrapUri, String className) {
		
		CodeMetricMetaData md = new CodeMetricMetaData();
		
		String query = 
			"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
			"PREFIX m: <http://darpa.mil/immortals/ontology/r2.0.0/measurement#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX cp: <http://darpa.mil/immortals/ontology/r2.0.0/measurement/cp1cp2#> " +
			"PREFIX imageScaling: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/imagescaling#> " +
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +

			"SELECT DISTINCT ?uri ?type " +
        	"GRAPH <" + bootstrapUri + "> { " +
			"	WHERE { " +
			"  		{ " +
			"  		?metricSet a m:MetricSet . " +
			"   	?metricSet im:hasProfiles ?profile . " +
			"  		?profile im:hasCodeUnit ?dfuPointer . " +
			"  		?dfuPointer im:hasClassName ?className . " +
			"   	?metricSet im:hasIndependentVariable ?uri . " +
			"		BIND(STR('INDEPENDENT_VARIABLE') as ?type) " +
			"  		} " +
			"  		UNION " +
			"		{ " +
			"    	?metricSet a m:MetricSet. " +
			"    	?metricSet im:hasProfiles ?profile . " +
			"    	?profile im:hasCodeUnit ?dfuPointer . " +
			"  		?dfuPointer im:hasClassName ?className . " +
			"   	?metricSet im:hasDependentVariable ?uri . " +
			"		BIND(STR('DEPENDENT_VARIABLE') as ?type) " +
			"  		} " +
			"  		UNION " +
			"		{ " +
			"    	?metricSet a m:MetricSet. " +
			"    	?metricSet im:hasProfiles ?profile . " +
			"    	?profile im:hasCodeUnit ?dfuPointer . " +
			"  		?dfuPointer im:hasClassName ?className . " +
			"    	?metricSet im:configurationVariable ?uri . " +
			"		BIND(STR('CONFIGURATION_VARIABLE') as ?type) " +
			"  		} " +
			"		filter (?className = 'com.bbn.immortals.ImageScalingClass') " +
			"	} " + 
			"}";

		ResultSet rs = getResultSet(query);
		
        rs.forEachRemaining(t -> 
        		md.addVariable(
        			new Variable(getResource(t, "uri"), VariableTypeValue.fromValue(getLiteral(t, "type")))
        		)
        	);
        
        return md;
	}

}
