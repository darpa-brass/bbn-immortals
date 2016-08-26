package com.securboration.immortals.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.securboration.immortals.maven.entrypoint.MavenPluginEntryPoint;

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
    
    private final Collection<String> namespaceMappings = new ArrayList<>();
    
    private final String targetNamespace;
    
    private final File outputFile;
    
    private final String outputLanguage;
    
    private final Collection<String> trimPrefixes = new ArrayList<>();
    
    private final boolean autoGenerateAdditionalPrefixes;
    
    private final String version;
    
    private final Collection<String> skipPackagePrefixes = new ArrayList<>();

    private final MavenPluginEntryPoint entrypoint;
    
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

    public PluginContext(MavenPluginEntryPoint context) {
        super();
        this.log = context.getLog();
        this.project = context.getProject();
        this.targetDir = context.getTarget();
        this.outputLanguage = context.getOutputLanguage();
        this.outputFile = new File(context.getOutputPath());
        this.autoGenerateAdditionalPrefixes = context.isAutoGenerateAdditionalPrefixes();
        this.version = context.getVersion();

        this.entryPoints.addAll(Arrays.asList(context.getEntryPoints()));
        
        this.namespaceMappings.addAll(Arrays.asList(context.getOutputPrefixMappings()));
        
        this.targetNamespace = context.getTargetNamespace();
        
        this.trimPrefixes.addAll(Arrays.asList(context.getTrimNames()));
        
        this.skipPackagePrefixes.addAll(Arrays.asList(context.getSkipPackagePrefixes()));
        
        this.entrypoint = context;

        this.buildPathClassloader = new ImmortalsPluginClassloader(this);
        init();
    }

    private void init() {
        Collection<File> files = FileUtils.listFiles(targetDir,
                new String[] { "class" }, true);

        files.forEach((File f) -> {
            System.out.printf("\t%s\n", f.getAbsolutePath());
        });
    }

    public Collection<String> getNamespaceMappings() {
        return namespaceMappings;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public String getOutputLanguage() {
        return outputLanguage;
    }

    public Collection<String> getTrimPrefixes() {
        return trimPrefixes;
    }

    public boolean isAutoGenerateAdditionalPrefixes() {
        return autoGenerateAdditionalPrefixes;
    }

    public String getVersion() {
        return version;
    }

    public Collection<String> getSkipPackagePrefixes() {
        return skipPackagePrefixes;
    }

    public MavenPluginEntryPoint getEntrypoint() {
        return entrypoint;
    }

}
