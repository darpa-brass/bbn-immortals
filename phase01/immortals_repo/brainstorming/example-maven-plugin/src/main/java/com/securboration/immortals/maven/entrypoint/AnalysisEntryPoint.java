package com.securboration.immortals.maven.entrypoint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import com.securboration.immortals.maven.PluginContext;
import com.securboration.immortals.maven.bytecode.DynamicClassWriter;
import com.securboration.immortals.maven.bytecode.MethodPrinterTransformer;
import com.securboration.immortals.maven.etc.ExceptionWrapper;

/**
 * Analysis starts here
 * 
 * @author jstaples
 *
 */
public class AnalysisEntryPoint {
    private final PluginContext context;

    public AnalysisEntryPoint(PluginContext pluginContext) {
        this.context = pluginContext;
    }

    /**
     * 
     * @param analyzeThese
     *            classpath entries where we should start the analysis
     * @param classpath
     *            a mechanism for retrieving classpath items
     * @throws ClassNotFoundException
     */
    public void analyze() throws ClassNotFoundException {

        for (String entryPoint : context.getEntryPoints()) {
            analyzeEntryPoint(entryPoint);
        }
    }

    private void analyzeEntryPoint(String className) {

        context.getLog().info("Analyzing entrypoint: " + className);

        ExceptionWrapper.wrap(() -> {

            final String classResourcePath = className.replace(".", "/")
                    + ".class";

            final InputStream classStream = context.getBuildPathClassloader()
                    .getResourceAsStream(classResourcePath);

            // Load the class buffer into an ASM model.
            ClassReader cr = new ClassReader(classStream);
            ClassNode cn = new ClassNode();

            cr.accept(cn, 0);// 0 = Don't expand frames or compute stack/local
                             // mappings

            analyze(cn);
        });
    }

    private void analyze(ClassNode cn) {

        context.getLog().info("analyzing ASM model for class " + cn.name);

        // TODO: traverse the class structure and build out our analysis models

        // TODO: for now, just transform the class such that it prints all
        // method invocations--a trivial placeholder functionality
        new MethodPrinterTransformer().transformClass(cn);

        boolean wasModified = true;// TODO: determine whether this class was
                                   // modified as a result of analysis. For now,
                                   // we know that it was.

        if (!wasModified) {
            return;
        }

        byte[] modifiedBytecode = DynamicClassWriter.getClassBytes(cn, context);

        try {
            writeBytecodeModifications(cn.name, modifiedBytecode);
        } catch (IOException e) {
            context.getLog().error("Unable to write class " + cn.name, e);
        }
    }

    private void writeBytecodeModifications(String className, byte[] bytecode)
            throws IOException {

        final String resourceName = className + ".class";
        final URL resourceLocation = context.getBuildPathClassloader()
                .findResource(resourceName);

        context.getLog()
                .info("about to write to " + resourceLocation.toString());

        writeUsingProtocol(resourceLocation, bytecode);
    }

    private void writeUsingProtocol(URL url, byte[] data) throws IOException {
        if (url.getProtocol().equals("file")) {
            String path = url.getPath();

            FileUtils.writeByteArrayToFile(new File(path), data);
            
            return;
        }

        throw new RuntimeException("no file found for url " + url.toString());

    }

}
