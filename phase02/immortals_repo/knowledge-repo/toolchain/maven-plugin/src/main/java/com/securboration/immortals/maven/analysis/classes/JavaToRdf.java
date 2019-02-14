package com.securboration.immortals.maven.analysis.classes;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.securboration.immortals.maven.PluginContext;
import com.securboration.immortals.maven.etc.ExceptionWrapper;
import com.securboration.immortals.maven.ontology.OntologyHelper;

/**
 * Analysis starts here
 *
 * @author jstaples
 *
 */
public class JavaToRdf {
    private final PluginContext context;

    public JavaToRdf(PluginContext pluginContext) {
        this.context = pluginContext;
    }

    private final String ns = "http://securboration.com/immortals/r1.0";
    private Model model =
            ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
//            ModelFactory.createDefaultModel();

    private Map<String,Resource> classesToResources = new HashMap<>();


    private static Model getInferencingModel(Model model)
    {
        return model;

//        return
//                ModelFactory.createInfModel(
//                        ReasonerRegistry.getTransitiveReasoner(),
//                        model);
    }

    /**
     *
     * @param analyzeThese
     *            classpath entries where we should start the analysis
     * @param classpath
     *            a mechanism for retrieving classpath items
     * @throws ClassNotFoundException
     */
    public void analyze() throws ClassNotFoundException {

        final String classpathPrefix = context.getTargetDir().getAbsolutePath().replace("\\", "/") + "/classes/";

//      System.out.printf("classpath prefix = %s\n", classpathPrefix);

      for(File classFile:FileUtils.listFiles(context.getTargetDir(), new String[]{"class"}, true))
      {
//          System.out.printf("found class %s\n", classFile.getAbsolutePath());

          final String path = classFile.getAbsolutePath().replace("\\", "/");
          String className = path.replace(classpathPrefix, "").replace(".class", "");

//          System.out.printf("\tafter removing prefix: %s\n", className);

          className = className.replace("/", ".");

//          System.out.printf("\t%s\n",className);

          analyzeClass(className);
      }

      ExceptionWrapper.wrap(()->{

          model = getInferencingModel(model);

          System.out.printf(
                  "serialized ontology:\n%s\n",
                  OntologyHelper.serializeModel(model, "Turtle"));
      });

    }


//    <owl:Restriction>
//    <owl:onProperty rdf:resource="#madeFromGrape"/>
//    <owl:maxCardinality rdf:datatype="http://www.w3.org/2001/XMLSchema#nonNegativeInteger">1</owl:maxCardinality>
//    </owl:Restriction>


    private static String makeUriName(String className){

        className = className.replace(".", "/");

        Type type = Type.getType(className);

        if(isArrayType(className)){
            type = type.getElementType();
        }

        return className.replace("com/securboration/immortals", "").replace(".", "/").replace("[]", "Array");
    }

    private File getFileForClass(String className){

        String classResource = className.replace(".", "/") + ".java";

        for(String root:context.getProject().getCompileSourceRoots())
        {
            final String path = root + "/" + classResource;

            File f = new File(path);

            if(f.exists()){
                return f;
            }
        }

        context.getLog().warn("could not find " + className + " on roots " + context.getProject().getCompileSourceRoots());

        return null;
    }

    private static boolean isArrayType(String className){
        return Type.getType(className).getSort() == Type.ARRAY;
    }

