package com.securboration.immortals.maven.entrypoint;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Settings;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import com.securboration.immortals.maven.PluginContext;

/**
 * Entry point for the maven plugin
 * 
 * @author jstaples
 *
 */
@Mojo(name = "javaToTriples", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class MavenPluginEntryPoint extends AbstractMojo {

    /*
     * Maven injects these @Parameter fields automatically for us
     * 
     * (similar to @Autowired or @Inject)
     */

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojo;

    @Parameter(defaultValue = "${plugin}", readonly = true)
    private PluginDescriptor plugin;

    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File basedir;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private File target;
    
    
    @Component
    private RepositorySystem repoSystem;
 
    @Parameter(defaultValue="${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;
    
    @Parameter(defaultValue="${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> projectRepos;
    
    @Parameter(defaultValue="${project.remotePluginRepositories}", readonly = true)
    private List<RemoteRepository> pluginRepos;
    
    @Parameter(defaultValue="${localRepository}", readonly = true)
    private ArtifactRepository localRepository;
    
    
    /**
     * Must be manually specified as part of the plugin configuration.  E.g.,
     * <configuration>
     *   <entryPoints>
     *     <entryPoint>com.securboration.AClass</entryPoint>
     *     <entryPoint>com.securboration.BClass</entryPoint>
     *     ...
     *   </entryPoints>
     * </configuration>
     */
    @Parameter(required = false)
    private String[] entryPoints = new String[]{};
    
    @Parameter(required = false)
    private String[] outputPrefixMappings = new String[]{};
    
    @Parameter(required = false)
    private String targetNamespace = "http://securboration.com/defaultNs#";
    
    @Parameter(required = false)
    private String outputPath = "ontology.ttl";
    
    @Parameter(required = false)
    private String outputLanguage = "Turtle";
    
    @Parameter(required = false)
    private String[] trimNames = new String[]{};
    
    @Parameter(required = false)
    private boolean autoGenerateAdditionalPrefixes = false;
    
    @Parameter(required = false)
    private String version = "no version defined";
    
    @Parameter(required = false)
    private String[] skipPackagePrefixes = new String[]{};

    private PluginContext getContext() {
        return new PluginContext(this);
    }

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("*** IMMoRTALS entry point ***");

        try {
            new AnalysisEntryPoint(getContext()).analyze();
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("analysis failed", e);
        }
    }

    public MavenSession getSession() {
        return session;
    }

    public MavenProject getProject() {
        return project;
    }

    public MojoExecution getMojo() {
        return mojo;
    }

    public PluginDescriptor getPlugin() {
        return plugin;
    }

    public Settings getSettings() {
        return settings;
    }

    public File getBasedir() {
        return basedir;
    }

    public File getTarget() {
        return target;
    }

    public RepositorySystem getRepoSystem() {
        return repoSystem;
    }

    public RepositorySystemSession getRepoSession() {
        return repoSession;
    }

    public List<RemoteRepository> getProjectRepos() {
        return projectRepos;
    }

    public List<RemoteRepository> getPluginRepos() {
        return pluginRepos;
    }

    public String[] getEntryPoints() {
        return entryPoints;
    }

    public String[] getOutputPrefixMappings() {
        return outputPrefixMappings;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getOutputLanguage() {
        return outputLanguage;
    }

    public String[] getTrimNames() {
        return trimNames;
    }

    public boolean isAutoGenerateAdditionalPrefixes() {
        return autoGenerateAdditionalPrefixes;
    }

    public String getVersion() {
        return version;
    }

    public String[] getSkipPackagePrefixes() {
        return skipPackagePrefixes;
    }

    public ArtifactRepository getLocalRepository() {
        return localRepository;
    }

}
