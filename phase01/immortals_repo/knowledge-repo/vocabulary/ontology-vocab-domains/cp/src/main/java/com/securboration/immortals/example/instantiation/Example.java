//package com.securboration.immortals.example.instantiation;
//
//import com.securboration.immortals.ontology.annotations.triples.Literal;
//import com.securboration.immortals.ontology.annotations.triples.Triple;
//import com.securboration.immortals.ontology.core.Resource;
//import com.securboration.immortals.ontology.cp.FunctionalitySpec;
//import com.securboration.immortals.ontology.cp.GmeInterchangeFormat;
//import com.securboration.immortals.ontology.cp.MissionSpec;
//import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
//import com.securboration.immortals.ontology.pojos.markup.Ignore;
//import com.securboration.immortals.uris.Uris.rdfs;
//
//@Ignore
//@Triple(
//    predicateUri=rdfs.comment$,
//    objectLiteral=@Literal(
//        "This subgraph provides a representative template for the " +
//        "instantiations to be provided by WebGME"
//        )
//    )
//@ConceptInstance
//public class Example extends GmeInterchangeFormat {
//    
//    public Example(){
//        this.setAvailableResources(new Resource[]{
//                ExampleHelper.getAndroidDeviceWithGpsReceiverAdvanced(),
//                ExampleHelper.getAndroidDeviceWithGpsReceiverSimple(),
//        });
//        
//        this.setFunctionalitySpec(new FunctionalitySpec[]{
//                ExampleHelper.getTrustedLocationProviderSpec(),
//                ExampleHelper.getImageProcessorSpec(),
//                ExampleHelper.getSaDataProviderSpec(),
//                
//        });
//        
//        this.setMissionSpec(new MissionSpec[]{
//                ExampleHelper.getImageReportRateSpec(),
//                ExampleHelper.getPliReportRateSpec(),
//                ExampleHelper.getNumClientsSpec()
//        });
//        
//        ExampleHelper.setPrecedences(this.getFunctionalitySpec());
//        ExampleHelper.setPrecedences(this.getMissionSpec());
//    }
//
//}
