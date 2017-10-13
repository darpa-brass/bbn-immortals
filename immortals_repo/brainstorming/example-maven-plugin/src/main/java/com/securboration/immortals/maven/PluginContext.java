package com.securboration.immortals.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * The context of the analysis as far as the build tool is concerned.
 * 
 * @author jstaples
 *
 */
public class PluginContext {
    private final Log log;

    private final File targetDir;

    private final MavenProject project;

    private final ImmortalsPluginClassloader buildPathClassloader;

    private final Collection<String> entryPoints = new ArrayList<>();

    public Collection<String> getEntryPoints() {
        return entryPoints;
    }

    public ImmortalsPluginClassloader getBuildPathClassloader() {
        return buildPathClassloader;
    }

    public Log getLog() {
        return log;
    }

    public MavenProject getProject() {
        return project;
    }

    public File getTargetDir() {
        return targetDir;
    }

    public PluginContext(Log log, MavenProject project, File targetDir,
            String[] entryPoints) {
        super();
        this.log = log;
        this.project = project;
        this.targetDir = targetDir;

        this.buildPathClassloader = new ImmortalsPluginClassloader(this);

        for (String entryPoint : entryPoints) {
            this.entryPoints.add(entryPoint);
        }

        init();
    }

    private void init() {
        Collection<File> files = FileUtils.listFiles(targetDir,
                new String[] { "class" }, true);

        files.forEach((File f) -> {
            System.out.printf("\t%s\n", f.getAbsolutePath());
        });
    }

}
