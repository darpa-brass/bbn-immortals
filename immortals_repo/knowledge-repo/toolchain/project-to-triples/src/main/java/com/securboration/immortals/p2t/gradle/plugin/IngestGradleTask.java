package com.securboration.immortals.p2t.gradle.plugin;

import java.io.*;
import java.util.*;

import com.securboration.dataflow.analyzer.DataflowAnalyzerPlatform;
import com.securboration.immortals.da.api.EssDynamicAnalysis;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.analysis.DataflowGraphComponent;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.gmei.ApplicationArchitecture;
import com.securboration.immortals.ontology.resources.Client;
import com.securboration.immortals.ontology.resources.PlatformResource;
import com.securboration.immortals.ontology.resources.Software;
import com.securboration.immortals.project2triples.ProjectToTriplesMain;
import com.securboration.immortals.repo.query.TriplesToPojo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import com.securboration.immortals.constraint.ConstraintAssessment;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.ratiocinate.engine.RatiocinationEngine;
import com.securboration.immortals.ratiocinate.engine.RatiocinationReport;
import com.securboration.immortals.repo.model.build.JarIngestor;
import com.securboration.immortals.utility.GradleTaskHelper;
import soot.G;
import soot.Scene;
import soot.options.Options;
import soot.toolkits.scalar.Pair;

import static com.securboration.dataflow.analyzer.DataflowAnalyzerPlatform.parseCallTraceStack;

public class IngestGradleTask extends ImmortalsGradleTask {

