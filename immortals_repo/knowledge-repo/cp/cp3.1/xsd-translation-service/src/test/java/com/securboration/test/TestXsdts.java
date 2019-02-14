package com.securboration.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class TestXsdts {
    
        
    public static void main(String[] args) throws Exception {
        
        final String eosServerUrl = "http://localhost:9999/xsdsts";
        
        
        final XsdtsClient client = new XsdtsClient(eosServerUrl);
        
        
        System.out.println("verifying connectivity to EOS endpoint @ " + eosServerUrl);
        {
            final long serverTime = client.ping();
            final long delta = System.currentTimeMillis() - serverTime;
            System.out.printf("\tping delta = %dms\n", delta);
        }
        
        final String[] schemaMagicStrings = new String[]{
                "xsi:schemaLocation=\"http://inetprogram.org/projects/MDL MDL_v0_8_17.xsd\"",
                "xsi:schemaLocation=\"http://inetprogram.org/projects/MDL MDL_v0_8_19.xsd\""
        };
        
        for(final String srcVersion:schemaMagicStrings){
            for(final String dstVersion:schemaMagicStrings){
                System.out.printf(
                    "testing [%s] --> [%s]\n", 
                    srcVersion,
                    dstVersion
                    );
                
                System.out.println(getTranslation(client,srcVersion,dstVersion));
            }
        }
    }
    
    private static DocumentSet getDocumentSetContainingMagicString(
            final String name, 
            final String magicString
            ){
        DocumentSet d = new DocumentSet();
        d.setName(name);
        
        Document doc = new Document();
        doc.setDocumentName(name + ".xsd");
        doc.setDocumentContent("the magic string is " + magicString);
        
        d.getDocuments().add(doc);
        
        return d;
    }
    
//    private static boolean isMdlVersion17(DocumentSet s){
//        return doesContainMagicString(s,"xsi:schemaLocation=\"http://inetprogram.org/projects/MDL MDL_v0_8_17.xsd\"");
//    }
//    
//    private static boolean isMdlVersion19(DocumentSet s){
//        return doesContainMagicString(s,"xsi:schemaLocation=\"http://inetprogram.org/projects/MDL MDL_v0_8_19.xsd\"");
//    }
    
    private static String getTranslation(
            final XsdtsClient client,
            final String srcSchema, 
            final String dstSchema
            ) throws UnsupportedEncodingException, JsonProcessingException, IOException{
        TranslationProblemDefinition p = new TranslationProblemDefinition();
        p.setDstSchema(getDocumentSetContainingMagicString("dst",dstSchema));
        p.setSrcSchema(getDocumentSetContainingMagicString("src",srcSchema));
        
        return client.getXsdTranslation(p);
    }

}
