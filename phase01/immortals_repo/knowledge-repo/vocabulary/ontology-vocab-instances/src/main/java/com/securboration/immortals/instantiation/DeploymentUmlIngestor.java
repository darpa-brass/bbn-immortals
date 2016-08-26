package com.securboration.immortals.instantiation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * 
 * 
 * @author jstaples
 *
 */
public class DeploymentUmlIngestor {
    
    public static void main(String[] args) throws IOException{
        DeploymentUmlIngestor.ingest(
                FileUtils.readFileToString(
                        new File(
                                "../bin/guest+sample_deployment_model_master.json")));
    }
    
    
    public static class DeploymentJson{
        
        private DeploymentJsonRoot root;
        private DeploymentJsonContainment containment;
//        private DeploymentJsonBases bases;
        private DeploymentJsonNode[] nodes;
        private DeploymentJsonRelid[] relids;
        private DeploymentJsonMetaSheets metaSheets;
        
        public DeploymentJson(){}
        
        private DeploymentJson(JSONObject obj){
            this.root = new DeploymentJsonRoot(obj.getJSONObject("root"));
            this.containment = new DeploymentJsonContainment(obj.getJSONObject("containment"));
//            this.bases = new DeploymentJsonBases(obj.getJSONObject("bases"));
            this.nodes = DeploymentJsonNode.parse(obj.getJSONObject("nodes"));
            this.relids = getRelids(obj.getJSONObject("relids"));
            this.metaSheets = new DeploymentJsonMetaSheets(obj.getJSONObject("metaSheets"));
        }
        
    }
    
    public static class DeploymentJsonRoot{
        
        private String path;
        private String guid;
        
        private DeploymentJsonRoot(){}
        private DeploymentJsonRoot(JSONObject obj){
            
        }
        
    }
    
    public static class DeploymentJsonContainment{
        
        private DeploymentJsonKeyValue[] keyValues;

        private DeploymentJsonContainment(){}
        private DeploymentJsonContainment(JSONObject obj){
            
            List<DeploymentJsonKeyValue> kvs = new ArrayList<>();
            
            traverseElementsRecursive(obj,null,kvs);
            
            keyValues = kvs.toArray(new DeploymentJsonKeyValue[]{});
        }
        
        private void traverseElementsRecursive(
                JSONObject current,
                DeploymentJsonKeyValue currentKeyValue,
                List<DeploymentJsonKeyValue> keyValues
                ){
            
            if(current == null){
                return;
            }
            
//            System.out.printf("looking at a %s\n", current.getClass().getName());
            
            if(JSONObject.getNames(current) == null){
//                System.out.printf("\tit's empty\n");
                currentKeyValue.value = new DeploymentJsonKeyValue[]{};
                return;
            }
            
            List<DeploymentJsonKeyValue> kvs = new ArrayList<>();
            for(String name:JSONObject.getNames(current)){
                
                String key = name;
                Object value = current.get(name);
                
                if(value  instanceof JSONObject){
                    //it's a nested structure, recurse
                    
                    DeploymentJsonKeyValue child = new DeploymentJsonKeyValue();
                    kvs.add(child);
                    
//                    System.out.printf("diving into key %s\n", key);
                    
                    traverseElementsRecursive(
                            (JSONObject)value,
                            child,
                            new ArrayList<>());
                } else {
                    System.out.printf("\t%s is a %s\n", name, current.get(name).getClass());
                    
                    throw new RuntimeException("unhandled case");
                }
            }
            
            if(currentKeyValue != null){
                currentKeyValue.value = kvs.toArray(new DeploymentJsonKeyValue[]{});
            }
            
        }
        
    }
    
    public static class DeploymentJsonKeyValue{
        
        private String key;
        private Object value;
        
        public DeploymentJsonKeyValue(){}
        private DeploymentJsonKeyValue(JSONObject o){
            
        } 
    }
    
    public static class DeploymentTypeValue{
        
        private String typeName;
        private Object value;
    }
    
    public static class DeploymentTypeDefinition{
        
        private String typeName;
        private String elementType;
        private String[] enumerations;
        
        private DeploymentTypeDefinition(String name,JSONObject o){
            this.typeName = name;
            this.elementType = o.getString("type");
            
            if(!o.isNull("enum")){
                this.enumerations = toStringArray(o.getJSONArray("enum"));
            }
        }
    }
    
    public static class DeploymentJsonBases{
        
        private DeploymentJsonBases(){}
        private DeploymentJsonBases(JSONObject obj){
            
        }
        
    }
    
    private static class DeploymentJsonPointer{
        private String pointerName;
        private DeploymentJsonPointerValue[] pointerValues;
        
        private Integer max;
        private Integer min;
    }
    
    private static class DeploymentJsonPointerValue{
        
