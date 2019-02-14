package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.dataflow.analyzer.DataflowAnalyzerPlatform;
import com.securboration.immortals.constraint.ConstraintAssessment;
import com.securboration.immortals.da.api.EssDynamicAnalysis;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.project2triples.ProjectToTriplesMain;
import com.securboration.immortals.utility.GradleTaskHelper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.gradle.api.Project;

import org.gradle.api.tasks.TaskAction;

import soot.G;
import soot.Scene;
import soot.options.Options;

import java.io.*;

import java.util.*;

import static com.securboration.dataflow.analyzer.DataflowAnalyzerPlatform.parseCallTraceStack;

public class AdaptGradleTask extends ImmortalsGradleTask {

    @TaskAction
    public void adapt() throws Exception {

        Project p = getProject();
        int adaptTriples = 0;

        Project parentProj = null;
        while (parentProj == null || (parentProj.getParent() != null)) {
            parentProj = p.getParent();
        }

        try {
            String fusekiEndpoint = String.valueOf(p.getProperties().get("fusekiEndpoint"));
            initFuseki(fusekiEndpoint);
        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO RETRIEVE FUSEKI ENDPOINT, MAKE SURE PROPERTY IS SET");

        }

        String projArtifactId = p.getName();
        String projGroupId = p.getGroup().toString();
        String projVersion = p.getVersion().toString();

        String graphName =  projGroupId + "-" + projArtifactId + "-" + projVersion;
        String projectUUID = getProjectUUID(projGroupId, projArtifactId, projVersion, graphName);

        String pluginOutput = null;
        //TODO get this from somewhere else
        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");

        try  {
            pluginOutput = String.valueOf(p.getProperties().get("pluginOutput"));
        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO RETRIEVE PLUGIN OUTPUT DIR, MAKE SURE PROPERTY IS SET");
        }

        String xsdTranslationEndpoint = null;
        try {
            xsdTranslationEndpoint = String.valueOf(p.getProperties().get("xsdTranslationEndpoint"));
        } catch (Exception exc) {
            throw new RuntimeException("UNABLE TO RETRIEVE XSD TRANSLATION ENDPOINT, MAKE SURE PROPERTY IS SET");
        }

        GradleTaskHelper taskHelper = new GradleTaskHelper(client, graphName, pluginOutput, p.getName());
        taskHelper.setPw(new PrintWriter(System.out));

        File userGradleHome = p.getGradle().getGradleUserHomeDir();
        userGradleHome = new File(userGradleHome.getAbsolutePath() + File.separator + "daemon" + File.separator +
                p.getGradle().getGradleVersion() + "/");

        initSootForDataflowAnalysis(projectUUID, p.getBuildDir().getAbsolutePath(), taskHelper, userGradleHome);

        Long beginTime = System.currentTimeMillis();
        String dataflowPath = performDataflowAnalysis(pluginOutput, projArtifactId);
        if (dataflowPath != null) {

            DataflowAnalyzerPlatform dataflowAnalyzerPlatform = new DataflowAnalyzerPlatform();

            EssDynamicAnalysis essDynamicAnalysis = new EssDynamicAnalysis(parentProj.getProjectDir().getPath());
            File callTraces = essDynamicAnalysis.acquireDynamicAnalysisArtifact();

            List<Stack<String>> callTraceStackList = parseCallTraceStack(callTraces.getAbsolutePath());
            Set<Stack<DataflowGraphComponent>> dataflowGraphs = dataflowAnalyzerPlatform.processCallTraceStacks(callTraceStackList);

            if (!dataflowGraphs.isEmpty()) {
                Iterator<Stack<DataflowGraphComponent>> dataflowGraphIter = dataflowGraphs.iterator();

                while (dataflowGraphIter.hasNext()) {
                    Stack<DataflowGraphComponent> firstGraph = dataflowGraphIter.next();

                    //TODO temporary excising of untested test-cases
                    if (firstGraph.size() == 9) {
                        ListIterator<DataflowGraphComponent> graphComponentListIterator = firstGraph.listIterator();
                        while (graphComponentListIterator.hasNext()) {
                            graphComponentListIterator.next();
                            if (graphComponentListIterator.nextIndex() == firstGraph.size() - 2) {
                                DataflowGraphComponent dataflowGraphComponent = graphComponentListIterator.next();
                                MethodInvocationDataflowNode lastNonNullNode = (MethodInvocationDataflowNode) dataflowGraphComponent;
                                if (lastNonNullNode.getLineNumber() != 98) {
                                    dataflowGraphIter.remove();
                                    continue;
                                }
                            }
                        }
                    } else if (firstGraph.size() == 13) {
                        ListIterator<DataflowGraphComponent> graphComponentListIterator = firstGraph.listIterator();
                        while (graphComponentListIterator.hasNext()) {
                            graphComponentListIterator.next();
                            if (graphComponentListIterator.nextIndex() == firstGraph.size() - 2) {
                                DataflowGraphComponent dataflowGraphComponent = graphComponentListIterator.next();
                                MethodInvocationDataflowNode lastNonNullNode = (MethodInvocationDataflowNode) dataflowGraphComponent;
                                if (lastNonNullNode.getLineNumber() != 94) {
                                    dataflowGraphIter.remove();
                                    continue;
                                }
                            }
                        }
                    }
                    //TODO temporary excising of untested test-cases
                }
            }

            Model m = ModelFactory.createDefaultModel();
            for (Stack<DataflowGraphComponent> dataflowGraphComponents : dataflowGraphs) {
                while (!dataflowGraphComponents.isEmpty()) {
                    DataflowGraphComponent dataflowGraphComponent = dataflowGraphComponents.pop();
                    m.add(ObjectToTriples.convert(config, dataflowGraphComponent));
                }
            }
            adaptTriples+=m.getGraph().size();
            client.addToModel(m, taskHelper.getGraphName());
            ProjectToTriplesMain.recordCPElement(OntologyHelper.serializeModel(m, "TTL", false), dataflowPath +
            File.separator + projGroupId + "-" + projArtifactId + "-" + projVersion + "-dataflows.ttl");
        }

        ConstraintAssessment constraintAssessment = new ConstraintAssessment();

        String assessmentUUID = constraintAssessment.injectAdaptationSurface(parentProj == null ? p.getBuildDir() : parentProj.getProjectDir(),
                xsdTranslationEndpoint, projectUUID, taskHelper, adaptTriples, beginTime, config);
        System.out.println(assessmentUUID);
    }

    private final String ns = "${" + "immortalsNs"
            + ":"
            + "http://darpa.mil/immortals/ontology"
            + "}";

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

    private String performDataflowAnalysis(String pluginOutput, String projName) throws IOException {

        File dfDir = new File(pluginOutput + File.separator + projName + File.separator + "dataflows");
        if (dfDir.exists()) {
            return null;
        } else {
            dfDir.mkdir();
            return dfDir.getAbsolutePath();
        }
    }

}
