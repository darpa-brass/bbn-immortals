package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.immortals.aframes.AnalysisFrameAssessment;
import com.securboration.immortals.utility.GradleTaskHelper;
import com.securboration.immortals.ontology.frame.AnalysisFrameAssessmentReport;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.util.*;

public class AnalysisFrameGradleTask extends ImmortalsGradleTask {
    
    @TaskAction
    public void analysisFrameAssessment() throws Exception {
        Project p = getProject();
        final String pluginOutput = String.valueOf(p.getProperties().get("pluginOutput"));

        String supplementsDirectory = String.valueOf(p.getProperties().get("supplements"));
        String behavior = String.valueOf(p.getProperties().get("behavior"));
        List<File> files = collectFiles(pluginOutput, supplementsDirectory, behavior);
        if (files.isEmpty()) {
            return;
        }

        String graphName = getGraphName(String.valueOf(p.getProperties().get("graphName")), p.getName(), files);
        this.setTaskHelper(new GradleTaskHelper(client, graphName, pluginOutput, p.getName()));
        pushUberGraph(graphName, files, false);
        
        
        String getDataflows = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_analysis:  <http://darpa.mil/immortals/ontology/r2.0.0/analysis#>\n" +
                "prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "\n" +
                "select distinct ?dataFlowEdge where {\n" +
                "\t\n" +
                "    graph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                " \n" +
                "\t\t  {?dataFlowEdge a IMMoRTALS_analysis:InterMethodDataflowEdge .}\n" +
                "\t}\n" +
                "\t\n" +
                "}";
        getDataflows = getDataflows.replace("???GRAPH_NAME???", graphName);

        GradleTaskHelper.AssertableSolutionSet flowSolutions = new GradleTaskHelper.AssertableSolutionSet();
        client.executeSelectQuery(getDataflows, flowSolutions);
        
        initializeReportsDirectory(pluginOutput + "/" + p.getName());
        this.getTaskHelper().setPw(new PrintWriter(new FileOutputStream(getLogPath(pluginOutput + "/" + p.getName(),
                GradleTaskHelper.TaskType.ANALYSIS_FRAME), true)));
        
        AnalysisFrameAssessmentReport report = AnalysisFrameAssessment.analysisFrameValidation(this.getTaskHelper(),
                flowSolutions.getSolutions(), config);
        serializeAndRecordReport(report, GradleTaskHelper.TaskType.ANALYSIS_FRAME, pluginOutput + "/" + p.getName());
    }
    
}
