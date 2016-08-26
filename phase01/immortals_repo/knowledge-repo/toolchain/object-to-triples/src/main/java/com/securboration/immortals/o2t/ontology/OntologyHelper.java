package com.securboration.immortals.o2t.ontology;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.DataRange;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.securboration.immortals.annotations.helper.AnnotationHelper;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.ontology.annotations.RdfsComment;
import com.securboration.immortals.ontology.annotations.etc.Constants;
import com.securboration.immortals.ontology.annotations.triples.FieldReference;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

/**
 * Simple utility class for reading/writing semantic models
 * 
 * @author jstaples
 *
 */
public class OntologyHelper {
    private static Logger logger = LoggerFactory
            .getLogger(OntologyHelper.class);

    /**
     * Serializes the provided model to the provided language
     * 
     * @param m
     *            a model to serialize
     * @param outputLanguage
     *            a language to serialize to (e.g., RDF/XML)
     * @return the serialized model
     * @throws IOException
     */
    public static String serializeModel(Model m, String outputLanguage)
            throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        m.write(output, outputLanguage);

        return new String(output.toByteArray());
    }

    /**
     * Loads a non-inferencing model from an input stream
     * 
     * @param input
     *            the stream to read from
     * @param inputLanguage
     *            the language to read from the stream
     * @return the model read
     * @throws IOException
     */
    public static Model loadModel(final InputStream input,
            final String inputLanguage) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        model.read(input, inputLanguage);

        return model;
    }

    /**
     * Prints a vaguely human readable representation of triples in the provided
     * model to the provided printstream
     * 
     * @param model
     *            the model whose triples we will print
     * @param p
     *            where to print
     */
    public static void printTriples(Model model, PrintStream p) {
        StmtIterator statements = model.listStatements();

        int count = 0;
        while (statements.hasNext()) {
            Statement s = statements.next();

            p.printf("%07d: [%s] [%s] [%s]\n", count, s.getSubject(),
                    s.getPredicate(), s.getObject());

            count++;
        }
    }
    
    
    private static void addProperties(
            Map<String,Object> namesToValues, 
            Model m,
            Resource addMetadataToThis,
            final String namespace
            ){
        
        for(String name:namesToValues.keySet()){
            Object value = namesToValues.get(name);
            Property p = m.getProperty(namespace+"#hasMetadata_"+name);
            addMetadataToThis.addLiteral(
                    p, 
                    m.createTypedLiteral(value)
                    );
        }
        
    }
    
    private static String getStackTrace(){
        StringBuilder sb = new StringBuilder();
        
        int counter = 0;
        for(StackTraceElement e:Thread.currentThread().getStackTrace()){
            counter++;
            if(counter <= 2){
                continue;
            }
            
            sb.append(
                    String.format(
                            "%s %s @ %d\n", 
                            e.getClassName(), 
                            e.getMethodName(), 
                            e.getLineNumber()
                            )
                    );
        }
        
        return sb.toString();
    }
    
    public static void addMetadata(
            ObjectToTriplesConfiguration context,
            Model m,
            final String namespace,
            final File outputFile
            ){
        if(!context.isAddMetadata()){
            return;
        }
        
        Resource r = 
                outputFile == null ? 
                        m.getResource(namespace.replace("#", "")+"#"+UUID.randomUUID().toString())
                        :
                        m.getResource(namespace.replace("#", "")+"#"+outputFile.getName());
        
        //r.addProperty(RDF.type, OWL.Ontology);
        r.addProperty(
            RDF.type, 
            m.getResource(
                namespace.replace("#", "")+"#OntologyFragmentMetadata"
                )
            );
                        
        //built in metadata
        {
            r.addLiteral(
                    OWL.versionInfo, 
                    context.getVersion());
            
            r.addLiteral(
                    RDFS.comment, 
                    "Automatically generated from bytecode by the "
                    + "object-to-triples converter.  DO NOT edit this file, "
                    + "as changes will be lost when the file is next "
                    + "generated. If changes are required, instead edit the "
                    + "POJO(s) from which this file was generated."
                    );
            
            r.addLiteral(
                RDFS.comment, 
                "Metadata about an ontology fragment this graph contains."
                );
        }
        
        //custom metadata
        {
            Map<String,Object> metadata = new LinkedHashMap<>();
            
            if(outputFile != null){
                metadata.put("Ontology_file", outputFile.getAbsolutePath());
            } else {
                metadata.put("Ontology_file", "none specified");
            }
            
            metadata.put("Creation_time", Calendar.getInstance());
            metadata.put("Creation_stack_trace", getStackTrace());
            
            metadata.put("Creator", "Securboration, Inc.");
            metadata.put("CreatorUrl", "http://www.securboration.com/");
            metadata.put("Built_by", System.getProperty("user.name"));
            
            metadata.put("Project", "IMMoRTALS");
            metadata.put("ProjectUrl", "https://dsl-external.bbn.com/tracsvr/immortals");
            
            addProperties(metadata,m,r,namespace);
        }
    }
    
    private static Set<String> getLongNames(Map<String,String> namespaceMappings){
        
        Set<String> longNames = new HashSet<>();
        
        for(String s:namespaceMappings.keySet()){
            
            longNames.add(s.substring(0, s.length()-1));
        }
        
        return longNames;
    }
    
    private static void addUri(Resource r,Collection<String> uris){
        if(r.isAnon()){
            return;
        }
        
        if(r.isLiteral()){
            return;
        }
        
        uris.add(r.getURI());
    }
    
    private static void addUri(RDFNode r,Collection<String> uris){
        if(r.isAnon()){
            return;
        }
        
        if(r.isLiteral()){
            return;
        }
        
        addUri(r.asResource(),uris);
    }
    
    private static Set<String> getUris(Model model){
        Set<String> uris = new HashSet<>();
        
        Iterator<Statement> statements = model.listStatements();
        while(statements.hasNext()){
            Statement s = statements.next();
            
            addUri(s.getSubject(),uris);
            addUri(s.getPredicate(),uris);
            addUri(s.getObject(),uris);
        }
        
        return uris;
    }
    
    private static boolean isArrayType(String className){
        return Type.getType(className).getSort() == Type.ARRAY;
    }
    
    private static Property getPropertyForType(
            ObjectToTriplesConfiguration context,
            Model model,
            Type t,
            String fieldName
            ){
        
        fieldName = fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
        
        if(t.getSort() == Type.METHOD){
            throw new RuntimeException("not expecting a method desc but found " + t.getDescriptor());
        }
        
        String className = t.getClassName();
        
        if(className.contains(".")){
            className = className.substring(className.lastIndexOf(".")+1);
        }
        
        if(context.shouldFlattenArrays()){
            className = className.replace("[]","");
        } else {
            className = className.replace("[]","Array");
        } 
        className = className.replace("$", ".");
        
        return model.createProperty(context.getTargetNamespace()+"#has"+fieldName+"_"+className);
    }
    
    public static Property getPropertyForField(
            ObjectToTriplesConfiguration context, 
            Model model, 
            String fieldName, 
            Type fieldType
            ){
        return getPropertyForType(context,model,fieldType,fieldName);
        //model.createProperty(context.getTargetNamespace()+"#has_"+fieldName);
    }
    
    public static Property getPropertyForField(
            ObjectToTriplesConfiguration context, 
            OntModel model,
            Resource ownerClass,
            Class<?> c,
            Field f
            ){
        Map<Type,Resource> mappings = OntologyHelper.getTypeMappings(model);
        
        Type t = Type.getType(f.getType());
        
        RDFNode classResource = analyzeType(context,model,t);
        
        Property p = 
                getPropertyForField(
                        context,
                        model,
                        f.getName(),
                        Type.getType(f.getType()));
        
        if(isPrimitive(mappings,t)){
            p.addProperty(RDF.type, OWL.DatatypeProperty);
        } else if(isEnum(context,t)){
            p.addProperty(RDF.type, OWL.DatatypeProperty);
        } else {
            p.addProperty(RDF.type, OWL.ObjectProperty);
        }
        p.addProperty(RDFS.range, classResource);
        p.addProperty(RDFS.domain, ownerClass);
        
        //pull a comment from an annotation
        final String comment = getFieldComments(f);
        if (comment != null) {
            p.addLiteral(RDFS.comment, comment);
        }
        
        return p;
    }
    
    private static boolean isPrimitive(Map<Type,Resource> mappings,Type t){
        
        if(mappings.containsKey(t)){
            return true;
        }
        
        if(t.getSort() == Type.OBJECT){
            return false;
        }
        
        if(t.getSort() == Type.ARRAY){
            return false;
        }
        
        return true;
    }
    
