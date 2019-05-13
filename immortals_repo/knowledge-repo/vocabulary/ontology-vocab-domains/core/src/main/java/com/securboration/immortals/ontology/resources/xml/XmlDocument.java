package com.securboration.immortals.ontology.resources.xml;

public class XmlDocument extends StructuredDocument {
    
    private String schemaNamespace;
    private String schemaVersion;
    
    private String xmlVersion = "1.0";
    
    private String encoding;
    
    
    public XmlDocument(){
        
    }


    
    public String getEncoding() {
        return encoding;
    }


    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }


    
    public String getXmlVersion() {
        return xmlVersion;
    }


    
    public void setXmlVersion(String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }



    
    public String getSchemaNamespace() {
        return schemaNamespace;
    }



    
    public void setSchemaNamespace(String schemaNamespace) {
        this.schemaNamespace = schemaNamespace;
    }



    
    public String getSchemaVersion() {
        return schemaVersion;
    }



    
    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }
    
}
