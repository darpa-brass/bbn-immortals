package com.securboration.immortals.o2t.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.json.JSONObject;

import com.securboration.immortals.deployment.pojos.DeploymentParser;
import com.securboration.immortals.deployment.pojos.FieldValue;
import com.securboration.immortals.deployment.pojos.ObjectInstance;
import com.securboration.immortals.deployment.pojos.TypeAbstraction;
import com.securboration.immortals.deployment.pojos.values.Value;
import com.securboration.immortals.deployment.pojos.values.ValueArray;
import com.securboration.immortals.deployment.pojos.values.ValueComplex;
import com.securboration.immortals.deployment.pojos.values.ValuePrimitive;
import com.securboration.immortals.i2t.ontology.ModelToTriples;
import com.securboration.immortals.i2t.ontology.ModelToTriples.NamingContext;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectNode;
import com.securboration.immortals.o2t.analysis.ObjectPrinter;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.etc.ExceptionWrapper;
import com.securboration.immortals.o2t.ontology.OntologyHelper;

public class DeploymentToTriplesTest {

    private static void log(String format, Object...args){
        System.out.println(String.format(format, args));
    }
    
    public static void main(String[] args) throws IOException{
        
        DeploymentParser p = getTestParser();
        
        System.out.println("printing types:");
        for(TypeAbstraction t:p.getTypes()){
            printObject(t);
            System.out.println();
        }
        
        System.out.println("printing instances:");
        for(ObjectInstance o:p.getInstances()){
            printObject(o);
            System.out.println();
        }
        
        ObjectToTriplesConfiguration c = getConfig();
        
        NamingContext n = new NamingContext();
        
        //raw JSON to model
        {
            final String pathToDeploymentJson = 
                    "../../../models/sample_android/resource/webgme/immortals_kds_dm_v02.json";
            
            final String json = 
                    FileUtils.readFileToString(new File(pathToDeploymentJson));
            
            JSONObject jsonObject = new JSONObject(json);
            
            Model m = ObjectToTriples.convert(c,jsonObject);
            
            System.out.println(
                    OntologyHelper.serializeModel(
                            m, 
                            "Turtle"
                            ));
        }
        
        //type hierarchy + instances
        {
            Model m = 
                    ModelToTriples.convert(
                            c, 
                            p.getTypes(), //type hierarchy
                            Arrays.asList(), //instances
                            n
                            );
            
            System.out.println(
                    OntologyHelper.serializeModel(
                            m, 
                            "Turtle"
                            ));
        }
        
        
        //just type hierarchy
        {
            Model m = 
                    ModelToTriples.convert(
                            c, 
                            p.getTypes(), //type hierarchy
                            Arrays.asList(), //instances
                            n
                            );
            
            System.out.println(
                    OntologyHelper.serializeModel(
                            m, 
                            "Turtle"
                            ));
        }
        
        //just instances
        {
            Model m = 
                    ModelToTriples.convert(
                            c, 
                            Arrays.asList(), //type hierarchy
                            p.getInstances(), //instances
                            n
                            );
            
            System.out.println(
                    OntologyHelper.serializeModel(
                            m, 
                            "Turtle"
                            ));
        }
        
        //run Adam's test instances
        {
            DeploymentParser parser = MockDeploymentParser.getParser();
            
            System.out.printf(
                    "generating model for [%d] types and [%d] instances\n", 
                    parser.getTypes().size(), 
                    parser.getInstances().size()
                    );
            
            Model m = 
                    ModelToTriples.convert(
                            c, 
                            parser.getTypes(), //type hierarchy
                            parser.getInstances(), //instances
                            n
                            );
            
            System.out.println(
                    OntologyHelper.serializeModel(
                            m, 
                            "Turtle"
                            ));
        }
    }
    
    private static ObjectToTriplesConfiguration getConfig(){
        ObjectToTriplesConfiguration c = new ObjectToTriplesConfiguration();
        c.setOutputFile(null);
        
        c.setTargetNamespace("http://vanderbilt.edu/immortals/ontology/r1.0.0");
        c.setNamespaceMappings(
                Arrays.asList(
                        "http://vanderbilt.edu/immortals/ontology/r1.0.0/edu/vanderbilt/immortals/models/deployment/com/securboration/test# deployment_spec",
                        "http://vanderbilt.edu/immortals/ontology/r1.0.0# gme_core"
                        ));
        
        return c;
    }
    
