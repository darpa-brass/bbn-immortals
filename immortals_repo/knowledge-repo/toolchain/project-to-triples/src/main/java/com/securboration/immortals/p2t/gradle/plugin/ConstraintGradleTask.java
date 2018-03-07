package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.immortals.constraint.ConstraintAssessment;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.utility.GradleTaskHelper;
import com.securboration.immortals.ontology.constraint.*;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.securboration.immortals.utility.GradleTaskHelper.mergeDataflowDataWithBytecodeStructures;
import static com.securboration.immortals.utility.GradleTaskHelper.recordCPElement;

public class ConstraintGradleTask extends ImmortalsGradleTask {
    
    @TaskAction
    public void constraintAssessment() throws Exception {
        Project p = getProject();
        Set<File> dependencies = p.getConfigurations().getByName("compile").getFiles();
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
        ConstraintAssessmentReport assessmentReport = assessment.constraintAnalysis(this.getTaskHelper(), config, dependencies);
        Model m = ObjectToTriples.convert(config, assessmentReport);
        assessmentReport.setTimeOfAssessment(timeStamp);
        String serial = OntologyHelper.serializeModel(m, "TTL", false);
        recordCPElement(serial, pluginOutput + "/" + p.getName() + "/reports/ConstraintAssessmentReport-" + timeStamp + ".ttl");
    }
}