    @TaskAction
    public void ingest() throws Exception {

        Project p = getProject();

        Project parentProj = null;
        while (parentProj == null || (parentProj.getParent() != null)) {
            parentProj = p.getParent();
        }

        String projArtifactId = p.getName();
        String projGroupId = p.getGroup().toString();
        String projVersion = p.getVersion().toString();
        int baseVocabTriples = 0;

        List<File> files = new ArrayList<>();
        String pluginOutput = null;
        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");

        try  {
            pluginOutput = String.valueOf(p.getProperties().get("pluginOutput"));
        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO RETRIEVE PLUGIN OUTPUT DIR, MAKE SURE PROPERTY IS SET");
        }

        try {
            String fusekiEndpoint = String.valueOf(p.getProperties().get("fusekiEndpoint"));
            initFuseki(fusekiEndpoint);
        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO RETRIEVE FUSEKI ENDPOINT, MAKE SURE PROPERTY IS SET");

        }

        final String version = "r2.0.0";
        final String graphName = projGroupId + "-" + projArtifactId + "-" + projVersion;

        GradleTaskHelper taskHelper = new GradleTaskHelper(client, graphName);
        taskHelper.setPw(new PrintWriter(System.out));

        {//get a list of all TTL files to process
            File pathFile = new File(pluginOutput);

            if(!pathFile.exists()){
                throw new RuntimeException("no file found for path " + pluginOutput);
            }

            if(pathFile.isDirectory()){
                files.addAll(
                        FileUtils.listFiles(pathFile, new String[]{"ttl"}, true)
                );
            } else if(pathFile.isFile()) {
                files.add(pathFile);
            } else {
                throw new RuntimeException(pluginOutput + " is not a normal file or dir");
            }
        }

        {//load everything into fuseki
            for(File f:files){
                System.out.println("READING IN: " + f.getName());
                if (!(f.length() > 50000000)) {
                    Model m = ModelFactory.createDefaultModel();
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
                    client.addToModel(m, graphName);
                } else {
                    System.err.println("File: " + f.getName() + " is too large for analysis, skipping...");
                }
            }
        }

        InputStream jarStream =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ontology/immortals-ontologies-package-" + version + ".jar");

        ByteArrayOutputStream jarBytes = new ByteArrayOutputStream();
        IOUtils.copy(jarStream, jarBytes);

        baseVocabTriples+=JarIngestor.ingest(client, jarBytes.toByteArray(), ns, version, graphName, ".ttl");

        int inferenceTriplesAdded = 0;
        {
            RatiocinationEngine engine =
                    new RatiocinationEngine(client,graphName);

            RatiocinationReport report = engine.execute();
            inferenceTriplesAdded+=report.getTriplesAdded();
        }

        String projectUUID = getProjectUUID(projGroupId, projArtifactId, projVersion, graphName);

        File userGradleHome = p.getGradle().getGradleUserHomeDir();
        userGradleHome = new File(userGradleHome.getAbsolutePath() + File.separator + "daemon" + File.separator +
                p.getGradle().getGradleVersion() + "/");
        initSootForDataflowAnalysis(projectUUID, p.getBuildDir().getAbsolutePath(), taskHelper, userGradleHome);

        int domainTriplesAdded = 0;
        String domainKnowledge = null;

        String dataflowPath = performDataflowAnalysis(pluginOutput, projArtifactId);
        DataflowAnalyzerPlatform dataflowAnalyzerPlatform = new DataflowAnalyzerPlatform(taskHelper.getClient(), graphName);
        try {
            domainKnowledge = String.valueOf(p.getProperties().get("domainKnowledge"));

            File domainKnowledgeFile = new File(domainKnowledge);
            if (!domainKnowledgeFile.exists()) {
                throw new Exception();
            }

            Model m = ModelFactory.createDefaultModel();
            m.read(
                    new ByteArrayInputStream(FileUtils.readFileToByteArray(domainKnowledgeFile)),
                    null,
                    "TURTLE"
            );
            domainTriplesAdded += m.getGraph().size();
            client.addToModel(m, graphName);

        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO RETRIEVE DOMAIN KNOWLEDGE TTL, MAKE SURE PROPERTY IS SET");
        }

        //TODO currently here, since I added another resource in the architecture, I now need to be more specific about which
        //TODO resource to delete
        String getAppResource = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_resources: <http://darpa.mil/immortals/ontology/r2.0.0/resources#>\n" +
                "prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "select ?resource where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t?arch IMMoRTALS:hasAvailableResources ?resource \n" +
                "\t; IMMoRTALS:hasProjectCoordinate ?coord .\n" +
                "\t\n" +
                "\t\t{?resource a  IMMoRTALS_resources:Device}\n" +
                "               UNION\n" +
                "\t\t{?resourceSubClass rdfs:subClassOf* IMMoRTALS_resources:Device .\n" +
                "\t\t?resource a ?resourceSubClass .}\n" +
                "\t?coord IMMoRTALS:hasArtifactId \"???ARTIFACT???\"\n" +
                "\t; IMMoRTALS:hasGroupId \"???GROUP???\"\n" +
                "\t; IMMoRTALS:hasVersion \"???VERSION???\" .\n" +
                "\t}\n" +
                "}\n" +
                "\t\n" +
                "\t";
        getAppResource = getAppResource.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???ARTIFACT???", projArtifactId)
                .replace("???GROUP???", projGroupId).replace("???VERSION???", projVersion);
        GradleTaskHelper.AssertableSolutionSet appResourceSolutions = new GradleTaskHelper.AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getAppResource, appResourceSolutions);

        Resource appResource = null;
        if (!appResourceSolutions.getSolutions().isEmpty()) {
            String appResourceUUID = appResourceSolutions.getSolutions().get(0).get("resource");
            appResource = (Resource) TriplesToPojo.convert(taskHelper.getGraphName(), appResourceUUID, taskHelper.getClient());

            //temporarily remove resource from graph
            String removeResource = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                    "\n" +
                    "DELETE WHERE {\n" +
                    "\tGRAPH<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                    "\t    ?arch IMMoRTALS:hasAvailableResources <???RESOURCE_UUID???> .\n" +
                    "\t}\n" +
                    "}";
            removeResource = removeResource.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???RESOURCE_UUID???", appResourceUUID);
            taskHelper.getClient().executeUpdate(removeResource);

        } else {
            throw new RuntimeException("UNABLE TO FIND RESOURCE SEMANTICS IN APPLICATION DOMAIN KNOWLEDGE");
        }

        Class<? extends Resource> dataflowResource = appResource.getClass();

