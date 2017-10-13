package com.securboration.immortals.instantiation.delete;
//package com.securboration.immortals.instantiation;
//
//import java.io.File;
//import java.io.IOException;
//import java.lang.reflect.Array;
//import java.util.ArrayList;
//
//import org.apache.commons.compress.utils.IOUtils;
//
//import com.securboration.immortals.ontology.bytecode.AClass;
//import com.securboration.immortals.ontology.bytecode.AMethod;
//import com.securboration.immortals.ontology.bytecode.BytecodeArtifact;
//
//public class EntryPoint { 
//    
//    public static Object[] getObjects() throws IOException{
//        
//        return new Object[]{
//                DeploymentUmlIngestor.ingest(
//                        new String(
//                                readBytesFromClasspath(
//                                        "guest+sample_deployment_model_master.json"))),
//                
//                JarIngestor.ingest(
//                        readBytesFromClasspath("marti-core.jar"), 
//                        "com.bbn", 
//                        "marti-core", 
//                        "r_IMMoRTALS_1.0"),
//                
//                JarIngestor.ingest(
//                        readBytesFromClasspath("immortals-brainstorming-1.0-SNAPSHOT.jar"), 
//                        "com.securboration", 
//                        "example", 
//                        "1.0-SNAPSHOT"),
//                
////                new Test(),
////                getClassModel()
//        };
//        
//    }
//    
//    private static byte[] readBytesFromClasspath(String path) throws IOException{
//        return IOUtils.toByteArray(
//                EntryPoint.class.getClassLoader().getResourceAsStream(path));
//    }
//    
//    private static class Test{
//        private Object[][] array = new Object[][]{{1,2d,'3'},{"a","b",'c'},{new Class<?>[]{File.class,Array.class}}};
//        private Class<?> classs = ArrayList.class;
//    }
//    
//    private static Object getClassModel(){
//        
//        BytecodeArtifact artifact = new BytecodeArtifact();
//        
//        artifact.setBinaryForm("test".getBytes());
//        artifact.setClasses(new AClass[]{new AClass(),new AClass()});
//        
//        artifact.getClasses()[0].setClassName("class1");
//        artifact.getClasses()[0].setMethods(new AMethod[]{new AMethod()});
//        artifact.getClasses()[0].getMethods()[0].setMethodName("aMethod");
//        artifact.getClasses()[0].getMethods()[0].setOwner(artifact.getClasses()[0]);
//        artifact.getClasses()[1].setClassName("class2");
//        artifact.getClasses()[1].setMethods(new AMethod[]{artifact.getClasses()[0].getMethods()[0]});
////        artifact.setClasses
//        
//        return artifact;
//        
//    }
//    
//    
//
//}
