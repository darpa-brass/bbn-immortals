package com.securboration.immortals.j2s.mapper;

/**
 * Enumeration of syntaxes the mapper can write models into
 * 
 * @author jstaples
 *
 */
public enum SemanticSyntax {
    
    JSON_LD("JSON-LD",".jsonld"),
    
    TURTLE("Turtle",".jsonld"),
    
    RDF_JSON("RDF/JSON",".rj"),
//    RDF_THRIFT("RDF Thrift",".rt"),
    RDF_XML("RDF/XML",".rdf"),
    
    N_TRIPLES("N-Triples",".nt"),
//    N_QUADS("N-Quads",".nq"),
    
//    TRIG("TriG",".trig"),
//    TRIX("TriX",".trix"),
    
    ;
    
    private final String name;
    private final String suffix;
    
    private SemanticSyntax(final String name, final String suffix){
        this.name = name;
        this.suffix = suffix;
    }

    
    public String getName() {
        return name;
    }

    
    public String getSuffix() {
        return suffix;
    }


}
