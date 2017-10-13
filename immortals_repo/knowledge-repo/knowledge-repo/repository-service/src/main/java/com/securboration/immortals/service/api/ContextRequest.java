package com.securboration.immortals.service.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * Created by CharlesEndicott on 6/22/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContextRequest {
    
    private ArrayList<String> graphUris;
    
    public void setGraphUris(ArrayList<String> _graphUris) {
        graphUris = _graphUris;
    }
    
    public ArrayList<String> getGraphUris() {
        return graphUris;
    }
    
}
