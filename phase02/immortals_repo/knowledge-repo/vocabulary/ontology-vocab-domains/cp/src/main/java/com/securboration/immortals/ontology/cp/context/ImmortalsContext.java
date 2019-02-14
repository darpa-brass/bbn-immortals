package com.securboration.immortals.ontology.cp.context;

import java.util.ArrayList;

/**
 * A challenge problem context, which comprises a set of graphs and metadata
 * about their use in the context.
 * 
 * @author jstaples
 *
 */
public class ImmortalsContext {
    
    private ArrayList<String> graphs;
    
    public ImmortalsContext(ArrayList<String> graphUris) {
        graphs = graphUris;
    }
    
    public void setGraphs(ArrayList<String> _graphs) {
        graphs = _graphs;
    }
    
    public ArrayList<String> getGraphs() {
        return graphs;
    }
    
}
