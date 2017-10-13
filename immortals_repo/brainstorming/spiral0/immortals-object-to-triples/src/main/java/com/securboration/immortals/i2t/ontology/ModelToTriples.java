package com.securboration.immortals.i2t.ontology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.objectweb.asm.Type;

import com.securboration.immortals.deployment.pojos.FieldValue;
import com.securboration.immortals.deployment.pojos.FieldValueContainer;
import com.securboration.immortals.deployment.pojos.ObjectInstance;
import com.securboration.immortals.deployment.pojos.TypeAbstraction;
import com.securboration.immortals.deployment.pojos.values.Value;
import com.securboration.immortals.deployment.pojos.values.ValueComplex;
import com.securboration.immortals.deployment.pojos.values.ValuePrimitive;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.etc.ExceptionWrapper;
import com.securboration.immortals.o2t.ontology.OntologyHelper;

/**
 * Utility class that converts a GME abstraction of an object instance into 
 * triples
 * 
 * @author jstaples
 *
 */
public class ModelToTriples {

    private static final String MODEL_CLASS_PREFIX = 
            "edu.vanderbilt.immortals.models.deployment";
    
    private final ObjectToTriplesConfiguration context;
    private final AnalysisContext analysisContext;
    
    private ModelToTriples(
            ObjectToTriplesConfiguration context,
            AnalysisContext analysisContext
            ){
        this.context = context;
        this.analysisContext = analysisContext;
    }
    