    public static void printObject(Object o){
//        if(false){
//            log(
//                 ReflectionToStringBuilder.toString(
//                    o, 
//                    MultilineRecursiveToStringWrapper.getStyle()));
//        }
        
        if(true){
            ExceptionWrapper.wrap(()->{
                ObjectNode n = ObjectNode.build(o);
                ObjectPrinter.getPrinterVisitor();
                
                n.accept(ObjectPrinter.getPrinterVisitor());
            });
        }
    }
    
    private static DeploymentParser getTestParser(){
        return new DeploymentParser(){
            
            TestTypes t = new TestTypes();

            @Override
            public Collection<ObjectInstance> getInstances() {
                return new ArrayList<>(t.instances);
            }

            @Override
            public Collection<TypeAbstraction> getTypes() {
                return new ArrayList<>(t.types);
            }

            @Override
            public void parse(String json) {
                log("parse called with the following json "
                        + "(it will be ignored): \n%s",json);
            }
            
        };
    }
    
//    private void init(){
//        TestTypeManager m = new TestTypeManager();
//        
//        m.createTestType()
//    }
    
    private static class TestTypes{
        
        private final Collection<TypeAbstraction> types = new ArrayList<>();
        private final Collection<ObjectInstance> instances = new ArrayList<>();
        
        //types
        private final TypeAbstraction root = 
                create("root",null);
        private final TypeAbstraction hardwarePlatform = 
                create("HardwarePlatform",root);
        private final TypeAbstraction mobileDevice = 
                create("MobileDevice",hardwarePlatform);
        private final TypeAbstraction androidDevice = 
                create("AndroidDevice",mobileDevice);
        private final TypeAbstraction ioResource = 
                create("IoResource",root);
        private final TypeAbstraction gpsReceiver = 
                create("GpsReceiver",ioResource);
        private final TypeAbstraction gpsEncryptionKey = 
                create("GpsEncryptionKey",root);
        
        //instances
        private final ObjectInstance android1 = 
                getAndroidDevice(
                        "marshmallow",
                        "6.0",
                        getGpsReceiver("honeywell","DRM-4000",5)
                        );
        
        private final ObjectInstance android2 = 
                getAndroidDevice(
                        "marshmallow",
                        "6.1",
                        getGpsReceiver("honeywell","DRM-3000",4)
                        );
        
        private final ObjectInstance android3 = 
                getAndroidDevice(
                        "lollipop",
                        "5.0",
                        getGpsReceiver("honeywell","DRM-3000",4)
                        );
        
        
        private ObjectInstance getGpsReceiver(
                String receiverVendor,
                String receiverModel,
                int numChannels
                ){
            ObjectInstance gpsEncryptionKey = getGpsEncryptionKey();
            
            return instantiate(
                    "a gps receiver instance",
                    gpsReceiver,
                    new FieldValue[] {//fields associated with GPS Receiver
                            createPrimitiveField("receiverVendor",receiverVendor),
                            createPrimitiveField("numChannels",numChannels), 
                            createPrimitiveField("receiverModel",receiverModel), 
                            
                            createComplexField(
                                    "encryptionConfiguration",
                                    gpsEncryptionKey.getInstanceType(),
                                    gpsEncryptionKey
                                    )
                            },
                    new FieldValue[] {//fields associated with IO resource
                            }
                    );
        }
        
        ObjectInstance encryptionKey = null;
        private ObjectInstance getGpsEncryptionKey(){
            
            if(encryptionKey != null){
                return encryptionKey;
            }
            
            ObjectInstance o = instantiate(
                    "a gps encryption key",
                    gpsEncryptionKey,
                    new FieldValue[] {//fields associated with GPS Receiver
                            createPrimitiveField("keyType","SAASM"),
                            createPrimitiveField("gdopUpper",1.05), 
                            createPrimitiveField("gdopLower",0.05)
                            }
                    );
            
            encryptionKey = o;
            return encryptionKey;
        }
        
