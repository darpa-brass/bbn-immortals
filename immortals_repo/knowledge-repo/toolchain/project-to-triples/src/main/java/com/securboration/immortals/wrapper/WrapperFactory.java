package com.securboration.immortals.wrapper;

import com.google.common.base.CaseFormat;
import com.securboration.immortals.ontology.analysis.MethodInvocationDataflowNode;
import com.securboration.immortals.ontology.lang.WrapperSourceFile;
import com.securboration.immortals.utility.Decompiler;
import com.securboration.immortals.utility.GradleTaskHelper;
import org.apache.commons.io.FileUtils;
import soot.*;
import soot.jimple.JasminClass;
import soot.options.Options;
import soot.util.JasminOutputStream;

import java.io.*;
import java.util.*;

public class WrapperFactory {
    
    private Map<String, Wrapper> wrappedClassToWrapper;
    
    public WrapperFactory() {
        wrappedClassToWrapper = new HashMap<>();
    }
    
    public Wrapper createWrapper(String className, List<String> dependencies, Set<String> dependentFiles) {
        
        boolean bootstrapRT = false;
        boolean bootstrapJCE = false;
        
        for (String dependentFile : dependentFiles) {
            if (dependentFile.contains("rt.jar")) {
                bootstrapRT = true;
                break;
            }  else if (dependentFile.contains("jce.jar")) {
                bootstrapJCE = true;
                break;
            }
        }

        
        Options.v().set_keep_line_number(true);
        PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");
        SootClass wrappedClass = null;
        
        try {
            
            if (bootstrapRT) {
                Scene.v().setSootClassPath("");
                StringBuilder sootCP = new StringBuilder("");
                for (String dependentFile : dependentFiles) {
                    if (sootCP.toString().equals("")) {
                        sootCP.append(dependentFile);
                    } else {
                        sootCP.append(File.pathSeparatorChar).append(dependentFile);
                    }
                }
                
                Scene.v().setSootClassPath(sootCP.toString());
                
            } else {
                Scene.v().extendSootClassPath(System.getProperty("java.home") + File.separatorChar 
                        + "lib" + File.separatorChar + "jce.jar");
            }
            
            if (!bootstrapJCE) {
                Scene.v().extendSootClassPath(System.getProperty("java.home") + File.separatorChar
                        + "lib" + File.separatorChar + "jce.jar");
            }
            
            StringBuilder sootCP = new StringBuilder(Scene.v().getSootClassPath());
            
            Scene.v().loadBasicClasses();
            for (String dependency : dependencies) {
                sootCP.append(File.pathSeparatorChar + dependency);
            }
            
            Scene.v().setSootClassPath(sootCP.toString());
            //Scene.v().extendSootClassPath(System.getProperty("java.home") + File.separatorChar + "lib" + File.separatorChar + "jce.jar");
            wrappedClass = Scene.v().loadClassAndSupport(className);

        } catch (Exception exc) {
            System.out.println("Issue loading dependencies/specified class");
        }
        
        return new Wrapper(wrappedClass);
    }
    
    public void wrap(Wrapper wrapper) {
        
        int modifiers = wrapper.getWrappedClass().getModifiers();
        if (Modifier.toString(modifiers).contains("abstract")) {
            modifiers = modifiers & ~Modifier.ABSTRACT;
        }

        SootClass wrappedClass = wrapper.getWrappedClass();
        SootClass wrapperClass = new SootClass("Wrapper" + wrapper.getWrappedClass().getShortName(), modifiers);
        Scene.v().addClass(wrapperClass);
        wrapperClass.setSuperclass(wrappedClass);

        SootField newField = new SootField("socketChannel", wrappedClass.getType(), Modifier.PRIVATE);
        wrapperClass.addField(newField);
        List<SootMethod> constructors = new ArrayList<>();
        Iterator methods = wrappedClass.getMethods().iterator();
        while (methods.hasNext()) {
            SootMethod oldMethod = (SootMethod) methods.next();
            if (!oldMethod.isFinal() && !oldMethod.isPrivate() && !oldMethod.isStatic()) {
                wrapper.synthesizeMethods(wrapperClass, oldMethod, newField.getName(), constructors);
            }
        }
        
        wrapper.setWrapperClass(wrapperClass);
    }