        private String pointerValue;
        private Integer max;
        private Integer min;
        
    }
    
    public static class DeploymentJsonNodeMetaAspect{
        
        private String[] instances;
        private String[] types;
        
    }
    
    private static String[] toStringArray(JSONArray array){
        if(array == null){
            return null;
        }
        
        List<String> values = new ArrayList<>();
        
        for(int i=0;i<array.length();i++){
            values.add((String)array.get(i));
        }
        
        return values.toArray(new String[]{});
    }
    
    public static class DeploymentJsonNodeMeta{
        
        private DeploymentJsonPointer[] pointers;
        private DeploymentJsonPointer children;
        private DeploymentJsonNodeMetaAspect aspects;
        private DeploymentTypeDefinition[] attributes;
        
        public DeploymentJsonNodeMeta(){}
        private DeploymentJsonNodeMeta(JSONObject o){
            
            loadAspects(o);
            loadChildren(o);
            loadPointers(o);
            loadAttributes(o);
        }
        
        private void loadAttributes(JSONObject o){
            if(o.isNull("attributes")){
                return;
            }
            
            o = o.getJSONObject("attributes");
            
            List<DeploymentTypeDefinition> attributes = new ArrayList<>();
            for(String name:JSONObject.getNames(o)){
                attributes.add(
                        new DeploymentTypeDefinition(
                                name,
                                o.getJSONObject(name)));
            }
            
            this.attributes = 
                    attributes.toArray(new DeploymentTypeDefinition[]{});
        }
        
        private void loadPointers(JSONObject o){
            
            if(o.isNull("pointers")){
                return;
            }
            
            JSONObject pointers = o.getJSONObject("pointers");
            
            this.pointers = getPointers(pointers);
        }
        
        private void loadChildren(JSONObject o){
            
            if(o.isNull("children")){
                return;
            }
            
            this.children = getPointer("children",o.getJSONObject("children"));
        }
        
        
        
        private void loadAspects(JSONObject o){
            
            if(o.isNull("aspects")){
                return;
            }
            
            JSONObject aspect = o.getJSONObject("aspects");
            
            aspects = new DeploymentJsonNodeMetaAspect();
            aspects.instances = toStringArray(aspect.getJSONArray("instances"));
            aspects.types = toStringArray(aspect.getJSONArray("types"));
        }
        
    }
    
    private static void verifyArraysSameLength(JSONArray...arrays){
        
        Integer length = null;
        
        for(JSONArray array:arrays){
            if(length == null){
                length = array.length();
            }
            
            if(length != array.length()){
                throw new RuntimeException("assumption violated, array length mismatch");
            }
        }
    }
    
    private static DeploymentJsonPointer getPointer(String name, JSONObject o){
        DeploymentJsonPointer p = new DeploymentJsonPointer();
        
        p.pointerName = name;
        
        JSONArray items = o.getJSONArray("items");
        JSONArray minItems = o.getJSONArray("minItems");
        JSONArray maxItems = o.getJSONArray("maxItems");
        
        verifyArraysSameLength(items,minItems,maxItems);
        
        List<DeploymentJsonPointerValue> values = new ArrayList<>();
        
        for(int i=0;i<items.length();i++){
            DeploymentJsonPointerValue pointerValue = 
                    new DeploymentJsonPointerValue();
            
            pointerValue.max = maxItems.getInt(i);
            pointerValue.min = minItems.getInt(i);
            pointerValue.pointerValue = items.getString(i);
            
            values.add(pointerValue);
        }
        
        p.pointerValues = values.toArray(new DeploymentJsonPointerValue[]{});
        
        return p;
    }
    
    private static DeploymentJsonPointer[] getPointers(JSONObject o){
        
        if(JSONObject.getNames(o)==null){
            return null;
        }
        
        List<DeploymentJsonPointer> pointers = new ArrayList<>();
        for(String name:JSONObject.getNames(o)){
            DeploymentJsonPointer pointer = new DeploymentJsonPointer();
            pointers.add(pointer);
            
            if(o.isNull(name)){
                continue;
            }
            
            Object pointerObject = o.get(name);
            
            if(pointerObject instanceof String){
                DeploymentJsonPointer p = new DeploymentJsonPointer();
                pointers.add(p);
                
                p.pointerName = name;
                p.pointerValues = new DeploymentJsonPointerValue[]{new DeploymentJsonPointerValue()};
                p.pointerValues[0].pointerValue = (String)pointerObject;
            } else if(pointerObject instanceof JSONObject) {
                pointers.add(getPointer(name,(JSONObject)pointerObject));
            } else {
                throw new RuntimeException("unhandled case for pointer key " + name + ": " + pointerObject.getClass().getName());
            }
        }
        
        return pointers.toArray(new DeploymentJsonPointer[]{});
        
    }
    
