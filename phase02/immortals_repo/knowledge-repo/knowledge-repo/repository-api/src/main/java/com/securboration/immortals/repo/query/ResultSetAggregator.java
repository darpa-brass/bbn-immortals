package com.securboration.immortals.repo.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;

import com.securboration.immortals.repo.ontology.FusekiClient.ResultSetProcessor;

public class ResultSetAggregator implements ResultSetProcessor{
    
    private final List<Map<String,RDFNode>> solutions = new ArrayList<>();

    @Override
    public void processQuerySolution(QuerySolution s) {
        Map<String,RDFNode> map = new HashMap<>();
        s.varNames().forEachRemaining(v->{
            map.put(v, s.get(v));
        });
        
        solutions.add(map);
    }

    
    public List<Map<String, RDFNode>> getSolutions() {
        return solutions;
    }
    
}