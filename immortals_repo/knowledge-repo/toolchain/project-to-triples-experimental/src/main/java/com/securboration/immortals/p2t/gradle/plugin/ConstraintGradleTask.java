package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.immortals.constraint.ConstraintAssessment;
import com.securboration.immortals.utility.GradleTaskHelper;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConstraintGradleTask extends ImmortalsGradleTask {
    
    @TaskAction
    public void constraintAssessment() throws Exception {
        Project p = getProject();
        String pluginOutput;
        try {
            ImmortalsGradlePlugin.ImmortalsPluginExtension extension = (ImmortalsGradlePlugin.ImmortalsPluginExtension) p.getExtensions().getByName("krgp");
            pluginOutput = extension.getTargetDir();
        } catch (Exception exc) {
            pluginOutput = String.valueOf(p.getProperties().get("pluginOutput"));
        }
        String supplementsDirectory = String.valueOf(p.getProperties().get("supplements"));
        String behavior = String.valueOf(p.getProperties().get("behavior"));
        List<File> files = collectFiles(pluginOutput, supplementsDirectory, behavior);
 
        String graphName = getGraphName(String.valueOf(p.getProperties().get("graphName")), p.getName(), files);
        this.setTaskHelper(new GradleTaskHelper(client, graphName, pluginOutput, p.getName()));
        
        pushUberGraph(graphName, files, false);
        
        initializeReportsDirectory(pluginOutput + "/" + p.getName());
        this.getTaskHelper().setPw(new PrintWriter(new FileOutputStream(getLogPath(pluginOutput + "/" + p.getName(),
                GradleTaskHelper.TaskType.CONSTRAINT), true)));
        ConstraintAssessment.constraintAnalysis(this.getTaskHelper(), config);
        String assessmentUUID = ConstraintAssessment.createAdaptationSurface(this.getTaskHelper(), config, getDependencies(this.getTaskHelper()));
    }

    protected static Set<File> getDependencies(GradleTaskHelper taskHelper) {

        Set<File> dependencies = new HashSet<>();

        String getDependencyPaths = "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\n" +
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n\n" +
                "select ?filePaths where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\n" +
                "\t\t?project IMMoRTALS:hasClasspaths ?classPaths .\n" +
                "\t\t\n" +
                "\t\t?classPaths IMMoRTALS:hasClasspathName ?classpathName\n" +
                "\t\tfilter(regex(?classpathName, \"compile\"))\n" +
                "\t\t?classPaths IMMoRTALS:hasElementHashValues ?hashes .\n" +
                "\t\t\n" +
                "\t\t?jars a IMMoRTALS_bytecode:JarArtifact\n" +
                "\t\t; IMMoRTALS:hasHash ?hashes\n" +
                "\t\t; IMMoRTALS:hasFileSystemPath ?filePaths .\n" +
                "\t}\n" +
                "}";
        getDependencyPaths = getDependencyPaths.replace("???GRAPH_NAME???", taskHelper.getGraphName());
        GradleTaskHelper.AssertableSolutionSet dependencySolutions = new GradleTaskHelper.AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getDependencyPaths, dependencySolutions);

        for (GradleTaskHelper.Solution dependencySolution : dependencySolutions.getSolutions()) {
            dependencies.add(new File(dependencySolution.get("filePaths")));
        }

        return dependencies;
    }
    
}
