package com.securboration.immortals.repo.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;

import com.securboration.immortals.j2t.analysis.JavaToOwl;
import com.securboration.immortals.j2t.analysis.JavaToTriplesConfiguration;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.repo.query.TriplesToPojo;
import com.securboration.immortals.repo.query.TriplesToPojo.SparqlPojoContext;

/**
 * Soup to nuts example of
 * <ol>
 * <li>defining a vocabulary using Java POJOs</li>
 * <li>creating instances of vocabulary concepts using Java POJOs</li>
 * <li>executing SPARQL queries against the resultant model</li>
 * <li>creating new knowledge that references the results of the queries</li>
 * </ol>
 * 
 * @author jstaples
 *
 */
public class PojoStressTest {
    
    public static void main(String[] args) throws Exception {
        
        new PojoStressTest().testPojoMapping();
        
    }
    
    @Test
    public void testPojoMapping() throws Exception {
    
        final FusekiClient client = 
                new FusekiClient("http://localhost:3030/ds");
        
        final ObjectToTriplesConfiguration o2t = 
                new ObjectToTriplesConfiguration("r2.0.0");
        
        final JavaToTriplesConfiguration j2t = 
                new JavaToTriplesConfiguration(o2t.getVersion());
        
        Model m = generateTestModel(o2t,j2t);
        
        System.out.println(OntologyHelper.serializeModel(m, "TURTLE", false));
        
        String graphName = "immortalsTest-"+UUID.randomUUID().toString();
        try{
            client.setModel(m, graphName);
            
            new PojoStressTest().runTest(client, graphName);
        } finally {
            client.deleteModel(graphName);
        }
    
    }
    
    
    
    private void runTest(
            final FusekiClient client,
            final String graphName
            ) throws IOException{
        ObjectToTriplesConfiguration o2t = 
                new ObjectToTriplesConfiguration("r2.0.0");
        
        SparqlPojoContext solutions = TriplesToPojo.sparqlSelect(
            getIndividualsQuery(graphName), 
            graphName, 
            client, 
            o2t
            );
        
        List<Vocabulary.BaseClass> values = new ArrayList<>();
        solutions.forEach(solution->{
            Object o = solution.get("x");
            
            System.out.printf("x is a %s\n", o.getClass().getName());
            
            Vocabulary.ContainerClass c = (Vocabulary.ContainerClass)o;
            
            for(Vocabulary.BaseClass v:c.values){
                System.out.printf("\t%s\n", v.getClass().getName());
                
                values.add(v);
                
                System.out.println("\t\t"+v.valuesSet);
                System.out.println("\t\t"+v.valuesList);
                
                Assert.assertTrue(new LinkedHashSet<>(
                    Arrays.asList(1,2,3,4,5,6,7,8,9,0)).containsAll(v.digitsSet)
                );
                System.out.println(v.valuesSet);
            }
        });
        
        
        {//create new knowledge
            Model newModel = ModelFactory.createDefaultModel();
            Vocabulary.ContainerClass newContainer = 
                    new Vocabulary.ContainerClass(
                        values.toArray(new Vocabulary.BaseClass[]{})
                        );
            newModel.add(ObjectToTriples.convert(o2t, newContainer));
            
            System.out.println("new model:");
            System.out.println(
                OntologyHelper.serializeModel(
                    newModel, 
                    "TURTLE", 
                    false
                    )
                );
        }
    }
    
    private static Model generateTestModel(
            ObjectToTriplesConfiguration objectToTriples,
            JavaToTriplesConfiguration javaToTriples
            ) throws ClassNotFoundException{
        Model m = ModelFactory.createDefaultModel();
        
        {//add schema
            JavaToOwl o = new JavaToOwl(javaToTriples);
            m.add(o.analyze(vocabularyClassess));
        }
        
        {//add individuals
            for(Object o:instances){
                m.add(ObjectToTriples.convert(objectToTriples, o));
            }
        }
        
        return m;
    }
    
    private static Collection<Class<?>> vocabularyClassess = Arrays.asList(
        Vocabulary.BaseClass.class,
        Vocabulary.ChildClassA.class,
        Vocabulary.ChildClassB.class,
        Vocabulary.ChildClassC.class,
        Vocabulary.ChildClassD.class,
        Vocabulary.ChildClassE.class,
        Vocabulary.ContainerClass.class,
        Vocabulary.AnEnum.class
    );
    
