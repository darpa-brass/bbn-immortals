package com.securboration.immortals.ontology.deployment.uml3;

/**
 * Describes the edge direction of a link between two diagram nodes
 * 
 * @author Securboration
 *
 */
public enum EdgeDirection {
    
    Undirected,//the edge is undirected
    Forward,//the edge points in the from-->to direction
    Backward,//the edge points in teh to-->from direction
    Bidirectional//the edge is bidirectional
    ;
}
