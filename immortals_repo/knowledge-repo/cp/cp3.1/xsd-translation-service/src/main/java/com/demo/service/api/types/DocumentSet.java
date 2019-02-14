package com.demo.service.api.types;

import java.util.ArrayList;
import java.util.List;

public class DocumentSet {
    
    private String name;
    
    private final List<Document> documents = new ArrayList<>();

    public DocumentSet(){
        
    }
    
    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }

    
    public List<Document> getDocuments() {
        return documents;
    }

}
