//package com.securboration.immortals.z;
//
//import com.securboration.immortals.ontology.functionality.imageprocessor.ImageProcessor;
//import com.securboration.immortals.ontology.functionality.locationprovider.LocationProvider;
//import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
//import com.securboration.immortals.ontology.property.Property;
//import com.securboration.immortals.ontology.resources.gps.properties.TrustedProperty;
//
//public class Example {
//    
//    @ConceptInstance
//    public static class Example1 extends DfuBooleanPropertyAssertion{
//        
//        public Example1(){
//            this.setFunctionality(
//                LocationProvider.class);
//            
//            this.setBoundProperties(new Property[]{
//                    getTrustedProperty()
//            });
//        }
//        
//        private Property getTrustedProperty(){
//            return new TrustedProperty();
//        }
//        
//    }
//    
//    @ConceptInstance
//    public static class Example2 extends DfuOperatorPropertyAssertion{
//        
//        public Example2(){
//            this.setFunctionality(ImageProcessor.class);
//            
//            this.setBoundProperties(new Property[]{
//                    getImageSizeProperty()
//            });
//            
//            this.setOperator(Operator.GREATER_THAN);
//        }
//        
//        private Property getImageSizeProperty(){
//            ImageSize s = new ImageSize();
//            
//            s.setHeightPixels(1024);
//            s.setWidthPixels(1024);
//            
//            return s;
//        }
//        
//    }
//    
//    
//    
//}