    public static class DeploymentJsonMetaAspectSet{
        
        private String name;
        private String[] guidsInSet;
        
        private DeploymentJsonMetaAspectSet(String name,JSONArray a){
            this.name = name;
            
            List<String> guids = new ArrayList<>();
            for(int i=0;i<a.length();i++){
                guids.add(a.getJSONObject(i).getString("guid"));
            }
            guidsInSet = guids.toArray(new String[]{});
        }
        
        
//"MetaAspectSet": [
//                          {
//                              "attributes": {},
//                              "guid": "0f7f33c5-62d4-d6f8-9afa-974a9947c140",
//                              "registry": {
//                                  "position": {
//                                      "x": 830,
//                                      "y": 313
//                                  }
//                              }
//                          },
    }
    
    private static DeploymentJsonMetaAspectSet[] getSets(JSONObject o){
        List<DeploymentJsonMetaAspectSet> sets = new ArrayList<>();
        
        if(JSONObject.getNames(o) == null){
            return new DeploymentJsonMetaAspectSet[]{};
        }
        
        for(String name:JSONObject.getNames(o)){
            sets.add(
                    new DeploymentJsonMetaAspectSet(
                            name,
                            o.getJSONArray(name)));
        }
        
        return sets.toArray(new DeploymentJsonMetaAspectSet[]{});
    }
    
    public static class DeploymentJsonNode{
        
        private String nodeTag;
        private DeploymentTypeValue[] attributes;
        private String baseTag;
        private DeploymentJsonNodeMeta meta;
        private String parentTag;
        private DeploymentJsonPointer[] pointers;
        private DeploymentJsonKeyValue[] registry;
        private DeploymentJsonMetaAspectSet[] sets;
        private DeploymentJsonKeyValue[] constraints;
        
        private DeploymentJsonNode(){}
        private DeploymentJsonNode(JSONObject obj){
            
            loadAttributes(obj.getJSONObject("attributes"));
            
            baseTag = getStringOrNull(obj,"base");
            loadMeta(obj.getJSONObject("meta"));
            parentTag = getStringOrNull(obj,"parent");
            loadPointers(obj.getJSONObject("pointers"));
            loadRegistry(obj.getJSONObject("registry"));
            loadSets(obj.getJSONObject("sets"));
            loadConstraints(obj.getJSONObject("constraints"));
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
            this.sets = getSets(o);
        }
        
        private void loadRegistry(JSONObject o){
            //TODO
        }
        
        private void loadPointers(JSONObject o){
            
            if(JSONObject.getNames(o)==null){
                return;
            }
            
            List<DeploymentJsonPointer> pointers = new ArrayList<>();
            for(String name:JSONObject.getNames(o)){
                DeploymentJsonPointer pointer = new DeploymentJsonPointer();
                pointers.add(pointer);
                
                if(o.isNull(name)){
                    continue;
                }
                
                Object pointerObject = o.get(name);
                
                if(pointerObject instanceof String){
                    DeploymentJsonPointer p = new DeploymentJsonPointer();
                    pointers.add(p);
                    
                    p.pointerName = name;
                    p.pointerValues = new DeploymentJsonPointerValue[]{new DeploymentJsonPointerValue()};
                    p.pointerValues[0].pointerValue = (String)pointerObject;
                } else {
                    throw new RuntimeException("unhandled case: " + pointerObject.getClass().getName());
                }
            }
            
            this.pointers = pointers.toArray(new DeploymentJsonPointer[]{});
            
        }
        
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
//                String value = o.getString(key);
                
                DeploymentTypeValue kv = new DeploymentTypeValue();
                kv.typeName = key;
                kv.value= o.get(key);
                
                System.out.printf("attribute value %s\n", kv.value.getClass().getName());//TODO
                
                kvs.add(kv);
//                System.out.printf("attributes: %s=%s\n", key,value);//TODO
            }
            
