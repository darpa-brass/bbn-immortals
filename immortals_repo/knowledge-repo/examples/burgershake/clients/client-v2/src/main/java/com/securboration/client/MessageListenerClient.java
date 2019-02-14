package com.securboration.client;

import org.example.burgershake.Meal;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import com.securboration.mls.wsdl.IngestMessageRequest;
import com.securboration.mls.wsdl.IngestMessageResponse;
import com.securboration.mls.wsdl.PingRequest;
import com.securboration.mls.wsdl.PingResponse;

public class MessageListenerClient extends WebServiceGatewaySupport {
    
    public MessageListenerClient(String serverEndpointUri) {
        serviceUri = serverEndpointUri;
    }
    
    private final String serviceUri;
    private final String pingActionUri = "http://mls.securboration.com/wsdl/ping";
    private final String ingestActionUri = "http://mls.securboration.com/wsdl/ingestMessage";
            
    
    public PingResponse ping() {
        PingRequest request = new PingRequest();
        request.setTimestamp(System.currentTimeMillis());
        
        
        PingResponse response = (PingResponse) 
                getWebServiceTemplate().marshalSendAndReceive(
                    serviceUri, 
                    request,
                    new SoapActionCallback(pingActionUri)
                    );
        
        return response;
    }
    
    public IngestMessageResponse ingestMessage(
            Meal message
            ) {
        IngestMessageRequest request = new IngestMessageRequest();
        request.setMessage(message);
        
        IngestMessageResponse response = (IngestMessageResponse) 
                getWebServiceTemplate().marshalSendAndReceive(
                    serviceUri, 
                    request,
                    new SoapActionCallback(ingestActionUri)
                    );
        
        return response;
    }
}
