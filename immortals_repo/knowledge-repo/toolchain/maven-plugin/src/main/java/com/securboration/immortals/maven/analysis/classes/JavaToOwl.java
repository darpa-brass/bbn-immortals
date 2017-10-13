package com.securboration.immortals.maven.analysis.classes;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.DataRange;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.securboration.immortals.maven.PluginContext;
import com.securboration.immortals.maven.etc.ExceptionWrapper;
import com.securboration.immortals.maven.ontology.OntologyHelper;

/**
 * Converts a java class to a semantic model
 * 
 * This is just a proof of concept for now--it badly needs to be cleaned up
 * 
 * @author jstaples
 *
 */
public class JavaToOwl {
    private final PluginContext context;
    
    private final String ns;
    private final File outputHere;
    private final String outputLang;

    public JavaToOwl(PluginContext pluginContext) {
        this.context = pluginContext;
        
        this.outputHere = pluginContext.getOutputFile();
        this.outputLang = pluginContext.getOutputLanguage();
        
        this.ns = pluginContext.getTargetNamespace();
    }
    
    
    private final OntModel model = 
            ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

    
    private static InfModel getInferencingModel(OntModel model)
    {
//        return model;
        
        return
                ModelFactory.createInfModel(
                        ReasonerRegistry.getOWLReasoner(),
                        model);
    }
    
    private boolean shouldAnalyze(String className){
        for(String prefix:context.getSkipPackagePrefixes()){
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

        final String classpathPrefix = 
                context.getTargetDir().getAbsolutePath().replace("\\", "/") + "/classes/";

        // System.out.printf("classpath prefix = %s\n", classpathPrefix);

        for (File classFile : 
            FileUtils.listFiles(
                    context.getTargetDir(),
                    new String[] { "class" }, 
                    true)) {
            
            final String path = 
                    classFile.getAbsolutePath().replace("\\", "/");
            String className = 
                    path.replace(classpathPrefix, "").replace(".class", "");

            className = className.replace("/", ".");

            if(!shouldAnalyze(className)){
                
                context.getLog().info("Skipping " + className + " because of a skip prefix match");
                continue;
            }

            analyzeClass(className);
        }

        ExceptionWrapper.wrap(() -> {

            OntologyHelper.addMetadata(context,model,ns);
            OntologyHelper.setNamespacePrefixes(context,model);

            final String serializedOntology = OntologyHelper
                    .serializeModel(model, outputLang);

            context.getLog()
                    .info("writing result to " + outputHere.getAbsolutePath());

            FileUtils.writeStringToFile(outputHere, serializedOntology);
        });

        return model;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    private void analyzeClass(String className) {

        context.getLog().info("Analyzing class: " + className);

        ExceptionWrapper.wrap(() -> {

            final String classResourcePath = className.replace(".", "/")
                    + ".class";

            final InputStream classStream = context.getBuildPathClassloader()
                    .getResourceAsStream(classResourcePath);
            
            if(classStream == null){
                throw new NullPointerException(
                        "could not find class " + className + 
                        " using path " + classResourcePath);
            }

            // Load the class buffer into an ASM model.
            ClassReader cr = new ClassReader(classStream);
            ClassNode cn = new ClassNode();

            cr.accept(cn, 0);// 0 = Don't expand frames or compute stack/local
                             // mappings

            analyzeClass(cn);
        });
    }
    
    private boolean isEnum(ClassNode cn){
        return (cn.access & Opcodes.ACC_ENUM) > 0;
    }
    
    
    
    private Resource getEnum(){
        Resource r = 
                OntologyHelper.getResourceForType(context,model,"Enumeration");
        
        r.addProperty(RDFS.subClassOf, OWL.DataRange);
        
        return r;
    }
    
    
    
    private void analyzeEnum(ClassNode cn){
        
        Resource enumeration = 
                OntologyHelper.getResourceForType(context,model,cn.name);
        enumeration.addProperty(RDF.type, getEnum());
        
        RDFList list = model.createList();
        for (FieldNode f : cn.fields) {

            if (f.name.equals("$VALUES")) {
                continue;
            }

            list = list.cons(model.createTypedLiteral(f.name));
        }
        
        DataRange range = model.createDataRange(list);
        enumeration.addProperty(RDFS.range, range);
    }
    
    private void analyzeClass(ClassNode cn){
        
        if(isEnum(cn)){
            
            /**
             * Enum definitions are embedded in datatype properties with range
             * restrictions on type string
             * 
             * So if a class has a field that's an enum, a property restricting
             * that field to the possible enum values will be generated for the
             * class.
             */
//            analyzeEnum(cn);
            return;
        }
        
        Resource firstClass = 
                OntologyHelper.getResourceForType(context,model,cn.name);
        Resource secondClass = 
                OntologyHelper.getResourceForType(context,model,cn.superName);
        
        if(!cn.superName.equals("java/lang/Object"))
        {
            firstClass.addProperty( RDFS.subClassOf, secondClass);
        }
        
        //make properties for all of the class fields
        for(FieldNode f:cn.fields){
            //note: the properties are added to the model
            OntologyHelper.getPropertyForField(context,model,firstClass,cn,f);
        }
    }

}
