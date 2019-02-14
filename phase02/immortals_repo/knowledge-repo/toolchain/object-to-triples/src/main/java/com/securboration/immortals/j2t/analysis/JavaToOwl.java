package com.securboration.immortals.j2t.analysis;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.objectweb.asm.Type;

import com.securboration.immortals.annotations.helper.AnnotationHelper;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.etc.ExceptionWrapper;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
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
    
    private Class<?> getMaximallyDisjointClassAlongC2(
            Class<?> c1,
            Class<?> c2
            ){
        
        if(c1.isAssignableFrom(c2)){
            throw new RuntimeException("expected disjoint classes");
        }
        
        if(c2.isAssignableFrom(c1)){
            throw new RuntimeException("expected disjoint classes");
        }
        
        boolean stop = false;
        Class<?> last = c2;
        while(!stop){
            if(c2 == null){
                throw new RuntimeException("no disjoint class found");
            } else if (c2.isAssignableFrom(c1)){
                return last;
            } else {
                last = c2;
                c2 = c2.getSuperclass();
            }
        }
        
        throw new RuntimeException("this should never be reached");
    }
    
    private void addDisjointAxioms(List<Class<?>> classes){
        
        if(!context.isAddDisjointAssertions()){
            return;
        }
        
        for(Class<?> c1:classes){
            for(Class<?> c2:classes){
                if(c1 == c2){
                    continue;
                }
                
                if(c1.isAssignableFrom(c2)){
                    continue;
                }
                
                if(c2.isAssignableFrom(c1)){
                    continue;
                }
                
                if(c1.isEnum()){
                    continue;
                }
                
                if(c2.isEnum()){
                    continue;
                }
                
                //c1 is disjoint from c2
                Resource rc1 = 
                        OntologyHelper.getResourceForType(
                            context, 
                            model, 
                            c1
                            );
                
                Resource rc2 = 
                        OntologyHelper.getResourceForType(
                            context, 
                            model, 
                            getMaximallyDisjointClassAlongC2(
                                c1,
                                c2
                                )
                            );
                
                model.add(rc1, OWL.disjointWith, rc2);
            }
        }
        
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
        
        final List<Class<?>> classes = new ArrayList<>();
        
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
                
                classes.add(context.getClassloader().loadClass(className));
                analyzeClass(className);
            }
            
            addDisjointAxioms(classes);
    
            ExceptionWrapper.wrap(() -> {
    
                OntologyHelper.addAutogenerationMetadata(context,model,ns,outputHere);
                OntologyHelper.setNamespacePrefixes(context,model);
    
                final String serializedOntology = 
                        OntologyHelper.serializeModel(
                            model, 
                            outputLang,
                            context.isValidateOntology()
                            );
    
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

            OntologyHelper.addAutogenerationMetadata(context,model,ns,outputHere);
            OntologyHelper.setNamespacePrefixes(context,model);

            final String serializedOntology = 
                    OntologyHelper.serializeModel(
                        model, 
                        outputLang,
                        context.isValidateOntology()
                        );

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
    
    
    
    private void analyzeClass(Class<?> c) throws IOException{
        
        if(AnnotationHelper.containsAnnotation(c,Ignore.class)){
            //skip anything explicitly marked as ignorable
            return;
        }
        
        if(AnnotationHelper.containsAnnotation(c,ConceptInstance.class)){
            instantiateConcept(c);
            return;
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
        
        classResource.addLiteral(RDFS.label,c.getSimpleName());
        
        OntologyHelper.addTriples(
                context,
                model,
                classResource,
                AnnotationHelper.getTriples(c)
                );
        
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
        
        Map<String,Set<String>> fieldRenaming = 
                ClassBytecodeHelper.getFieldNameMapping(c);
        
        Map<String,Method> propertyNamesToInterfaceMethods = 
                ClassBytecodeHelper.getPropertyToInterfaceMethodMapping(c);
        
        //add properties for all of the class fields to the model
        for(Field f:c.getDeclaredFields()){
            
            String fieldName = f.getName();
            
            Set<String> propertiesForField = fieldRenaming.get(fieldName);
            
            if(propertiesForField == null || propertiesForField.size() == 0){
                propertiesForField = new HashSet<>();
                propertiesForField.add(fieldName);
            }
            
            for(String propertyForField:propertiesForField){
                Method interfaceMethod = 
                        propertyNamesToInterfaceMethods.get(propertyForField);
                
                Property fieldProperty = 
                        OntologyHelper.getPropertyForField(
                                context,
                                model,
                                classResource,
                                c,
                                f,
                                propertyForField
                                );
                
                //pull any triples from annotations on the field
                OntologyHelper.addTriples(
                        context,
                        model,
                        fieldProperty,
                        AnnotationHelper.getTriples(f)
                        );
                
                if(interfaceMethod != null){
                    //pull any triples from annotations on the interface
                    OntologyHelper.addTriples(
                        context,
                        model,
                        fieldProperty,
                        AnnotationHelper.getTriples(interfaceMethod)
                        );
                }
            }
            
            
        }
        
        //add provenance iff enabled
        if(context.isAddSchemaPojoProvenance()){
            Property p = OntologyHelper.getPropertyForField(
                context, 
                model, 
                "pojoProvenance", 
                Type.getType(String.class)
                );
            
            model.add(classResource,p,c.getName());
        }
    }

}
