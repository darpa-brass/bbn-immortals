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
import com.securboration.immortals.ontology.resources.gps.GpsSatelliteConstellation;
import com.securboration.immortals.ontology.resources.network.NetworkConnection;
import com.securboration.immortals.ontology.server.ServerPlatform;
import com.securboration.immortals.uris.Uris.rdfs;

@ConceptInstance
/**
 * Uber example.
 * 
 * @author Securboration
 */
public class GmeInterchangeFormatUberExample extends GmeInterchangeFormat {
    
    
    @Ignore
    private static class Instances{
        static final NetworkConnection networkConnection1 = 
                new NetworkConnection();
        
        static final AndroidPlatform androidDevice1 = 
                ExampleHelper.getAndroidDeviceTemplate(networkConnection1);
        
        static final ServerPlatform server = 
                ExampleHelper.getServer(networkConnection1);
        
        static final GpsSatelliteConstellation constellation = 
                ExampleHelper.getConstellation(0);
        
        static{
            networkConnection1.setLocalDevice(androidDevice1);
            networkConnection1.setRemoteDevice(server);
            networkConnection1.setOneWay(false);
            networkConnection1.setHumanReadableDescription(
                "a bidirectional connection between a MARTI server and ATAK " +
                "client");
        }
    }
    
    public GmeInterchangeFormatUberExample(){
        this.setSessionIdentifier("session-1234");
        
        this.setAvailableResources(new Resource[]{
                Instances.androidDevice1,
                Instances.server,
                Instances.constellation
        });
        
        this.setFunctionalitySpec(new FunctionalitySpec[]{
                ExampleHelper.getTrustedLocationProviderSpec(),
        });
        
        this.setMissionSpec(new MissionSpec[]{
                ExampleHelper.getPliReportRateSpec(Instances.androidDevice1),
                ExampleHelper.getNumClientsSpec(Instances.server),
                ExampleHelper.getImageReportRateSpec(Instances.androidDevice1),
                ExampleHelper.getMinimumBandwidth(Instances.networkConnection1),
        });
    }

}
