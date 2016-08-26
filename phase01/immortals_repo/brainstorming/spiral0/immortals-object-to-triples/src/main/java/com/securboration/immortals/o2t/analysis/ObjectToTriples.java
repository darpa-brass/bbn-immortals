package com.securboration.immortals.o2t.analysis;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.DataRange;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.objectweb.asm.Type;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.etc.ExceptionWrapper;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.annotations.triples.Triple;

/**
 * Utility class that converts an object instance to triples
 * 
 * @author jstaples
 *
 */
public class ObjectToTriples {

    private final ObjectToTriplesConfiguration context;
    
    private ObjectToTriples(ObjectToTriplesConfiguration context){
        this.context = context;
    }
    
    public static Model convert(
            ObjectToTriplesConfiguration config,
            Object o
            ){
        AnalysisContext analysisContext = 
                new AnalysisContext(config.getNamingContext());
        
        config.getLog().info("about to analyze an object of type " + o.getClass().getName() + "...");
        
        ObjectToTriples converter = new ObjectToTriples(config);
        
        
        ExceptionWrapper.wrap(() -> {
            
            converter.analyze(o,analysisContext);
            
            OntologyHelper.addMetadata(config,analysisContext.model,config.getTargetNamespace());
            OntologyHelper.setNamespacePrefixes(config,analysisContext.model);               
            
            final String serializedOntology = 
                    OntologyHelper.serializeModel(
                            analysisContext.model, 
                            config.getOutputLanguage()
                            );
            
            if(config.getOutputFile() == null){
                config.getLog().info("skipping serialization to file");
            } else {
                config.getLog().info(
                        "writing result to " + config.getOutputFile().getAbsolutePath());
    
                FileUtils.writeStringToFile(
                        config.getOutputFile(), 
                        serializedOntology);
            }
        });
        
        return analysisContext.model;
    }
    
    private static Triple[] getTriplesFromClass(Class<?> c){
        return c.getAnnotationsByType(Triple.class);
    }
    
    private void analyze(Object o,AnalysisContext c) throws IllegalArgumentException, IllegalAccessException{
        
        ObjectNode object = ObjectNode.build(o);
        
        object.accept(new ObjectVisitor(c));
    }
    
    public static class NamingContext{
        private final Map<Object,String> objectsToNames = new HashMap<>();
        
        public String getNameForObject(
                Object o,
                String classUri
                ){
            if(objectsToNames.containsKey(o)){
                return objectsToNames.get(o);
            }
            
            String name = classUri+"-"+UUID.randomUUID();
            
            objectsToNames.put(o, name);
            
            return name;
        }
    }
    
    private static class AnalysisContext{
        
        final OntModel model;
        
        private final NamingContext namingContext;
        Map<Object,Resource> instancesToResources = new HashMap<>();
        
        AnalysisContext(NamingContext namingContext){
            this.namingContext = namingContext;
            this.model = ModelFactory.createOntologyModel();
        }
        
        
        
        private Resource findResourceForInstance(Object instance){
            return instancesToResources.get(instance);
        }
        
        private void defineInstance(Object instance, Resource r){
            instancesToResources.put(instance, r);
        }
        
    }
    
    
    
    private class ObjectVisitor implements ObjectNodeVisitor{
        private final AnalysisContext analysisContext;
        
        public ObjectVisitor(AnalysisContext c) {
            super();
            this.analysisContext = c;
        }
        
        private Resource getResourceForClass(Class<?> c){
            
            return getResourceForType(Type.getType(c));
        }
        
        private Resource getResourceForType(Type t){
            
            final String internalName = t.getClassName();
            
            return OntologyHelper.getResourceForType(
                    context,
                    analysisContext.model,
                    internalName
                    );
        }
        
        private Literal getPrimitiveLiteral(Object instance){
            Resource primitive = 
                    OntologyHelper.getTypeMappings(analysisContext.model).get(
                            Type.getType(instance.getClass()));
            
            if(primitive != null){
                return analysisContext.model.createTypedLiteral(instance);
            }
            
            return null;
        }
        
        private DataRange getRangeForEnum(Class<?> c){
            
            RDFList list = analysisContext.model.createList();
            
            for (Object enumerationValue:c.getEnumConstants()) {
                Enum<?> enumeration = (Enum<?>)enumerationValue;
                
                list = list.cons(analysisContext.model.createTypedLiteral(enumeration.name()));
            }
            
            return analysisContext.model.createDataRange(list);
        }
        
