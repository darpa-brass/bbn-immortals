package com.securboration.immortals.test.j2s.example;

import java.io.IOException;

import com.securboration.immortals.example.instantiation.ExampleHelper;
import com.securboration.immortals.j2s.mapper.PojoMappingContext;
import com.securboration.immortals.j2s.mapper.SemanticSyntax;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.cp.FunctionalitySpec;
import com.securboration.immortals.ontology.cp.GmeInterchangeFormat;
import com.securboration.immortals.ontology.cp.MissionSpec;

public class TestMain {
    
    /**
     * Example illustrating the use of the pojo to semantic mapper
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException{
        
        final SemanticSyntax syntax = SemanticSyntax.TURTLE;
        final Object gmeInterchangeInstance = generateExampleInterchangeModel();
        
        PojoMappingContext mappingContext = 
                PojoMappingContext.acquireContext("r2.0.0");
        
        mappingContext.addToModel(gmeInterchangeInstance);
        
        System.out.printf("Dumping model as [%s]:\n", syntax.getName());
        System.out.println(mappingContext.convertAdded(syntax));
    }
    
    private static GmeInterchangeFormat generateExampleInterchangeModel(){
        
        GmeInterchangeFormat g = new GmeInterchangeFormat();
        
        g.setAvailableResources(new Resource[]{
                ExampleHelper.getAndroidDeviceWithGpsReceiverAdvanced(),
                ExampleHelper.getAndroidDeviceWithGpsReceiverSimple(),
        });
        
        g.setFunctionalitySpec(new FunctionalitySpec[]{
                ExampleHelper.getTrustedLocationProviderSpec(),
                ExampleHelper.getImageProcessorSpec(),
                ExampleHelper.getSaDataProviderSpec(),
                
        });
        
        g.setMissionSpec(new MissionSpec[]{
                ExampleHelper.getImageReportRateSpec(),
                ExampleHelper.getPliReportRateSpec(),
                ExampleHelper.getNumClientsSpec()
        });
        
        ExampleHelper.setPrecedences(g.getFunctionalitySpec());
        ExampleHelper.setPrecedences(g.getMissionSpec());
        
        return g;
    }

}