        private ObjectInstance getAndroidDevice(
                String versionName,
                String version,
                ObjectInstance gpsReceiver
                ){
            return instantiate(
                    "android-"+UUID.randomUUID().toString(),
                    androidDevice,
                    new FieldValue[] {//fields associated with AndroidDevice
                            createPrimitiveField("androidVersionName",versionName),
                            createPrimitiveField("androidVersionTag",version), 
                            },
                    new FieldValue[] {//fields associated with MobileDevice
                            createComplexField("gpsReceiver",gpsReceiver.getInstanceType(),gpsReceiver)
                            },
                    new FieldValue[] {//fields associated with HardwarePlatform
                            }
                    );
        }
        
        /**
         * Example use: we have three classes
         * 
         * class A{
         *  static int ax = 99;
         *  int ay;
         * }
         * 
         * class B extends A{
         *  int bz;
         * }
         * 
         * class C extends B{
         *  int cq;
         * }
         * 
         * C instance = new C();
         * c.cq = 5;
         * c.bz = 4;
         * c.av = 3;
         * 
         * Would be represented as an instantiate call with fields being an 
         * array of length 3.  Fields[0] would contain cq.  Fields[1] would 
         * contain bz.  Fields[2] would contain av.  The TypeAbstraction for A
         * would contain ax.
         * 
         * @param name the name of the object to instantiate
         * @param type the type of object to instantiate
         * @param fields an array of fields.  Fields[0] are the field values 
         * defined in this object instance.  Fields[1] are the fields defined in
         * the parent.  Fields[2] are the fields defined in the grandparent. 
         * Etc.
         * @return an object instance
         */
        private ObjectInstance instantiate(
                final String name,
                TypeAbstraction type,
                FieldValue[]...fields
                ){
            ObjectInstance o = new ObjectInstance();
            ObjectInstance returnValue = o;
            
            o.setName(name);
            o.setComments("a test object instance");
            o.setInstanceType(type);
            if(fields.length > 0){
                o.setFieldValues(fields[0]);
                
//                for(int j=0;j<fields[0].length;j++){
//                    System.out.printf(
//                            "adding field [%s] to instance [%s] @ [%s]\n", 
//                            fields[0][j].getValue(),
//                            name,
//                            type.getTypeName());
//                }//TODO
            }
            
            type = type.getParent();
            for(int i=1;i<fields.length;i++){
                
//                for(int j=0;j<fields[i].length;j++){
//                    System.out.printf(
//                            "adding field %s to instance %s @ %s\n", 
//                            fields[i][j].getValue(),
//                            name,
//                            type.getTypeName());
//                }//TODO
                
                ObjectInstance parent = new ObjectInstance();
                
                parent.setInstanceType(type);
                o.setInstanceParent(parent);
                parent.setFieldValues(fields[i]);
                
                o = parent;
                type = type.getParent();
            }
            
            instances.add(returnValue);
            return returnValue;
        }
        
        private TypeAbstraction create(
                final String name,
                TypeAbstraction parent
                ){
            TypeAbstraction t = new TypeAbstraction();
            t.setName(name);
            t.setParent(parent);
            t.setName("com.securboration.test."+name);
            
            types.add(t);
            return t;
        }
        
        private static FieldValue createPrimitiveField(
                String name,
                Object o 
                ){
            FieldValue f = new FieldValue();
            f.setName(name);
            f.setValue(ValuePrimitive.instantiatePrimitive(o));
            
            return f;
        }
        
        private static FieldValue createComplexField(
                String name,
                TypeAbstraction t,
                ObjectInstance o
                ){
            FieldValue f = new FieldValue();
            f.setName(name);
            
            ValueComplex v = new ValueComplex();
            v.setType(t);
            v.setValue(o);
            
            f.setValue(v);
            
            return f;
        }
        
        private static FieldValue createArrayField(
                String name,
                TypeAbstraction arrayType,
                Object...values
                ){
            FieldValue f = new FieldValue();
            f.setName(name);
            
            ValueArray v = new ValueArray();
            v.setArrayType(arrayType);
            
            List<Value> arrayValues = new ArrayList<>();
            for(Object o:values){
                if(o instanceof ObjectInstance){
                    ObjectInstance i = (ObjectInstance)o;
                    ValueComplex value = new ValueComplex();
                    value.setType(((ObjectInstance) o).getInstanceType());
                    value.setValue(i);
                    arrayValues.add(value);
                } else {
                    arrayValues.add(ValuePrimitive.instantiatePrimitive(o));
                }
            }
            v.setArrayValues(arrayValues.toArray(new Value[]{}));
            f.setValue(v);
            
            return f;
        }
    }
    
}
