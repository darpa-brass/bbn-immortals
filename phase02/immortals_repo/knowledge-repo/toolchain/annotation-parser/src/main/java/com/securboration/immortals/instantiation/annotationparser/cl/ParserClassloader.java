package com.securboration.immortals.instantiation.annotationparser.cl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple classloader extension that allows classes that extend the POJO API to
 * be loaded by the annotation parser after compilation
 * 
 * @author jstaples
 *
 */
public class ParserClassloader extends ClassLoader {
    
    private final ClassLoader parent;
    private final URLClassLoader child;

    public ParserClassloader(
            ClassLoader parent,
            String...classpath
            ) throws MalformedURLException {
        this.parent = parent;
        
        List<URL> urls = new ArrayList<>();
        for(String s:classpath){
            File f = new File(s);
            URL url = f.toURI().toURL();
            urls.add(url);
        }
        
        child = new URLClassLoader(urls.toArray(new URL[]{}));
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try{
            return parent.loadClass(name);
        } catch(ClassNotFoundException e){
            return child.loadClass(name);
        }
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new RuntimeException("not implemented");
    }

}
