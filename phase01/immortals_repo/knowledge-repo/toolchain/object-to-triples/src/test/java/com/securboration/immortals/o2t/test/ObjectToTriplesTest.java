package com.securboration.immortals.o2t.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import org.objectweb.asm.Type;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;

public class ObjectToTriplesTest {
    
    @Test
    public void testObjectToTriplesMaps() throws IOException{
        
        testObjectToTriples(
                "nullMap",
                new MapObjects.NullMapObject()
                );
        
        testObjectToTriples(
                "trivialMap",
                new MapObjects.TrivialMapObject()
                );
        
        testObjectToTriples(
                "crazyMap",
                new MapObjects.PathologicalMapObject()
                );
    }
    
    @Test
    public void testObjectToTriplesLists() throws IOException{
        
        testObjectToTriples(
                "nullArray",
                new ArrayObjects.NullArrayObject()
                );
        
        testObjectToTriples(
                "trivialArray",
                new ArrayObjects.TrivialArrayObject()
                );
        
        testObjectToTriples(
                "pathologicalArrays",
                new ArrayObjects.PathologicalArrayObject()
                );
        
        testObjectToTriples(
                "simple array list",
                new CollectionObjects.ArrayListObject()
                );
        
        testObjectToTriples(
                "composite array list",
                new CollectionObjects.CompositeArrayListObject()
                );
        
        testObjectToTriples(
            "asm Type",
            Type.getType(this.getClass())
            );
    }
    
    private static void printDividerStart(String tag){
        System.out.println();
        for(int i=0;i<10;i++){
            System.out.printf("[%s] ",tag);
        }
        System.out.println();
    }
    
    private static void printDividerEnd(String tag){
        System.out.println();
        for(int i=0;i<10;i++){
            System.out.printf("[%s]^",tag);
        }
        System.out.println();
    }
    
    private static void testObjectToTriples(
            String tag,
            Object o
            ) throws IOException{
        
        final ObjectToTriplesConfiguration config = getConfig();
        
        printDividerStart(tag);
        
        Model m = 
                ObjectToTriples.convert(config, o);
        
        System.out.println(
                OntologyHelper.serializeModel(
                        m, 
                        "Turtle",
                        false
                        ));
        
        printDividerEnd(tag);
    }
    
    private static ObjectToTriplesConfiguration getConfig(){
        ObjectToTriplesConfiguration c = new ObjectToTriplesConfiguration("r1.0.0");
        
        c.setTargetNamespace("http://securboration.com/immortals/ontology/r1.0.0/test");
        
        return c;
    }
    
    
    private static class CollectionObjects{
        private static class CompositeArrayListObject{
            
            private List<List<Object>> list;
            
            CompositeArrayListObject()
            {
                List<Object> duplicated = 
                        Arrays.asList("second list");
                list = new ArrayList<>(
                        Arrays.asList(
                                new ArrayListObject().list,
                                duplicated,
                                duplicated,
                                duplicated
                                ));
            }
        }
        
        private static class ArrayListObject{
            private List<Object> list = 
                    new ArrayList<>(Arrays.asList(
                            "1",2,3,'4',"5",6l,7d));
        }
    }
    
    private static class ArrayObjects{
        private static class NullArrayObject{
            private int[] nullArray = null;
        }
        
        private static class TrivialArrayObject{
            private Object[] saneArray = new String[]{"item1","item2"};
        }
        
        private static class PathologicalArrayObject{
            private Object[] saneArray = new String[]{"item1","item2"};
            private Object[] crazyArray = new Object[]{new Object[]{new int[]{1,3,4},new String[]{},"this is a test".toCharArray()},new Object[]{new byte[]{5},new long[]{6}}};
        }
    }
    
    private static class MapObjects{
        private static class NullMapObject{
            private Map<?,?> nullMap = null;
            private String value = "test";
        }
        
        private static class TrivialMapObject{
            private Map<String,Object> m = new HashMap<>();
            
            {
                m.put("key1", "value1");
                m.put("key2", "value2");
                m.put("key3", "value3");
                
                m.put("key4", new Object[]{"value 4.1",4.2f,4.3d});
                m.put("key5", new Object[][]{{"value 5.1"},{5.2f},{5.3d}});
            }
        }
        
        private static class PathologicalMapObject{
            private Map<Map<String,String>,Map<String,String>> crazyMap = new HashMap<>();
            
            {
                Map<String,String> k1 = new HashMap<>();
                k1.put("k1k1", "k1v1");
                k1.put("k1k2", "k1v2");
                k1.put("k1k3", "k1v3");
                
                Map<String,String> v1 = new HashMap<>();
                v1.put("v1k1", "v1v1");
                v1.put("v1k2", "v1v2");
                v1.put("v1k3", "v1v3");
                
                crazyMap.put(k1, v1);
            }
        }
    }
    
    
    
}