    public List<String> wrapWithCipher(Wrapper wrapper, String cipherImpl, WrapperSourceFile[] wrapperSourceFiles,
                                       GradleTaskHelper taskHelper, boolean plugin, List<MethodInvocationDataflowNode> methodNodes) throws IOException {
        String complexity;
        //TODO infer complexity based on soot or javaparser analysis
        if (wrapper.getWrappedClass().getName().equals("java.net.Socket")) {
            complexity = "simple";
        } else {
            complexity = "complex";
        }
        
        List<String> augmentedMethods = new ArrayList<>();
        
        int modifiers = wrapper.getWrappedClass().getModifiers();
        if (Modifier.toString(modifiers).contains("abstract")) {
            modifiers = modifiers & ~Modifier.ABSTRACT;
        }

        SootClass wrappedClass = wrapper.getWrappedClass();
        String wrapperClassName = "Wrapper" + wrapper.getWrappedClass().getShortName();
        SootClass wrapperClass = new SootClass(wrapperClassName, modifiers);
        wrapper.setWrapperClass(wrapperClass);
        Scene.v().addClass(wrapperClass);
        wrapperClass.setSuperclass(wrappedClass);
        
        checkForInaccessibleMethods(wrappedClass, methodNodes);
        
        String checkForPreviousAugmentation = "prefix IMMoRTALS_constraint: <http://darpa.mil/immortals/ontology/r2.0.0/constraint#> \n" +
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                "\n" +
                "select ?sourceFile where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t\n" +
                "\t\t?impacts IMMoRTALS:hasProducedSourceFiles ?sourceFile .\n" +
                "\t\t\n" +
                "\t\t?sourceFile IMMoRTALS:hasFileName \"???FILE_NAME???\" .\n" +
                "\t}\n" +
                "}";
        checkForPreviousAugmentation = checkForPreviousAugmentation.replace("???FILE_NAME???", wrapperClassName + ".java")
        .replace("???GRAPH_NAME???", taskHelper.getGraphName());
        GradleTaskHelper.AssertableSolutionSet previousAugmentationSolutions = new GradleTaskHelper.AssertableSolutionSet();
        
        taskHelper.getClient().executeSelectQuery(checkForPreviousAugmentation, previousAugmentationSolutions);
        
        if (!previousAugmentationSolutions.getSolutions().isEmpty()) {
            return augmentedMethods;
        }
        
        SootField newField = new SootField(wrappedClass.getShortName().toLowerCase(), wrappedClass.getType(), Modifier.PRIVATE);
        wrapperClass.addField(newField);
        
        SootClass cipherImplClass = Scene.v().loadClassAndSupport(cipherImpl);
        
        List<SootMethod> constructors = new ArrayList<>();
        Iterator methods = wrappedClass.getMethods().iterator();
        while (methods.hasNext()) {
            SootMethod oldMethod = (SootMethod) methods.next();
            
            if (!oldMethod.isFinal() && !oldMethod.isPrivate() && !oldMethod.isStatic()
                    && oldMethod.getModifiers() != 0) {
                wrapper.synthesizeMethods(wrapperClass, oldMethod, newField.getName(), constructors);
            }
        }

        Decompiler decompiler = new Decompiler();
        String bareBonesWrapperPath = this.produceWrapperClassFile(wrapper);
        
        String pathToJavaSource;
        if (plugin) {
            pathToJavaSource = taskHelper.getResultsDir() + "Wrapper" + wrapper.getWrappedClass().getShortName() + ".java";
        } else {
            pathToJavaSource = bareBonesWrapperPath.substring(0, bareBonesWrapperPath.lastIndexOf("/")) + "/Wrapper"
                    + wrapper.getWrappedClass().getShortName() + ".java";
        }
        
        File producedFile = decompiler.decompileClassFile(bareBonesWrapperPath, pathToJavaSource, plugin);
        String producedFileSource = FileUtils.readFileToString(producedFile);
        WrapperSourceFile wrapperSourceFile = new WrapperSourceFile();
        wrapperSourceFile.setFileName(producedFile.getName().replace(".java", "[1/2].java"));
        wrapperSourceFile.setFileSystemPath(producedFile.getAbsolutePath());
        wrapperSourceFile.setSource(producedFileSource);
        wrapperSourceFiles[0] = wrapperSourceFile;

        // Doesn't matter which super constructor to call, as long as one gets called
        SootMethod initMethod = wrapper.createCipherInitializationMethod(wrapperClass, cipherImplClass);
        augmentedMethods.add(initMethod.getSignature());
        
        if (complexity.equals("simple")) {

            SootField streamImplField = new SootField(wrapper.getStreamType().replace("java.io.", "").toLowerCase(),
                    Scene.v().getRefType(wrapper.getStreamType()), Modifier.PRIVATE);
            wrapperClass.addField(streamImplField);
            
            SootMethod wrapMethod = wrapper.createStreamAugmentationMethod(cipherImplClass, wrapperClass);
            augmentedMethods.add(wrapMethod.getSignature());
            
            SootMethod getStreamImplMethod = wrapper.createGetStreamImplMethod(wrapperClass);
            augmentedMethods.add(getStreamImplMethod.getSignature());
            
            SootMethod newConstructor = wrapper.createCipherWrapperConstructorSimple(wrapperClass, newField,
                    cipherImplClass, initMethod, wrapMethod, getStreamImplMethod, streamImplField);
            augmentedMethods.add(newConstructor.getSignature());
        } else {

            SootField cipherField = new SootField(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, cipherImplClass.getShortName()),
                    cipherImplClass.getType(), Modifier.PRIVATE);
            wrapperClass.addField(cipherField);
            
            SootMethod newConstructor = wrapper.createCipherWrapperConstructorComplex(wrapperClass, newField,
                    constructors.get(0), initMethod, cipherField);
            augmentedMethods.add(newConstructor.getSignature());
        }
        
