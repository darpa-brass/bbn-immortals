package com.securboration.depedencyquerier;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * Hello world!
 *
 */
public class HashQuerier 
{
	public static String fusekiDestination = "http://localhost:3030/ds/sparql";
	public static String prefixes = "PREFIX im: <http://darpa.mil/immortals/ontology/r2.0.0#> "+ 
			"PREFIX dfu: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> "+
			"PREFIX lp_func: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/locationprovider#> "+ 
			"PREFIX bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#> "+ 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+ 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+ 
			"PREFIX owl: <http://www.w3.org/2002/07/owl#> ";
	
    public static void main( String[] args )
    {
    	if (args.length == 0){
    		System.out.println("no hash provided");
    	}
    	else{
    		HashMap<String,String> solutionMap = new HashMap<String,String>();
    		for (String s : args){
    			String x = HashQuery(s);
    			if (x != null){
        			solutionMap.put(s, x);
    			}
    		}
    	}
    	//ClaytonTest("A0Kt1VWjN00gf3SEVfDNuAZU3dOIhDORHgUFKtOO6zI=");
    }
    
	public static String HashQuery(String hash){
		String queryString = queryStringBuilder(hash);
		Query q = QueryFactory.create(queryString);
		QueryEngineHTTP qe = QueryExecutionFactory.createServiceRequest(fusekiDestination, q);
		ResultSet st = qe.execSelect();
		ArrayList<RDFNode> graph = new ArrayList<RDFNode>();
		while(st.hasNext()){
			QuerySolution QS = st.next();
			graph.add(QS.get("g"));
		}
		
		if (graph.size() != 1){
			return null;
		}
		else {
			return graph.get(0).toString();
		}
	}
	
	public static String queryStringBuilder(String hash){
		String q = prefixes + "SELECT ?g ?s ?p WHERE { GRAPH ?g {?s a bytecode:AClass . ?s im:hasBytecodePointer '"+ hash +"' .} }";
		System.out.println(q);
		return q;
	}
}
