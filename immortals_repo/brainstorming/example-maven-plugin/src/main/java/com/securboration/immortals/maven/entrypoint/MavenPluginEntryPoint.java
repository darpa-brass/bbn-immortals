package com.securboration.immortals.maven.entrypoint;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import com.securboration.immortals.maven.PluginContext;

/**
 * Entry point for the maven plugin
 * 
 * @author jstaples
 *
 */
@Mojo(name = "analyze", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
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
    @Parameter
    private String[] entryPoints;

    private PluginContext getContext() {
        return new PluginContext(getLog(), project, target, entryPoints);
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

}