        wrapper.setWrapperClass(wrapperClass);
        
        bareBonesWrapperPath = this.produceWrapperClassFile(wrapper);

        if (plugin) {
            pathToJavaSource = taskHelper.getResultsDir() + "Wrapper" + wrapper.getWrappedClass().getShortName() + ".java";
        } else {
            pathToJavaSource = bareBonesWrapperPath.substring(0, bareBonesWrapperPath.lastIndexOf("/")) + "/Wrapper"
                    + wrapper.getWrappedClass().getShortName() + ".java";
        }
        
        producedFile = decompiler.decompileClassFile(bareBonesWrapperPath, pathToJavaSource, plugin);
        producedFileSource = FileUtils.readFileToString(producedFile);
        wrapperSourceFile = new WrapperSourceFile();
        wrapperSourceFile.setFileName(producedFile.getName().replace(".java", "[2/2].java"));
        wrapperSourceFile.setFileSystemPath(producedFile.getAbsolutePath());
        wrapperSourceFile.setSource(producedFileSource);
        wrapperSourceFiles[1] = wrapperSourceFile;
        
        return augmentedMethods;
    }
    
    public String produceWrapperClassFile(Wrapper wrapper) throws IOException {

        String fileName = SourceLocator.v().getFileNameFor(wrapper.getWrapperClass(), Options.output_format_class);
        File classFile = new File(fileName);
        OutputStream streamOut = new JasminOutputStream(new FileOutputStream(classFile.getAbsolutePath()));
        PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
        JasminClass jasminClass = new JasminClass(wrapper.getWrapperClass());
        jasminClass.print(writerOut);
        writerOut.flush();
        streamOut.close();
        
        return fileName.replace("\\", "/");
    }

    public void produceWrapperClassJimple(Wrapper wrapper) throws IOException {

        String fileName = SourceLocator.v().getFileNameFor(wrapper.getWrapperClass(), Options.output_format_jimple);
        OutputStream streamOut = new FileOutputStream(fileName);
        PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
        Printer.v().printTo(wrapper.getWrapperClass(), writerOut);
        writerOut.flush();
        streamOut.close();
    }
    
    private void checkForInaccessibleMethods(SootClass wrappedClass, List<MethodInvocationDataflowNode> invokedMethods) {
        Iterator methods = wrappedClass.getMethods().iterator();
        while (methods.hasNext()) {
            SootMethod oldMethod = (SootMethod) methods.next();
            invokedMethods.removeIf(nextMethodNode -> nextMethodNode.getJavaMethodName()
                    .equals(oldMethod.getName()));
        }
    }
}
