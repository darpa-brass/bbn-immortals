package com.securboration.immortals.service.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securboration.immortals.repo.api.RepositoryUnsafe;
import com.securboration.immortals.service.config.ImmortalsServiceProperties;

/**
 * A service for stringifying an individual in an ontology
 * 
 * @author jstaples
 *
 */
@RestController
@RequestMapping("/stringify")
public class StringificationService {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(StringificationService.class);
    
    @Autowired(required = true)
    private ImmortalsServiceProperties properties;
    
    @Autowired(required = true)
    private RepositoryUnsafe repository;
    
	/**
	 * Return a human-readable pure text visualization of an existing graph in Fuseki
	 * 
	 * @param graphName
	 *            the name of a graph managed by Fuseki.  E.g., 
	 *            http://localhost:3030/ds/data/IMMoRTALS_r2.0.0
	 * @param individualUri
	 *            the URI of an individual to print
	 * @return a textual representation of the triples about the indicated
	 *         individual in the indicated graph
	 */
    @RequestMapping(
            method = RequestMethod.GET,
            value="/individual",
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String getGraph(
            @RequestParam("graphName")
            String graphName,
            @RequestParam("individualUri")
            String individualUri
            ){
    	logger.info(
    			"received a request to print individual %s in %s\n", 
    			individualUri,
    			graphName
    			);
    	
    	return Printer.stringify(graphName,individualUri, repository);
    }
    
    
    
    private static class Printer {
    	
    	
    	private static final Set<String> avoidThesePredicates = new HashSet<>();static{
    		avoidThesePredicates.add("http://www.w3.org/2000/01/rdf-schema#subClassOf");
//    		avoidThesePredicates.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    	}
    	
    	private static final Set<String> dontRecurseIntoThesePredicates = new HashSet<>();static{
    	    dontRecurseIntoThesePredicates.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        }
    	
    	private static boolean shouldIgnore(final String predicate){
    		return avoidThesePredicates.contains(predicate);
    	}
    	
    	private static boolean shouldRecurse(final String predicate){
    	    return !dontRecurseIntoThesePredicates.contains(predicate);
    	}
    	
    	private static void process(
    			final String graphName,
    			RepositoryUnsafe repository,
    			GraphNode currentNode,
    			Map<String,GraphNode> visited,
    			Set<GraphNode> allNodes
    			){
    		allNodes.add(currentNode);
    		
    		
    		final String graphSpec;
    		if(graphName == null || graphName.isEmpty()){
    			graphSpec = "";
    		} else {
    			graphSpec = "GRAPH <" + graphName + ">";
    		}
    		
    		repository.executeSparqlQuery(
    				"SELECT ?p ?o \n"+ 
    				"WHERE { \n"+ 
    				"  " + graphSpec + " {\n"+ 
    				"    <"+currentNode.getLabel()+"> ?p ?o .\n"+
    				"  }\n"+
    				"}\n", 
    				(QuerySolution q)->{
    					
    					final RDFNode predicateNode = q.get("p");
    					final String predicate = predicateNode.asResource().getURI();
    					final RDFNode object = q.get("o");
    					
    					if(shouldIgnore(predicate)){
    						return;
    					}
    					
    					System.out.printf("\t%s %s %s\n", currentNode.getLabel(), predicate, object);
    					
    					
    					if(object.isLiteral()){
    						Edge edge = currentNode.createEdge(
    								predicate, 
    								predicateNode.as(Property.class),
    								new GraphNode(
    										object.toString(),
    										NodeType.LITERAL,
    										object
    										)
    								);
    						
    						allNodes.add(edge.getSink());
    					} else {
    						final String uri = object.asResource().getURI();
    						
    						GraphNode visitedNode = visited.get(uri);
    						
    						boolean recurse = false;
    						if(visitedNode == null){
    							visitedNode = new GraphNode(uri,NodeType.OBJECT,object);
    							
    							visited.put(uri, visitedNode);
    							recurse = true;
    						}
    						
    						if(recurse){
    						    recurse = shouldRecurse(predicate);
    						}//TODO
    						
    						currentNode.createEdge(
    								predicate,
    								predicateNode.as(Property.class), 
    								visitedNode
    								);
    						allNodes.add(visitedNode);
    						if(recurse){
    							process(graphName,repository,visitedNode,visited,allNodes);
    						}
    					}
    				}
    		);
    		
    	}
    	
    	private static GraphNode process(
    			final RepositoryUnsafe repository,
    			final String graphName,
    			final String individualUri,
    			Set<GraphNode> nodesInGraph
    			){
    		GraphNode start = 
    				new GraphNode(
    						individualUri,
    						NodeType.OBJECT,
    						null
    						);
    		
    		Map<String,GraphNode> map = new HashMap<>();
    		map.put(individualUri, start);
    		
    		process(
    				graphName,
    				repository,
    				start,
    				map,
    				nodesInGraph
    				);
    		
    		return start;
    	}
    	

    	private static String stringify(
    			final String graphName,
    			final String individualUri,
    			final RepositoryUnsafe repository
    			){
    		Set<GraphNode> allNodes = new LinkedHashSet<>();
    		
    		GraphNode root = 
    				process(
    					repository,
    					graphName,
    					individualUri,
    					allNodes
    				);
    		
    		return stringify(root);
    	}
    	
    	private static String stringify(GraphNode root){
    		StringBuilder sb = new StringBuilder();
    		
    		stringify(root,new HashSet<>(),sb);
    		
    		return sb.toString();
    	}
    	
    	private static void stringify(
    			GraphNode current,
    			Set<GraphNode> visited,
    			StringBuilder sb
    			){
    		
    		if(visited.contains(current)){
    			return;
    		}
    		visited.add(current);
    		
    		for(Edge e:current.getEdges()){
    			if(e.getName().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && e.getSink().getLabel().equals("http://www.w3.org/2000/01/rdf-schema#Class")){
    				return;
    			}
    		}
    		
    		final String S = NameHelper.shorten(current.getLabel());
    		
    		Set<GraphNode> visitThese = new LinkedHashSet<>();
    		
    		if(current.getEdges().size() == 0){
    		    return;
    		}
    		
    		sb.append(S);
    		sb.append("\n");
    		
    		Map<String,Set<String>> sorted = new TreeMap<>();
    		for(Edge e:current.getEdges()){
                final String P = NameHelper.shorten(e.getName());
                
                GraphNode target = e.getSink();
                
                final String O;
                if(target.getNodeType().equals(NodeType.LITERAL)){
                    O = target.getLabel();
                } else {
                    O = NameHelper.shorten(target.getRdfNode().asResource().getURI());
                    visitThese.add(target);
                }
                
                Set<String> objects = sorted.get(P);
                if(objects == null){
                    objects = new TreeSet<>();
                    sorted.put(P,objects);
                }
                
                objects.add(O);
            }
    		
    		for(String predicate:sorted.keySet()){
    		    for(String object:sorted.get(predicate)){
    		        sb.append(String.format("\t-- %s --> %s\n",predicate,object));
    		    }
    		}
    		
//    		for(Edge e:current.getEdges()){
//    			final String P = NameHelper.shorten(e.getName());
//    			
//    			GraphNode target = e.getSink();
//    			
//    			final String O;
//    			if(target.getNodeType().equals(NodeType.LITERAL)){
//    				O = target.getLabel();
//    			} else {
//    				O = NameHelper.shorten(target.getRdfNode().asResource().getURI());
//    				visitThese.add(target);
//    			}
//    			
//    			sb.append(String.format("\t-- %s --> %s\n",P,O));
//    			
//    		}
    		
    		sb.append("\n");
    		
    		//recurse
    		for(GraphNode g:visitThese){
    			stringify(g,visited,sb);
    		}
    	}
    	
    }
    
    private static class NameHelper {
    	public static String shorten(String name){
    		
    		name = name.replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "a");
    		
    		if(!name.startsWith("http://darpa.mil/immortals/ontology/")){
    			return name;
    		} else {
    			return name.substring(name.indexOf("#")+1);
    		}
    	}
    }
    
    private static enum NodeType{
		LITERAL,
		OBJECT
	}
    
    private static class GraphNode {
    	
    	private final String name;
    	private final NodeType nodeType;
    	private final RDFNode rdfNode;
    	
    	public RDFNode getRdfNode() {
    		return rdfNode;
    	}

    	public NodeType getNodeType() {
    		return nodeType;
    	}

    	public String getLabel() {
    		return name;
    	}

    	public GraphNode(String name,NodeType nodeType,RDFNode node) {
    		super();
    		this.name = name;
    		this.nodeType = nodeType;
    		this.rdfNode = node;
    	}

    	private final Set<Edge> edges = new LinkedHashSet<>();

    	public Set<Edge> getEdges() {
    		return edges;
    	}

    	public Edge createEdge(String name,Property property,GraphNode sink) {
    		Edge edge = new Edge(name,property,sink);
    		edges.add(edge);
    		
    		return edge;
    	}
    }
    
    private static class Edge {
    	
    	private final String name;
    	private final GraphNode sink;
    	private final Property property;
    	
    	public String getName() {
    		return name;
    	}

    	public GraphNode getSink() {
    		return sink;
    	}
    	
    	public Property getProperty() {
    		return property;
    	}

    	public Edge(String name,Property property,GraphNode sink) {
    		super();
    		this.name = name;
    		this.sink = sink;
    		this.property = property;
    	}

    }

}