        if (dataflowPath != null) {

            EssDynamicAnalysis essDynamicAnalysis = new EssDynamicAnalysis(parentProj.getProjectDir().getPath());
            File callTraces = essDynamicAnalysis.acquireDynamicAnalysisArtifact();

            List<Stack<String>> callTraceStackList = parseCallTraceStack(callTraces.getAbsolutePath());
            Set<Stack<DataflowGraphComponent>> dataflowGraphs = dataflowAnalyzerPlatform.processCallTraceStacks(callTraceStackList, dataflowResource);

            removeDuplicateFlows(dataflowGraphs);
            //TODO temporary excising of duplicates...stupid
           /* if (!dataflowGraphs.isEmpty()) {
                Iterator<Stack<DataflowGraphComponent>> dataflowGraphIter = dataflowGraphs.iterator();

                boolean removedDupOut = false;
                boolean removedDupIn = false;
                while (dataflowGraphIter.hasNext()) {
                    Stack<DataflowGraphComponent> graph = dataflowGraphIter.next();

                    if (graph.size() == 35 && !removedDupOut) {
                        dataflowGraphIter.remove();
                        removedDupOut = true;
                        continue;
                    }

                    if (graph.size() == 41 && !removedDupIn) {
                        dataflowGraphIter.remove();
                        removedDupIn = true;
                        continue;
                    }

                    if (removedDupIn && removedDupOut) {
                        break;
                    }
                }
            }*/
            //TODO temporary excising of duplicates...stupid

            Model m = ModelFactory.createDefaultModel();
            for (Stack<DataflowGraphComponent> dataflowGraphComponents : dataflowGraphs) {
                while (!dataflowGraphComponents.isEmpty()) {
                    DataflowGraphComponent dataflowGraphComponent = dataflowGraphComponents.pop();
                    m.add(ObjectToTriples.convert(config, dataflowGraphComponent));
                }
            }
            baseVocabTriples+=m.getGraph().size();
            ProjectToTriplesMain.recordCPElement(OntologyHelper.serializeModel(m, "TTL", false), dataflowPath +
                    File.separator + projGroupId + "-" + projArtifactId + "-" + projVersion + "-dataflows.ttl");
            client.addToModel(m, taskHelper.getGraphName());
            m.close();
        } else {
            //read in existing dataflows
            Collection<File> dataflowFiles = FileUtils.listFiles(new File(pluginOutput + File.separator + p.getName()
                    + File.separator + "dataflows"), new String[]{"ttl"}, false);
            for (File dataflowFile : dataflowFiles) {
                Model m = ModelFactory.createDefaultModel();
                try {
                    m.read(
                            new ByteArrayInputStream(FileUtils.readFileToByteArray(dataflowFile)),
                            null,
                            "TURTLE"
                    );
                } catch (FileNotFoundException exc) {
                    // relative path specified
                    m.read(
                            new ByteArrayInputStream(FileUtils.readFileToByteArray(dataflowFile.getAbsoluteFile())),
                            null,
                            "TURTLE"
                    );
                } catch (Exception exc) {
                    if (dataflowFile.getParentFile().getName().contains("Pax")) {
                        System.out.println("read pax file...");
                        continue;
                    } else {
                        exc.printStackTrace();
                    }
                }
                client.addToModel(m, graphName);
            }
        }

