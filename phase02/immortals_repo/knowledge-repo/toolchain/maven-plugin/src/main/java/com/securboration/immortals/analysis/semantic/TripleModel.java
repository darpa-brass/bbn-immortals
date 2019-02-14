package com.securboration.immortals.analysis.semantic;

import com.securboration.immortals.analysis.AnalysisBaseType;

public class TripleModel extends AnalysisBaseType {
    
    public TripleModel(){super();}
    public TripleModel(String s){super(s);}
    
    private String modelContent;
    private TripleModelLanguage language;
    
    public static enum TripleModelLanguage{
        RDF_XML("RDF/XML"),
        TURTLE("Turtle"),
        N3("N-Triples"),
        JSON_LD("JSON-LD"),
        RDF_JSON("RDF/JSON"),
        TriG("TriG"),
        N4("N-Quads"),
        TriX("TriX"),
        RDF_THRIFT("RDF Thrift")
        //TODO: other languages here
        ;
        
        private final String tag;
        private TripleModelLanguage(final String tag){
            this.tag = tag;
        }
        
        public String getLanguageTag(){
            return tag;
        }
    }

    public String getModelContent() {
        return modelContent;
    }

    public void setModelContent(String modelContent) {
        this.modelContent = modelContent;
    }

    public TripleModelLanguage getLanguage() {
        return language;
    }

    public void setLanguage(TripleModelLanguage language) {
        this.language = language;
    }
    
}
