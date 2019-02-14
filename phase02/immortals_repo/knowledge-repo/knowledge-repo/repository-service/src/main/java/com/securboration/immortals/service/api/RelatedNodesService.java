package com.securboration.immortals.service.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
 * A service for returning the relationships to nodes in a graph
 * 
 * @author jstaples
 *
 */
@RestController
@RequestMapping("/relationships")
public class RelatedNodesService {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(RelatedNodesService.class);
    
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
            value="/getRelatedNodes",
            produces=MediaType.APPLICATION_JSON_VALUE
            )
    public Object getRelatedNodes(
            @RequestParam(value="graphName",required=false, defaultValue="")
            String graphName,
            @RequestParam(value="individualUri",required=false, defaultValue="")
            String individualUri
            ){
    	logger.info(
    			"received a request to print individual %s in %s\n", 
    			individualUri,
    			graphName
    			);
    	
    	if(isQuery(individualUri)){
    	    String query = individualUri;
    	    
//    	    if(graphName != null && !graphName.isEmpty()){
//    	        query = query.replace("${GRAPH}", graphName);
//    	    }
    	    
    	    System.out.println("query: " + individualUri);//TODO
    	    
    	    return getSparqlQueryResults(query);
    	} else if(isQuerySolution(individualUri)){
    	    final String querySolution = individualUri;
    	    
    	    return getQuerySolutionAsGraphNode(querySolution);
    	} else if(isQueryResult(individualUri)){
    	    final String queryResult = individualUri;
    	    
    	    return getQueryResultAsGraphNode(queryResult);
    	} else {
    	    return Printer.process(
                repository,
                graphName,
                individualUri,
                new HashSet<>()
            );
    	}
    }
    
    private static Collection<String> getQueryVariables(List<QuerySolution> solutions){
        Set<String> s = new HashSet<>();
        
        for(QuerySolution solution:solutions){
            Iterator<String> vars = solution.varNames();
            
            while(vars.hasNext()){
                s.add(vars.next());
            }
        }
        
        return s;
    }
    
    /**
     * 
     * @param solutions
     * @return null if 0 or more than 1 variables are found in the provided
     * solutions, the variable name if exactly 1 is found
     */
    private static String getQueryVariable(List<QuerySolution> solutions){
        Collection<String> vars = getQueryVariables(solutions);
        
        if(vars.size() == 0){
            return null;
        } else if(vars.size() > 1){
            return null;
        } else {
            return vars.iterator().next();
        }
    }
    
    private GraphNode getSparqlQueryResults(String query){
        List<QuerySolution> solutions = new ArrayList<>();
        
        repository.executeSparqlQuery(query, (solution)->{
            solutions.add(solution);
        });
        
        final String queryVar = getQueryVariable(solutions);
        
        if(queryVar == null){
            //it's a complex query or one with no solutions so return a view of
            //query hasSolution s1
            //query hasSolution s2
            //...
            
            final String name = convertToString(solutions);
            GraphNode g = new GraphNode(name,NodeType.QUERY_RESULT,null);
            
            int counter = 1;
            for(QuerySolution solution:solutions){
                final String property = "hasSolution";
                final String solutionUri = convertToString(solution,"solution"+counter);
                
                GraphNode o = new GraphNode(solutionUri,NodeType.QUERY_SOLUTION,null);
                
                g.createEdge(property, null, o);
                
                counter++;
            }
            
            return g;
        } else {
            //it's a simple query with at least one solution so return a view of
            //query has[varName] [solution 1]
            //...
            
            GraphNode g = new GraphNode("solution",NodeType.QUERY_SOLUTION,null);
            
            for(QuerySolution s:solutions){
                
                RDFNode n =  s.get(queryVar);
                
                if(n.isAnon()){
                    continue;
                } else if(n.isLiteral()){
                    GraphNode o = new GraphNode(n.toString(),NodeType.LITERAL,null);
                    g.createEdge(queryVar, null, o);
                } else if(n.isResource()){
                    GraphNode o = new GraphNode(n.toString(),NodeType.OBJECT,null);
                    g.createEdge(queryVar, null, o);
                }
            }
            
            return g;
            
        }
        
        
    }
    
    private static final String QS_SERIALIZE_NEWLINE_SIGIL = "QQQQQ";
    private static final String QR_SERIALIZE_NEWLINE_SIGIL = "XXXXX";
    private static final String QS_SERIALIZE_COLUMN_DELIM  = "JJJJJ";
    private static final String QR_SERIALIZE_COLUMN_DELIM  = "VVVVV";
    
    private static void newLineQs(StringBuilder sb){
        sb.append(QS_SERIALIZE_NEWLINE_SIGIL);
    }
    
    private static void newColQs(StringBuilder sb){
        sb.append(QS_SERIALIZE_COLUMN_DELIM);
    }
    
    private static void newLineQr(StringBuilder sb){
        sb.append(QR_SERIALIZE_NEWLINE_SIGIL);
    }
    
    private static void newColQr(StringBuilder sb){
        sb.append(QR_SERIALIZE_COLUMN_DELIM);
    }
    
    private GraphNode getQuerySolutionAsGraphNode(String s){
        
        System.out.println();
        System.out.println(s);
        System.out.println();//TODO
        
        if(!s.startsWith("@@querysolution@@")){
            throw new RuntimeException("expected a query var mapping but got " + s);
        }
        String[] parts = s.split(QS_SERIALIZE_NEWLINE_SIGIL);
        
        if(parts.length == 0){
            throw new RuntimeException("invalid string form for query solution: " + s);
        }
        
        String name = parts[1];
        
        GraphNode g = new GraphNode(name,NodeType.QUERY_SOLUTION,null);
        
        for(int i=2;i<parts.length;i++){
            final String line = parts[i].trim();
            
            if(line.isEmpty()){
                continue;
            }
            System.out.println(line);
            System.out.println();//TODO
            
            String[] columns = line.split(QS_SERIALIZE_COLUMN_DELIM);
            
            String predicate = columns[0];
            String object = columns[1];
            NodeType type = NodeType.valueOf(columns[2]);
            
            GraphNode o = new GraphNode(object,type,null);
            
            g.createEdge(predicate, null, o);
        }
        
        return g;
    }
    
    private GraphNode getQueryResultAsGraphNode(String s){
        
        if(!s.startsWith("@@queryresult@@")){
            throw new RuntimeException("expected a query result but got " + s);
        }
        String[] parts = s.split(QR_SERIALIZE_NEWLINE_SIGIL);
        
        if(parts.length == 0){
            throw new RuntimeException("invalid string form for query result: " + s);
        }
        
        String name = parts[1];
        
        GraphNode g = new GraphNode(name,NodeType.QUERY_RESULT,null);
        
        for(int i=2;i<parts.length;i++){
            System.out.println(parts[i]);//TODO
            System.out.println();//TODO
            if(parts[i].trim().isEmpty()){
                continue;
            }
            String[] columns = parts[i].split(QR_SERIALIZE_COLUMN_DELIM);
            
            String predicate = columns[0];
            String object = columns[1];
            NodeType type = NodeType.valueOf(columns[2]);
            
            GraphNode o = new GraphNode(object,type,null);
            
            g.createEdge(predicate, null, o);
        }
        
        return g;
    }
    
    private String convertToString(List<QuerySolution> solutions){
        StringBuilder sb = new StringBuilder();
        sb.append("@@queryresult@@");
        newLineQr(sb);
        sb.append("QueryResult");
        newLineQr(sb);
        
        int counter = 1;
        for(QuerySolution q:solutions){
            String stringForm = convertToString(q,"query"+counter);
            counter++;
            
            sb.append("hasSolution");
            newColQr(sb);
            sb.append(stringForm);
            newColQr(sb);
            sb.append(NodeType.QUERY_SOLUTION.name());
            newLineQr(sb);
        }
        
        return sb.toString();
    }
    
    private String convertToString(QuerySolution solution,String name){
        StringBuilder sb = new StringBuilder();
        sb.append("@@querysolution@@");
        newLineQs(sb);
        sb.append(name);
        newLineQs(sb);
        
        Iterator<String> vars = solution.varNames();
        
        while(vars.hasNext()){
            String var = vars.next();
            
            RDFNode node = solution.get(var);
            
            if(node.isAnon()){
                continue;//ignore anonymous nodes
            }
            
            sb.append(var);
            newColQs(sb);
            sb.append(node.toString());
            newColQs(sb);
            if(node.isLiteral()){
                sb.append(NodeType.LITERAL.name());
            } else {
                sb.append(NodeType.OBJECT.name());
            }
            newLineQs(sb);
        }
        
        System.out.println(sb.toString());//TODO
        
        return sb.toString();
    }
    
    
    @RequestMapping(
            method = RequestMethod.GET,
            value="/query",
            produces=MediaType.APPLICATION_JSON_VALUE
            )
    public Object executeQuery(
            @RequestParam("query")
            String query
            ){
        logger.info(
            "received a request to execute query %s\n", 
            query
            );
        
        StringBuilder sb = new StringBuilder();
        AtomicInteger solutionCounter = new AtomicInteger(1);
        
        repository.executeSparqlQuery(query, (solution)->{
            
            sb.append("solution").append(solutionCounter.getAndIncrement()).append(":\n");
            Iterator<String> vars = solution.varNames();
            
            while(vars.hasNext()){
                String var = vars.next();
                
                RDFNode node = solution.get(var);
                
                sb.append("  ?").append(var).append(" = ").append(node.toString());
                sb.append("\n");
            }
            sb.append("\n");
            
        });
        
        //TODO
        
        return sb.toString();
    }
    
    private static boolean isQueryResult(String s){
        if(s.startsWith("@@queryresult@@")){
            return true;
        }
        
        return false;
    }
    
    private static boolean isQuerySolution(String s){
        if(s.startsWith("@@querysolution@@")){
            return true;
        }
        
        return false;
    }
    
    private static boolean isQuery(String s){
        if(s.toUpperCase().contains("PREFIX ")){
            return true;
        }
        
        if(s.toUpperCase().contains("SELECT ")){
            return true;
        }
        
        if(s.toUpperCase().contains("WHERE ")){
            return true;
        }
        
        return false;
    }
    
    
    
    private static class Printer {
    	
    	private static void process(
    			final String graphName,
    			RepositoryUnsafe repository,
    			GraphNode currentNode
    			){
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
    				"}\n"+
    				"ORDER BY ?p\n", 
    				(QuerySolution q)->{
    					
    					final RDFNode predicateNode = q.get("p");
    					final String predicate = predicateNode.asResource().getURI();
    					final RDFNode object = q.get("o");
    					
    					System.out.printf("\t%s %s %s\n", currentNode.getLabel(), predicate, object);
    					
    					
    					if(object.isLiteral()){
    						currentNode.createEdge(
    								predicate, 
    								predicateNode.as(Property.class),
    								new GraphNode(
    										object.toString(),
    										NodeType.LITERAL,
    										object
    										)
    								);
    					} else {
    						final String uri = object.asResource().getURI();
    						
    						currentNode.createEdge(
    								predicate,
    								predicateNode.as(Property.class), 
    								new GraphNode(uri,NodeType.OBJECT,object)
    								);
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
    		
    		process(
    				graphName,
    				repository,
    				start
    				);
    		
    		return start;
    	}
    	
    }
    
    private static enum NodeType{
		LITERAL,
		OBJECT,
		
		QUERY_RESULT,
		QUERY_SOLUTION
	}
    
    private static class GraphNode {
    	
    	private final String name;
    	private final NodeType nodeType;
    	
    	
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
    	
    	public String getName() {
    		return name;
    	}

    	public GraphNode getSink() {
    		return sink;
    	}

    	public Edge(String name,Property property,GraphNode sink) {
    		super();
    		this.name = name;
    		this.sink = sink;
    	}

    }

}
