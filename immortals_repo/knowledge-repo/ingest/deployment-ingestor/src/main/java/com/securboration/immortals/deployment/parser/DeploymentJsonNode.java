package com.securboration.immortals.deployment.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class DeploymentJsonNode{
        
        private String nodeName;
        private DeploymentTypeValue[] attributes;
        private String baseTag;
        private DeploymentJsonNodeMeta meta;
        private String parentTag;
        private DeploymentJsonPointer[] pointers;
        private DeploymentJsonKeyValue[] registry;
        private DeploymentJsonMetaAspectSet[] sets;
        private DeploymentJsonKeyValue[] constraints;
        
        public DeploymentJsonNode(){}
        private DeploymentJsonNode(String name,JSONObject obj){
            System.out.println(name);
            nodeName = name;
            loadAttributes(obj.getJSONObject("attributes"));
            baseTag = getStringOrNull(obj,"base");
            loadMeta(obj.getJSONObject("meta"));
            parentTag = getStringOrNull(obj,"parent");
            loadPointers(obj.getJSONObject("pointers"));
            loadRegistry(obj.getJSONObject("registry"));
            loadSets(obj.getJSONObject("sets"));
            //loadConstraints(obj.getJSONObject("constraints"));
        }
        
        private static String getStringOrNull(JSONObject obj,String key){
            if(obj.isNull(key)){
                return null;
            } 
            return obj.getString(key);
        }
        
        private void loadConstraints(JSONObject o){
            //TODO: looks like it's always empty (for now?)
            this.constraints = new DeploymentJsonKeyValue[]{};
        }
        
        private void loadSets(JSONObject o){
            this.sets = DeploymentUmlIngestor.getSets(o);
        }
        
        private void loadRegistry(JSONObject o){
            //TODO
        }
        
        private void loadPointers(JSONObject o){
            if(JSONObject.getNames(o)==null){
                return;
            }
            
            this.pointers = DeploymentUmlIngestor.getPointers(o);
        }
        
//        private void loadPointers(JSONObject o){
//            
//            if(JSONObject.getNames(o)==null){
//                return;
//            }
//            
//            List<DeploymentJsonPointer> pointers = new ArrayList<>();
//            for(String name:JSONObject.getNames(o)){
//                DeploymentJsonPointer pointer = new DeploymentJsonPointer();
//                
//                if(o.isNull(name)){
//                    continue;
//                }
//                
//                pointers.add(pointer);
//                
//                Object pointerObject = o.get(name);
//                
//                if(pointerObject instanceof String){
//                    DeploymentJsonPointer p = new DeploymentJsonPointer();
//                    pointers.add(p);
//                    
//                    p.pointerName = name;
//                    p.pointerValues = new DeploymentJsonPointerValue[]{new DeploymentJsonPointerValue()};
//                    p.pointerValues[0].pointerValue = (String)pointerObject;
//                } else {
//                    throw new RuntimeException("unhandled case: " + pointerObject.getClass().getName());
//                }
//            }
//            
//            this.pointers = pointers.toArray(new DeploymentJsonPointer[]{});
//            
//        }
        
        private void loadMeta(JSONObject o){
            this.meta = new DeploymentJsonNodeMeta(o);
        }
        
        private void loadAttributes(JSONObject o){
            
            if(o == null){
                return;
            }
            
            if(JSONObject.getNames(o) == null){
                return;
            }
            
            List<DeploymentTypeValue> kvs = new ArrayList<>();
            for(String key:JSONObject.getNames(o)){
                String value = o.optString(key);
                
                DeploymentTypeValue kv = new DeploymentTypeValue();
                kv.typeName = key;
                kv.value= value;
                
                kvs.add(kv);
//                System.out.printf("attributes: %s=%s\n", key,value);//TODO
            }
            
            attributes = kvs.toArray(new DeploymentTypeValue[]{});
            
        }
        
        public static DeploymentJsonNode[] parse(JSONObject obj){
            
            List<DeploymentJsonNode> nodes = new ArrayList<>();
            for(String name:JSONObject.getNames(obj)){

                nodes.add(new DeploymentJsonNode(name,obj.getJSONObject(name)));
                
            }
            
            return nodes.toArray(new DeploymentJsonNode[]{});
        }
        public DeploymentTypeValue[] getAttributes() {
            return attributes;
        }
        public void setAttributes(DeploymentTypeValue[] attributes) {
            this.attributes = attributes;
        }
        public String getBaseTag() {
            return baseTag;
        }
        public void setBaseTag(String baseTag) {
            this.baseTag = baseTag;
        }
        public DeploymentJsonNodeMeta getMeta() {
            return meta;
        }
        public void setMeta(DeploymentJsonNodeMeta meta) {
            this.meta = meta;
        }
        public String getParentTag() {
            return parentTag;
        }
        public void setParentTag(String parentTag) {
            this.parentTag = parentTag;
        }
        public DeploymentJsonPointer[] getPointers() {
            return pointers;
        }
        public void setPointers(DeploymentJsonPointer[] pointers) {
            this.pointers = pointers;
        }
        public DeploymentJsonKeyValue[] getRegistry() {
            return registry;
        }
        public void setRegistry(DeploymentJsonKeyValue[] registry) {
            this.registry = registry;
        }
        public DeploymentJsonMetaAspectSet[] getSets() {
            return sets;
        }
        public void setSets(DeploymentJsonMetaAspectSet[] sets) {
            this.sets = sets;
        }
        public DeploymentJsonKeyValue[] getConstraints() {
            return constraints;
        }
        public void setConstraints(DeploymentJsonKeyValue[] constraints) {
            this.constraints = constraints;
        }
        public String getNodeName() {
            return nodeName;
        }
        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }
        
    }