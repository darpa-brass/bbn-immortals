//package com.securboration.immortals.deployment.Main;
//
//import java.io.File;
//import java.io.IOException;
//
//import org.apache.commons.io.FileUtils;
//
//import com.securboration.immortals.deployment.model.JsonToDiagram;
//import com.securboration.immortals.deployment.parser.DeploymentJson;
//import com.securboration.immortals.deployment.parser.DeploymentUmlIngestor;
//import com.securboration.immortals.o2t.analysis.ObjectNode;
//import com.securboration.immortals.o2t.analysis.ObjectPrinter;
//import com.securboration.immortals.o2t.etc.ExceptionWrapper;
//import com.securboration.immortals.ontology.deployment.uml.Diagram;
//
///**
// * 
// * 
// * 
// * @author jstaples
// *
// */
//public class Main2 {
//    
//    public static void main(String[] args) throws IOException, IllegalArgumentException, IllegalAccessException{
//        final String pathToDeploymentJson = 
//                "../../../models/sample_android/resource/webgme/immortals_spiral1_deployment_master.json";
//        
//        DeploymentJson parsed = 
//                DeploymentUmlIngestor.ingest(
//                        FileUtils.readFileToString(
//                                new File(pathToDeploymentJson)));
//        
//        Diagram converted = JsonToDiagram.convert(
//                "guest+sample_deployment_model_master.json",
//                parsed);
//        
//        printObject(converted);
//        
////        ObjectToTriplesConfiguration config = 
////                new ObjectToTriplesConfiguration();
////        config.setNamespaceMappings(
////                Arrays.asList(
////                        "http://darpa.mil/immortals/ontology/r1.0.0# IMMoRTALS"));
////        config.setTargetNamespace("http://darpa.mil/immortals/ontology/r1.0.0");
////        config.setOutputFile(null);
////        config.setTrimPrefixes(
////                Arrays.asList(
////                        "com/securboration/immortals/ontology"));
////        
////        System.out.println(
////                OntologyHelper.serializeModel(
////                        ObjectToTriples.convert(config, converted),
////                        "Turtle"));
////        
////        FileUtils.writeStringToFile(
////                new File("guest+sample_deployment_model_master.ttl"),
////                OntologyHelper.serializeModel(
////                        ObjectToTriples.convert(config, converted),
////                        "Turtle"));
////        
////        
////        DiagramToSourceConfig d2sc = 
////                new DiagramToSourceConfig(
////                        "com.securboration.immortals.deployment.autogen",
////                        new File("./target/generated/d2s/java"));
////        DiagramToSource.generateSource(d2sc, converted);
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
