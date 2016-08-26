package com.securboration.example2.app;

import com.securboration.example.annotations.Dfu;
import com.securboration.example.annotations.FunctionalDfuAspect;
import com.securboration.example.annotations.SemanticTypeBinding;
import com.securboration.example.annotations.StatefulDfuAspect;

/**
 * A client to a remote server. For now, this is very sparse. A more thorough
 * example is needed to explore the details here.
 * 
 * @author jstaples
 *
 */
@Dfu
public class Client {

    @StatefulDfuAspect
    @SemanticTypeBinding(semanticType = "file://ontology.immortals.securboration.com/r1.0/Datatypes.owl#ServerEndpointUrl")
    private String serverUrl;

    @FunctionalDfuAspect
    public void transmit(
            @SemanticTypeBinding(semanticType = "file://ontology.immortals.securboration.com/r1.0/ServerModel1516.owl#JsonEncodedGeoSpatialCoordinate")
            byte[] data
            ) {
        //POST the provided data to the endpoint URL
    }

    
}
