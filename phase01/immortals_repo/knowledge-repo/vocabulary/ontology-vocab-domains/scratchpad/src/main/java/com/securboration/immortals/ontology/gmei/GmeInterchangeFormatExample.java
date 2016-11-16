package com.securboration.immortals.ontology.gmei;

import com.securboration.immortals.ontology.android.AndroidPlatform;
import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.cp.FunctionalitySpec;
import com.securboration.immortals.ontology.cp.GmeInterchangeFormat;
import com.securboration.immortals.ontology.cp.MissionSpec;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.resources.network.NetworkConnection;
import com.securboration.immortals.ontology.server.ServerPlatform;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Simple example."
        )
    )
@ConceptInstance
public class GmeInterchangeFormatExample extends GmeInterchangeFormat {
    
    
    @Ignore
    private static class Instances{
        static final NetworkConnection networkConnection1 = 
                new NetworkConnection();
        
        static final NetworkConnection networkConnection2 = 
                new NetworkConnection();
        
        static final AndroidPlatform androidDevice1 = 
                ExampleHelper.getAndroidDeviceWithGpsReceiverAdvanced(networkConnection1);
        
        static final AndroidPlatform androidDevice2 = 
                ExampleHelper.getAndroidDeviceWithGpsReceiverAdvanced(networkConnection2);
        
        static final ServerPlatform server = 
                ExampleHelper.getServer(networkConnection1,networkConnection2);
        
        static{
            networkConnection1.setLocalDevice(androidDevice1);
            networkConnection1.setRemoteDevice(server);
            networkConnection1.setOneWay(false);
            
            networkConnection2.setLocalDevice(androidDevice2);
            networkConnection2.setRemoteDevice(server);
            networkConnection2.setOneWay(false);
        }
    }
    
    public GmeInterchangeFormatExample(){
        this.setAvailableResources(new Resource[]{
                Instances.androidDevice1,
                Instances.androidDevice2,
                Instances.server
        });
        
        this.setFunctionalitySpec(new FunctionalitySpec[]{
                ExampleHelper.getTrustedLocationProviderSpec(),
        });
        
        this.setMissionSpec(new MissionSpec[]{
                
                ExampleHelper.getPliReportRateSpec(Instances.androidDevice2),
                ExampleHelper.getNumClientsSpec(Instances.server),
                ExampleHelper.getImageReportRateSpec(Instances.androidDevice2),
                ExampleHelper.getMinimumBandwidth(Instances.networkConnection2),
                
//                ExampleHelper.getPliReportRateSpec(Instances.androidDevice1),
//                ExampleHelper.getPliReportRateSpec(Instances.androidDevice2),
//                ExampleHelper.getNumClientsSpec(Instances.server),
//                ExampleHelper.getImageReportRateSpec(Instances.androidDevice1),
//                ExampleHelper.getImageReportRateSpec(Instances.androidDevice2),
//                ExampleHelper.getMinimumBandwidth(Instances.networkConnection1),
//                ExampleHelper.getMinimumBandwidth(Instances.networkConnection2),
        });
    }

}
