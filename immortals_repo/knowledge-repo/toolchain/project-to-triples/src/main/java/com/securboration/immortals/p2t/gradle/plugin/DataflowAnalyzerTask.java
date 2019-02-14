package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.utility.GradleTaskHelper;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataflowAnalyzerTask extends ImmortalsGradleTask {

    @TaskAction
    public void analyzeFlows() {

        Project p = getProject();

        String projArtifactId = p.getName();
        String projGroupId = p.getGroup().toString();
        String projVersion = p.getVersion().toString();

        String pluginOutput = null;
        String adslJar = null;
        ObjectToTriplesConfiguration config = new ObjectToTriplesConfiguration("r2.0.0");

        try {
            ImmortalsGradlePlugin.ImmortalsPluginExtension extension = (ImmortalsGradlePlugin.ImmortalsPluginExtension) p.getExtensions().getByName("krgp");
            //TODO
            pluginOutput = extension.getTargetDir();
           // adslJar = extension.getPathToADSLJar();
            // pluginOutput = "C:\\BBNImmortals\\knowledge-repo\\vocabulary\\ontology-static\\ontology\\_ANALYSIS\\_krgp";

        } catch (UnknownDomainObjectException exc) {
            pluginOutput = String.valueOf(p.getProperties().get("pluginOutput"));
        }

        GradleTaskHelper taskHelper = new GradleTaskHelper(client, null, pluginOutput, p.getName());

        String path = taskHelper.getResultsDir();
        // TODO TEMP
        //path = "C:\\Users\\CharlesEndicott\\Documents\\comprehensiveKRTTLs\\ingest";
        // TODO TEMP
        List<File> files = new ArrayList<>();

    }

}
