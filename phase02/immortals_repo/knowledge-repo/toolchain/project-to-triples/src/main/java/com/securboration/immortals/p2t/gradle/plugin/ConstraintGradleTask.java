package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.immortals.constraint.ConstraintAssessment;
import com.securboration.immortals.utility.GradleTaskHelper;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

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
        
        ConstraintAssessment assessment = new ConstraintAssessment();
        initializeReportsDirectory(pluginOutput + "/" + p.getName());
        this.getTaskHelper().setPw(new PrintWriter(new FileOutputStream(getLogPath(pluginOutput + "/" + p.getName(),
                GradleTaskHelper.TaskType.CONSTRAINT), true)));
        assessment.constraintAnalysis(this.getTaskHelper(), config);
    }
}