            attributes = kvs.toArray(new DeploymentTypeValue[]{});
            
        }
        
        private static DeploymentJsonNode[] parse(JSONObject obj){
            
            List<DeploymentJsonNode> nodes = new ArrayList<>();
            for(String name:JSONObject.getNames(obj)){
                
                nodes.add(new DeploymentJsonNode(obj.getJSONObject(name)));
                
            }
            
            return nodes.toArray(new DeploymentJsonNode[]{});
        }
        
    }
    
    public static class DeploymentJsonRelid{
        
        private String pointerId;
        private String tag;
        
        private DeploymentJsonRelid(){}
        
    }
    
    private static DeploymentJsonRelid[] getRelids(JSONObject o){
        List<DeploymentJsonRelid> relids = new ArrayList<>();
        
        for(String name:JSONObject.getNames(o)){
            DeploymentJsonRelid relid = new DeploymentJsonRelid();
            relids.add(relid);
            
            relid.pointerId = name;
            
            if(!o.isNull(name)){
                relid.tag = o.getString(name);
            }
        }
        
        return relids.toArray(new DeploymentJsonRelid[]{});
    }
    
    public static class DeploymentJsonMetaSheets{
        
        private DeploymentJsonMetaSheets(){}
        private DeploymentJsonMetaSheets(JSONObject obj){
            //TODO: example is empty so not much I can do here
        }
        
    }
    
    public static Object ingest(String json){

        System.out.printf("read the following json:\n%s\n", json);

        JSONObject obj = new JSONObject(json);
        
        return new DeploymentJson(obj);
        
        //root
//        "root": {
//            "path": "",
//            "guid": "03d36072-9e09-7866-cb4e-d0a36ff825f6"
//        },
        
        //containment
//        "containment": {
//            "0f808d8e-8f3a-1558-193d-e6d57694a531": {
//                "0a4ad592-09dd-56be-b0e0-def2be871cc5": {},
//                "0f7f33c5-62d4-d6f8-9afa-974a9947c140": {},
//                "26aed610-016f-1296-3bf5-e71f30492015": {},
//                "297ef813-2e8d-0e9c-8a52-5c877be1d47f": {},
//                "2ee36f69-f186-d1db-75c0-3aed1092424e": {},
//                "41b7ae61-af1f-ff06-4620-160d8a30492d": {},
//                "4b975f30-41e3-164e-876b-37adc6c365d5": {},
//                "4e360bd4-384b-6c28-6bc5-457cfbd76dac": {},
//                "59449563-0ba7-cc29-4746-6e87baece552": {},
//                "63368f5f-51d5-cb93-55d5-4ac1f586e436": {},
//                "638b7d42-2e96-9811-f626-141d804e2bad": {},
//                "71080d37-8a2b-58fe-0493-3d84dfbef222": {},
//                "7d00cb32-b502-7d64-3512-d58b5926a5dd": {},
//                "80427e96-72bd-93a4-217a-30e512545a90": {},
//                "849fb7de-dd1f-f0be-0fe3-799f01f6a075": {},
//                "8f983192-4405-d174-d99e-ece731c900ad": {},
//                "9f2d2c14-eca7-d038-5752-1e09dadef5c9": {},
//                "a9cbc2fd-33d8-3c07-dc9f-6f7309ae7271": {},
//                "aeda7e4f-1c68-9ee3-bae2-80a8e7fc1671": {},
//                "b12eaf52-29c6-328b-bc3f-ff4959a0f826": {},
//                "b1976139-6870-d91e-b67e-97c0bcdac5f7": {},
//                "c5aac0d1-9052-1bd9-7d72-0ecdf6dce3e7": {},
//                "c5c61472-04ab-810f-cafc-2ab2bd269fb2": {},
//                "cd822f95-e8c3-acab-cc51-625260804633": {},
//                "f6cbd8e7-37a7-3377-b5a0-372adb2d42a8": {}
//            },        
        
        //bases
//        "bases": {},
        
        //nodes
//        "nodes": {
//            "01f9a81e-af1e-9397-92e8-9efd906545c2": {
//                "attributes": {
//                    "name": "sa compiled"
//                },
//                "base": "4e360bd4-384b-6c28-6bc5-457cfbd76dac",
//                "meta": {},
//                "parent": "8748321b-0a05-febe-96be-b043f3f990d5",
//                "pointers": {
//                    "base": "4e360bd4-384b-6c28-6bc5-457cfbd76dac"
//                },
//                "registry": {
//                    "position": {
//                        "x": 169,
//                        "y": 261
//                    }
//                },
//                "sets": {},
//                "constraints": {}
//            },
//        ...
        
        
        //relids
//        "relids": {
//            "01f9a81e-af1e-9397-92e8-9efd906545c2": "t",
//            "03d36072-9e09-7866-cb4e-d0a36ff825f6": null,
//            "08d0c775-a49e-9ee4-b5f5-f3a34655f70e": "8",
//            "0a4ad592-09dd-56be-b0e0-def2be871cc5": "1733425449",
//            "0f7f33c5-62d4-d6f8-9afa-974a9947c140": "1218526787",
//            "0f808d8e-8f3a-1558-193d-e6d57694a531": "452381045",
//            "26aed610-016f-1296-3bf5-e71f30492015": "f",
//            "297ef813-2e8d-0e9c-8a52-5c877be1d47f": "1894554917",
//            ...
        
        
        //metaSheets
        
        
//        String pageName = obj.getJSONObject("pageInfo").getString("pageName");
//        
//        JSONArray arr = obj.getJSONArray("posts");
//        for (int i = 0; i < arr.length(); i++)
//        {
//            String post_id = arr.getJSONObject(i).getString("post_id");
//            ......
//        }
    }

}
