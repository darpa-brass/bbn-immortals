package com.securboration.immortals.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;

import com.securboration.immortals.maven.etc.ExceptionWrapper;
import com.securboration.immortals.maven.etc.Printing;

/**
 * A custom classloader integrated with a build classpath
 * 
 * @author jstaples
 *
 */
public class ImmortalsPluginClassloader extends URLClassLoader {

    public ImmortalsPluginClassloader(PluginContext context) {

        super(getUrls(context));
    }
    
    private static String resolve(Dependency d,PluginContext context) throws MalformedURLException{
        ArtifactRepository repo = context.getEntrypoint().getLocalRepository();
        
        context.getLog().info("local repository found @ " + repo.getBasedir());
        
//        DefaultArtifact(String groupId, String artifactId, String version, String scope, String type, String classifier, ArtifactHandler artifactHandler)
        
        Artifact a = new DefaultArtifact(d.getGroupId(),d.getArtifactId(),d.getVersion(),d.getScope(),d.getType(),d.getClassifier(),new DefaultArtifactHandler());
        
        Artifact result = repo.find(a);
        
//        System.out.printf("\t%s\n", Printing.deepPrint(result));
//        
//        System.out.printf("%s\n", result.getFile().getAbsolutePath());
        
        File f = new File(result.getFile().getAbsolutePath()+".jar");
        
        context.getLog().info("found dependency @ " + f.getAbsolutePath());
        
        return f.getAbsolutePath();
        
        
//        request.setArt
//        
//        r.resolve(arg0)
        
    }

    private static URL[] getUrls(PluginContext c) {

        Set<URL> urls = new LinkedHashSet<>();

        Set<String> classpathElements = new LinkedHashSet<>();

        // TODO: for now we're not getting any dependencies

        try {

            classpathElements
                    .addAll(c.getProject().getCompileClasspathElements());

            classpathElements
                    .addAll(c.getProject().getRuntimeClasspathElements());
            
//            c.getProject().
            for(Dependency d:c.getProject().getDependencies()){
                System.out.printf("dependency [%s]\n",d.getManagementKey());
                
                System.out.printf("\tgid=%s\n",d.getGroupId());
                System.out.printf("\taid=%s\n",d.getArtifactId());
                System.out.printf("\tver=%s\n",d.getVersion());
                
                
                System.out.printf("\t%s\n", Printing.deepPrint(d));
                
                ExceptionWrapper.wrap(()->{
                    classpathElements.add(resolve(d,c));
                });
            }

            for(Artifact a:c.getProject().getArtifacts()){
                System.out.printf("artifact [%s]\n", a.getFile().getAbsolutePath());
            }
            
//            RepositorySystem s;
//            s.resolve(null).get
            
//            MavenSession s;
//            
//            s.getRepositorySession().getDependencyManager().manageDependency(null)..selectDependency(null);

            // TODO: how to deal with system scoped artifacts since this aspect
            // is deprecated?

            for (String classpathElement : classpathElements) {

                File f = new File(classpathElement);

                if (!f.exists()) {
                    c.getLog().warn("could not find dir " + classpathElement
                            + ", it will be skipped");
                    continue;
                }

                try {
                    URL url = f.toURI().toURL();

                    c.getLog().info("added resource @ " + url);
                    urls.add(url);
                } catch (MalformedURLException e) {
                    c.getLog().error("malformed URL for " + classpathElement
                            + ", it will be skipped");
                }
            }
        } catch (DependencyResolutionRequiredException e) {
            throw new RuntimeException(e);
        }

        return urls.toArray(new URL[] {});
    }

}
