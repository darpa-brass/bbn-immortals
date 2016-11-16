//package com.securboration.immortals.example.complete;
//
//import com.securboration.immortals.example.complete.Example.DataTypes.SaImage;
//import com.securboration.immortals.ontology.android.AndroidPlatform;
//import com.securboration.immortals.ontology.constraint.PropertyConstraintType;
//import com.securboration.immortals.ontology.functionality.compression.AspectDeflate;
//import com.securboration.immortals.ontology.functionality.compression.Compressor;
//import com.securboration.immortals.ontology.functionality.dataproperties.Compressed;
//import com.securboration.immortals.ontology.functionality.datatype.Image;
//import com.securboration.immortals.ontology.resources.NetworkResource;
//import com.securboration.immortals.ontology.unix.UnixPlatform;
//
//import mil.darpa.immortals.annotation.dsl.ontology.action.Monolithic.DataflowAnalysis;
//import mil.darpa.immortals.annotation.dsl.ontology.action.Monolithic.DfuProvenance;
//import mil.darpa.immortals.annotation.dsl.ontology.action.Monolithic.NetworkReceive;
//import mil.darpa.immortals.annotation.dsl.ontology.action.Monolithic.NetworkSend;
//import mil.darpa.immortals.annotation.dsl.ontology.action.Monolithic.PropertyConstraintAssertion;
//
///**
// * Self-contained example illustrating how we should annotate a system for
// * communicating SA data
// * 
// * @author jstaples
// *
// */
//public class Example {
//    
//    /**
//     * The bag of DFUs that can be swapped and/or leveraged to transform data
//     * 
//     * @author jstaples
//     *
//     */
//    public static class Dfus{
//        
//        public static class Cipher{
//            
//        }
//        
//        public static class Archiver{
//            
//        }
//        
//        //lossy transformation
//        public static class ImageResampler{
//            
//        }
//    }
//    
//    public static class DataTypes{
//        public static class SaImage{
//            
//        }
//    }
//    
//    /**
//     * Models the behavior of an SA client. Periodically transmits SA data to a
//     * server.
//     * 
//     * Note: this class is *NOT* a DFU.  Rather, it transmits data whose bag of
//     * properties was affected by an upstream call to a DFU method.
//     * 
//     * @author jstaples
//     *
//     */
//    public static class SaClient{
//        
//        private SaService stub;
//        
//        @NetworkSend(/* static analysis artifact */
//            humanReadableDesc=
//                    "Indicates that this method, running on " +
//                    "an instance of AndroidPlatform, sends data type " +
//                    "Image via connection type NetworkResource (ie any " +
//                    "network resource) to any instance of UnixPlatform.  ",
//            
//            //the type of data being sent is Image (the only arg to this method)
//            dataTypeCommunicated=Image.class,
//            
//            //the send happens via any instance of a NetworkResource subclass
//            // in the deployment model
//            communicationNetworkTemplate=NetworkResource.class,
//            
//            //this node (the one doing the send) is an AndroidPlatform instance
//            // in the deployment model
//            thisNodeTemplate=AndroidPlatform.class,
//            
//            //the recipient node is a UnixPlatform instance in the deployment 
//            // model
//            remoteNodeTemplate=UnixPlatform.class
//            )
//        public void transmitImage(
//                @DataflowAnalysis(/* static analysis artifact */
//                    humanReadableDesc="Indicates that the image arg is " +
//                        "tainted by a compression DFU, resulting in the " +
//                        "unambiguous presence of a Compressed property",
//                    propertyConstraints={
//                        @PropertyConstraintAssertion(
//                            constraint=PropertyConstraintType.WILL_HAVE_PROPERTY,
//                            property=Compressed.class
//                        )
//                    },
//                    provenance=@DfuProvenance(
//                        classUsingFunctionality = ClientBusinessLogic.class,
//                        functionalityImplemented = Compressor.class,
//                        aspectOfFunctionality = AspectDeflate.class
//                    )
//                )
//                SaImage image
//                ){
//            stub.receiveSaImage(image);
//        }
//        
//        private static class ClientBusinessLogic{
//            //client business logic would go here...
//        }
//        
//    }
//    
//    /**
//     * Models SA server
//     * 
//     * @author jstaples
//     *
//     */
//    public static class SaService{
//        
//        
//        @NetworkReceive(
//            //the type of data being received is Image
//            dataTypeCommunicated=Image.class,
//            
//            //the receive happens via any instance of a NetworkResource subclass
//            // in the deployment model
//            communicationNetworkTemplate=NetworkResource.class,
//            
//            //this node (the one doing the rx) is a UnixPlatform instance
//            // in the deployment model
//            thisNodeTemplate=UnixPlatform.class,
//            
//            //the origin node is an AndroidPlatform instance in the deployment 
//            // model
//            remoteNodeTemplate=AndroidPlatform.class
//            )
//        public void receiveSaImage(SaImage image){
//            //server does something with the image
//        }
//        
//    }
//    
//    
//    
//
//}
