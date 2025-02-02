package com.securboration.immortals.instantiation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;

import com.securboration.immortals.instantiation.bytecode.JarIngestor;
import com.securboration.immortals.instantiation.bytecode.SourceFinder;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.PassthroughTranslator;
import com.securboration.immortals.o2t.analysis.ObjectNode;
import com.securboration.immortals.o2t.analysis.ObjectPrinter;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.etc.ExceptionWrapper;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.bytecode.JarArtifact;
import com.securboration.immortals.ontology.bytecode.application.Classpath;

/**
 * Aggregation for the POJO instantiations to convert to triples. Performs the
 * conversion.
 * 
 * @author jstaples
 *
 */
public class InstancesToTriplesMain {
    
    private static String immortalsLibVersion = null;
    private static SourceFinder sourceFinder = null;
    private static String projectRoot = null;
    
    /**
     * Ingests a subset of the JARs in the IMMoRTALS shared directory and 
     * produces a model of the knowledge they contain.  The model is then
     * serialized and dumped to the local file system.
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        final String outputBasePath = args[0];//location to dump resulting model
        final String version = args[1];//version of resulting model
        final String projectRoot = args[2];//root of IMMoRTALS SVN checkout
        final String repoUrl = args[3];//url of checked-out SVN repo
        final String immortalsLibVersion = args[4];//version of SVN repo to use
        final String[] validPrefixes = args[5].split(",");//valid repo path prefixes
        
        /*
         * List of directories containing .java files that were compiled into
         * JARs in the local repository.  These must be provided at the root
         * of the classpath.
         */
        final int N = 6;
        final String[] sourceRoots = new String[args.length - N];
        System.arraycopy(args, N, sourceRoots, 0, args.length-N);

        InstancesToTriplesMain.projectRoot = projectRoot;
        InstancesToTriplesMain.immortalsLibVersion = immortalsLibVersion;
        
        ObjectToTriplesConfiguration config = 
                new ObjectToTriplesConfiguration(version);
        
        InstancesToTriplesMain.sourceFinder = 
                new SourceFinder(projectRoot,repoUrl,sourceRoots);
        
        Map<String, Object> namedInstances = getNamedInstances(validPrefixes);
        
        for (String key : namedInstances.keySet()) {
            final String outputPath = outputBasePath + "/" + key + ".ttl";
            
            System.out.println("converting POJO with tag " + key + " to model");
            
            Object value = namedInstances.get(key);
            Model model = ObjectToTriples.convert(config, value);
            
            System.out.println("writing model @" + outputPath);
            
            FileUtils.writeStringToFile(
                    new File(outputPath),
                    OntologyHelper.serializeModel(
                        model, 
                        "Turtle",
                        config.isValidateOntology()
                        )
                    );
        }
    }
    
   private static Map<String, Object> getNamedInstances(final String[] relativeBasePaths) throws IOException {
        final Map<String, Object> map = new HashMap<>();
        
        //ingest JARs from the shared repository
        for(String relativeBasePath:relativeBasePaths){
            addJarModels(
                relativeBasePath,
                map
                );
        }
    
        // TODO: ideally there would be a cleaner intermediate bytecode
        //artifact for us to analyze
        if(System.getProperty("analyzeATAK") != null){
            addClasspathModel(
                    "client/ATAKLite/build/intermediates/classes/release",
                    map
                    );
        }
       
        //fine-grained class models
//TODO: these could still be useful
//        if(System.getProperty("include-asm-models") != null)
//        {
//            for(ClassNode cn:annotationsToTriples.getClasses()){
//                map.put(
//                    "bytecode-class-models/"+cn.name.replace("/","."), 
//                    cn
//                    );
//            }
//        }
//        
        return map;
    }
    
    private static void addClasspathModel(
            String relativeClasspathRoot,
            Map<String,Object> map
            ) throws IOException{
        
        File classpathRoot = getFile(relativeClasspathRoot);
        
        if(!classpathRoot.exists()){
            throw new RuntimeException(
                    "unable to locate classpath root @ " + 
                    relativeClasspathRoot
                    );
        }
        
        System.out.println(
                "indexing classpath " + classpathRoot.getAbsolutePath());
        
        Classpath artifact = 
                JarIngestor.ingestDirectory(
                        classpathRoot, 
                        sourceFinder
                        );
        
        String pathTag = 
                relativeClasspathRoot.replace("/", "_").replace("\\", "_");
        
        map.put(
                "bytecode/"+pathTag+"_classpath",
                artifact
                );
        
    }
    
    private static void addJarModels(
            String relativeJarDir,
            Map<String,Object> map
            ) throws IOException{
        
        File jarDir = getFile(relativeJarDir);
        
        if(!jarDir.exists()){
            System.err.printf(
                    "unable to locate jar dir @ " , 
                    relativeJarDir);
            return;
        }
        
        for(File jar:FileUtils.listFiles(jarDir, new String[]{"jar"}, true)){
            System.out.println(
                "indexing jar " + jar.getAbsolutePath() + 
                " (" + jar.length()  + "B)"
                );
            
            JarArtifact artifact = 
                    JarIngestor.ingest(
                            FileUtils.readFileToByteArray(jar),
                            jar.getName(),
                            null,
                            null,
                            null,
                            sourceFinder 
                            );
            
            artifact.setCoordinate(JarIngestor.getCoordinate(jar));
            
            map.put(
                    "bytecode/"+jar.getName(),
                    artifact
                    );
        }
    }

    private static void printObject(Object o) {
        ExceptionWrapper.wrap(() -> {
            ObjectNode n = ObjectNode.build(new PassthroughTranslator(),o);
            ObjectPrinter.getPrinterVisitor();

            n.accept(ObjectPrinter.getPrinterVisitor());
        });
    }
    
    private static File getFile(String pathRelativeToProjectRoot){
        pathRelativeToProjectRoot = 
                pathRelativeToProjectRoot.replace(
                        "${version}", 
                        immortalsLibVersion);
        
        String path = projectRoot + "/" + pathRelativeToProjectRoot;
        
        return new File(path);
    }

}
