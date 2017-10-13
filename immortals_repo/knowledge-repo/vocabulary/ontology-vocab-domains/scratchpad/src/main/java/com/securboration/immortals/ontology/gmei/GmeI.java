package com.securboration.immortals.ontology.gmei;

import com.securboration.immortals.ontology.android.AndroidPlatform;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.resources.network.NetworkConnection;
import com.securboration.immortals.ontology.server.ServerPlatform;

@Ignore
public class GmeI {
    
    public static final NetworkConnection networkConnection1 = 
            new NetworkConnection();
    
    public static final NetworkConnection networkConnection2 = 
            new NetworkConnection();
    
    public static final AndroidPlatform androidDevice1 = 
            ExampleHelper.getAndroidDeviceWithGpsReceiverAdvanced(networkConnection1);
    
    public static final AndroidPlatform androidDevice2 = 
            ExampleHelper.getAndroidDeviceWithGpsReceiverAdvanced(networkConnection2);
    
    public static final ServerPlatform server = 
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