        Model m = ModelFactory.createDefaultModel();
        for (Pair<String, DataType> newData : dataflowAnalyzerPlatform.getClassNamesToNewData()) {

            Software applicationSoftware = new Software();
            applicationSoftware.setApplicationName(projArtifactId);
            applicationSoftware.setDataInSoftware(new DataType[]{newData.getO2()});

            String getArchUUID = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                    "\n" +
                    "select ?archUUID where {\n" +
                    "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                    "\t\n" +
                    "\t?aClass IMMoRTALS:hasClassName \"???CLASS_NAME???\" .\n" +
                    "\t\n" +
                    "\t?classArt IMMoRTALS:hasClassModel ?aClass\n" +
                    "\t; IMMoRTALS:hasHash ?hash .\n" +
                    "\t\n" +
                    "\t?javaProj IMMoRTALS:hasCompiledSourceHash ?hash\n" +
                    "\t; IMMoRTALS:hasCoordinate ?coord .\n" +
                    "\t\n" +
                    "\t?coord IMMoRTALS:hasArtifactId  ?archId\n" +
                    "\t; IMMoRTALS:hasGroupId ?groupId\n" +
                    "\t; IMMoRTALS:hasVersion ?version .\n" +
                    "\t\n" +
                    "\t?archUUID IMMoRTALS:hasProjectCoordinate ?archCoords .\n" +
                    "\t\n" +
                    "\t?archCoords IMMoRTALS:hasArtifactId ?archId\n" +
                    "\t; IMMoRTALS:hasGroupId ?groupId\n" +
                    "\t; IMMoRTALS:hasVersion ?version .\n" +
                    "\t}\n" +
                    "}";
            getArchUUID = getArchUUID.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                    .replace("???CLASS_NAME???", newData.getO1().replace(".", "/"));
            GradleTaskHelper.AssertableSolutionSet archUUIDSolutions = new GradleTaskHelper.AssertableSolutionSet();
            taskHelper.getClient().executeSelectQuery(getArchUUID, archUUIDSolutions);

            if (!archUUIDSolutions.getSolutions().isEmpty()) {
                String archUUID = archUUIDSolutions.getSolutions().get(0).get("archUUID");
                ApplicationArchitecture applicationArchitecture = new ApplicationArchitecture();
                Resource resource = dataflowResource.newInstance();
                if (resource instanceof Client) {
                    Client client = (Client) resource;
                    client.setResources(new PlatformResource[]{applicationSoftware});
                    applicationArchitecture.setAvailableResources(new Resource[]{client});
                    config.getNamingContext().setNameForObject(applicationArchitecture, archUUID);
                    m.add(ObjectToTriples.convert(config, applicationArchitecture));
                }
            }

            //TODO can only handle one data currently
            break;
            //TODO
        }

        taskHelper.getClient().addToModel(m, taskHelper.getGraphName());
        domainTriplesAdded+=m.getGraph().size();

