package com.securboration.immortals.deployment.parser2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.securboration.immortals.deployment.parser3.Parser;
import com.securboration.immortals.deployment.pojos.DeploymentParser;
import com.securboration.immortals.deployment.pojos.FieldValue;
import com.securboration.immortals.deployment.pojos.ObjectInstance;
import com.securboration.immortals.deployment.pojos.TypeAbstraction;
import com.securboration.immortals.deployment.pojos.values.ValueComplex;

public class ParserImpl implements DeploymentParser {

    private Parser typeParser = new Parser();
    private NodeMapping nodeMapping;
    
    private Map<String,TypeAbstraction> types = new HashMap<>();
    private Map<String,ObjectInstance> instances = new HashMap<>();
    private Map<String,Node> nodes;
    
    private Map<String,Node> pointers = new HashMap<>();
    
    //nodeIDs -> pointers with a "from" link originating at that node
    private Map<String,List<PointerAbstraction>> pointerAbstractions = new HashMap<>();
    
    private static class PointerAbstraction{
        final String from;
        final String type;
        final String to;
        PointerAbstraction(String from, String type, String to) {
            super();
            this.from = from;
            this.type = type;
            this.to = to;
        }
        
    }
    
    private void buildInstances(){
        extractPointers();
        
        addPointerDerivedFields();
    }
    
    private void addPointerDerivedFields(){
        //extract the pointers
        instances.forEach((k, v) -> {
            Node n = node(k);
            
            if(isPointer(n)){
                return;
            }
            
            if(!pointerAbstractions.containsKey(k)){
                return;
            }
            
            System.out.printf(
                    "node [%s] (%s) has pointer-derived field(s):\n", 
                    n.getNodeId(),
                    getAttributeValue(n,"name")
                    );
            for(PointerAbstraction p:pointerAbstractions.get(k)){
                System.out.printf(
                        "\t--[%s]--> %s\n", 
                        getAttributeValue(node(p.type),"name"),
                        p.to
                        );
                
                ObjectInstance fieldOwner = instances.get(n.getNodeId());
                ObjectInstance fieldObject = instances.get(p.to);
                
                if(fieldOwner.getFieldValues() == null){
                    fieldOwner.setFieldValues(new FieldValue[]{});
                }
                
                //TODO: inefficient array appending
                fieldOwner.setFieldValues(
                        Arrays.copyOf(
                                fieldOwner.getFieldValues(), 
                                fieldOwner.getFieldValues().length+1));
                
                FieldValue[] fields = fieldOwner.getFieldValues();
                
                fields[fields.length-1] = 
                        getPointerFieldValue(
                                fieldObject,
                                (String)getAttributeValue(node(p.type),"name")
                                );
            }
        });
    }
    
    private FieldValue getPointerFieldValue(
            ObjectInstance fieldObject,
            String name
            ){
        FieldValue f = new FieldValue();
        
        f.setName(name);
        
        ValueComplex v = new ValueComplex();
        v.setPointer(true);
        v.setType(fieldObject.getInstanceType());
        v.setValue(fieldObject);
        
        f.setValue(v);
        
        return f;
    }
    
    private Node node(String id){
        
        if(!nodes.containsKey(id)){
            throw new RuntimeException("no node with id " + id);
        }
        
        return nodes.get(id);
    }
    
    private void extractPointers(){
        //extract the pointers
        instances.forEach((k, v) -> {
            Node n = nodes.get(k);
            
            if(isPointer(n)){
                pointers.put(k,n);
                
                System.out.printf("node %s is a pointer\n", k);
                
                String from = getRelationship(n,"src").getToId();
                String to = getRelationship(n,"dst").getToId();
                
                {
                    PointerAbstraction p = 
                            new PointerAbstraction(from,k,to);
                    List<PointerAbstraction> s = pointerAbstractions.get(from);
                    if(s == null){
                        s = new ArrayList<>();
                        pointerAbstractions.put(from, s);
                    }
                    s.add(p);
                }
                
                System.out.printf(
                        "%s --%s--> %s\n",
                        from,
                        getAttributeValue(n,"name"),
                        to
                        );
            }
        });
    }
    
    private static Relationship getRelationship(Node n,String name){
        
        Relationship match = null;
        for(Relationship r:n.getRelationships()){
            
            boolean matches = r.getRelationshipName().equals(name);
            
            if(matches){
                if(match != null){
                    throw new RuntimeException(
                            "multiple matches for pointer type " + name + 
                            " in node " + n.getNodeName());
                }
                
                match = r;
            }
        }
        
        if(match == null){
            throw new RuntimeException(
                    "no match for pointer type " + name + " on node " + 
                    n.getNodeName());
        }
        
        return match;
    }
    
    private Object getAttributeValue(Node n,String attribute){
        
        ThreadLocal<Object> l = new ThreadLocal<>();//TODO: wrapper idiom
        l.set(null);
        
        visitInheritanceTree(n,(i)->{
            for(Attribute a:i.getAttributes()){
                if(a.getKey().equals(attribute) && l.get() == null){
                    l.set(a.getValue());
                }
            }
        });
        
        if(l.get() == null){
            throw new RuntimeException(
                    "couldn't find attribute " + attribute + " in node " + 
                            n.getNodeId() + " or in its bases");
        }
        
        return l.get();
    }
    
    /**
     * 
     * @param n
     * @return true iff this node or one of its bases has pointer relationships
     */
    private boolean isPointer(Node n){
        AtomicBoolean b = new AtomicBoolean(false);//TODO: wrapper idiom
        visitInheritanceTree(n,i->{
            if(n.getRelationships() != null && n.getRelationships().length > 0){
                b.set(true);
            }
        });
        
        return b.get();
    }
    
    interface NodeHierarchyVisitor{
        void visitNode(Node n);
    }
    
    private void visitInheritanceTree(Node n,NodeHierarchyVisitor v){
        v.visitNode(n);
        
        String next = n.getBaseTag();
        if(next == null){
            return;
        }
        
        visitInheritanceTree(nodes.get(next),v);
    }
    
    @Override
    public void parse(String deploymentJson) {
        typeParser.parse(deploymentJson);
        typeParser.getTypes().forEach(t->types.put(t.getUuid(), t));
        typeParser.getInstances().forEach(t->instances.put(t.getUuid(), t));
        nodeMapping = Node.getNodeMapping(deploymentJson);
        
        nodes = nodeMapping.getMapping();
        
        buildInstances();
    }

    @Override
    public Collection<TypeAbstraction> getTypes() {
        return types.values();
    }

    @Override
    public Collection<ObjectInstance> getInstances() {
        return instances.values();
    }

}