    private static Collection<Object> instances = Arrays.asList(
    
        new Vocabulary.ContainerClass(
            new Vocabulary.BaseClass[]{
                new Vocabulary.BaseClass("test1",-1234,"abcd".getBytes(),'q',false),
                new Vocabulary.ChildClassA("test2",+1234,"wxyz".getBytes(),'Q',true),
                new Vocabulary.ChildClassE("test3",+1234,"wxyz".getBytes(),'e',true),
            }
            )
        );
    
    public static class Vocabulary{
        
        public static enum AnEnum{
            A,
            B,
            C,
            D
        }
        
        @SuppressWarnings("unused")
        public static class BaseClass{
            private String text;
            private int number;
            private short shortNumber;
            private long longNumber;
            private double floatNumber;
            private byte byteNumber;
            private byte[] binary;
            private char character;
            private boolean bool;
            private Set<String> valuesSet;
            private List<String> valuesList;
            private AnEnum anEnum;
            private Class<? extends BaseClass> classField = this.getClass();
            private Class<?>[] otherClassField = new Class<?>[]{
                BaseClass.class,
                ChildClassB.class,
            };
            
            private AnEnum[] anEnumArray = AnEnum.values();
            private List<AnEnum> anEnumList = Arrays.asList(AnEnum.values());
            private List<Object> obfuscatedEnumList = Arrays.asList(AnEnum.values());
            
            private List<Integer> digitsList = Arrays.asList(1,2,3,4,5,6,7,8,9,0);
            private Set<Integer> digitsSet = new LinkedHashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9,0));
            
            
            public BaseClass(String text, int number, byte[] binary,
                    char character, boolean bool) {
                super();
                this.byteNumber = (byte)number;
                this.shortNumber = (short)number;
                this.longNumber = number;
                this.floatNumber = number;
                this.text = text;
                this.number = number;
                this.binary = binary;
                this.character = character;
                this.bool = bool;
                this.valuesSet = new HashSet<>(Arrays.asList(text+"_",text+"-",text+"!"));
                this.valuesList = Arrays.asList(text,text,text);
                
                if(bool){
                    this.anEnum = AnEnum.A;
                } else {
                    this.anEnum = AnEnum.D;
                }
            }
            
            public BaseClass(){}
        }
        
        public static class ChildClassA extends BaseClass{

            public ChildClassA(String text, int number, byte[] binary,
                    char character, boolean bool) {
                super(text, number, binary, character, bool);
            }
            public ChildClassA(){super();}
            //TODO
            //private String text;//note: overlaps with same named parent field
        }
        
        public static class ChildClassB extends BaseClass{

            public ChildClassB(String text, int number, byte[] binary,
                    char character, boolean bool) {
                super(text, number, binary, character, bool);
            }
            public ChildClassB(){super();}
        }
        public static class ChildClassC extends ChildClassB{

            public ChildClassC(String text, int number, byte[] binary,
                    char character, boolean bool) {
                super(text, number, binary, character, bool);
            }
            public ChildClassC(){super();}
        }
        public static class ChildClassD extends ChildClassC{

            public ChildClassD(String text, int number, byte[] binary,
                    char character, boolean bool) {
                super(text, number, binary, character, bool);
            }
            public ChildClassD(){super();}
        }
        public static class ChildClassE extends ChildClassD{
            public ChildClassE(String text, int number, byte[] binary,
                    char character, boolean bool) {
                super(text, number, binary, character, bool);
            }
            public ChildClassE(){super();}
        }
        
        public static class ContainerClass{
            private BaseClass[] values;

            public ContainerClass(BaseClass[] values) {
                super();
                this.values = values;
            }
            
            public ContainerClass(){}
        }
    }
    
    private String getIndividualsQuery(
            final String graphName
            ){
        String query = (
                "" +
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "SELECT ?x WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?GRAPH?> { \r\n" + 
                "        ?x a <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/repo/example#PojoStressTest.Vocabulary.ContainerClass> .\r\n" +
                "    } .\r\n" + 
                "}"
                ).replace("?GRAPH?", graphName);
        
        System.out.println(query);
        
        return query;
    }

}
