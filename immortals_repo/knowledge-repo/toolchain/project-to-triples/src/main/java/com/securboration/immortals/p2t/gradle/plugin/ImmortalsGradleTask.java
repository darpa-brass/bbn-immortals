package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.immortals.utility.GradleTaskHelper;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.repo.ontology.FusekiClient;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.gradle.api.DefaultTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ImmortalsGradleTask extends DefaultTask {
    
    protected FusekiClient client = new FusekiClient("http://localhost:3030/ds");
    private String graphName;
    private final String KRGP_DIR = "/krgp/";
    ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration(String.valueOf(getProject().getVersion()));
    String timeStamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
    
    private GradleTaskHelper taskHelper;


    protected void serializeAndRecordReport(Object object, GradleTaskHelper.TaskType taskType, String projectPath) throws IOException {
        Model m = ObjectToTriples.convert(config, object);
        String serialReport = OntologyHelper.serializeModel(m, "TTL", false);
        if (taskType == GradleTaskHelper.TaskType.ANALYSIS_FRAME) {
            GradleTaskHelper.recordCPElement(serialReport, projectPath + "/reports/FrameReport-" + timeStamp + ".ttl");
        } else if (taskType == GradleTaskHelper.TaskType.CONSTRAINT) {
            GradleTaskHelper.recordCPElement(serialReport, projectPath + "/reports/ConstraintReport-" + timeStamp + ".ttl");
        }
    }

    protected void pushUberGraph(String graphName, List<File> files, boolean existingGraph) throws FileNotFoundException {
            if (!client.getGraphNames().contains("http://localhost:3030/ds/data/" + graphName)) {
                Model m = ModelFactory.createDefaultModel();
                for (File file : files) {
                    m.read(new FileInputStream(file), null, "TURTLE");
                }
                client.setModel(m, "http://localhost:3030/ds/data/" + graphName);
            } else if (existingGraph) {
                Model m = ModelFactory.createDefaultModel();
                for (File file : files) {
                    m.read(new FileInputStream(file), null, "TURTLE");
                }
                client.addToModel(m, "http://localhost:3030/ds/data/" + graphName);
            }

    }
    
    protected void initializeReportsDirectory(String pluginOutputPath) {
        File reportsDirectory = new File(pluginOutputPath + "/reports/");
        reportsDirectory.mkdirs();
    }
    
    protected String getGraphName(String graphName, String projectName, List<File> files) throws FileNotFoundException {
        if (graphName.equals("null")) {
            
            for (String fusekiGraph : client.getGraphNames()) {
                String getTaskVestiges = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                        "\n" +
                        "select ?vestige where {\n" +
                        "\tgraph<???GRAPH_NAME???> {\n" +
                        "\t\t?vestige a IMMoRTALS:TaskVestige\n" +
                        "\t}\n" +
                        "}";
                getTaskVestiges = getTaskVestiges.replace("???GRAPH_NAME???", fusekiGraph);

                GradleTaskHelper.AssertableSolutionSet vestigeSolutions = new GradleTaskHelper.AssertableSolutionSet();
                client.executeSelectQuery(getTaskVestiges, vestigeSolutions);
                if (vestigeSolutions.getSolutions().size() != 0) {
                    return fusekiGraph.substring(30);
                }
            }
            graphName = projectName + "-ubergraph-" + UUID.randomUUID();
            String insertTaskVestige = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                    "\n" +
                    "insert data {\n" +
                    "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                    "\t\tIMMoRTALS:TaskVestige-???UUID??? a IMMoRTALS:TaskVestige .\n" +
                    "\t}\n" +
                    "}";
            insertTaskVestige = insertTaskVestige.replace("???GRAPH_NAME???", graphName).replace("???UUID???", UUID.randomUUID().toString());
            client.executeUpdate(insertTaskVestige);
            pushUberGraph(graphName, files, true);
        }
        return graphName;
    }
    
    protected String getLogPath(String pluginOutputPath, GradleTaskHelper.TaskType taskType) {
        String logPath = "";
        
        if (taskType == GradleTaskHelper.TaskType.CONSTRAINT) {
            logPath = pluginOutputPath + "/reports/constraintLog-" + timeStamp +".txt";
        } else if (taskType == GradleTaskHelper.TaskType.ANALYSIS_FRAME) {
            logPath = pluginOutputPath + "/reports/frameLog-" + timeStamp +".txt";
        } else if (taskType == GradleTaskHelper.TaskType.BYTECODE) {
            logPath = pluginOutputPath + "/reports/bytecodeLog-" + timeStamp + ".txt";
        }
        return logPath;
    }
    
    protected List<File> collectFiles(String projectArtifactsPath, String supplements, String behavior) {

        Collection<File> bytecodeStructures = FileUtils.listFiles(new File(projectArtifactsPath), new String[]{"ttl"}, true);
        List<File> files = (List<File>) bytecodeStructures;
        
        if (behavior.equals("null")) {
            behavior = "good";
        }

        if (!supplements.equals("null")) {
            files.addAll(FileUtils.listFiles(new File(supplements), null, true));
        } else {
            File file = new File(projectArtifactsPath 
                    + "structures/supplements");
        }
        
        return filterSupplements(behavior, files);
    }
    
    private static List<File> filterSupplements(String behavior, List<File> files) {
        
        switch (behavior) {
            case("good"):
                return files.stream().filter(file -> !file.getAbsolutePath().contains("supplements" + File.separator + "dataflows" + File.separator
                        + "badflows")).collect(Collectors.toList());
            case ("bad"):
                return files.stream().filter(file -> !file.getAbsolutePath().contains("supplements" + File.separator + "dataflows" + File.separator
                        + "goodflows")).collect(Collectors.toList());
            default:
                return files.stream().filter(file -> !file.getAbsolutePath().contains("supplements" + File.separator + "dataflows" + File.separator
                        + "badflows")).collect(Collectors.toList());
        }
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public GradleTaskHelper getTaskHelper() {
        return taskHelper;
    }

    public void setTaskHelper(GradleTaskHelper taskHelper) {
        this.taskHelper = taskHelper;
    }
}
