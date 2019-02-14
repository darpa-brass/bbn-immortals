package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.immortals.instantiation.annotationparser.bytecode.BytecodeHelper;
import com.securboration.immortals.instantiation.annotationparser.traversal.AnnotationParser;
import com.securboration.immortals.instantiation.annotationparser.traversal.JarTraverser;
import com.securboration.immortals.instantiation.bytecode.JarIngestor;
import com.securboration.immortals.instantiation.bytecode.SourceFinder;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.bytecode.JarArtifact;
import com.securboration.immortals.ontology.java.project.AnalysisMetrics;
import com.securboration.immortals.semanticweaver.ObjectMapper;
import com.securboration.immortals.utility.GradleTaskHelper;
import org.apache.commons.io.FileUtils;
import org.apache.jena.assembler.AssemblerHelp;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDF;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.securboration.immortals.utility.GradleTaskHelper.recordCPElement;

public class MineGradleTask extends ImmortalsGradleTask {

    private SourceFinder sf;
    private static final String splitter = System.getProperty("path.separator");

    @TaskAction
    public void mine() throws IOException {

        Project p = getProject();
        String pluginOutput = null;
        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");
        //TODO, don't need class files, just path to directory containing them...
        Long beginTime = System.currentTimeMillis();

        List<String> myList = new ArrayList<String>(Arrays.asList(p.fileTree("src").getAsPath().split(System.getProperty("path.separator"))));
        Set<String> sourceFiles = new HashSet<>();
        int mineTriples = 0;

        for(int i = 0; i < myList.size(); i++){
            String sourceFile = myList.get(i);
            if(sourceFile.contains(".java")){
                if (sourceFile.contains("/test/") || sourceFile.contains("\\test\\") || sourceFile.contains("/" + "androidTest" + "/")){
                    detectDirectory(sourceFile, sourceFiles);
                }
                else{
                    detectDirectory(sourceFile, sourceFiles);
                }
            }
        }

        String[] sourceFileArr = new String[sourceFiles.size()];
        int i = 0;
        for (String sourceFile : sourceFiles) {
            sourceFileArr[i] = sourceFile;
            i++;
        }

        sf = new SourceFinder(p.getRootDir().getAbsolutePath(), "[Not supplied]", sourceFileArr);

        try {
            ImmortalsGradlePlugin.ImmortalsPluginExtension extension = (ImmortalsGradlePlugin.ImmortalsPluginExtension) p.getExtensions().getByName("krgp");
            //TODO
            pluginOutput = extension.getTargetDir();
           // pluginOutput = "C:\\BBNImmortals\\knowledge-repo\\vocabulary\\ontology-static\\ontology\\_ANALYSIS\\_krgp";

        } catch (UnknownDomainObjectException exc) {
            pluginOutput = String.valueOf(p.getProperties().get("pluginOutput"));
        }
        if (pluginOutput == null) {
            pluginOutput = String.valueOf(p.getProperties().get("pluginOutput"));
        }

        GradleTaskHelper taskHelper = new GradleTaskHelper(client, null, pluginOutput, p.getName());

        String path = taskHelper.getResultsDir();
        List<File> files = new ArrayList<>();

        {//get a list of all TTL files to process
            File pathFile = new File(path);

            if(!pathFile.exists()){
                throw new RuntimeException("no file found for path " + path);
            }

            if (pathFile.isDirectory()) {
                files.addAll(FileUtils.listFiles(pathFile, new String[]{"ttl"}, true));
            } else if (pathFile.isFile()) {
                files.add(pathFile);
            } else {
                throw new RuntimeException(path + " is not a normal file or dir");
            }
        }

        Model m = ModelFactory.createDefaultModel();
        {
            for(File f:files){
                System.out.println("READING IN: " + f.getName());
                if (!(f.length() > 50000000)) {
                    try {
                        m.read(
                                new ByteArrayInputStream(FileUtils.readFileToByteArray(f)),
                                null,
                                "TURTLE"
                        );
                    } catch (FileNotFoundException exc) {
                      // relative path specified
                        m.read(
                                new ByteArrayInputStream(FileUtils.readFileToByteArray(f.getAbsoluteFile())),
                                null,
                                "TURTLE"
                        );
                    } catch (Exception exc) {
                        if (f.getParentFile().getName().contains("Pax")) {
                            System.out.println("read pax file...");
                            continue;
                        } else {
                            exc.printStackTrace();
                        }
                    }
                }
            }
        }

        StmtIterator metricIter = m.listStatements(new SimpleSelector(null, m.createProperty(
                "http://darpa.mil/immortals/ontology/r2.0.0#hasAnalysisMetrics"), (RDFNode) null));
        Resource currentProjectMetrics = null;
        if (metricIter.hasNext()) {
            Statement s = metricIter.nextStatement();

            currentProjectMetrics = (Resource) s.getObject();
        }

        Set<String> repoPaths = new HashSet<>();
        StmtIterator iter = m.listStatements(new SimpleSelector(null, m.createProperty(
                "http://darpa.mil/immortals/ontology/r2.0.0#hasDfuModuleRepo"), (RDFNode) null));

        List<Resource> dfuRepos = new ArrayList<>();
        while (iter.hasNext()) {

            Statement s = iter.nextStatement();

            if (s.getObject().isResource()) {
                dfuRepos.add(s.getObject().asResource());
            }
        }

        for (Resource dfuRepo : dfuRepos) {

            StmtIterator repoIter = m.listStatements(new SimpleSelector(dfuRepo, null, (RDFNode) null) {
                @Override
                public boolean selects(Statement s) {
                    return s.getPredicate().getLocalName().equals("hasPathToRepo");
                }
            });

            if (repoIter.hasNext()) {

                Statement repoStmt = repoIter.nextStatement();

                RDFNode repoPath = repoStmt.getObject();
                if (repoPath.isLiteral()) {
                    repoPaths.add(repoPath.asLiteral().getString());
                }
            }
        }

        Set<String> supplementaryClassFileArray = new HashSet<>();
        for (String repoPath : repoPaths) {

            File repoFile = new File(repoPath);

            if (repoFile.exists()) {

                List<File> repoResourceFiles = new ArrayList<>(FileUtils.listFiles(repoFile, new String[]{"jar"}, true));
                String[] supplementaryClassFiles = new String[repoResourceFiles.size()];
                for (i = 0; i < supplementaryClassFiles.length; i++) {
                    supplementaryClassFiles[i] = repoResourceFiles.get(i).getAbsolutePath();
                }

                AnnotationParser jarParser = new AnnotationParser(config, supplementaryClassFiles);
                repoResourceFiles.removeIf(jarFile -> jarFile.getName().contains("sources"));
                repoResourceFiles.removeIf(jarFile -> jarFile.getName().contains("all"));
                //TODO more accurate excision would be parsing the pom for for group name and removing if it belongs to immortals
                //repoResourceFiles.removeIf(jarFile -> jarFile.getName().contains("immortals"));

                supplementaryClassFileArray.addAll(repoResourceFiles.stream().map(file -> file.getAbsolutePath()).collect(Collectors.toList()));

                for (File repoResourceFile : repoResourceFiles) {

                    if (!(repoResourceFile.length() > 15000000)) {
                        JarArtifact jal = analyzeJar(repoResourceFile);
                        Model jarModel = ObjectToTriples.convert(config, jal);
                        mineTriples+=jarModel.getGraph().size();
                        recordCPElement(OntologyHelper.serializeModel(jarModel, "Turtle", false),
                                taskHelper.getResultsDir() + "/jars/JarMining-" + jal.getName() + ".ttl");

                        JarTraverser.traverseJar(repoResourceFile, jarParser);
                    }
                }

            } else {
                System.err.println("DFU MODULE NOT FOUND: " + repoPath);
            }
        }

        //////////////TODO//////////////
        myList = new ArrayList<>(Arrays.asList(p.fileTree("build").getAsPath().split(splitter)));
        ArrayList<String> testClassFiles = new ArrayList<>();
        for(i = 0; i < myList.size(); i++){
            String classFile = myList.get(i);
            if (classFile.endsWith(".class")) {
                if (classFile.contains("/test/") || classFile.contains("\\test\\") || classFile.contains(File.separator + "validation" + File.separator)){
                    testClassFiles.add(classFile);
                    myList.remove(i);
                }
            }
        }

        Configuration configuration = p.getConfigurations().getByName("compile");
        configuration.forEach(file -> supplementaryClassFileArray.add(file.getAbsolutePath()));
        supplementaryClassFileArray.add(p.getBuildDir().getAbsolutePath() + File.separator + "classes" + File.separator +
                    "java" + File.separator + "main");

        ObjectToTriplesConfiguration sourceAnnotConfig = new ObjectToTriplesConfiguration("r2.0.0");
        AnnotationParser sourceAnnotParser = new AnnotationParser(sourceAnnotConfig, supplementaryClassFileArray.toArray(new String[supplementaryClassFileArray.size()]));
        for (String mainClassFile : myList) {
            File classFile = new File(mainClassFile);
            if (classFile.exists()) {
                byte[] bytecode = new byte[0];
                try {
                    bytecode = FileUtils.readFileToByteArray(classFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String hash =
                        BytecodeHelper.hash(bytecode);
                sourceAnnotParser.visitClass(hash, bytecode);
            }
        }

        Model sourceAnnots = ModelFactory.createDefaultModel();
        sourceAnnotConfig.getMapper().getObjectsToSerialize().forEach(art -> sourceAnnots.add(ObjectToTriples.convert(sourceAnnotConfig, art)));
        mineTriples+=sourceAnnots.getGraph().size();
        String serialSourceAnnots = OntologyHelper.serializeModel(sourceAnnots, "TTL", false);
        recordCPElement(serialSourceAnnots, taskHelper.getResultsDir() + File.separator + p.getName() + File.separator + "dfus" + File.separator
                + "SourceAnnotations-"+ UUID.randomUUID().toString() + ".ttl");

        ObjectToTriplesConfiguration testAnnotConfig = new ObjectToTriplesConfiguration("r2.0.0");
        AnnotationParser testAnnotParser = new AnnotationParser(testAnnotConfig, supplementaryClassFileArray.toArray(new String[supplementaryClassFileArray.size()]));

        testClassFiles.forEach(testClass -> {
            File testFile = new File(testClass);
            if (testFile.exists()) {
                byte[] bytecode = new byte[0];
                try {
                    bytecode = FileUtils.readFileToByteArray(testFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String hash =
                        BytecodeHelper.hash(bytecode);
                testAnnotParser.visitClass(hash, bytecode);
            }
        });

        Model testAnnots = ModelFactory.createDefaultModel();
        testAnnotConfig.getMapper().getObjectsToSerialize().forEach(art -> testAnnots.add(ObjectToTriples.convert(testAnnotConfig, art)));
        mineTriples+=testAnnots.getGraph().size();
        String serialTestAnnots = OntologyHelper.serializeModel(testAnnots, "TTL", false);
        recordCPElement(serialTestAnnots, taskHelper.getResultsDir() + "/tests/"
                + "TestAnnotations-"+ UUID.randomUUID().toString() + ".ttl");
        /////////////TODO//////////////

        Model dfuModel = ModelFactory.createDefaultModel();

        ObjectMapper mapper = config.getMapper();

        if (mapper.getObjectsToSerialize().size() == 0){
            return;
        }

        for (Object o : mapper.getObjectsToSerialize()){
            Model objectModel = ObjectToTriples.convert(config, o);
            dfuModel.add(objectModel);
        }
        mineTriples+=dfuModel.getGraph().size();

        Long endTime = System.currentTimeMillis() - beginTime;

        AnalysisMetrics analysisMetricsDummy = new AnalysisMetrics();
        analysisMetricsDummy.setMineExec(endTime);
        analysisMetricsDummy.setMineTriples(mineTriples);
        config.getNamingContext().setNameForObject(analysisMetricsDummy, currentProjectMetrics.getURI());
        Model newMetrics = ObjectToTriples.convert(config, analysisMetricsDummy);

        dfuModel.add(newMetrics);
        UUID modelID = UUID.randomUUID();
        String serialModel = OntologyHelper.serializeModel(dfuModel, "Turtle", false);
        recordCPElement(serialModel, taskHelper.getResultsDir()
                + "/dfus/DfuMining-" + modelID +".ttl");
    }

    private JarArtifact analyzeJar(File jarFile) throws IOException {

        Path p = jarFile.toPath();
        byte[] jar = Files.readAllBytes(p);
        String name = p.getFileName().toString();
        BytecodeArtifactCoordinate bac;

        try{
            bac = JarIngestor.getCoordinate(p.toFile());
        }
        catch (Exception e){
            bac = new BytecodeArtifactCoordinate();
            bac.setArtifactId("[Error]");
            bac.setGroupId("[Error]");
            bac.setVersion("[Error]");
        }

        return JarIngestor.ingest(jar, name, bac.getGroupId(), bac.getArtifactId(), bac.getVersion(), sf, true);
    }
    private void detectDirectory(String src, Set<String> dirSet){
        int i = src.lastIndexOf(File.separator);
        if (i != -1){
            src = src.substring(0, i);
        }
        if (!dirSet.contains(src)){
            dirSet.add(src);
        }
    }
}
