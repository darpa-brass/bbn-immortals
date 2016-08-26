package com.securboration.immortals.j2t.analysis;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import com.securboration.immortals.annotations.helper.AnnotationHelper;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.etc.ExceptionWrapper;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;

/**
 * Converts java classes to a semantic model
 * 
 * @author jstaples
 *
 */
public class JavaToOwl {
    private final JavaToTriplesConfiguration context;
    
    private final String ns;
    private final File outputHere;
    private final String outputLang;

    public JavaToOwl(JavaToTriplesConfiguration pluginContext) {
        this.context = pluginContext;
        
        this.outputHere = pluginContext.getOutputFile();
        this.outputLang = pluginContext.getOutputLanguage();
        
        this.ns = pluginContext.getTargetNamespace();
    }
    
    
    private final OntModel model = 
            ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    
    private boolean shouldAnalyze(String className){
        for(String prefix:context.getSkipPrefixes()){
            if(className.startsWith(prefix)){
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 
     * @param analyzeThese
     *            classpath entries where we should start the analysis
     * @param classpath
     *            a mechanism for retrieving classpath items
     * @throws ClassNotFoundException
     */
    public Model analyze() throws ClassNotFoundException {
        
        for(String classPath:context.getClassPaths()){
            final File targetDir = new File(classPath);
            
            if(!targetDir.isDirectory()){
                throw new RuntimeException(classPath + " is not a directory");
            }
            
            final String classpathPrefix = 
                    targetDir.getAbsolutePath().replace("\\", "/")+"/";
            
            for (File classFile : 
                FileUtils.listFiles(
                        targetDir,
                        new String[] { "class" }, 
                        true)) {
                
                final String path = 
                        classFile.getAbsolutePath().replace("\\", "/");
                String className = 
                        path.replace(classpathPrefix, "").replace(".class", "");
    
                className = className.replace("/", ".");
    
                if(!shouldAnalyze(className)){
                    
                    context.getLog().info(
                            "Skipping " + className + 
                            " because of a skip prefix match");
                    continue;
                }
    
                analyzeClass(className);
            }
    
            ExceptionWrapper.wrap(() -> {
    
                OntologyHelper.addMetadata(context,model,ns,outputHere);
                OntologyHelper.setNamespacePrefixes(context,model);
    
                final String serializedOntology = 
                        OntologyHelper.serializeModel(model, outputLang);
    
                if(outputHere != null){
                    context.getLog().info(
                            "writing result to " + outputHere.getAbsolutePath());
        
                    FileUtils.writeStringToFile(outputHere, serializedOntology);
                }
            });
        }

        return model;
    }
    
    public Model analyze(Collection<Class<?>> classes) throws ClassNotFoundException {
        for(Class<?> analyzeThis:classes){
            analyzeClass(analyzeThis.getName());
        }

        ExceptionWrapper.wrap(() -> {

            OntologyHelper.addMetadata(context,model,ns,outputHere);
            OntologyHelper.setNamespacePrefixes(context,model);

            final String serializedOntology = 
                    OntologyHelper.serializeModel(model, outputLang);

            if(outputHere != null){
                context.getLog().info(
                        "writing result to " + outputHere.getAbsolutePath());
    
                FileUtils.writeStringToFile(outputHere, serializedOntology);
            }
        });

        return model;
    }
    
    private void analyzeClass(String className) {

        context.getLog().info("Analyzing class: " + className);

        ExceptionWrapper.wrap(() -> {
            analyzeClass(context.getClassloader().loadClass(className));
        });
    }
    
    private void instantiateConcept(Class<?> conceptInstance){
        
        System.out.println(
            "instantiating concept " + conceptInstance.getName());
        
        Constructor<?> constructor;
        try {
            constructor = conceptInstance.getConstructor();
            
            Model m = 
                ObjectToTriples.convert(
                    context, 
                    constructor.newInstance()
                    );
            
            this.model.add(m);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void analyzeClass(Class<?> c){
        
        if(AnnotationHelper.containsAnnotation(c,Ignore.class)){
            //skip anything explicitly marked as ignorable
            return;
        }
        
        if(AnnotationHelper.containsAnnotation(c,ConceptInstance.class)){
            instantiateConcept(c);
        }
        
        if(c.isEnum()){
            
            /**
             * Enum definitions are embedded in datatype properties with range
             * restrictions on type string
             * 
             * So if a class has a field that's an enum, a property restricting
             * that field to the possible enum values will be generated for the
             * class.
             */
            return;
        }
        
        Class<?> superClass = c.getSuperclass();
        
        Resource classResource = 
                OntologyHelper.getResourceForType(context,model,c);
        
        OntologyHelper.addTriples(
                context,
                model,
                classResource,
                getTriplesFromClass(c));
        
        if((superClass==null) || superClass.equals(Object.class)){
            //it's an Object or its super class is Object so make it a base 
            //class
            classResource.addProperty(
                    RDFS.subClassOf, 
                    OntologyHelper.getBaseResource(context, model)
                    );
        } else {
            Resource superResource = 
                    OntologyHelper.getResourceForType(context,model,superClass);
            
            classResource.addProperty( 
                    RDFS.subClassOf, 
                    superResource
                    );
        }
        
        //add properties for all of the class fields to the model
        for(Field f:c.getDeclaredFields()){
            Property fieldProperty = 
                    OntologyHelper.getPropertyForField(
                            context,
                            model,
                            classResource,
                            c,
                            f);
            
            //pull any triples from annotations on the field
            OntologyHelper.addTriples(
                    context,
                    model,
                    fieldProperty,
                    getTriplesFromField(f)
                    );
        }
    }
    
    private static Triple[] getTriplesFromClass(Class<?> c){
        return c.getAnnotationsByType(Triple.class);
    }
    
    private static Triple[] getTriplesFromField(Field f){
        return f.getAnnotationsByType(Triple.class);
    }

}
