package com.securboration.server;

import org.inetprogram.projects.mdl.MDLRootType;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.securboration.mls.wsdl.IngestMessageRequest;
import com.securboration.mls.wsdl.IngestMessageResponse;
import com.securboration.mls.wsdl.PingRequest;
import com.securboration.mls.wsdl.PingResponse;



@Endpoint
public class ServerEndpoint {
    
    private static final String NS = 
//            "http://mls.securboration.com/wsdl/MessageListenerServiceTypes";
//            "http://inetprogram.org/projects/MDL";
            "http://mls.securboration.com/wsdl";
    
    @PayloadRoot(
        namespace = NS, 
        localPart = "pingRequest"
        )
    @ResponsePayload
    public PingResponse ping(
            @RequestPayload 
            PingRequest request
            ) {
        PingResponse response = new PingResponse();
        response.setDelta(System.currentTimeMillis() - request.getTimestamp());
        return response;
    }
    
    @PayloadRoot(
        namespace = NS, 
        localPart = "ingestMessageRequest"
        )
    @ResponsePayload
    public IngestMessageResponse ingestMessage(
            @RequestPayload
            IngestMessageRequest ingestMessageRequest
            ) {
        //TODO: do something interesting here, for now this is an echo 
        
        MDLRootType incomingMessage = ingestMessageRequest.getMessage();
        
        IngestMessageResponse response = new IngestMessageResponse();
        response.setMessage(incomingMessage);
        
        return response;
    }

}
