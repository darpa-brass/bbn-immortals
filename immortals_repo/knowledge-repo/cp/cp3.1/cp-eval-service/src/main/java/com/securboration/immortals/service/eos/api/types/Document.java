package com.securboration.immortals.service.eos.api.types;

public class Document extends EosType {
    
    private String documentContent;
    private String documentName;
    
    public Document(){
        
    }
    
    public Document(String s){
        super(s);
    }

    
    public String getDocumentContent() {
        return documentContent;
    }

    
    public void setDocumentContent(String documentContent) {
        this.documentContent = documentContent;
    }

    
    public String getDocumentName() {
        return documentName;
    }

    
    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
    
    
    

}
