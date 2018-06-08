package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.immortals.utility.GradleTaskHelper;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class PluginCleanupGradleTask extends DefaultTask {
    
    @TaskAction
    public void cleanup() throws IOException {
        Project p = getProject();
        GradleTaskHelper.emitZipArchive(new File(p.getProjectDir() + ".zip"),
                new File(p.getProjectDir() + "/krgp/"));
        
    }
}