//    private static boolean isPrimitive(Type t){
//        if(t.getSort() == Type.OBJECT){
//            return false;
//        }
//        
//        if(t.getSort() == Type.ARRAY){
//            return false;
//        }
//        
//        return true;
//    }
    
    private static boolean isEnum(ObjectToTriplesConfiguration context,Type t) {
        
        if(t.getSort() == Type.ARRAY){
            return false;
        }
        
        return getClass(context,t).isEnum();
    }
    
    private static Class<?> getClass(ObjectToTriplesConfiguration context,Type t){
        try{
            final String className = t.getClassName().replace("/", ".");
            
            Class<?> c = context.getClassloader().loadClass(className);
            
            return c;
        } catch(ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }
    
    public static RDFNode getResourceForEnum(
            ObjectToTriplesConfiguration context,
            OntModel model,
            Class<?> enumeration, 
            String value
            ){
        return model.createTypedLiteral(value);
    }
    
    private static DataRange getRange(
            ObjectToTriplesConfiguration context, 
            OntModel model, 
            Type t
            ){
        
        Class<?> enumeration = getClass(context,t);
        
        RDFList list = model.createList();
        for (Object o:enumeration.getEnumConstants()) {
            Enum<?> e = (Enum<?>)o;
            
            list = list.cons(model.createTypedLiteral(e.name()));
        }
        
        return model.createDataRange(list);
    }
    
    private static RDFNode analyzeType(
            ObjectToTriplesConfiguration context, 
            OntModel model, 
            Type t
            ){
        
        Map<Type,Resource> mappings = OntologyHelper.getTypeMappings(model);
        
        Resource r = mappings.get(t);
        
        if(r != null){
            return r;
        }
        
        if(t.getSort()==Type.METHOD){
            throw new RuntimeException("wasnt expecting a method type here");
        } else if(t.getSort()==Type.ARRAY){
            
            //first flatten the array if desired and possible (only 1-D arrays
            // can be flattened cleanly)
            if(context.shouldFlattenArrays()){
                Type elementType = t.getElementType();
                
                if(t.getDimensions() == 1){
                    return analyzeType(
                        context,
                        model,
                        elementType
                        );
                }
            }
            
            
            //create an array wrapper
            
            Resource arrayResource = 
                    getResourceForType(
                            context,
                            model,t.getClassName());
            arrayResource.addProperty(
                    RDFS.subClassOf,
                    getArrayResource(context,model));
            
            Type elementType = t.getElementType();
            
            if(t.getDimensions() > 1){
                elementType = Type.getType(t.getInternalName().substring(1));
            }
            
            Property p = 
                    getPropertyForType(context,model,elementType,"Element");
            
            if(isPrimitive(mappings,t.getElementType())){
                p.addProperty(RDF.type, OWL.DatatypeProperty);
            } else {
                p.addProperty(RDF.type, OWL.ObjectProperty);
            }
            
            p.addProperty(RDFS.range, analyzeType(context,model,elementType));
            p.addProperty(RDFS.domain, arrayResource);
            
            return arrayResource;
            
        } else if(t.getSort()==Type.OBJECT){
            
            if(isEnum(context,t)){
                return getRange(context,model,t);
            }
            
            return OntologyHelper.getResourceForType(context,model,t.getClassName());
        }
        
        throw new RuntimeException("unhandled corner case: " + t.getClassName());
    }
    
    public static Resource getBaseResource(
            ObjectToTriplesConfiguration config,
            OntModel model
            ){
        return getResourceForType(config,model,"ImmortalsBaseClass");
    }
    
    public static Resource getBaseProperty(
            ObjectToTriplesConfiguration config,
            OntModel model
            ){
        return getResourceForType(config,model,"ImmortalsBaseProperty");
    }
    
    public static Resource getArrayResource(
            ObjectToTriplesConfiguration config,
            OntModel model
            ){
        Resource array = getResourceForType(config,model,"Array");
        
        array.addProperty(RDFS.subClassOf, getBaseResource(config,model));
        
        return array;
    }
    
    public static Class<?> getClass(
            Type t,
            ObjectToTriplesConfiguration context
            ) throws ClassNotFoundException {
        return context.getClassloader().loadClass(t.getClassName());
    }
    
    public static Resource getResourceForType(
            ObjectToTriplesConfiguration context,
            OntModel model,
            Class<?> c
            ){
          return getResourceForType(
                  context,
                  model,
                  Type.getType(c).getClassName());
    }
    
    //TODO: cleanup
    public static String getInstanceId(Class<?> c){
        ConceptInstance instanceAnnotation = 
                AnnotationHelper.getAnnotationOfType(
                    c,
                    ConceptInstance.class
                    );
        
        return "~instance"+instanceAnnotation.id();
    }
    
    private static Resource getResourceForType(
            ObjectToTriplesConfiguration context, 
            OntModel model, 
            Class<?> c,
            boolean isConceptInstance
            ) {
        final String baseName = Type.getType(c).getClassName();

        if (isConceptInstance) {
            final String name = 
                    baseName + "-" + 
                    getInstanceId(c);
            
            final String uri = OntologyHelper.makeUriName(context, name);
            
            return model.createResource(uri);
        }

        return getResourceForType(
                context, 
                model,
                baseName
                );
    }
    
    private static String getClassloaderName(String className){

        className = className.replace("/", ".");
        
        if(!className.contains("[]")){
            return className;
        }
        
        final int firstBracketIndex = className.indexOf("[]");
        final int bracketOffset = className.length() - firstBracketIndex;
        
        if(bracketOffset %2 != 0){
            throw new RuntimeException("unhandled case for array type: " + className);
        }
        
        final int dimension = bracketOffset/2;
        
        StringBuilder sb = new StringBuilder();
        
        for(int i=0;i<dimension;i++){
            sb.append("[");
        }
        sb.append("L");
        
        sb.append(className.substring(0,firstBracketIndex));
        
        sb.append(";");
        
        return sb.toString();
    }
    
    private static Class<?> loadClass(
            ObjectToTriplesConfiguration context,
            String className
            ){
        final String loadableName = getClassloaderName(className);
        
        try {
            return context.getClassloader().loadClass(loadableName);
        } catch (ClassNotFoundException e) {
            context.getLog().warn(
                    "unable to load class with simple name " + className);
        }
        
        //the error caught earlier is reflected by a return value of null
        return null;
    }
    
    public static Resource getResourceForType(
            ObjectToTriplesConfiguration context,
            OntModel model,
            String className
            ){
        final String uri = OntologyHelper.makeUriName(context, className);

        Resource resource = model.createClass(uri);

        Class<?> c = loadClass(context, className);

        if (c == null) {
            // nothing else to be done, the class couldn't be found
            return resource;
        }

        //pull a comment from an annotation
        final String comment = getClassComments(c);
        if (comment != null) {
            resource.addLiteral(RDFS.comment, comment);
        }

        return resource;
    }
    
    private static Property getPropertyFromFieldAnnotation(
            ObjectToTriplesConfiguration config,
            OntModel model,
            FieldReference fieldReference
            ){
        
        final String fieldName = fieldReference.fieldName();
        final Class<?> fieldOwner = fieldReference.fieldOwner();
        
        if(fieldName.equals(Constants.UndefinedString)){
            return null;
        }
        
        if(fieldOwner.equals(Constants.UndefinedClass.class)){
            return null;
        }
        
        try {
            Field f = fieldOwner.getDeclaredField(fieldName);
            
            Resource fieldOwnerResource = 
                    getResourceForType(config,model,fieldOwner);
            
            Property p = getPropertyForField(
                    config,
                    model,
                    fieldOwnerResource,
                    fieldOwner,
                    f);
            
            return p;
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    public static boolean isConceptInstance(Class<?> c){
        return c.getAnnotation(ConceptInstance.class) != null;
    }
    
    /**
     * Adds triples derived from @Triple annotations on a Class to the model
     * 
     * @param config
     * @param model
     * @param defaultValue
     */
    public static void addTriples(
            ObjectToTriplesConfiguration config,
            OntModel model,
            Resource defaultValue,
            Triple[] triples
            ){
        
        for(Triple triple:triples){
            
            Resource s = null;
            Property p = null;
            Object o = null;
            
            if(triple.nakedTriple().length > 0){
                s = model.createResource(triple.nakedTriple()[0]);
                p = model.createProperty(triple.nakedTriple()[1]);
                o = model.createResource(triple.nakedTriple()[2]);
                
                model.add(s,p,(Resource)o);
                
                continue;
            }
            
            //get the subject
            {
                //the subject is a class
                Class<?> subjectClass = triple.subjectClass();
                if(subjectClass != Constants.UndefinedClass.class){
                    s = getResourceForType(
                            config,
                            model,
                            subjectClass,
                            isConceptInstance(subjectClass)
                            );
                }
                
                //the subject is a URI
                if(s == null && !triple.subjectUri().equals(Constants.UndefinedString)){
                    s = model.createResource(triple.subjectUri());
                }
                
                //the subject is a property
                if(s == null){
                    s = getPropertyFromFieldAnnotation(
                            config,
                            model,
                            triple.subjectField());
                }
                
                //if nothing was provided, the subject is the default
                if(s == null){
                    s = defaultValue;
                }
            }
            
            //get the object
            {
                //the object is a Class
                Class<?> objectClass = triple.objectClass();
                if(objectClass != Constants.UndefinedClass.class){
                    o = getResourceForType(
                            config,
                            model,
                            objectClass,
                            isConceptInstance(objectClass)
                            );
                }
                
                //the object is a URI
                if(o == null && !triple.objectUri().equals(Constants.UndefinedString)){
                    o = model.createResource(triple.objectUri());
                }
                
                //the object is a property
                if(o == null){
                    o = getPropertyFromFieldAnnotation(
                            config,
                            model,
                            triple.objectField());
                }
                
                //the object is a literal
                if(o == null && !triple.objectLiteral().literalType().equals(Constants.UndefinedString)){
                    o = model.createTypedLiteral(
                            triple.objectLiteral().value(), 
                            NodeFactory.getType(triple.objectLiteral().literalType()));
                }
                
                //if nothing was provided, the object is the defaultValue
                if(o == null){
                    o = defaultValue;
                }
            }
            
            //get the predicate
            {
                //the predicate is a field->property
                p = getPropertyFromFieldAnnotation(
                        config,
                        model,
                        triple.predicateField());
                
                //the predicate is a URI
                if(p == null && !triple.predicateUri().equals(Constants.UndefinedString)){
                    p = model.createProperty(triple.predicateUri());
                }
                
                if(p == null){
                    throw new RuntimeException(
                            "no predicate specified and one cannot be "
                            + "inferred from the annotated class ");
                }
            }
            
            if(o == null){
                throw new NullPointerException("the object cannot be null");
            }
            if(o instanceof Literal){
                model.add(s,p,(Literal)o);
            } else if(o instanceof Resource){
                model.add(s,p,(Resource)o);
            } else {
                throw new RuntimeException(
                        "unhandled case: " + o.getClass().getName());
            }
        }
    }
    
    
    
    private static String getFieldComments(Field f){
        
        RdfsComment comment = f.getAnnotation(RdfsComment.class);
        
        if(comment == null){
            return null;
        }
        
        return comment.value();
    }
    
    private static String getClassComments(Class<?> c){
        
        RdfsComment comment = c.getAnnotation(RdfsComment.class);
        
        if(comment == null){
            return null;
        }
        
        return comment.value();
    }
    
    public static Map<Type,Resource> getTypeMappings(Model model){
        Map<Type,Resource> map = new HashMap<>();
        
        //primitive types
        map.put(
                Type.BOOLEAN_TYPE, 
                model.getResource(XSDDatatype.XSDboolean.getURI()));
        map.put(
                Type.getType(Boolean.class), 
                model.getResource(XSDDatatype.XSDboolean.getURI()));
        
        map.put(
                Type.CHAR_TYPE, 
                model.getResource(XSDDatatype.XSDunsignedByte.getURI()));
        map.put(
                Type.getType(Character.class), 
                model.getResource(XSDDatatype.XSDunsignedByte.getURI()));
        
        map.put(
                Type.DOUBLE_TYPE, 
                model.getResource(XSDDatatype.XSDdouble.getURI()));
        map.put(
                Type.getType(Double.class), 
                model.getResource(XSDDatatype.XSDunsignedByte.getURI()));
        
        map.put(
                Type.FLOAT_TYPE, 
                model.getResource(XSDDatatype.XSDfloat.getURI()));
        map.put(
                Type.getType(Float.class), 
                model.getResource(XSDDatatype.XSDunsignedByte.getURI()));
        
        map.put(
                Type.INT_TYPE, 
                model.getResource(XSDDatatype.XSDint.getURI()));
        map.put(
                Type.getType(Integer.class), 
                model.getResource(XSDDatatype.XSDunsignedByte.getURI()));
        
        map.put(
                Type.LONG_TYPE, 
                model.getResource(XSDDatatype.XSDlong.getURI()));
        map.put(
                Type.getType(Long.class), 
                model.getResource(XSDDatatype.XSDunsignedByte.getURI()));
        
        map.put(
                Type.SHORT_TYPE, 
                model.getResource(XSDDatatype.XSDshort.getURI()));
        map.put(
                Type.getType(Short.class), 
                model.getResource(XSDDatatype.XSDunsignedByte.getURI()));
        
        map.put(
                Type.BYTE_TYPE, 
                model.getResource(XSDDatatype.XSDbyte.getURI()));
        map.put(
                Type.getType(Byte.class), 
                model.getResource(XSDDatatype.XSDunsignedByte.getURI()));
        
        //non primitive types
        map.put(
                Type.getType(String.class), 
                model.getResource(XSDDatatype.XSDstring.getURI()));
        map.put(
                Type.getType(byte[].class), 
                model.getResource(XSDDatatype.XSDbase64Binary.getURI()));
        map.put(
                Type.getType(Date.class), 
                model.getResource(XSDDatatype.XSDdate.getURI()));
        map.put(
                Type.getType(Calendar.class), 
                model.getResource(XSDDatatype.XSDdateTime.getURI()));
        
        map.put(
                Type.getType(Class.class), 
                RDFS.Class);
        
        
        return map;
    }
    
    public static String makeUriName(
            ObjectToTriplesConfiguration context,
            String className
            ){
        
        className = className.replace(".", "/");
        
        Type type = Type.getType(className);
        
        if(isArrayType(className)){
            type = type.getElementType();
        }
        
        for(String trimPrefix:context.getTrimPrefixes()){
            className = className.replace(trimPrefix, "");
        }
        
        className = className.replace(".", "/").replace("[]", "Array");
        className = className.replace("$", ".");
        
        StringBuilder sb = new StringBuilder();
        sb.append(context.getTargetNamespace());
        
        String[] pieces = className.split("/");
        
        if(!className.startsWith("/") && pieces.length > 1){
            sb.append("/");
        }
        for(int i=0;i<pieces.length-1;i++){
            sb.append(pieces[i]);
            
            if(i<pieces.length-2){
                sb.append("/");
            }
        }
        
        sb.append("#");
        
        sb.append(pieces[pieces.length-1]);
        
        return sb.toString();
    }
    
    
    public static void setNamespacePrefixes(ObjectToTriplesConfiguration context,Model model){
        
        Map<String,String> namespaceMappings = 
                getNamespaceMappings(context);
        
        for(String longName:namespaceMappings.keySet()){
            String shortName = namespaceMappings.get(longName);
            
            model.setNsPrefix(shortName, longName);
        }
        
        if(!context.isAutoGenerateAdditionalPrefixes()){
            return;
        }
        
        Set<String> longNames = getLongNames(namespaceMappings);
        Set<String> uris = getUris(model);
        
        Map<String,String> derivedMappings = new HashMap<>();
        
        for(String uri:uris){
            
            String match = null;
            for(String longName:longNames){
                
                if(uri.startsWith(longName)){
                    if(match == null || longName.length() > match.length()){
                        match = longName;
                    }
                }
            }
            
            if(match != null){
                
                if(!uri.contains("#")){
                    continue;
                }
                
                String temp = uri;
                temp = temp.replace(match, "");
                
                String[] parts = temp.split("#");
                
                if(parts.length != 2){
                    throw new RuntimeException("expected one # character in string " + temp);
                }
                
                String packageRemainder = parts[0];
                
                if(packageRemainder.isEmpty()){
                    continue;
                }
                
                if(packageRemainder.startsWith("/")){
                    packageRemainder = packageRemainder.substring(1);
                }

                final String oldLongName = match+"#";
                final String oldShortName = namespaceMappings.get(oldLongName);
                final String newShortName = oldShortName+"_"+packageRemainder.replace("/", "_");
                final String newLongName = match+"/"+packageRemainder+"#";
                
//                System.out.printf("%s -> %s\n\t%s -> %s\n", oldLongName, oldShortName, newLongName, newShortName);
                
                derivedMappings.put(newLongName, newShortName);
            }
        }
        
        for(String longName:derivedMappings.keySet()){
            String shortName = derivedMappings.get(longName);
            
            model.setNsPrefix(shortName, longName);
        }
    }
    
    private static Map<String,String> getNamespaceMappings(ObjectToTriplesConfiguration pluginContext){
        Map<String,String> namespaceMappings = new HashMap<>();
        
        for(String mapping:pluginContext.getNamespaceMappings()){
            
            String[] parts = mapping.split(" ");
            
            if (parts.length != 2) {
                pluginContext.getLog().warn("Namespace mapping should be a space separated pair of strings mapping a uri prefix to a short prefix.  Instead, got " + mapping + " (this will be ignored)");
                continue;
            }
            
            namespaceMappings.put(parts[0], parts[1]);
        }
        
        return namespaceMappings;
    }
}