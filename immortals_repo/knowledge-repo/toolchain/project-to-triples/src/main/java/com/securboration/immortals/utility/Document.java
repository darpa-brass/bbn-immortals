package com.securboration.immortals.utility;

public class Document {

    private String documentName;
    private String documentContent;
    private boolean primarySchemaDoc;

    public Document(){

    }


    public String getDocumentName() {
        return documentName;
    }


    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }


    public String getDocumentContent() {
        return documentContent;
    }


    public void setDocumentContent(String documentContent) {
        this.documentContent = documentContent;
    }


    
    public boolean isPrimarySchemaDoc() {
        return primarySchemaDoc;
    }


    
    public void setPrimarySchemaDoc(boolean primarySchemaDoc) {
        this.primarySchemaDoc = primarySchemaDoc;
    }



}