    private String getClassComments(File f, String className){

        StringBuilder sb = new StringBuilder();
        ExceptionWrapper.wrap(()->{
            final String content = FileUtils.readFileToString(f);
            String[] parts = content.split("class " + getSimpleName(className));

            String[] headerLines = parts[0].split("\\r?\\n");
            for(String line:headerLines){
                line = line.trim();
                if(line.startsWith("/*") || line.startsWith("*")){
                    sb.append(line);
                }
            }
        });

        sb.append("\n@creationTimestamp ");
        sb.append(new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z").format(new Date(f.lastModified())));

        String comment = sb.toString().replace("/**","").replace("*/","").replace("*", "").replace("@author Securboration", "\n@author jstaples");

//        System.out.printf("found class comment %s\n", comment);

        return comment;

    }

    public static void main(String[] args){
        String pattern="Resource resource;";
        String text =  "{ /**asfasdf*/Resource resource;}";
        text = text + text;

        System.out.printf("%d\n", text.indexOf(pattern));

        Pattern p = Pattern.compile(Pattern.quote(pattern));

        for(String v:p.split(text)){
            System.out.printf("value: %s\n", v);
        }
    }

    private String getFieldComments(File f, String className, FieldNode field){

        StringBuilder sb = new StringBuilder();
        ExceptionWrapper.wrap(()->{
            final String content = FileUtils.readFileToString(f);
            String[] parts = content.split("class " + getSimpleName(className));

            String body = parts[1];

//            System.out.printf("body= %s\n", body);


            String pattern =
                    Pattern.quote(
                            getSimpleName(Type.getType(field.desc).getClassName() + " " + field.name + ";"));
            Pattern p = Pattern.compile(pattern);

            String[] fieldSplits = p.split(body);

//            for(String split:fieldSplits){
//                System.out.printf("value: %s\n", split);//TODO
//            }
//
//            System.out.printf("pattern=%s\n", pattern);
//
//            System.out.printf("fieldSplit = %s\n",fieldSplits[0]);

            Stack<String> stack = new Stack<>();
            String[] bodyLines = fieldSplits[0].split("\\r?\\n");
            boolean stop = false;
            for(int i=0;i<bodyLines.length && !stop;i++){
                String line = bodyLines[bodyLines.length-1-i];

                line = line.trim();

                if(line.startsWith("/*"))
                {
                    stop = true;
                }
//                if(line.isEmpty())
//                {
//                    stop = true;
//                }

                stack.push(line);
            }

            while(!stack.isEmpty())
            {
                sb.append(stack.pop());
            }
        });

        sb.append("\n@creationTimestamp ");
        sb.append(new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z").format(new Date(f.lastModified())));

        String comment = sb.toString().replace("//", "").replace("/**","").replace("*/","").replace("*", "").replace("@author Securboration", "\n@author jstaples");

//        System.out.printf("found class comment %s\n", comment);

        return comment;

    }

    private Resource getResourceForType(String className){

        Resource resource = model.createResource(ns+"#"+makeUriName(className));

        File f = getFileForClass(className);
        if(f != null){
            String comment = getClassComments(f,className);

            resource.addLiteral(RDFS.comment, comment);
        }

        return resource;
    }

    private void analyzeClass(String className) {

        context.getLog().info("Analyzing entrypoint: " + className);

        ExceptionWrapper.wrap(() -> {

            final String classResourcePath = className.replace(".", "/")
                    + ".class";

            final InputStream classStream = context.getBuildPathClassloader()
                    .getResourceAsStream(classResourcePath);

            if(classStream == null){
                throw new NullPointerException("could not find class " + className + " using path " + classResourcePath);
            }

            // Load the class buffer into an ASM model.
            ClassReader cr = new ClassReader(classStream);
            ClassNode cn = new ClassNode();

            cr.accept(cn, 0);// 0 = Don't expand frames or compute stack/local
                             // mappings

            analyzeClass(cn);
        });
    }//TODO


    private Map<Type,Resource> getTypeMappings(){
        Map<Type,Resource> map = new HashMap<>();

        //primitive types
        map.put(
                Type.BOOLEAN_TYPE,
                model.getResource(XSDDatatype.XSDboolean.getURI()));
        map.put(
                Type.CHAR_TYPE,
                model.getResource(XSDDatatype.XSDunsignedByte.getURI()));
        map.put(
                Type.DOUBLE_TYPE,
                model.getResource(XSDDatatype.XSDdouble.getURI()));
        map.put(
                Type.FLOAT_TYPE,
                model.getResource(XSDDatatype.XSDfloat.getURI()));
        map.put(
                Type.INT_TYPE,
                model.getResource(XSDDatatype.XSDint.getURI()));
        map.put(
                Type.LONG_TYPE,
                model.getResource(XSDDatatype.XSDlong.getURI()));
        map.put(
                Type.SHORT_TYPE,
                model.getResource(XSDDatatype.XSDshort.getURI()));

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


        return map;
    }

    private static String getSimpleName(String className){
        className = className.replace(".", "/");
        String[] parts = className.split("/");

        return parts[parts.length-1];
    }


    private Resource analyzeType(Type t){

        Map<Type,Resource> mappings = getTypeMappings();

        Resource r = mappings.get(t);

        if(r != null){
            return r;
        }

        if(t.getSort()==Type.METHOD){
            throw new RuntimeException("wasnt expecting a method type here");
        } else if(t.getSort()==Type.ARRAY){
            //create an array wrapper

            Resource arrayResource = getResourceForType(t.getClassName());
            arrayResource.addProperty(RDFS.subClassOf,getResourceForType("com/securboration/immortals/array"));

            Property p = model.createProperty(ns+"#has" + getSimpleName(t.getElementType().getClassName()) + "Element");
            p.addProperty(RDF.type, RDF.Property);
            p.addProperty(RDFS.range, analyzeType(t.getElementType()));
            p.addProperty(RDFS.domain, arrayResource);

            return arrayResource;

        } else if(t.getSort()==Type.OBJECT){
            return getResourceForType(t.getClassName());
        }

        throw new RuntimeException("unhandled corner case");
    }

    /**
    Given a properties in a class
    e.g. NetworkStackAbstraction (found in immortals) ./resources/network/NetworkStackAbstraction.java

    public class NetworkStackAbstraction extends NetworkResource {

        \/**
         * Describes how messages will be reliably delivered in an environment where
         * individual messages may be lost or corrupted.
         *
         * E.g., TCP, UPD
         *\/
        private TransportLayerAbstraction transportLayer;

    ...goes to RDF/Turtle...

    IMMoRTALS:has_transportLayer
            a             owl:ObjectProperty ;
            rdfs:comment  " Describes how messages will be reliably delivered in an environment where individual messages may be lost or corrupted. E.g., TCP, UPD\n@creationTimestamp 2016.02.17 at 09:44:45 CST" ;
            rdfs:domain   IMMoRTALS_resources_network:NetworkStackAbstraction ;
            rdfs:range    IMMoRTALS_resources_network:TransportLayerAbstraction .

    */
    private Property getPropertyForField(Resource ownerClass,ClassNode cn,FieldNode field){

//        final String resourceUri = makeUriName(cn.name+"."+field.name);
//
//        final String fieldTypeUri = makeUriName(field.desc);



        Type t = Type.getType(field.desc);

//        if(t == Type.INT_TYPE){
//            t = Type.getType(Integer.class);
//        }
//
//        if(t == Type.LONG_TYPE){
//            t = Type.getType(Long.class);
//        }
//
//        if(t == Type.DOUBLE_TYPE){
//            t = Type.getType(Double.class);
//        }

        System.out.printf("%s\n", t.getClassName());

        Resource classResource = analyzeType(t);

//        analyzeType(classResource,t);


        Property p = model.createProperty(ns+"#has_"+field.name);
        p.addProperty(RDF.type, RDF.Property);
        p.addProperty(RDFS.range, classResource);
        p.addProperty(RDFS.domain, ownerClass);

        {
            File f = getFileForClass(cn.name);
            if(f != null){
                String comment = getFieldComments(f,cn.name,field);

                p.addLiteral(RDFS.comment, comment);
            }
        }

//        ownerClass.addProperty(p, arg1)

//        p.addProperty(RDFS., RDFS.Datatype);

//        ownerClass



        return p;
    }


//    <owl:Class>
//    <owl:oneOf rdf:parseType="Collection">
//      <owl:Thing rdf:about="#Eurasia"/>
//      <owl:Thing rdf:about="#Africa"/>
//      <owl:Thing rdf:about="#NorthAmerica"/>
//      <owl:Thing rdf:about="#SouthAmerica"/>
//      <owl:Thing rdf:about="#Australia"/>
//      <owl:Thing rdf:about="#Antarctica"/>
//    </owl:oneOf>
//  </owl:Class>

    private Resource getEnum(){
        return getResourceForType("com/securboration/immortals/enumeration");
    }

    private void analyzeEnum(ClassNode cn){

        Resource enumeration =
                getResourceForType(cn.name);

        enumeration.addProperty(RDFS.subClassOf, getEnum());

        for(FieldNode f:cn.fields){
            System.out.printf("\tenum %s has field %s\n", cn.name, f.name);

            if(f.name.equals("$VALUES")){
                continue;
            }

            Resource r = getResourceForType(cn.name+"/"+f.name);

            r.addProperty(RDF.type, enumeration);

//            enumeration.addOneOf(r);
        }

//        RDFList list = model.createList(resources.iterator());
//
//        enumeration.setOneOf(list);


//
//        enumeration.setOneOf(list);

    }

    /**
    Given a properties in a class
    e.g. NetworkStackAbstraction (found in immortals) ./resources/network/NetworkStackAbstraction.java

    \/**
     * Standard OSI model of a network stack
     *
     * @author Securboration
     *
     *\/
    public class NetworkStackAbstraction extends NetworkResource { ... }

      ...goes to RDF/Turtle...

    IMMoRTALS_resources_network:NetworkStackAbstraction
            a                owl:Class ;
            rdfs:comment     " Standard OSI model of a network stack \n@author jstaples\n@creationTimestamp 2016.02.17 at 09:44:45 CST" ;
            rdfs:subClassOf  IMMoRTALS_resources:NetworkResource .
    */
    private void analyzeClass(ClassNode cn){

        if((cn.access & Opcodes.ACC_ENUM) > 0){
            analyzeEnum(cn);
            return;
        }

        Resource firstClass = getResourceForType(cn.name);
        Resource secondClass = getResourceForType(cn.superName);

        if(!cn.superName.equals("java/lang/Object"))
        {
            firstClass.addProperty( RDFS.subClassOf, secondClass);
        }


//        <rdf:Description rdf:about="http://securboration.com/immortals#HasClasspath">
//            <rdf:type rdf:resource="http://www.w3.org/2000/01/rdf-schema#Datatype"/>
//            <rdfs:range rdf:resource="http://securboration.com/immortals#Classpath"/>
//            <rdfs:comment rdf:datatype="http://www.w3.org/2001/XMLSchema#string">Specifies that an analysis package has a classpath</rdfs:comment>
//            <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">HasClasspath datatype property</rdfs:label>
//            <rdfs:domain rdf:resource="http://securboration.com/immortals#BytecodeAnalysisPackage"/>
//            <rdfs:subClassOf rdf:resource="http://securboration.com/immortals#JvmBytecodeProperty"/>
//        </rdf:Description>

        for(FieldNode f:cn.fields){

            System.out.printf("\t%s %s\n", f.name,f.desc);
            Property p = getPropertyForField(firstClass,cn,f);

        }


//
//
//
//        //TODO: walk over the fields and create properties for them
//
//        DynamicClassWriter cw = new DynamicClassWriter(0, context);
//
//        List<String> traversal = cw.getTraversal(cn.name);
//
//        int count = 0;
//        for(String s:traversal){
//
//            for(int i=0;i<count;i++){
//                System.out.printf("  ");
//            }
//            System.out.printf("%s\n",s);
//            count++;
//        }


    }//TODO

}
