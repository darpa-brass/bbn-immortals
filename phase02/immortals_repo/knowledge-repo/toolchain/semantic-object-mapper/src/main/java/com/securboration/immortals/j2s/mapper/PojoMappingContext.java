package com.securboration.immortals.j2s.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;

/**
 * Used to map Plain Old Java Objects (POJOs) to semantic constructs.  The usage
 * pattern for this component is as follows:
 * <ol>
 * <li>{@link #acquireContext(String)} is called once to obtain a context.  
 * <b>Note</b>: the version name must be consistent with the ontology URIs.  
 * E.g., 
 * <a href="http://darpa.mil/immortals/ontology/r2.0.0/bytecode#"/>
 * http://darpa.mil/immortals/ontology/r2.0.0/bytecode#</a> has a version of 
 * r2.0.0</li>
 * <li>{@link #addToModel(Object)} is called one or more times on arbitrary Java 
 * Objects.  Although it will work on any Java object no matter how complex, 
 * this method is typically only used with instances of 
 * com.securboration.immortals.ontology.* classes because these classes are
 * modeled in the various IMMoRTALS domain ontologies</li>
 * <li>{@link #convertAdded(SemanticSyntax)} is invoked one or more times to 
 * write the resultant model to the indicated syntax</li>
 * </ol>
 * 
 * @see {@link com.securboration.immortals.j2s.mapper.SemanticSyntax}
 *  
 * @author jstaples
 *
 */
public class PojoMappingContext {
    
    private final ObjectToTriplesConfiguration config;
    private final Collection<Object> objectsToMap = new ArrayList<>();
    
    private PojoMappingContext(final String ontologyVersion){
        this.config = new ObjectToTriplesConfiguration(ontologyVersion);
    }
    
    /**
     * Acquire a context for building a model.
     * 
     * @param ontologyVersion
     *            e.g., r2.0.0
     * @return a context to which POJO concept instances can be added
     */
    public static PojoMappingContext acquireContext(final String ontologyVersion){
        return new PojoMappingContext(ontologyVersion);
    }
    
    /**
     * Add the indicated object to the underlying model
     * 
     * @param o
     *            an object to add. Typically a
     *            com.securboration.immortals.ontology.* class instance
     */
    public void addToModel(Object o){
        objectsToMap.add(o);
    }
    
    /**
     * Convert all previously added objects into a single printable
     * representation, which is returned
     * 
     * @param outputFormat
     *            the syntax to serialize the underlying model into
     * @return a String representing the serialization of the underlying model
     *         in the indicated syntax
     * @throws IOException
     *             if something goes wrong
     */
    public String convertAdded(SemanticSyntax outputFormat) throws IOException{
        return OntologyHelper.serializeModel(
            getCurrentModel(), 
            outputFormat.getName(), 
            false
            );
    }
    
    public Model getCurrentModel(){
        Model master = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        OntModelSpec s;
        for(Object o:objectsToMap){
            master.add(ObjectToTriples.convert(config, o));
        }
        
        return master;
    }
    
    public ObjectToTriplesConfiguration getConfiguration(){
        return config;
    }

}