        ConstraintAssessment.architectureAnalysis(taskHelper, inferenceTriplesAdded, domainTriplesAdded, baseVocabTriples, config);
    }

    private void removeDuplicateFlows(Set<Stack<DataflowGraphComponent>> dataflowGraphs) {

        Iterator<Stack<DataflowGraphComponent>> dataflowGraphIter1 = dataflowGraphs.iterator();
        int i = 1;
        while (dataflowGraphIter1.hasNext()) {
            Stack<DataflowGraphComponent> dataflowGraph1 = dataflowGraphIter1.next();
            Iterator<Stack<DataflowGraphComponent>> dataflowGraphIter2 = dataflowGraphs.iterator();
            for (int j = 0; j != i; j++) {
                if (!dataflowGraphIter2.hasNext()) {
                    return;
                } else {
                    dataflowGraphIter2.next();
                }
            }

            while (dataflowGraphIter2.hasNext()) {
                Stack<DataflowGraphComponent> dataflowGraph2 = dataflowGraphIter2.next();
                DataflowGraphComponent[] dataflowGraphComponents1 = dataflowGraph1.toArray(new DataflowGraphComponent[0]);
                DataflowGraphComponent[] dataflowGraphComponents2 = dataflowGraph2.toArray(new DataflowGraphComponent[0]);

                if (dataflowGraphComponents1[0].equals(dataflowGraphComponents2[0]) &&
                        dataflowGraphComponents1[dataflowGraphComponents1.length - 1].equals(
                                dataflowGraphComponents2[dataflowGraphComponents2.length - 1])) {
                    dataflowGraphIter1.remove();
                    i--;
                    break;
                }
            }
            i++;
        }
    }

    private Class<? extends Resource> retrieveResource() {
        //TODO
        return Client.class;
    }

    private String performDataflowAnalysis(String pluginOutput, String projName) throws IOException {

        File dfDir = new File(pluginOutput + File.separator + projName + File.separator + "dataflows");
        if (dfDir.exists()) {
            return null;
        } else {
            dfDir.mkdir();
            return dfDir.getAbsolutePath();
        }
    }

    private String getProjectUUID(String projGroupId, String projArtifactId, String projVersion, String graphName) {

        String getProjUUIDQuery = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select ?proj where {\n" +
                "    graph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t?proj IMMoRTALS:hasCoordinate ?coord .\n" +
                "\t\n" +
                "\t?coord IMMoRTALS:hasGroupId \"???GROUP???\"\n" +
                "\t; IMMoRTALS:hasArtifactId \"???ARTIFACT???\"\n" +
                "\t; IMMoRTALS:hasVersion \"???VERSION???\" .\n" +
                "\t}\n" +
                "}";
        getProjUUIDQuery = getProjUUIDQuery.replace("???GRAPH_NAME???", graphName).replace("???GROUP???", projGroupId)
                .replace("???ARTIFACT???", projArtifactId).replace("???VERSION???", projVersion);
        GradleTaskHelper.AssertableSolutionSet projUUIDSolutions = new GradleTaskHelper.AssertableSolutionSet();
        client.executeSelectQuery(getProjUUIDQuery, projUUIDSolutions);

        if (!projUUIDSolutions.getSolutions().isEmpty()) {
            return projUUIDSolutions.getSolutions().get(0).get("proj");
        } else {
            return null;
        }
    }

    private void initSootForDataflowAnalysis(String projectUUID, String pathToProjClasses, GradleTaskHelper taskHelper, File gradleUserHome) throws IOException {

        copySootResources(gradleUserHome);
        G.reset();
        Options.v().set_whole_program(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_output_format(Options.output_format_class);
        Options.v().set_no_writeout_body_releasing(true);
        //Options.v().set_java_version(Options.java_version_7);
        //TODO might need this PackManager.v().writeOutput();

        List<String> pathToDependencies = getProjectDependencies(projectUUID, taskHelper);
        StringBuilder sb = new StringBuilder();
        sb.append(Scene.v().getSootClassPath());
        sb.append(File.pathSeparatorChar);
        sb.append(pathToProjClasses);
        sb.append("/classes/java/main");
        sb.append(File.pathSeparatorChar);

        for (String pathToDependency : pathToDependencies) {
            sb.append(pathToDependency);
            sb.append(File.pathSeparatorChar);
        }

        //Scene.v().setSootClassPath(Scene.v().getSootClassPath()
        // + File.pathSeparatorChar + pathToProjClasses + "/classes/java/main/"
        // + File.pathSeparatorChar + pathToADSLJar);

        Scene.v().setSootClassPath(sb.toString());
        Scene.v().loadBasicClasses();
    }

    private void copySootResources(File gradleUserHome) throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("javabase.txt");
        if (is == null) {
            throw new RuntimeException("ERROR LOADING SOOT RESOURCES");
        }

        File sootResources = new File(gradleUserHome.getAbsolutePath() + File.separator + "javabase.txt");
        sootResources.createNewFile();
        sootResources.deleteOnExit();

        try (FileOutputStream out = new FileOutputStream(sootResources)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private List<String> getProjectDependencies(String projectUUID, GradleTaskHelper taskHelper) {

        List<String> dependencyPaths = new ArrayList<>();

        String queryForProjectDependencies = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "select ?dependencyPath where {\n" +
                "    graph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t<???PROJ_UUID???> IMMoRTALS:hasDependencies ?dependencies .\n" +
                "\t\n" +
                "\t?dependencies IMMoRTALS:hasSystemPath ?dependencyPath .\n" +
                "\t}\n" +
                "}";
        queryForProjectDependencies = queryForProjectDependencies.replace("???GRAPH_NAME???", taskHelper.getGraphName())
                .replace("???PROJ_UUID???", projectUUID);
        GradleTaskHelper.AssertableSolutionSet dependencySolutions = new GradleTaskHelper.AssertableSolutionSet();
        client.executeSelectQuery(queryForProjectDependencies, dependencySolutions);

        if (!dependencySolutions.getSolutions().isEmpty()) {
            for (GradleTaskHelper.Solution dependencySolution : dependencySolutions.getSolutions()) {
                String dependencyPath = dependencySolution.get("dependencyPath");
                dependencyPaths.add(dependencyPath);
            }
        }

        return dependencyPaths;
    }

    private final String ns = "${" + "immortalsNs"
            + ":"
            + "http://darpa.mil/immortals/ontology"
            + "}";

}
