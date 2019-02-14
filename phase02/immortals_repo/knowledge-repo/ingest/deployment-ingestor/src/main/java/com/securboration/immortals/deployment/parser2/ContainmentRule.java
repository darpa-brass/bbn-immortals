package com.securboration.immortals.deployment.parser2;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ContainmentRule {

    private final String nodeId;
    private final int minCardinality;
    private final int maxCardinality;
    
    public String getNodeId() {
        return nodeId;
    }
    public int getMinCardinality() {
        return minCardinality;
    }
    public int getMaxCardinality() {
        return maxCardinality;
    }
    
    private ContainmentRule(
            String nodeId, 
            int minCardinality,
            int maxCardinatlity
            ) {
        super();
        this.nodeId = nodeId;
        this.minCardinality = minCardinality;
        this.maxCardinality = maxCardinatlity;
    }
    
    
    public static ContainmentRule[] getContainment(
            String fromId,
            NodeMapping mapping
            ){
        JSONObject nodeJson = mapping.getJson(fromId);
        
        if(!nodeJson.has("meta")){
            return new ContainmentRule[]{};
        }
        
        JSONObject meta = nodeJson.getJSONObject("meta");
        
        if(!meta.has("children")){
            return new ContainmentRule[]{};
        }
        
        JSONObject childrenNode = meta.getJSONObject("children");
        
        JSONArray children = childrenNode.getJSONArray("items");
        JSONArray minItems = childrenNode.getJSONArray("minItems");
        JSONArray maxItems = childrenNode.getJSONArray("maxItems");
        
        List<ContainmentRule> rules = new ArrayList<>();
        for(int i=0;i<children.length();i++){
            String child = children.getString(i);
            int min = minItems.getInt(i);
            int max = maxItems.getInt(i);
            
            rules.add(new ContainmentRule(child,min,max));
        }
        
        return rules.toArray(new ContainmentRule[]{});
    }
    
}