        private RDFNode instantiate(Object instance){
            
            //treat classes like primitives
            if(instance instanceof Class){
                return getResourceForClass((Class<?>)instance);
            }
            
            //check whether it's a primitive type
            {
                RDFNode primitive = 
                        getPrimitiveLiteral(instance);
                
                if(primitive != null){
                    //return the primitive as a literal
                    return primitive;
                }
            }
            
            //now we know it's not a primitive, but it could be something we've 
            // seen before
            {
                Resource existingDefinition = 
                        analysisContext.findResourceForInstance(instance);
                
                if(existingDefinition != null){
                    return existingDefinition;
                }
            }
            
            // now we know it's not a primitive and it's not something we've
            // seen before so we'll go ahead and instantiate it
            Type t = Type.getType(instance.getClass());
            
            Class<?> c = instance.getClass();
            
            if(c.isEnum()){
                //it's an enum
                
                Literal literal = 
                        analysisContext.model.createTypedLiteral(
                                ((Enum<?>)instance).name());
                
                return literal;
            }
            
            Resource classInSchema = getResourceForClass(c);
            
            final String resourceName = 
                    analysisContext.namingContext.getNameForObject(
                            instance,
                            classInSchema.getURI()
                            );
            Resource r = 
                    analysisContext.model.createResource(resourceName);
            analysisContext.defineInstance(instance, r);
            r.addProperty(RDF.type,classInSchema);
            
            if(t.getSort()==Type.ARRAY){
                r.addProperty(RDFS.comment,"an instance of array type " + c.getName() + " with hash=" + instance.hashCode());
                
                Property p = 
                        analysisContext.model.createProperty(
                                context.getTargetNamespace()+"#hasElement");
                
                Property elementTypeProperty = 
                        analysisContext.model.createProperty(
                                context.getTargetNamespace()+"#hasElementType");

                for(int i=0;i<Array.getLength(instance);i++){
                    Object element = Array.get(instance, i);
                    
                    if(element != null){
                        analysisContext.model.add(
                                r,
                                p,
                                instantiate(element));
                        
                        if(r.hasProperty(elementTypeProperty)){
                            continue;
                        }
                        
                        Type elementType = t.getElementType();
                        
                        if(t.getDimensions() > 1){
                            elementType = Type.getType(t.getInternalName().substring(1));
                        }
                        
                        Resource elementTypeResource = 
                                isEnum(element)?
                                        getRangeForEnum(element.getClass())
                                        :
                                        getResourceForType(elementType);
                        
                        analysisContext.model.add(
                                r,
                                elementTypeProperty,
                                elementTypeResource);
                    }
                }
            }
            
            // it's not a primitive and it's not an array so proceed with object
            // instantiation
            else if(t.getSort()==Type.OBJECT){
                r.addProperty(RDFS.comment,"an instance of object type " + c.getName() + " with hash=" + instance.hashCode());
            }
            
            return r;
            
        }

        @Override
        public void visitPrimitiveField(
                ObjectNode primitiveFieldOwner,
                ObjectNode primitiveFieldValue
                ){
            
            //do nothing
            
        }
        
        private boolean isEnum(Object o){
            return o.getClass().isEnum();
        }
        
        @Override
        public void visitObjectField(
                ObjectNode objectFieldOwner,
                ObjectNode objectFieldValue
                ){
            
            if(objectFieldValue.getValue() == null){
                return;
            }
            
            if(isEnum(objectFieldOwner.getValue())){
                return;
            }
            
            Resource fieldOwner = 
                    instantiate(objectFieldOwner.getValue()).asResource();
            
            RDFNode fieldValue = 
                    instantiate(objectFieldValue.getValue());
            
            Property p = OntologyHelper.getPropertyForField(
                    context,
                    analysisContext.model,
                    objectFieldValue.getFieldName(), 
                    Type.getType(objectFieldValue.getPossibleType()));
            
            if(objectFieldValue.getFieldName() == null){
                throw new RuntimeException("shouldn't happen");
            }
            
            Property property = analysisContext.model.createProperty(p.getURI());
            
            analysisContext.model.add(fieldOwner,property,fieldValue);
        }
        
        @Override
        public ArrayElementVisitor visitArrayField(
                ObjectNode arrayFieldOwner,
                ObjectNode arrayFieldValue
                ){
            return new ElementVisitor(analysisContext);
        }
    }
    
    /**
     * An API invoked while walking through an array. See
     * {@link ObjectNode#accept} and {@link ObjectNodeVisitor#visitArrayField}
     * 
     * @author jstaples
     *
     */
    private class ElementVisitor implements ArrayElementVisitor{
        
        public ElementVisitor(AnalysisContext c) {
            super();
        }
        
        @Override
        public void visitArrayElement(
                ObjectNode array,
                final int index,
                ObjectNode elementAtIndex){}
    }
    
}
