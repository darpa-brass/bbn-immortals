package com.securboration.immortals.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;

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

    private static URL[] getUrls(PluginContext c) {

        Set<URL> urls = new LinkedHashSet<>();

        Set<String> classpathElements = new LinkedHashSet<>();

        // TODO: for now we're not getting any dependencies

        try {

            classpathElements
                    .addAll(c.getProject().getCompileClasspathElements());

            classpathElements
                    .addAll(c.getProject().getRuntimeClasspathElements());

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