    public static Model convert(
            ObjectToTriplesConfiguration config,
            Collection<TypeAbstraction> types,
            Collection<ObjectInstance> instances,
            NamingContext c
            ){
        AnalysisContext analysisContext = 
                new AnalysisContext(c);
        
        ModelToTriples converter = 
                new ModelToTriples(config,analysisContext);
        
        for(TypeAbstraction t:types){
            config.getLog().info(
                    "about to analyze type " + t.getName() + "...");
            
            converter.analyze(t,analysisContext);
        }
        
        for(ObjectInstance o:instances){
            config.getLog().info(
                    "about to analyze an object of type " + o.getName() + "...");
            
            converter.instantiate(o);
        }
        
        ExceptionWrapper.wrap(() -> {
            
            OntologyHelper.addMetadata(
                    config,
                    analysisContext.model,
                    config.getTargetNamespace()
                    );
            
            OntologyHelper.setNamespacePrefixes(
                    config,
                    analysisContext.model
                    );               
            
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
    
    private Type getType(Value v){
        
        assertNotNull(v,"unhandled case: value is null");
        
        if(v instanceof ValuePrimitive){
            ValuePrimitive<?> p = (ValuePrimitive<?>)v;
            
            return Type.getType(p.getType());
        } else if (v instanceof ValueComplex){
            ValueComplex c = (ValueComplex)v;
            
            return getTypeFromAbstraction(c.getType());
        }
        
        return fail("unhandled case: " + v.getClass().getName());
    }
    
    private static <T> T fail(String message){
        throw new RuntimeException(message);
    }
    
    private static void assertNotNull(Object o,String message){
        if(o == null){
            throw new RuntimeException(message);
        }
    }
    
    private Property getPropertyForField(
            FieldValue v
            ){
        Type fieldType = getType(v.getValue());
        
        assertNotNull(
                v.getName(),
                "no field name was provided"
                );
        
        return
                OntologyHelper.getPropertyForField(
                        context, 
                        analysisContext.model, 
                        sanitize(v.getName()), 
                        fieldType
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
    
    private RDFNode getFieldValue(FieldValue value){
        
        Value v = value.getValue();
        
        assertNotNull(v,"unhandled case: value is null");
        
        if(v instanceof ValuePrimitive){
            ValuePrimitive<?> p = (ValuePrimitive<?>)v;
            
            return getPrimitiveLiteral(p.getValue());
        } else if (v instanceof ValueComplex){
            ValueComplex c = (ValueComplex)v;
            
            return instantiate(c.getValue());
        }
        
        return fail("unhandled case: " + v.getClass().getName());
        
    }
    
    private void analyze(TypeAbstraction type,AnalysisContext c){
        
        Resource typeResource = 
                getResourceForClass(type);
        
        typeResource.addLiteral(
                RDFS.label,
                type.getName());
        
        String comment = type.getComments();
        if(comment == null){
            comment = "no comment provided";
        }
        typeResource.addLiteral(
                RDFS.comment,
                comment
                );
        
        TypeAbstraction baseType = type.getParent();
        
        if(baseType == null){
            return;
        }
        
        Resource baseClassResource = 
                getResourceForClass(baseType);
        
        typeResource.addProperty(
                RDFS.subClassOf, 
                baseClassResource);
        
        analyze(baseType,c);
    }
    
    private static class Traverser{
        
        private static FieldValue getStaticFieldValue(
                String name,
                final TypeAbstraction type
                ){
            
            if(type == null){
                return null;
            }
            
            if(type.getFieldValues() == null){
                return getStaticFieldValue(name,type.getParent());
            }
            
            for(FieldValue v:type.getFieldValues()){
                if(v.getName().equals(name)){
                    return v;
                }
            }
            
            return getStaticFieldValue(name,type.getParent());
        }
        
        private static Iterator<ObjectInstance> instanceHierarchy(final ObjectInstance current){
            return new Iterator<ObjectInstance>(){
                
                ObjectInstance instance = current;
                
                @Override
                public boolean hasNext() {
                    
                    if(instance == null){
                        return false;
                    }
                    
                    if(instance.getInstanceParent() == null){
                        return false;
                    }
                    return true;
                }

                @Override
                public ObjectInstance next() {
                    instance = instance.getInstanceParent();
                    
                    return instance;
                }
                
            };
        }
        
        private static Iterator<TypeAbstraction> typeHierarchy(
                final TypeAbstraction current
                ){
            return new Iterator<TypeAbstraction>(){
                
                TypeAbstraction type = current;
                
                @Override
                public boolean hasNext() {
                    
                    if(type == null){
                        return false;
                    }
                    
                    if(type.getParent() == null){
                        return false;
                    }
                    return true;
                }

                @Override
                public TypeAbstraction next() {
                    type = type.getParent();
                    
                    return type;
                }
                
            };
        }
        
    }
    
    private static final Set<String> ignoreFields = new HashSet<>(
            Arrays.asList(
                    "uriName",
                    "uriGen",
                    "uriPrefix",
                    "uriExt"
                    ));
    
    private static List<FieldValue> getFields(FieldValueContainer c){
        
        List<FieldValue> fields = new ArrayList<>();
        
        if(c.getFieldValues() == null){
            return fields;
        }
        
        for(FieldValue v:c.getFieldValues()){
            
            if(ignoreFields.contains(v.getName())){
                continue;
            }
            
            fields.add(v);
        }
        
        return fields;
    }
    
    private Property getGmeInfoProperty(){
        return analysisContext.model.createProperty("gmeInfo");
    }
    
    private Resource instantiate(
            ObjectInstance instance
            ){
        
        //check if it's an instance we've seen before
        {
            Resource existingDefinition = 
                    analysisContext.findResourceForInstance(instance);
            
            if(existingDefinition != null){
                return existingDefinition;
            }
        }
        
        //now we know it's not something we've seen before so we'll instantiate
        {
            TypeAbstraction type = instance.getInstanceType();
            
            if(type == null){
                return null;//TODO
            }
            
            Resource classInSchema = 
                    getResourceForClass(type);
            
            final String resourceName = 
                    analysisContext.namingContext.getNameForObject(
                            instance,
                            classInSchema.getURI()
                            );
            Resource r = 
                    analysisContext.model.createResource(resourceName);
            analysisContext.defineInstance(instance, r);
            r.addProperty(RDF.type,classInSchema);
            
            String comment = instance.getComments();
            if(comment == null){
                comment = "no comment provided";
            }
            r.addLiteral(
                    RDFS.comment, 
                    comment);
            r.addLiteral(
                    getGmeInfoProperty(), 
                    "this instance derived from GME node " + instance.getUuid());
            r.addLiteral(
                    getGmeInfoProperty(), 
                    "this instance's type derived from GME node " + type.getUuid());
            
            Set<String> definedFields = new HashSet<>();
            Map<String,String> fieldNamesToPaths = new HashMap<>();
            
            //TODO: the following two blocks can be abstracted into a single 
            //method
            
            {//walk the instance hierarchy
                List<String> currentPath = new ArrayList<>();
                Traverser.instanceHierarchy(instance).forEachRemaining((current)->{
                    
                    Set<String> fieldsDefinedThisIteration = new HashSet<>();
                    
                    currentPath.add("[" + current.getUuid() + "/\"" + current.getName() + "\"]");
                    
                    //add properties for each field
                    List<FieldValue> fieldValues = getFields(current);
                    
                    fieldValues.forEach(v->{
                        System.out.println(currentPath + " has instance field: [" + v.getName() + "]");
                     });
                    
                    for(FieldValue v:fieldValues){
                        
                        if(definedFields.contains(v.getName())){
                            //but only if the field wasn't previously 
                            // defined (ie overridden) in a base class
                            
                            {
                                System.out.printf("filtering overridden field [%s]\n", v.getName());
                                System.out.printf("\tfound unused (overridden) definition @ %s\n", currentPath.toString());
                                System.out.printf("\tinstead using definition @ %s\n", fieldNamesToPaths.get(v.getName()));
                            }//TODO
                            
                            continue;
                        }
                        
                        fieldNamesToPaths.put(
                                v.getName(), 
                                currentPath.toString()
                                );
                        
                        fieldsDefinedThisIteration.add(v.getName());
                        
                        Property fieldProperty = getPropertyForField(v);
                        RDFNode fieldValue = getFieldValue(v);

                        r.addProperty(fieldProperty, fieldValue);
                    }
                    
                    //add the fields we've seen this iteration to the ignore list
                    definedFields.addAll(fieldsDefinedThisIteration);
                    
                });
            }
            
            
            {//walk the class hierarchy
                List<String> currentPath = new ArrayList<>();
                
                Traverser.typeHierarchy(type).forEachRemaining((current)->{
                    
                    Set<String> fieldsDefinedThisIteration = new HashSet<>();
                    
                    currentPath.add("[" + current.getUuid() + "/\"" + current.getName() + "\"]");
                    
                    //add properties for each field
                    List<FieldValue> fieldValues = getFields(current);
                    
                    fieldValues.forEach(v->{
                        System.out.println(currentPath + " has static field: [" + v.getName() + "]");
                     });
                    
                    for(FieldValue v:fieldValues){
                        
                        if(definedFields.contains(v.getName())){
                            //but only if the field wasn't previously 
                            // defined (ie overridden) in a base class
                            
                            {
                                System.out.printf("filtering overridden field [%s]\n", v.getName());
                                System.out.printf("\tfound unused (overridden) definition @ %s\n", currentPath.toString());
                                System.out.printf("\tinstead using definition @ %s\n", fieldNamesToPaths.get(v.getName()));
                            }//TODO
                            
                            continue;
                        }
                        
                        fieldNamesToPaths.put(
                                v.getName(), 
                                currentPath.toString()
                                );
                        
                        fieldsDefinedThisIteration.add(v.getName());
                        
                        Property fieldProperty = getPropertyForField(v);
                        RDFNode fieldValue = getFieldValue(v);

                        r.addProperty(fieldProperty, fieldValue);
                    }
                    
                    //add the fields we've seen this iteration to the ignore list
                    definedFields.addAll(fieldsDefinedThisIteration);
                    
                });
            }
            
            return r;
        }
    }
    
    public static class NamingContext{
        private final Map<ObjectInstance,String> objectsToNames = new HashMap<>();
        
        private String getNameForObject(
                ObjectInstance o,
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
    
    private Object getPrimitiveValue(FieldValue v){
        return ((ValuePrimitive<?>)v.getValue()).getValue();
    }
    
    private String getStringValue(FieldValue v){
        return (String)getPrimitiveValue(v);
    }
    
    private Resource getResourceFromFields(TypeAbstraction t){
        final String uriGen = 
                getStringValue(Traverser.getStaticFieldValue("uriGen", t));
        
        if(!uriGen.equals("semantic")){
            return null;
        }
        
        final String uriPrefix = 
                getStringValue(Traverser.getStaticFieldValue("uriPrefix", t));
        
        if(uriPrefix==null || uriPrefix.isEmpty()){
            return null;
        }
        
        final String uriName = 
                getStringValue(Traverser.getStaticFieldValue("uriName", t));
        
        if(uriName==null || uriName.isEmpty()){
            return null;
        }
        
        final String uriExt = 
                getStringValue(Traverser.getStaticFieldValue("uriExt", t));
        
        if(uriExt==null || uriExt.isEmpty()){
            return null;
        }
        
        System.out.printf("getting resource for class %s\n", t.getName());//TODO
        System.out.printf("\turiGen = %s\n", uriGen);
        System.out.printf("\turiPrefix = %s\n", uriPrefix);
        System.out.printf("\turiName = %s\n", uriName);
        System.out.printf("\turiExt = %s\n", uriExt);
        
        final String uri = uriPrefix + uriExt + uriName;
        return analysisContext.model.createClass(uri);
    }
    
    private Resource getResourceForClass(TypeAbstraction t){
        
        Resource manualUriResource = getResourceFromFields(t);
        
        if(manualUriResource != null){
            return manualUriResource;
        }
        
        return getResourceForType(getTypeFromAbstraction(t));
    }
    
    private static class AnalysisContext{
        
        final OntModel model;
        
        private final NamingContext namingContext;
        Map<ObjectInstance,Resource> instancesToResources = new HashMap<>();
        
        AnalysisContext(NamingContext namingContext){
            this.namingContext = namingContext;
            this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        }
        
        
        
        private Resource findResourceForInstance(ObjectInstance instance){
            return instancesToResources.get(instance);
        }
        
        private void defineInstance(ObjectInstance instance, Resource r){
            instancesToResources.put(instance, r);
        }
        
    }
    
    private Type getTypeFromAbstraction(TypeAbstraction t){
        String name = t.getName();
        
        if(name == null){
            throw new RuntimeException(
                    "unable to create Type from a type abstraction with null typeName field");
        }
        
        name = MODEL_CLASS_PREFIX + "." + sanitize(name);
        
        return Type.getType("L"+name+";");
    }
    
    private String camelCase(String name){
        StringBuilder sb = new StringBuilder();
        
        for(String part:name.split(" ")){
            if(part.isEmpty()){
                continue;
            }
            
            sb.append(part.substring(0, 1).toUpperCase());
            
            if(part.length() > 1){
                sb.append(part.substring(1));
            }
        }
        
        return sb.toString();
    }
    
    private String sanitize(String name){
        
        name = name.replace(":", "");
        name = name.replace(".", "/");
        name = camelCase(name);
        name = name.replace(" ", "");
        
        return name;
    }
    
    private Resource getResourceForType(Type t){
        
        final String internalName = t.getClassName();
        
        return OntologyHelper.getResourceForType(
                context,
                analysisContext.model,
                internalName
                );
    }
    
}
