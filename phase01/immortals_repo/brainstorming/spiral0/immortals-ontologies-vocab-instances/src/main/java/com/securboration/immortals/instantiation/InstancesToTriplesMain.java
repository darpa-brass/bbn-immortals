package com.securboration.immortals.instantiation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;

import com.securboration.immortals.instantiation.bytecode.AnnotationsToTriples;
import com.securboration.immortals.instantiation.bytecode.JarIngestor;
import com.securboration.immortals.instantiation.bytecode.SourceFinder;
import com.securboration.immortals.instantiation.bytecode.UriMappings;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectNode;
import com.securboration.immortals.o2t.analysis.ObjectPrinter;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.etc.ExceptionWrapper;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.bytecode.BytecodeArtifact;

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
    private static AnnotationsToTriples annotationsToTriples = null;
    private static String projectRoot = null;
    
    /**
     * 
     * @param args
     *            args[0] is the output base path, args[1] is the version to
     *            apply to the generated ontologies
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        final String outputBasePath = args[0];
        final String version = args[1];
        projectRoot = args[2];
        final String repoUrl = args[3];
        immortalsLibVersion = args[4];
        
        final int N = 5;
        final String[] sourceRoots = new String[args.length - N];
        System.arraycopy(args, N, sourceRoots, 0, args.length-N);
        
        ObjectToTriplesConfiguration config = 
                getObjectToTriplesConfig(version);
        
        sourceFinder = new SourceFinder(projectRoot,repoUrl,sourceRoots);
        annotationsToTriples = getAnnotationsToTriples(config);

        Map<String, Object> namedInstances = getNamedInstances();
        for (String key : namedInstances.keySet()) {
            final String outputPath = outputBasePath + "/" + key + ".ttl";
            
            System.out.println("converting POJO with tag " + key + " to model");
            
            Object value = namedInstances.get(key);
            Model model = ObjectToTriples.convert(config, value);
            
            System.out.println("writing model @" + outputPath);
            
            FileUtils.writeStringToFile(
                    new File(outputPath),
                    OntologyHelper.serializeModel(model, "Turtle"));
        }

    }
    
    private static AnnotationsToTriples getAnnotationsToTriples(ObjectToTriplesConfiguration config){
        
        UriMappings uriMappings = new UriMappings(config);
        
        return new AnnotationsToTriples(uriMappings);
    }

    private static ObjectToTriplesConfiguration getObjectToTriplesConfig(
            final String version
            ) {
        // TODO: other constants could come from args, for now they're hard
        // coded

        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration();
        config.setNamespaceMappings(
                Arrays.asList("http://darpa.mil/immortals/ontology/" + version
                        + "# IMMoRTALS"));
        config.setTargetNamespace(
                "http://darpa.mil/immortals/ontology/" + version);
        config.setOutputFile(null);
        config.setTrimPrefixes(
                Arrays.asList("com/securboration/immortals/ontology"));

        return config;
    }

    private static Map<String, Object> getNamedInstances() throws IOException {
        Map<String, Object> map = new HashMap<>();

        // TODO

        // test objects
        {
            map.put("test/test0",
                    Arrays.asList(new int[][]{{1,2,3},{4,5,6},{7,8,9}}));
            
            map.put("test/test1",
                    Arrays.asList("test1", "test2", '3', 4d, 5, true, new Object[][][][]{{{{1,3,"5"},{'a',Arrays.asList("test")}}}}));
        }
        
        // bytecode
        {
            addJarModels(
                    "shared/IMMORTALS_REPO",
                    map);
            
            // TODO: ideally there would be a cleaner intermediate bytecode
            //artifact for us to analyze
            if(System.getProperty("analyzeATAK") != null){
                addClasspathModel(
                        "client/ATAKLite/build/intermediates/classes/release",
                        map);
            }
        }

        // deployment model
        {
            //TODO: waiting on Vanderbult to attempt to build a deployment model
//            map.put("deployment/deployment-model", getDeploymentInstance());
        }
        
        //dfus
        {
            map.put(
                    "functionality/dfus", 
                    annotationsToTriples.getDfus()
                    );
        }
        
        return map;
    }
    
    private static void addClasspathModel(
            String relativeClasspathRoot,
            Map<String,Object> map
            ) throws IOException{
        
        File classpathRoot = getFile(relativeClasspathRoot);
        
        if(!classpathRoot.exists()){
            System.err.printf(
                    "unable to locate classpath root @ " , 
                    relativeClasspathRoot);
            return;
        }
        
        System.out.println(
                "indexing classpath " + classpathRoot.getAbsolutePath());
        
        BytecodeArtifact artifact = 
                JarIngestor.ingest(
                        classpathRoot, 
                        sourceFinder, 
                        annotationsToTriples
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
            
//            if(!jar.getName().contains(immortalsLibVersion)){
//                continue;
//            }//TODO
            
            System.out.println("indexing jar " + jar.getAbsolutePath() + " (" + jar.length()  + "B)");
            
            BytecodeArtifact artifact = 
                    JarIngestor.ingest(
                            FileUtils.readFileToByteArray(jar), 
                            null,
                            null,
                            null,
                            sourceFinder, 
                            annotationsToTriples
                            );
            
            artifact.setCoordinate(JarIngestor.getCoordinate(jar));
            
            map.put(
                    "bytecode/"+jar.getName(),
                    artifact);
        }
    }

    private static void printObject(Object o) {
        ExceptionWrapper.wrap(() -> {
            ObjectNode n = ObjectNode.build(o);
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
