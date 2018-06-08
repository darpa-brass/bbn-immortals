package com.securboration.immortals.o2t.analysis;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.DataRange;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.xerces.util.XMLChar;
import org.objectweb.asm.Type;

import com.securboration.immortals.annotations.helper.AnnotationHelper;
import com.securboration.immortals.j2t.analysis.ClassBytecodeHelper;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.etc.ExceptionWrapper;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.dfu.instance.DfuInstance;

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
            Object o,
            String name
            ){
        config.getNamingContext().bindUri(o, name);
        
        return convert(config,o);
    }
    
    public static Model convert(
            ObjectToTriplesConfiguration config,
            Object o
            ){
        final ObjectUriGenerator uriGenerator = 
                config.isEnforceUriSymmetry() ? config.getUriGenerator() : null;
        
        AnalysisContext analysisContext = 
                new AnalysisContext(
                    config.getNamingContext(),
                    uriGenerator
                    );
        
        if(config.isDiveIntoConceptInstances()){
            analysisContext.diveIntoConceptInstances = true;
        }
        
        config.getLog().info("about to analyze an object of type " + o.getClass().getName() + "...");
        
        ObjectToTriples converter = new ObjectToTriples(config);
        
        
        ExceptionWrapper.wrap(() -> {
            
            converter.analyze(o,analysisContext);
            
            OntologyHelper.addAutogenerationMetadata(
                config,
                analysisContext.model,
                config.getTargetNamespace(),
                config.getOutputFile()
                );
            
            OntologyHelper.setNamespacePrefixes(
                config,
                analysisContext.model
                );               
            
            final String serializedOntology = 
                    OntologyHelper.serializeModel(
                            analysisContext.model, 
                            config.getOutputLanguage(),
                            config.isValidateOntology()
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
    
    private void analyze(Object o,AnalysisContext c) throws IllegalArgumentException, IllegalAccessException{
        
        ObjectNode object = ObjectNode.build(context.getObjectTranslator(),o);
        
        object.accept(new ObjectVisitor(c));
    }
    
    public static class NamingContext{
        
        private final Set<Class<?>> singletonsInstantiated = new HashSet<>();
        
        private final Map<Object,String> objectsToNames = new HashMap<>();
        
        private final Set<Object> objectsWithProvidedNames = new HashSet<>();
        
        public void copyBindingsTo(NamingContext other){
            other.objectsToNames.putAll(this.objectsToNames);
        }
        
        /**
         * Ensures that the object provided will be referenced via the URI
         * provided
         * 
         * @param o
         *            an object to bind to a URI
         * @param classUri
         *            the URI to which the object is bound
         */
        public void bindUri(Object o, String classUri){
            
            if(objectsToNames.containsKey(classUri)){
                if(!objectsToNames.get(classUri).equals(o)){
                    throw new RuntimeException(
                        "attempted to bind two different objects to " + classUri
                        );
                }
            }
            
            objectsToNames.put(o, classUri);
        }
        
        /**
         * Declares an explicit name for an object placeholder. Whenever a
         * reference to the object is subsequently encountered during
         * serialization, it will not be dived into to generate additional
         * triples.
         * 
         * @param o
         *            an object to use as a placeholder
         * @param classUri
         *            the explicit URI to use
         */
        public void setNameForObject(
                Object o, 
                String classUri
                ){
            if(objectsToNames.containsKey(o)){
                throw new RuntimeException(
                    "attempted renaming of object (current URI = " + 
                            objectsToNames.get(o) + ", new URI = " + classUri);
            }
            
            objectsToNames.put(o, classUri);
            objectsWithProvidedNames.add(o);
        }
        
        public void clearNameForObject(Object o){
            objectsToNames.remove(o);
            objectsWithProvidedNames.remove(o);
        }
        
        public String getNameForObject(Object o){
            if(!objectsToNames.containsKey(o)){
                throw new RuntimeException(
                    "attempted to retrieve name for previously unseen object"
                    );
            }
            
            return objectsToNames.get(o);
        }
        
        public String getNameForObject(
                ObjectUriGenerator g,
                Object o,
                String classUri
                ) throws IllegalArgumentException, IllegalAccessException{
            if(objectsToNames.containsKey(o)){
                return objectsToNames.get(o);
            }
            
            if (o instanceof DfuInstance) {
                String durableUri = ((DfuInstance) o).getDurableUri();
                if (durableUri != null) {
                    objectsToNames.put(o, classUri + "-" + durableUri);
                    return classUri + "-" + durableUri;
                }
            }
            
            if(OntologyHelper.isConceptInstance(o.getClass())){
                objectsToNames.put(o, classUri);
                return classUri;
            }
            
            final String uuid;
            if(g != null){
                uuid = g.generateUuid(o);
            } else {
                uuid = UUID.randomUUID().toString();
            }
            String name = classUri + "-"+uuid;
            objectsToNames.put(o, name);
            
            return name;
        }
        
        private boolean isSingletonInstantiated(Class<?> c){
            return singletonsInstantiated.contains(c);
        }
        
        private void registerSingleton(Class<?> c){
            singletonsInstantiated.add(c);
        }
        
        private String getOverriddenName(Object o){
            
            if(!objectsWithProvidedNames.contains(o)){
                return null;
            }
            
            return objectsToNames.get(o);
        }
    }
    
    private static class AnalysisContext{
        
        final OntModel model;
        
        final OntModel resourceModel = 
                ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        
        private final NamingContext namingContext;
        private Map<Object,Resource> instancesToResources = new HashMap<>();
        
        private final ObjectUriGenerator uriGenerator;
        
        private boolean diveIntoConceptInstances = false;
        
        AnalysisContext(
            NamingContext namingContext,
            ObjectUriGenerator uriGenerator
            ){
            this.namingContext = namingContext;
            this.model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
            this.uriGenerator = uriGenerator;
        }
        
        private Resource findResourceForInstance(Object instance){
            return instancesToResources.get(instance);
        }
        
        private void defineInstance(Object instance, Resource r){
            instancesToResources.put(instance, r);
        }
        
    }
    
    private static class XmlSanitizer {
        
        private static final Charset UTF8_CHARSET = 
                Charset.forName("UTF-8");
        
        private static synchronized String sanitize(String s) {
            if(s == null || s.isEmpty()){
                return s;
            }
            
            boolean containsInvalidChar = false;
            
            for(char c:s.toCharArray()){
                if(!XMLChar.isValid(c)){
                    containsInvalidChar = true;
                    break;
                }
            }
            
            if(!containsInvalidChar){
                return s;
            }
            
            final byte[] base64Bytes = 
                    Base64.getEncoder().encode(s.getBytes(UTF8_CHARSET));
            
            s = new String(base64Bytes,UTF8_CHARSET);
            
            return "BINARYSTRING:" + UTF8_CHARSET.name() + ":" + s;
        }

    }
    
    
    private class ObjectVisitor implements ObjectNodeVisitor{
        private final AnalysisContext analysisContext;
        
        public ObjectVisitor(AnalysisContext c) {
            super();
            this.analysisContext = c;
        }
        
        private Resource getResourceForClass(Class<?> c){
            
            if(OntologyHelper.isConceptInstance(c)){
                Resource classInSchema = getResourceForType(Type.getType(c));
                
                String resourceName;
                try {
                    resourceName = analysisContext.namingContext.getNameForObject(
                            analysisContext.uriGenerator,
                            c.newInstance(),
                            classInSchema.getURI()
                            );
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                Resource r = 
                        analysisContext.model.createResource(resourceName);
                
                if(context.isAddPojoProvenance()){
                    Property p = OntologyHelper.getPropertyForField(
                        context, 
                        analysisContext.model, 
                        "pojoProvenance", 
                        Type.getType(String.class)
                        );
                    
                    analysisContext.model.add(r,p,c.getName());
                }
                
                return r;
            }
            
            Resource r = getResourceForType(Type.getType(c));
            
            context.getNamingContext().bindUri(c, r.getURI());
            
            return r;
        }
        
        
        
        private Resource getResourceForType(Type t){
            
            final String internalName = t.getClassName();
            
            Resource r = OntologyHelper.getResourceForType(
                    context,
                    analysisContext.resourceModel,
                    internalName
                    );
            
            return r;
        }
        
        
        
        private Literal getPrimitiveLiteral(Object instance){
            if(instance == null){
                return null;
            }
            
            instance = context.getObjectTranslator().translate(instance);
            
            Resource primitive = 
                    OntologyHelper.getTypeMappings(analysisContext.model).get(
                            Type.getType(instance.getClass()));
            
            if(primitive != null){
                
                boolean shouldSanitize = 
                        context.isSanitizeInvalidXmlStrings() 
                        && 
                        (instance instanceof String)
                        ;
                
                if(shouldSanitize){
                    instance = XmlSanitizer.sanitize((String)instance);
                }
                
                return analysisContext.model.createTypedLiteral(instance);
            }
            
            return null;
        }
        
        private DataRange getRangeForEnum(Class<?> c){
            
            RDFList list = analysisContext.model.createList();
            
            for (Object enumerationValue:c.getEnumConstants()) {
                Enum<?> enumeration = (Enum<?>)enumerationValue;
                
                RDFNode enumValue = 
                        OntologyHelper.getResourceForEnum(
                            context, 
                            analysisContext.model, 
                            c, 
                            enumeration.name()
                            );
                
                list = list.cons(enumValue);
            }
            
            return analysisContext.model.createDataRange(list);
        }
        
        /**
         * Builds up the required class hierarchy for an object instance
         * 
         * @param instance
         * @return
         */
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
            
            final Class<?> c = instance.getClass();
            
            //now we know it's not a primitive, but it could be something we've 
            // seen before
            {
                Resource existingDefinition = 
                        analysisContext.findResourceForInstance(instance);
                
                if(existingDefinition != null){
                    return existingDefinition;
                }
                
                //check if it's something with an overridden name
                {
                    String overrideUri = 
                            context.getNamingContext().getOverriddenName(
                                instance
                                );
                    if(overrideUri != null){
                        return analysisContext.model.getResource(overrideUri);
                    }
                }
            }
            
            // now we know it's not a primitive and it's not something we've
            // seen before so we'll go ahead and instantiate it
            Type t = Type.getType(instance.getClass());
            
            if(c.isEnum()){
                //it's an enum
                
                return OntologyHelper.getResourceForEnum(
                    context, 
                    analysisContext.model,
                    c, 
                    ((Enum<?>)instance).name()
                    );
            }
            
            Resource classInSchema = getResourceForClass(c);
            
            final String resourceName;
            
            try {
                resourceName = 
                        analysisContext.namingContext.getNameForObject(
                                analysisContext.uriGenerator,
                                instance,
                                classInSchema.getURI()
                                );
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            
            Resource r = 
                    analysisContext.model.createResource(resourceName);
            if(OntologyHelper.isConceptInstance(c)){
                Class<?> p = c.getSuperclass();
                if(p != null){
                    r.addProperty(
                        RDF.type, 
                        getResourceForType(Type.getType(p)));
                }
                
            } else if(c.isAnonymousClass()){
                Class<?> p = c.getSuperclass();
                if(p != null){
                    r.addProperty(
                        RDF.type, 
                        getResourceForType(Type.getType(p)));
                }
            }
            else{
                r.addProperty(RDF.type,classInSchema);
            }
            analysisContext.defineInstance(instance, r);
            
            
            if(t.getSort()==Type.ARRAY){
                
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
                
                if(!OntologyHelper.isConceptInstance(c)){
                    
                    if(context.isAddKeys()){
                        r.addProperty(
                            OntologyHelper.getUuidProperty(
                                context, 
                                analysisContext.model
                                ),
                            analysisContext.model.createTypedLiteral(
                                r.getURI().substring(r.getURI().length()-36)
                                )
                            );//add a uuid property to the instance
                    }
                } else {
                    
                    Triple[] triples = AnnotationHelper.getTriples(c);
                    if(triples != null && triples.length > 0){
                        OntologyHelper.addTriples(
                            context, 
                            analysisContext.model, 
                            r, 
                            triples
                            );
                    }
                    
                }
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
        
        private void visitObjectFieldFlattened(
                Resource fieldOwner,
                Property fieldProperty,
                ObjectNode objectFieldValue
                ){
            Object instance = objectFieldValue.getValue();
            
            if(instance == null){
                return;
            }
            
            final int length = Array.getLength(instance);
            for(int i=0;i<length;i++){
                Object element = Array.get(instance, i);
                
                if(element == null){
                    continue;
                }
                
                RDFNode elementValue = 
                        instantiate(element);
                
                analysisContext.model.add(
                        fieldOwner,
                        fieldProperty,
                        elementValue
                        );
            }
        }
        
        private void visitObjectField(
                Resource fieldOwner,
                Property fieldProperty,
                ObjectNode objectFieldValue
                ){
            
            RDFNode fieldValue = 
                    instantiate(objectFieldValue.getValue());
            
            analysisContext.model.add(fieldOwner,fieldProperty,fieldValue);
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
            
            if(objectFieldValue.getFieldName() == null){
                throw new RuntimeException("shouldn't happen");
            }
            
            if(objectFieldValue.getReflectionField() == null){
                throw new RuntimeException("expected to find a Field");
            }
            
            if(context.getIgnoredFields().contains(objectFieldValue.getReflectionField())){
                //it's been explicitly ignored so skip over this field
                return;
            }
            
            Resource fieldOwner = 
                    instantiate(objectFieldOwner.getValue()).asResource();
            
            Set<String> properties = new HashSet<>();
            try{
                properties.addAll(ClassBytecodeHelper.getPropertiesForField(
                    objectFieldOwner.getValue(), 
                    objectFieldValue.getFieldName()
                    ));
            } catch(IOException e){
                throw new RuntimeException(e);
            }
            
            if(properties.size() == 0){
                throw new RuntimeException("expected > 0 properties");
            }
            
            for(String propertyName:properties){
                Property p = OntologyHelper.getPropertyForField(
                    context,
                    analysisContext.resourceModel,
                    propertyName, 
                    Type.getType(objectFieldValue.getPossibleType()));
            
                final boolean shouldFlatten = 
                        //only consider flattening if the config says to do so
                        (context.shouldFlattenArrays()) 
                        //only consider flattening the object if it's an array
                        && (objectFieldValue.getValue().getClass().isArray())
                        ;
                
                if(shouldFlatten){
                    visitObjectFieldFlattened(fieldOwner,p,objectFieldValue);
                } else {
                    visitObjectField(fieldOwner,p,objectFieldValue);
                }
            }
        }
        
        @Override
        public ArrayElementVisitor visitArrayField(
                ObjectNode arrayFieldOwner,
                ObjectNode arrayFieldValue
                ){
            return new ElementVisitor(analysisContext);
        }

        @Override
        public boolean shouldDiveInto(ObjectNode objectFieldValue) {
            Object value = objectFieldValue.getValue();
            
            if(value == null){
                return false;
            }
            
//            //if it's an object with an overridden name, don't dive in
//            if(context.getNamingContext().getOverriddenName(value) != null){
//                return false;
//            }
            
            Class<?> c = value.getClass();
            
            //if it's not a singleton concept, dive in
            if(!OntologyHelper.isConceptInstance(c)){
                return true;
            }
            
            if(analysisContext.diveIntoConceptInstances){
                return true;
            }
            
            //otherwise we have to be careful not to dive into the same concept
            // twice (ever)
            
            final boolean seenBefore = 
                    analysisContext.
                        namingContext.
                        isSingletonInstantiated(c);
            
            if(seenBefore){
                //we've already seen this type before, so stop recursing
                return false;
            }
            
            analysisContext.
                namingContext.
                registerSingleton(c);
            
            return true;
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
