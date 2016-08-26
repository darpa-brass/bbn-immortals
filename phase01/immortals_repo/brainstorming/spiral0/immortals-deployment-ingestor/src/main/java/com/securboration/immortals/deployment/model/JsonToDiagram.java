//package com.securboration.immortals.deployment.model;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.TreeMap;
//
//import com.securboration.immortals.deployment.parser.DeploymentJson;
//import com.securboration.immortals.deployment.parser.DeploymentJsonContainment;
//import com.securboration.immortals.deployment.parser.DeploymentJsonKeyValue;
//import com.securboration.immortals.deployment.parser.DeploymentJsonNode;
//import com.securboration.immortals.deployment.parser.DeploymentJsonPointer;
//import com.securboration.immortals.deployment.parser.DeploymentJsonPointerValue;
//import com.securboration.immortals.deployment.parser.DeploymentTypeValue;
//import com.securboration.immortals.o2t.analysis.ObjectNode;
//import com.securboration.immortals.o2t.analysis.ObjectPrinter;
//import com.securboration.immortals.o2t.etc.ExceptionWrapper;
//import com.securboration.immortals.ontology.deployment.uml.Diagram;
//import com.securboration.immortals.ontology.deployment.uml.DiagramNode;
//import com.securboration.immortals.ontology.deployment.uml.DiagramNodeSet;
//import com.securboration.immortals.ontology.deployment.uml.DiagramRelationship;
//import com.securboration.immortals.ontology.deployment.uml.Multiplicity;
//import com.securboration.immortals.ontology.deployment.uml.MultiplicityBound;
//
//public class JsonToDiagram {
//    
//    private final DeploymentJson json;
//    private final Map<String,DeploymentJsonNode> idsToNodes;
//    private final String jsonName;
//    
//    
//    private final Map<String,DiagramNode> converted = new HashMap<>();
//    
//    private JsonToDiagram(final String name,DeploymentJson jsonModel){
//        this.jsonName = name;
//        this.idsToNodes = getIdsToNodes(jsonModel);
//        this.json = jsonModel;
//    }
//    
//    private DeploymentJsonNode getNode(String id){
//        return idsToNodes.get(id);
//    }
//    
//    private static String getName(DeploymentJsonNode n){
//        return getAttributeValue(n,"name");
//    }
//    
//    private static String getAttributeValue(DeploymentJsonNode n,String key){
//        
//        if(n.getAttributes() == null){
//            return null;
//        }
//        
//        for(DeploymentTypeValue v:n.getAttributes()){
//            if(v.getTypeName().equals(key)){
//                return v.getValue();
//            }
//        }
//        
//        return null;
//    }
//    
//    private static interface RelationshipTraverser{
//        
//        public void visitRelationship(
//                DeploymentJsonNode fromNode,
//                String pointerName,
//                Integer max,
//                Integer min,
//                DeploymentJsonNode currentNode
//                );
//        
//    }
//    
//    private void traverse(
//            DeploymentJsonNode startingNode,
//            RelationshipTraverser traverser
//            ){
//        
//        traverse(startingNode,new HashSet<>(),traverser);
//    }
//    
//    private void traverse(
//            DeploymentJsonNode currentNode,
//            Set<DeploymentJsonNode> visited,
//            RelationshipTraverser traverser
//            ){
//        if(visited.contains(currentNode)){
//            return;
//        }
//        visited.add(currentNode);
//        
//        Map<String,List<String>> pointers = new TreeMap<>();
//        for(DeploymentJsonPointer p:currentNode.getPointers()){
//            final String pointerRelationship = p.getPointerName();
//            
//            List<String> pointerValues = new ArrayList<>();
//            for(DeploymentJsonPointerValue pv:p.getPointerValues()){
//                pointerValues.add(pv.getPointerValue());
//            }
//            
//            pointers.put(pointerRelationship,pointerValues);
//        }
//        
//        
//        if(!pointers.containsKey("base") && currentNode.getBaseTag() != null){
//            pointers.put("base", Arrays.asList(currentNode.getBaseTag()));
//        }
//        
//        if(!pointers.containsKey("parent") && currentNode.getParentTag() != null){
//            pointers.put("parent", Arrays.asList(currentNode.getParentTag()));
//        }
//        
////        
////        {
////            System.out.printf("[%s]\n", currentNode.getNodeName());
//////            System.out.printf("\t-- BASE --> [%s]\n", base);
//////            System.out.printf("\t-- PARENT --> [%s]\n", parent);
////            
////            for(String key:pointers.keySet()){
////                System.out.printf("\t-- %s -->\n", key,pointers.get(key));
////                
////                for(String pointer:pointers.get(key)){
////                    System.out.printf("\t\t%s (%s)\n", pointer, getName(pointer));
////                }
////            }
////        }
//        
//        
//        {
//            //add relationships
//            for(String pointerType:pointers.keySet()){
//                for(String pointer:pointers.get(pointerType)){
//                    traverser.visitRelationship(
//                            currentNode, 
//                            pointerType, 
//                            null, 
//                            null, 
//                            getNode(pointer));
//                }
//            }
//            
//            //recurse
//            for(String pointerType:pointers.keySet()){
//                for(String pointer:pointers.get(pointerType)){
//                    traverse(
//                            getNode(pointer),
//                            visited,
//                            traverser);
//                    
//                }
//            }
//        }
//        
//    }
//    
//    private List<DiagramNodeSet> extractNodeSets(Map<String,DiagramNode> converted){
//        List<DiagramNodeSet> sets = new ArrayList<>();
//        
//        DeploymentJsonContainment c = json.getContainment();
//        
//        for(DeploymentJsonKeyValue kv:c.getKeyValues()){
//            DiagramNodeSet set = new DiagramNodeSet();
//            
//            final String key = kv.getKey();
//            final Object value = kv.getValue();
//            
//            set.setNodeSetDescriptor(converted.get(key));
//            List<DiagramNode> setNodes = new ArrayList<>();
//            if(value instanceof DeploymentJsonKeyValue[]){
//                
//                System.out.printf("containment key %s points to an array\n", key);
//                DeploymentJsonKeyValue[] values = (DeploymentJsonKeyValue[])value;
//                
//                for(DeploymentJsonKeyValue kvv:values){
//                    System.out.printf("\t%s\n", kvv.getKey());
//                    
//                    setNodes.add(converted.get(kvv.getKey()));
//                }
//            } else if(value instanceof DeploymentJsonKeyValue){
////                DeploymentJsonKeyValue v = (DeploymentJsonKeyValue)value;
////                
////                setNodes.add(converted.get(kv));
//            } else {
//                throw new RuntimeException("unhandled case");
//            }
//            
//            set.setNodes(setNodes.toArray(new DiagramNode[]{}));
//            sets.add(set);
//            
//            System.out.println("found kv with v=" + value.getClass().getName());
//            printObject(kv);
//            System.out.println();
//        }
//        
//        return sets;
//    }
//    
//    private Diagram convert(){
//        printObject(json);
//        
////        //sa client
////        convert(idsToNodes.get("8f365718-64ee-53a7-29cb-bbc2268798b3"));
////        convert(idsToNodes.get("8748321b-0a05-febe-96be-b043f3f990d5"));
////        convert(idsToNodes.get("41b7ae61-af1f-ff06-4620-160d8a30492d"));
//        
//        Map<String,DiagramNode> convertedNodes = new HashMap<>();
//        for(DeploymentJsonNode node:json.getNodes()){
//            DiagramNode converted = convert(node);
//            convertedNodes.put(converted.getId(),converted);
//        }
//        
//        List<DiagramNodeSet> nodeSets = extractNodeSets(convertedNodes);
//        
////        c.getKey
//        
////        json.getContainment()
//        
//        Diagram diagram = new Diagram();
//        
//        diagram.setName(jsonName);
//        diagram.setNodes(nodeSets.toArray(new DiagramNodeSet[]{}));
//      
//      return diagram;
//    }
//    
//    public static Diagram convert(String name,DeploymentJson jsonModel){
//        return new JsonToDiagram(name,jsonModel).convert();
//    }
//    
//    private static Map<String,DeploymentJsonNode> getIdsToNodes(DeploymentJson d){
//        Map<String,DeploymentJsonNode> map = new HashMap<>();
//        
//        for(DeploymentJsonNode node:d.getNodes()){
//            map.put(node.getNodeName(),node);
//        }
//        
//        return map;
//    }
//    
//    private DiagramNode getPlaceholder(String id){
//        
//        if(converted.containsKey(id)){
//            return converted.get(id);
//        }
//        
//        DiagramNode placeholder = new DiagramNode();
//        placeholder.setId(id);
//        
//        converted.put(id, placeholder);
//        
//        return placeholder;
//    }
//    
//    private Collection<DiagramRelationship> convert(
//            DiagramNode fromNode,
//            DeploymentJsonPointer pointer
//            ){
//        
//        List<DiagramRelationship> converted = new ArrayList<>();
//        
//        final Multiplicity fromMultiplicity = 
//                getMultiplicity(pointer.getMin(),true,pointer.getMax(),true);
//        for(DeploymentJsonPointerValue p:pointer.getPointerValues()){
//            DiagramRelationship r = new DiagramRelationship();
//            r.setRelationshipName(
//                    pointer.getPointerName());
//            r.setFrom(fromNode);
//            r.setTo(getPlaceholder(p.getPointerValue()));
//            r.setFromMultiplicity(
//                    fromMultiplicity);
//            r.setToMultiplicity(
//                    getMultiplicity(p.getMin(),true,p.getMax(),true));
//            
//            converted.add(r);
//        }
//        
//        return converted;
//    }
//    
//    private static Multiplicity getMultiplicity(
//            final Integer lower, 
//            final Boolean lowerInclusive, 
//            final Integer upper, 
//            final Boolean upperInclusive
//            ){
//        Multiplicity m = new Multiplicity();
//        
//        m.setLowerBound(getMultiplicity(lower,lowerInclusive));
//        m.setUpperBound(getMultiplicity(upper,upperInclusive));
//        
//        if(m.getLowerBound() == null && m.getUpperBound() == null){
//            return null;
//        }
//        
//        return m;
//    }
//    
//    private static MultiplicityBound getMultiplicity(final Integer value, final Boolean inclusive){
//        
//        if(value == null){
//            return null;
//        }
//        
//        if(value == -1){
//            return null;//TODO: need a better description of schema to tell if this is the right thing to do
//        }
//        
//        MultiplicityBound b = new MultiplicityBound();
//        
//        b.setBoundValue(value);
//        b.setInclusive(inclusive);
//        
//        return b;
//    }
//    
//    private DiagramNode convert(DeploymentJsonNode d){
//        {
//            System.out.printf(
//                    "\nabout to process node %s [%s]\n",
//                    d.getNodeName(),
//                    getName(d));
//            printObject(d);
//            
//            System.out.printf("hierarchy for %s (%s):\n", d.getNodeName(),getName(d));
//            traverse(d,new RelationshipTraverser(){
//
//                @Override
//                public void visitRelationship(
//                        DeploymentJsonNode fromNode,
//                        String pointerName, 
//                        Integer max, 
//                        Integer min,
//                        DeploymentJsonNode currentNode
//                        ) {
//                    System.out.printf(
//                            "\t%s -- [%s] --> %s\n", 
//                            getName(fromNode), 
//                            pointerName, 
//                            getName(currentNode));
//                }});
//            
//            System.out.println();
//        }
//        
//        DiagramNode diagramNode = getPlaceholder(d.getNodeName());
//        
//        final List<DiagramRelationship> relationships = new ArrayList<>();
//        for(DeploymentJsonPointer pointer:d.getPointers()){
//            relationships.addAll(convert(diagramNode,pointer));
//        }
//        
//        final List<DiagramRelationship> metaRelationships = new ArrayList<>();
//        if(d.getMeta() != null && d.getMeta().getPointers() != null){
//            for(DeploymentJsonPointer pointer:d.getMeta().getPointers()){
//                metaRelationships.addAll(convert(diagramNode,pointer));
//            }
//        }
//        
//        diagramNode.setName(getName(d));
//        diagramNode.setId(d.getNodeName());
//        diagramNode.setRelationships(
//                relationships.toArray(new DiagramRelationship[]{}));
//        diagramNode.setMetaRelationships(
//                metaRelationships.toArray(new DiagramRelationship[]{}));
//        
//        return diagramNode;
//    }
//    
//    private static void printObject(Object o){
//        ExceptionWrapper.wrap(()->{
//            ObjectNode n = ObjectNode.build(o);
//            ObjectPrinter.getPrinterVisitor();
//            
//            n.accept(ObjectPrinter.getPrinterVisitor());
//        });
//    }
//    
//}
