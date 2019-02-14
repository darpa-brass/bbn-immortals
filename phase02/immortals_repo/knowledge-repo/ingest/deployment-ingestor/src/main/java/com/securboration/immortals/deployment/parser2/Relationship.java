package com.securboration.immortals.deployment.parser2;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Relationship {
    
    private static JSONObject getPointerMeta(
            String currentNodeId,
            NodeMapping mapping,
            String pointerName
            ){
        
        System.out.printf("looking for pointer [%s] in [%s]\n", pointerName,currentNodeId);//TODO
        
        JSONObject currentNode = mapping.getJson(currentNodeId);
        
        if(currentNode.has("meta")){
            JSONObject meta = currentNode.getJSONObject("meta");
            
            if(meta.has("pointers")){
                JSONObject pointersMeta = meta.getJSONObject("pointers");
                
                if(pointersMeta.has(pointerName)){
                    System.out.printf("* found pointer [%s] in [%s]\n", pointerName,currentNodeId);//TODO
                    
                    return pointersMeta.getJSONObject(pointerName);
                }
            }
        }
        
        //look at the base
        Node n = mapping.getNode(currentNodeId);
        String base = n.getBaseTag();
        
        if(base == null){
            return null;
        }
        
        return getPointerMeta(base,mapping,pointerName);
    }
    
    public static Relationship[] getRelationships(
            String fromId,
            NodeMapping mapping
            ){
        
        JSONObject nodeJson = mapping.getJson(fromId);
        
        if(!nodeJson.has("pointers")){
            return new Relationship[]{};
        }
        
        JSONObject pointers = nodeJson.getJSONObject("pointers");
        
        if(JSONObject.getNames(pointers) == null){
            return new Relationship[]{};
        }
        
        List<Relationship> relationships = new ArrayList<>();
        for(String relationshipName:JSONObject.getNames(pointers)){
            
            List<RelationshipEdge> edges = new ArrayList<>();
                            
            int minCardinality;
            int maxCardinality;
            
            JSONObject pointerMeta = 
                    getPointerMeta(fromId,mapping,relationshipName);
            
            if(pointerMeta == null){
                System.out.flush();
                System.err.printf("FIX THIS: couldn't find meta info for relationship with name %s on node %s\n", fromId,relationshipName);
                System.err.flush();
                continue;
            }//TODO
            
            JSONArray pointerNodes = pointerMeta.getJSONArray("items");
            JSONArray pointerNodesMinCardinality = pointerMeta.getJSONArray("minItems");
            JSONArray pointerNodesMaxCardinality = pointerMeta.getJSONArray("maxItems");
            minCardinality = pointerMeta.getInt("min");
            maxCardinality = pointerMeta.getInt("max");
            
            for(int i=0;i<pointerNodes.length();i++){
                final String relationshipNodeId = pointerNodes.getString(i);
                final int relationshipIdMinCardinality = pointerNodesMinCardinality.getInt(i);
                final int relationshipIdMaxCardinality = pointerNodesMaxCardinality.getInt(i);
                
                RelationshipEdge r = 
                        new RelationshipEdge(
                                relationshipNodeId,
                                relationshipIdMinCardinality,
                                relationshipIdMaxCardinality
                                );
                
                edges.add(r);
            }
            
            String toId = pointers.optString(relationshipName);
            if(toId.isEmpty()){
                //TODO: might want more robust handling here
                continue;
            }
            
            relationships.add(
                    new Relationship(
                            fromId,
                            relationshipName,
                            edges.toArray(new RelationshipEdge[]{}),
                            toId,
                            minCardinality,
                            maxCardinality));
        }
        
        return relationships.toArray(new Relationship[]{});
    }

    private final String fromId;
    private final String relationshipName;
    private final RelationshipEdge[] edges;
    private final String toId;
    
    //the cardinality that applies to all edges
    private final int minCardinality;
    private final int maxCardinality;

    private Relationship(
            String fromId, 
            String relationshipName, 
            RelationshipEdge[] edges, 
            String toId,
            int minCardinality, 
            int maxCardinality
            ) {
        super();
        this.fromId = fromId;
        this.relationshipName = relationshipName;
        this.edges = edges;
        this.toId = toId;
        this.minCardinality = minCardinality;
        this.maxCardinality = maxCardinality;
    }

    public String getFromId() {
        return fromId;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public RelationshipEdge[] getEdges() {
        return edges;
    }

    public String getToId() {
        return toId;
    }

    public int getMinCardinality() {
        return minCardinality;
    }

    public int getMaxCardinality() {
        return maxCardinality;
    }
    
}
