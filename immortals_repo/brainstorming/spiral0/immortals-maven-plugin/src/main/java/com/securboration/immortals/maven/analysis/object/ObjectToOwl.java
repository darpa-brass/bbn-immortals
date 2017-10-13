package com.securboration.immortals.maven.analysis.object;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.DataRange;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.objectweb.asm.Type;

import com.securboration.immortals.maven.PluginContext;
import com.securboration.immortals.maven.etc.ExceptionWrapper;
import com.securboration.immortals.maven.ontology.OntologyHelper;

/**
 * Utility class that converts an object instance to triples
 * 
 * @author jstaples
 *
 */
public class ObjectToOwl {

    private final PluginContext context;
    
    public ObjectToOwl(PluginContext context){
        this.context = context;
    }
    
    public void analyze(){
        ExceptionWrapper.wrap(()->{initWrapped();});
    }
    
    private void initWrapped() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException{
        
        context.getLog().info(
                String.format(
                        "detected the following entrypoints: %s\n", 
                        context.getEntryPoints()));
        for(String entryPoint:this.context.getEntryPoints()){
            
            Class<?> c = 
                    context.getBuildPathClassloader().loadClass(entryPoint);
            
            Method m = 
                    c.getMethod("getObjects");
            
            Object returnValue = m.invoke(null);
            
            for(int i=0;i<Array.getLength(returnValue);i++){
                
                AnalysisContext analysisContext = new AnalysisContext();
                
                Object o = Array.get(returnValue, i);
                
                context.getLog().info("about to analyze an object of type " + o.getClass().getName() + "...");
                //ObjectNode.build(o).accept(ObjectPrinter.getPrinterVisitor());
                
                analyze(o,analysisContext);
                
                final int currentIndex = i;
                ExceptionWrapper.wrap(() -> {
                    
                    OntologyHelper.addMetadata(context,analysisContext.model,context.getTargetNamespace());
                    OntologyHelper.setNamespacePrefixes(context,analysisContext.model);
                    
                    final String originalPath = context.getOutputFile().getAbsolutePath();
                    String type = null;
                    String[] parts = originalPath.split("\\.");
                    String path = parts[0];
                    if(originalPath.contains(".")){
                        type = parts[parts.length-1];
                    } else {
                        type = context.getOutputLanguage();
                    }                
                    
                    path = path+"Instances" + currentIndex + "." + type;

                    final String serializedOntology = 
                            OntologyHelper.serializeModel(
                                    analysisContext.model, 
                                    context.getOutputLanguage()
                                    );

                    context.getLog()
                            .info("writing result to " + path);

                    FileUtils.writeStringToFile(new File(path), serializedOntology);
                });
            }
        }
    }
    
    
    
    private void analyze(Object o,AnalysisContext c) throws IllegalArgumentException, IllegalAccessException{
        
        ObjectNode object = ObjectNode.build(o);
        
        object.accept(new ObjectVisitor(c));
    }
    
    private class AnalysisContext{
        
        final OntModel model;
        
        Map<Class<?>,Resource> classesToResources = new HashMap<>();
        
        Map<Object,Resource> instancesToResources = new HashMap<>();
        
        AnalysisContext(){
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
            
            return OntologyHelper.getResourceForType(context,analysisContext.model,internalName);
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
                return analysisContext.model.createTypedLiteral((((Class<?>)instance).getName()));
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
            
            Resource r = analysisContext.model.createResource(classInSchema.getURI()+UUID.randomUUID());
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
                        
                        Resource elementTypeResource = 
                                isEnum(element)?
                                        getRangeForEnum(element.getClass())
                                        :
                                        getResourceForType(t.getElementType());
                        
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
            
//            System.out.printf("owner: %s\n", primitiveFieldOwner.toString());
//            
//            Resource owner = 
//                    getResourceForClass(primitiveFieldOwner.getActualType());
//            
//            
//            
//            Resource newInstance = 
            
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
                throw new RuntimeException("shouldn't happen");//TODO
            }
            
            Property property = analysisContext.model.createProperty(p.getURI());
            
            analysisContext.model.add(fieldOwner,property,fieldValue);
            
//            System.out.printf("[%s] [%s] [%s]\n",fieldOwner,property,fieldValue);//TODO
//            analysisContext.schema.get.getPropertyForField(ownerClass, cn, field)
            //TODO: add triples specifying that the instance belongs to an instance of the supertype
            
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
        private final AnalysisContext analysisContext;
        
        public ElementVisitor(AnalysisContext c) {
            super();
            this.analysisContext = c;
        }
        
        @Override
        public void visitArrayElement(
                ObjectNode array,
                final int index,
                ObjectNode elementAtIndex){}
    }
    
}
