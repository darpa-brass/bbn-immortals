//package com.securboration.immortals.example.instantiation;
//
//import com.securboration.immortals.ontology.annotations.triples.Literal;
//import com.securboration.immortals.ontology.annotations.triples.Triple;
//import com.securboration.immortals.ontology.core.Resource;
//import com.securboration.immortals.ontology.cp.FunctionalitySpec;
//import com.securboration.immortals.ontology.cp.GmeInterchangeFormat;
//import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
//import com.securboration.immortals.uris.Uris.rdfs;
//
//@Triple(
//    predicateUri=rdfs.comment$,
//    objectLiteral=@Literal(
//        "Extension of Example1 consisting of an android device connected to " +
//        "military-grade GPS receiver hardware.  Also includes the satellite " +
//        "constellation and models of the radio links between satellites and " +
//        "the receiver."
//        )
//    )
//@ConceptInstance
//public class Example2_AdvancedAndroidGps extends GmeInterchangeFormat {
//    
//    public Example2_AdvancedAndroidGps(){
//        this.setAvailableResources(new Resource[]{
//                ExampleHelper.getAndroidDeviceWithGpsReceiverAdvanced()
//        });
//        
//        this.setFunctionalitySpec(new FunctionalitySpec[]{
//                ExampleHelper.getImageProcessorSpec()
//        });
//    }
//
//}
