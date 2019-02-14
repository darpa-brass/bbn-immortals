package mil.darpa.immortals.core.das.sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class ImageScalingCodeMetrics extends SparqlQuery {

	public static List<Map<String, String>> select(String bootstrapUri, String className, String dependentVariableUri, double inputMegapixels) {
		
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();;
		
		String query = 
			"PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> " +
			"PREFIX m: <http://darpa.mil/immortals/ontology/r2.0.0/measurement#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX cp: <http://darpa.mil/immortals/ontology/r2.0.0/measurement/cp1cp2#> " +
			"PREFIX imageScaling: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/imagescaling#> " +
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +

			"SELECT DISTINCT ?inputMegapixels ?outputMegapixels ?scalingFactor " +
			"WHERE { " +
			"	GRAPH <" + bootstrapUri + "> { " +
			"  		?metricSet a m:MetricSet. " +
			"  		?metricSet im:hasDependentVariable ?dependentVariable . " +
			" 		?metricSet im:hasProfiles ?profile . " +
			"  		?profile im:hasCodeUnit ?dfuPointer . " +
			" 		?dfuPointer im:hasClassName ?className . " +

			"   	?profile im:hasMeasuredProperty ?inputMegapixelsProperty . " +
			"  		?inputMegapixelsProperty a cp:InputImageSizeMegapixels . " +
			"  		?inputMegapixelsProperty im:hasNumMegapixels ?inputMegapixels. " +

			"    	?profile im:hasMeasuredProperty ?outputMegapixelsProperty . " +
			"  		?outputMegapixelsProperty a cp:OutputImageSizeMegapixels . " +
			"		?outputMegapixelsProperty im:hasNumMegapixels ?outputMegapixels. " +
			 
			"  		?profile im:hasMeasuredProperty ?imageScalingFactorProperty . " +
			" 		?imageScalingFactorProperty a imageScaling:ImageScalingFactor . " +
			"		?imageScalingFactorProperty im:hasScalingFactor ?scalingFactor. " +
			  
			"  		filter (?className = '" + className + "' " +
			"  		 	&& ?inputMegapixels = " + inputMegapixels + ") " +
			"  	} " +
			"} " +
			"ORDER BY DESC(?scalingFactor)";

			
		ResultSet rs = getResultSet(query);
		
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			
			Map<String, String> metric = new HashMap<String, String>();
			metric.put("inputMegapixels", getLiteral(qs, "inputMegapixels"));
			metric.put("outputMegapixels", getLiteral(qs, "outputMegapixels"));
			metric.put("scalingFactor", getLiteral(qs, "scalingFactor"));
			results.add(metric);
		}
        
        return results;
	}

}
