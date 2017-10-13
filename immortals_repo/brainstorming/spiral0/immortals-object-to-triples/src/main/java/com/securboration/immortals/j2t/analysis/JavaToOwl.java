package com.securboration.immortals.j2t.analysis;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import com.securboration.immortals.o2t.etc.ExceptionWrapper;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.annotations.triples.Triple;

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
    
                OntologyHelper.addMetadata(context,model,ns);
                OntologyHelper.setNamespacePrefixes(context,model);
    
                final String serializedOntology = 
                        OntologyHelper.serializeModel(model, outputLang);
    
                context.getLog().info(
                        "writing result to " + outputHere.getAbsolutePath());
    
                FileUtils.writeStringToFile(outputHere, serializedOntology);
            });
        }

        return model;
    }
    
    private void analyzeClass(String className) {

        context.getLog().info("Analyzing class: " + className);

        ExceptionWrapper.wrap(() -> {
            analyzeClass(context.getClassloader().loadClass(className));
        });
    }
    
    private void analyzeClass(Class<?> c){
        
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
        Resource superResource = 
                OntologyHelper.getResourceForType(context,model,superClass);
        
        OntologyHelper.addTriples(
                context,
                model,
                classResource,
                getTriplesFromClass(c));
        
        if(superClass.equals(null) || superClass.equals(Object.class)){
            //it's an Object or its super class is Object so make it a base 
            //class
            classResource.addProperty(
                    RDFS.subClassOf, 
                    OntologyHelper.getBaseResource(context, model)
                    );
        } else {
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
