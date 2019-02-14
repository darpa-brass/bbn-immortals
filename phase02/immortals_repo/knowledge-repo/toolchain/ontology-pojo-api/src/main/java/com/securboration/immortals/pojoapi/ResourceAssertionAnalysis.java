package com.securboration.immortals.pojoapi;

import com.securboration.immortals.ontology.domains.resources.MethodResourceInstanceDependencyAssertion;
import com.securboration.immortals.ontology.domains.resources.MethodResourceTypeDependencyAssertion;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class ResourceAssertionAnalysis {
    
    public Set<MethodResourceTypeDependencyAssertion> expandTypeDependencyDomain(Model m, MethodResourceTypeDependencyAssertion origin) {
        String queryString;

        Set<MethodResourceTypeDependencyAssertion> assertions = new HashSet<>();
        assertions.add(origin);
        Stack<MethodResourceTypeDependencyAssertion> methodStack = new Stack<>();
        methodStack.push(origin);

        boolean endOfGraph = false;
        
        while (!endOfGraph || !methodStack.isEmpty()) {
            MethodResourceTypeDependencyAssertion assertion = methodStack.pop();
            queryString = typeDependencyAssertionQuery.replace("???METHODNAME???" , assertion.getMethodName())
                    .replace("???METHODDESC???", assertion.getMethodDesc())
                    .replace("???METHODOWNERNAME???", assertion.getMethodOwner());
            Query query = QueryFactory.create(queryString);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, m)){
                ResultSet results = qexec.execSelect();
                if (!results.hasNext()) {
                    endOfGraph = true;
                }
                while(results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    System.out.println("Found called method with descriptors: " + soln.get("calledMethodName").toString() + ", " +
                            soln.get("calledMethodDesc").toString() +", " + soln.get("calledMethodOwner").toString());
                    assertion = new MethodResourceTypeDependencyAssertion();
                    assertion.setMethodName(soln.get("calledMethodName").toString());
                    assertion.setMethodDesc(soln.get("calledMethodDesc").toString());
                    assertion.setMethodOwner(soln.get("calledMethodOwner").toString());
                    assertion.setResourceUtilized(origin.getResourceUtilized());
                    assertion.setOriginAssertion(origin);
                    if (assertions.add(assertion)) {
                        methodStack.push(assertion);
                    }
                    endOfGraph = false;
                }
            }
        }
        return assertions;
    }


    public Set<MethodResourceInstanceDependencyAssertion> expandInstanceDependencyDomain(Model m, MethodResourceInstanceDependencyAssertion origin) {
        String queryString;

        Set<MethodResourceInstanceDependencyAssertion> assertions = new HashSet<>();
        assertions.add(origin);
        Stack<MethodResourceInstanceDependencyAssertion> methodStack = new Stack<>();
        methodStack.push(origin);

        boolean endOfGraph = false;

        while (!endOfGraph || !methodStack.isEmpty()) {
            MethodResourceInstanceDependencyAssertion assertion = methodStack.pop();
            queryString = instanceDependencyAssertionQuery.replace("???METHODPOINTER???", assertion.getConsumerPointer());
            Query query = QueryFactory.create(queryString);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, m)){
                ResultSet results = qexec.execSelect();
                if (!results.hasNext()) {
                    endOfGraph = true;
                }
                while(results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    System.out.println("Found called method with pointer: " + soln.get("calledMethodPointer"));
                           
                    assertion = new MethodResourceInstanceDependencyAssertion();
                    assertion.setConsumerPointer(soln.get("calledMethodPointer").toString());
                    assertion.setOriginAssertion(origin);
                    assertion.setResourceConsumed(origin.getResourceConsumed());
                    if (assertions.add(assertion)) {
                        methodStack.push(assertion);
                    }
                    endOfGraph = false;
                }
            }
        }
        return assertions;
    }
    
    public static final String instanceDependencyAssertionQuery = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
            "            select distinct ?calledMethodPointer where {{{\n" +
            "             ?callgraph a IMMoRTALS:StaticCallGraphEdge \n" +
            "\t\t\t ; IMMoRTALS:hasCalledHash \"???METHODPOINTER???\"\n" +
            "\t\t\t ; IMMoRTALS:hasCallerHash ?calledMethodPointer .\n" +
            "    } filter (?calledMethodPointer != \"\")} union {{\n" +
            "    \t\t?callgraph a IMMoRTALS:DynamicCallGraphEdge \n" +
            "\t\t\t ; IMMoRTALS:hasCalledHash \"???METHODPOINTER???\"\n" +
            "\t\t\t ; IMMoRTALS:hasCallerHash ?calledMethodPointer .\n" +
            "    } filter (?calledMethodPointer != \"\")}}";
    
    public static final String typeDependencyAssertionQuery = "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\n" +
            "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
            "prefix IMMoRTALS_dfu_instance: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> \n" +
            "\n" +
            "\n" +
            "select distinct ?calledMethodName ?calledMethodDesc ?calledMethodOwner where {{?x IMMoRTALS:hasMethodName \"???METHODNAME???\"\n" +
            "\t\t\t\t; IMMoRTALS:hasMethodDesc \"???METHODDESC???\"\n" +
            "\t\t\t\t; IMMoRTALS:hasOwner ?y .\n" +
            "\t\t\t\t?y a IMMoRTALS_bytecode:AClass\n" +
            "\t\t\t\t; IMMoRTALS:hasClassName \"???METHODOWNERNAME???\" .\n" +
            " {\n" +
            "\t\t\t\t?x IMMoRTALS:hasBytecodePointer ?p .\n" +
            "  \t\t\t\t?callgraph a IMMoRTALS:StaticCallGraphEdge\n" +
            "  \t\t\t\t; IMMoRTALS:hasCalledHash ?p\n" +
            "  \t\t\t\t; IMMoRTALS:hasCallerHash ?calledMethod .\n" +
            "  } filter (?p != ?calledMethod)\n" +
            "  \t\t\t\t?calledMethodUUID a IMMoRTALS_bytecode:AMethod\n" +
            "  \t\t\t\t; IMMoRTALS:hasBytecodePointer ?calledMethod \n" +
            "  \t\t\t\t; IMMoRTALS:hasMethodName ?calledMethodName\n" +
            "  \t\t\t\t; IMMoRTALS:hasMethodDesc ?calledMethodDesc\n" +
            "  \t\t\t\t; IMMoRTALS:hasOwner ?z .\n" +
            "  \n" +
            "  \t\t\t\t?z a IMMoRTALS_bytecode:AClass\n" +
            "  \t\t\t\t; IMMoRTALS:hasClassName ?calledMethodOwner .\n" +
            "} union {?x IMMoRTALS:hasMethodName \"???METHODNAME???\"\n" +
            "\t\t\t\t; IMMoRTALS:hasMethodDesc \"???METHODDESC???\"\n" +
            "\t\t\t\t; IMMoRTALS:hasOwner ?y .\n" +
            "\t\t\t\t?y a IMMoRTALS_bytecode:AClass\n" +
            "\t\t\t\t; IMMoRTALS:hasClassName \"???METHODOWNERNAME???\" .\n" +
            " {\n" +
            "\t\t\t\t?x IMMoRTALS:hasBytecodePointer ?p .\n" +
            "  \t\t\t\t?callgraph a IMMoRTALS:DynamicCallGraphEdge\n" +
            "  \t\t\t\t; IMMoRTALS:hasCalledHash ?p\n" +
            "  \t\t\t\t; IMMoRTALS:hasCallerHash ?calledMethod .\n" +
            "  } filter (?p != ?calledMethod)\n" +
            "  \t\t\t\t?calledMethodUUID a IMMoRTALS_bytecode:AMethod\n" +
            "  \t\t\t\t; IMMoRTALS:hasBytecodePointer ?calledMethod \n" +
            "  \t\t\t\t; IMMoRTALS:hasMethodName ?calledMethodName\n" +
            "  \t\t\t\t; IMMoRTALS:hasMethodDesc ?calledMethodDesc\n" +
            "  \t\t\t\t; IMMoRTALS:hasOwner ?z .\n" +
            "  \n" +
            "  \t\t\t\t?z a IMMoRTALS_bytecode:AClass\n" +
            "  \t\t\t\t; IMMoRTALS:hasClassName ?calledMethodOwner .} }  ";
}
