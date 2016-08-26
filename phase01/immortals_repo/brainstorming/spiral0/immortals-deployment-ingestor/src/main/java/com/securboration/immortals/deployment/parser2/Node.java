package com.securboration.immortals.deployment.parser2;

import org.json.JSONObject;

public class Node{
    
    //UUID for this node
    private final String nodeId;
    
    //human readable name for this node, may be inherited from a base
    private final String nodeName;
    
    //this node inherits all properties from its base node
    private final String baseTag;
    
    //a composition relationship between parent and this node.  I.e., the parent
    // contains this node
    private final String parentTag;
    
    //the relationships that bind explicitly to this node I.e., pointers
    private Relationship[] relationships;
    
    //the attributes that bind explicitly to this node
    private final Attribute[] attributes;
    
    //the rules governing what this node contains (ie fields)
    private ContainmentRule[] containmentRules;
    
    public static NodeMapping getNodeMapping(String json){
        JSONObject jsonObject = new JSONObject(json);
        
        NodeMapping m = new NodeMapping();
        
        for(String nodeId:JSONObject.getNames(jsonObject)){
            JSONObject node = jsonObject.getJSONObject(nodeId);
            
            Node n = new Node(nodeId,node);
            m.visitNode(n,node);
        }
        
//        //now we have a complete mapping of uuids to Nodes, so fill in the 
//        // actual hierarchical relationships
//        for(Node n:m.getMapping().values()){
//            //set the bases
//            n.baseNode = m.getNode(n.baseTag);
//            
//            //set the parents
//            n.parentNode = m.getNode(n.parentTag);
//        }
        
        
        for(Node n:m.getMapping().values()){
            //build relationships
            n.buildRelationships(m);
            
            //build containment rules
//            n.buildContainmentRules(m);//TODO, dont think we need this
        }
        
        return m;
    }
    
    private void buildContainmentRules(NodeMapping m){
        containmentRules = ContainmentRule.getContainment(nodeId, m);
    }
    
    private void buildRelationships(NodeMapping m){
        log("building relationship for %s\n", nodeId);
        
        relationships = 
                Relationship.getRelationships(
                        nodeId,
                        m
                        );
    }
    
    private static String getName(JSONObject attributes){
        if(!attributes.has("name")){
            return null;
        }
        
        return attributes.getString("name");
    }
    
    private static void log(String format, Object...args){
        System.out.printf(format, args);//TODO
    }
    
    private Node(String name,JSONObject obj){
        log("instantiating %s from a %s\n", name, obj.getClass().getName());
        
        nodeId = name;
        baseTag = getStringOrNull(obj,"base");
        parentTag = getStringOrNull(obj,"parent");
        
        nodeName = getName(obj.getJSONObject("attributes"));
        
        attributes = 
                Attribute.getAttributes(
                        obj.getJSONObject("attributes"));
    }
        
    private static String getStringOrNull(JSONObject obj,String key){
        if(obj.isNull(key)){
            return null;
        } 
        return obj.getString(key);
    }

    public String getNodeId() {
        return nodeId;
    }
    
    private static Object getAttributeValue(Attribute[] attributes,String key){
        for(Attribute a:attributes){
            if(a.getKey().equals(key)){
                return a.getValue();
            }
        }
        
        return null;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getBaseTag() {
        return baseTag;
    }

    public String getParentTag() {
        return parentTag;
    }

    public Relationship[] getRelationships() {
        return relationships;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public ContainmentRule[] getContainmentRules() {
        return containmentRules;
    }
        
}
