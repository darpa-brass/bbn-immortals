//package com.securboration.immortals.o2t.entrypoint;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Arrays;
//
//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdf.model.ModelFactory;
//import org.apache.jena.rdf.model.Resource;
//import org.apache.jena.vocabulary.RDFS;
//
//import com.securboration.immortals.j2t.analysis.JavaToOwl;
//import com.securboration.immortals.j2t.analysis.JavaToTriplesConfiguration;
//import com.securboration.immortals.o2t.analysis.ObjectToTriples;
//
//public class Main {
//    
//    public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, IOException{
//        
//        final String testVersion = "rTEST";
//        
//        JavaToTriplesConfiguration config = 
//                new JavaToTriplesConfiguration(testVersion);
//        config.setClassPaths(
//                Arrays.asList(
//                        new File(".").getCanonicalPath()+"/target/classes"
//                        ));
//        
//        ObjectToTriples.convert(config,new TestArray()).write(System.out, "TURTLE");
//        
//        new JavaToOwl(config).analyze(
//                Arrays.asList(
//                        Main.class,
//                        TestArray.class,
//                        Test2.class
//                        )).write(System.out, "TURTLE");
//        
////        test(new Test1());
////        
////        test(getTestModel());
////        
////        test(null);
////        
////        test(Arrays.asList("1",2,'3',4d,new Object()));
////        
////        test(new Test4());
//        
//    }
//    
//    private static Model getTestModel(){
//        org.apache.jena.rdf.model.impl.ModelCom m = 
//                (org.apache.jena.rdf.model.impl.ModelCom)ModelFactory.createDefaultModel();
//        
////        System.out.printf("%s\n", m.getClass().getName());
//        
//        Resource r1 = m.getResource("http://securboration.com/immortals#Test1");
//        
//        Resource r2 = m.getResource("http://securboration.com/immortals#Test2");
//        
//        r1.addProperty(RDFS.subClassOf,r2);
//        r2.addProperty(RDFS.subClassOf,r1);
//        
//        return m;
//    }
//    
//    private static class TestArray{
//        String field1 = "aString";
//        int[] field2 = new int[]{1,2,3,4};
//        float[] field3 = null;
//        
//        Object[] field4 = new Object[]{1,"2",'3',4.0d,5f,6l,new Test2("test1"),new Test2("test2")};
//        
//        int[][] field5 = new int[][]{{1,2,3},{2,3,4},{3,4,5}};
//        
//        int[][][] field6 = new int[][][]{{{1,2,3}},{{2,3,4,5,6}},{{3,4,5}}};
//    }
//    
//    private static class Test1{
//        String stringField = "test";
//        
//        Object[] arrayField = {
//                1,
//                "two",
//                "two",
//                '3',
//                4d,
//                5f,
//                6l,
//                new Test3("testObject",new Test2("another test object"))
//        };
//    }
//    
//    private static class Test4 extends Test1{
//        String anotherField = "anothervalue";
//        
//        String stringField = "this field overrides parent";
//        
//        String f = super.stringField;
//    }
//    
//    private static class Test2{
//        final String name;
//        
//        private Test2(String name){
//            this.name = name;
//        }
//    }
//    
//    private static class Test3{
//        
//        final String name;
//        final Test2 parent;
//        final Test2 other;
//        final Test3 other2 = null;
//        final Object s;
//        
//        private Test3(String name,Test2 parent){
//            this.name = name;
//            this.parent = parent;
//            this.other = parent;
//            this.s = this;
//        }
//    }
//
//}
