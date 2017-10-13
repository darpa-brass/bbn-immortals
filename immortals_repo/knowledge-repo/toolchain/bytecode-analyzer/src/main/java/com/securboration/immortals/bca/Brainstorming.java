package com.securboration.immortals.bca;

import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.Image;

public class Brainstorming {
    
    public static class MergedAnalysisGraph{
        
    }
    
//    public static void businessLogic(){
//        String image = FileUtils.readFileToString(.image..);
//    }
    
    public static void f(
            String name, 
            //inferred @Image
            String image
            ){
        
        byte[] imageBytes = image.getBytes();
        
        g(imageBytes);
        
    }
    
    @Image
    public static byte[] g(
            @Image
            byte[] image
            ){
        return image;
    }

}
