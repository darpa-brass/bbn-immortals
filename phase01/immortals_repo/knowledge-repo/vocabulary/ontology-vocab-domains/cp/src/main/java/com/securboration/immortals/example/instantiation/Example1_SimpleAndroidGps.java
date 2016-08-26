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
//        "A simple example consisting of an android device with commercial-" +
//        "grade GPS receiver hardware"
//        )
//    )
//@ConceptInstance
//public class Example1_SimpleAndroidGps extends GmeInterchangeFormat {
//    
//    public Example1_SimpleAndroidGps(){
//        this.setAvailableResources(new Resource[]{
//                ExampleHelper.getAndroidDeviceWithGpsReceiverSimple()
//        });
//        
//        this.setFunctionalitySpec(new FunctionalitySpec[]{
//                ExampleHelper.getTrustedLocationProviderSpec()
//        });
//    }
//
//}
