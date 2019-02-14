package com.securboration.immortals.adapt.engine;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.securboration.immortals.service.eos.nonapi.types.EvaluationContext;



@Component
public class AdaptationEngine {
    
    public AdaptationEngine(){
        
    }
    
    public void adapt(EvaluationContext context) throws IOException{
//        System.out.println("TODO: implement adaptation engine here");
//        
//        final File traceDictionary = 
//                new File(
//                    context.getEvaluationWorkingDir(),
//                    "ess/rampartData/dictionaries/client.dict"
//                    );
//        
//        if(!traceDictionary.exists()){
//            for(File f:FileUtils.listFiles(context.getEvaluationWorkingDir(), null, true)){
//                System.out.printf("\t%s\n", f.getAbsolutePath());
//            }//TODO
//            
//            throw new RuntimeException("dictionary does not exist: " + traceDictionary.getAbsolutePath());
//        }
//        
//        final File traceTarGz = 
//                new File(context.getEvaluationWorkingDir(),"ess/analysis.tar.gz");
//        
//        final File traceAnalysisOutputDir = 
//                new File(context.getEvaluationWorkingDir(),"ess/traceAnalysis");
//        
//        TraceAnalyzer.analyze(
//            traceTarGz,
//            traceDictionary,
//            traceAnalysisOutputDir, 
//            "com/securboration/client/ClientRunner clientAction ()V", 
//            "java/net/HttpURLConnection getOutputStream"
//            );
//        
//        
//        final String s = 
//                "2 invocations of java/io/OutputStream write ([B)V\r\n" + 
//                "    11 occurrences of\r\n" + 
//                "    1          METHOD_BEGIN    com/securboration/client/ClientRunner clientAction ()V\r\n" + 
//                "    2          METHOD_BEGIN    com/securboration/client/ClientRunner clientActionInternal (Lcom/securboration/client/test/Report;)V\r\n" + 
//                "    3          METHOD_BEGIN    com/securboration/client/ClientRunner clientAction (Ljava/lang/String;)V\r\n" + 
//                "    4          METHOD_BEGIN    com/securboration/client/MessageListenerClient ingestMessage (Lorg/inetprogram/projects/mdl/MDLRootType;)Lcom/securboration/mls/wsdl/IngestMessageResponse;\r\n" + 
//                "    5          METHOD_BEGIN    com/securboration/client/MessageListenerClient soapRequest (Ljava/lang/String;Ljava/lang/String;)[B\r\n" + 
//                "    6          METHOD_BEGIN    com/securboration/client/MessageListenerClient httpRequest (Ljava/lang/String;Ljava/lang/String;[B[Ljava/lang/String;)[B\r\n" + 
//                "    7          METHOD_BEGIN    java/io/OutputStream write ([B)V\r\n" + 
//                "    \r\n" + 
//                "2 invocations of java/io/InputStream read ([B)I\r\n" + 
//                "    99 occurrences of \r\n" + 
//                "    0          METHOD_BEGIN    com/securboration/client/ClientRunner clientAction ()V\r\n" + 
//                "    1          METHOD_BEGIN    com/securboration/client/ClientRunner clientActionInternal (Lcom/securboration/client/test/Report;)V\r\n" + 
//                "    2          METHOD_BEGIN    com/securboration/client/ClientRunner clientAction (Ljava/lang/String;)V\r\n" + 
//                "    3          METHOD_BEGIN    com/securboration/client/MessageListenerClient ingestMessage (Lorg/inetprogram/projects/mdl/MDLRootType;)Lcom/securboration/mls/wsdl/IngestMessageResponse;\r\n" + 
//                "    4          METHOD_BEGIN    com/securboration/client/MessageListenerClient soapRequest (Ljava/lang/String;Ljava/lang/String;)[B\r\n" + 
//                "    5          METHOD_BEGIN    com/securboration/client/MessageListenerClient httpRequest (Ljava/lang/String;Ljava/lang/String;[B[Ljava/lang/String;)[B\r\n" + 
//                "    6          METHOD_BEGIN    com/securboration/client/MessageListenerClient copy (Ljava/io/InputStream;Ljava/io/OutputStream;)V\r\n" + 
//                "    7          METHOD_BEGIN    java/io/InputStream read ([B)I"
//                ;
//        
//        File tmp = File.createTempFile(
//            "test", 
//            "txt",
//            context.getEvaluationWorkingDir()
//            );
//        tmp.deleteOnExit();
//        
//        FileUtils.writeStringToFile(tmp, s, StandardCharsets.UTF_8);
//        
//        
//        
//        DataflowAnalyzer.analyze(
//            new File("ess/client/target/ess.jar").getAbsolutePath(), 
//            //TODO: next path is hard coded--figure out a nice way to get it
//            new File("C:/Users/Securboration/.m2/repository/com/securboration/immortals-adsl-generate/r2.0.0/immortals-adsl-generate-r2.0.0.jar").getAbsolutePath(),//TODO 
//            tmp.getAbsolutePath()
//            );
    }
    
    

}



