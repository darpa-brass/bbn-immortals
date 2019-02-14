package com.securboration.immortals.deployment.parser2;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class NodeMapping {

    private final Map<String,Node> nodeMap = new HashMap<>();
    private final Map<String,JSONObject> jsonMap = new HashMap<>();
    
    private Node fco = null;
    private Node root = null;
    
    public void visitNode(Node n,JSONObject o){
        final String nodeId = n.getNodeId();
        
        putUnique(nodeMap,nodeId,n);
        putUnique(jsonMap,nodeId,o);
    }
    
    private static <K,V> void putUnique(Map<K,V> map,K key,V value){
        if(map.containsKey(key)){
            
            if(map.get(key) != value){
                throw new RuntimeException("name collision for key " + key);
            }
            
            return;
        }
        
        map.put(key, value);
    }
    
    public JSONObject getJson(String uuid){
        return jsonMap.get(uuid);
    }
    
    public Node getNode(String uuid){
        return nodeMap.get(uuid);
    }
    
    public Map<String,Node> getMapping(){
        return new HashMap<>(nodeMap);
    }
    
    public Node getFco() {
        return fco;
    }

    public Node getRoot() {
        return root;
    }
    
}
