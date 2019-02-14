package com.securboration.immortals.swri.eval;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FusekiDatasetCudgel {
    
    public static void main(String[] args) throws IOException{
        FusekiDatasetCudgel.createDataset("http://localhost:3030", "ds");
    }
    
    public static void createDataset(
            final String fusekiEndpointUrl,
            final String datasetName
            ) throws IOException{
        
        EndpointHelper $ = new EndpointHelper();
        
        try{
            $.httpRequest(
                "GET", 
                fusekiEndpointUrl + "/$/stats/" + datasetName, 
                null
                );//this will 404 if the dataset does not exist
        } catch(RuntimeException|IOException e){
            //the attempt to retrieve the dataset stats failed, meaning it 
            // doesn't exist.  attempt to create it
            $.httpRequest(
                "POST", 
                fusekiEndpointUrl + "/$/datasets", 
                String.format("dbName=%s&dbType=mem",datasetName).getBytes(StandardCharsets.UTF_8),
                "Content-Type","application/x-www-form-urlencoded"
                );
        }
        
        //verify that the creation was a success
        $.httpRequest(
            "GET", 
            fusekiEndpointUrl + "/$/stats/" + datasetName, 
            null
            );//this will 404 if the dataset does not exist
    }

}
