package com.securboration.immortals.wrapper;

import com.google.common.base.CaseFormat;
import com.securboration.immortals.aframes.AnalysisFrameAssessment;
import com.securboration.immortals.ontology.analysis.MethodInvocationDataflowNode;
import com.securboration.immortals.ontology.constraint.ConstructorAdaptation;
import com.securboration.immortals.ontology.constraint.FieldAdaptation;
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
import java.util.stream.Collectors;

public class WrapperFactory {
    
    private Map<String, Wrapper> wrappedClassToWrapper;
    
    public WrapperFactory() {
        wrappedClassToWrapper = new HashMap<>();
    }
    
    public Wrapper createWrapper(String className, List<String> dependencies, Set<String> dependentFiles, String androidJarPath) {


      /*  if (androidJarPath != null) {

            //G.reset();
            Scene.v().setSootClassPath("");
            //Scene.v().removeClass(Scene.v().getSootClass("java.nio.channels.SocketChannel"));

            StringBuilder sootCP = new StringBuilder();
            sootCP.append(androidJarPath);
            Scene.v().setSootClassPath(sootCP.toString());
            SootClass wrappedClass = Scene.v().loadClassAndSupport(className);

            for (String dependency : dependencies) {
                sootCP.append(File.pathSeparatorChar + dependency);
            }
            Scene.v().setSootClassPath(sootCP.toString());
            Scene.v().loadBasicClasses();

            return new Wrapper(wrappedClass);
        }*/

        Scene.v().getSootClassPath();

        String sootClasspath = Scene.v().getSootClassPath();
        String[] cpElements = sootClasspath.split(File.pathSeparator);

        if (cpElements.length > 1) {
            SootClass wrappedClass = Scene.v().loadClassAndSupport(className);
            return new Wrapper(wrappedClass);
        }
        
       /* boolean bootstrapRT = false;
        boolean bootstrapJCE = false;
        
        for (String dependentFile : dependentFiles) {
            if (dependentFile.contains("rt.jar")) {
                bootstrapRT = true;

                break;
            }  else if (dependentFile.contains("jce.jar")) {
                bootstrapJCE = true;
                break;
            }
        }*/

        dependentFiles.removeIf(file -> file.contains("rt"));
        
        Options.v().set_keep_line_number(true);
        PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");
        SootClass wrappedClass = null;
        
        try {
            /*if (bootstrapRT) {
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
                bootstrapJCE = true;
            }
            
            if (!bootstrapJCE) {
                Scene.v().extendSootClassPath(System.getProperty("java.home") + File.separatorChar
                        + "lib" + File.separatorChar + "jce.jar");
            }*/
            Options.v().set_allow_phantom_refs(true);
            StringBuilder sootCP = new StringBuilder();

            sootCP.append(androidJarPath);
            for (String dependency : dependencies) {
                sootCP.append(File.pathSeparatorChar + dependency);
            }

           /* if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                sootCP.append(androidJarPath);
                for (String dependency : dependencies) {
                    sootCP.append(File.pathSeparatorChar + dependency);
                }
            } else {
                sootCP.append(File.separator + androidJarPath);
                for (String dependency : dependencies) {
                    sootCP.append(File.pathSeparatorChar + File.separator + dependency);
                }
            }*/
           // sootCP.append(androidJarPath);
            //Scene.v().loadBasicClasses();

            Scene.v().setSootClassPath(sootCP.toString());
            //Scene.v().extendSootClassPath(System.getProperty("java.home") + File.separatorChar + "lib" + File.separatorChar + "jce.jar");
            wrappedClass = Scene.v().loadClassAndSupport(className);
            Scene.v().loadBasicClasses();

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

    public boolean wrapWithCipher(Wrapper wrapper, String cipherImpl, WrapperSourceFile[] wrapperSourceFiles,
                                       GradleTaskHelper taskHelper, boolean plugin, List<MethodInvocationDataflowNode> methodNodes) throws IOException {
        String complexity;
        
        if (wrapper == null) {
            System.out.println("WRAPPER OBJECT IS NULL");
        } else if (wrapper.getWrappedClass() == null) {
            System.out.println("WRAPPED CLASS IS NULL");
        } else if (wrapper.getWrappedClass().getName() == null) {
            System.out.println("WRAPPED CLASS NAME IS NULL???");
        }
        
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
        SootClass wrapperClass;
        
        checkForInaccessibleMethods(wrappedClass, methodNodes);

        SootClass cipherImplClass = Scene.v().loadClassAndSupport(cipherImpl);

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
        String[] argTypes;
        if (!previousAugmentationSolutions.getSolutions().isEmpty()) {
            
            //TODO
            wrapperClass = Scene.v().loadClassAndSupport(wrapperClassName);
            wrapper.setWrapperClass(wrapperClass);

            SootField streamImplField = new SootField(wrapper.getStreamType().replace("java.io.", "").toLowerCase(),
                    Scene.v().getRefType(wrapper.getStreamType()), Modifier.PRIVATE);
            List<Type> fieldTypes = wrapperClass.getFields().stream()
                                                            .map(SootField::getType)
                                                            .collect(Collectors.toList());
            
            if (fieldTypes.stream().anyMatch(type -> type.equals(streamImplField.getType())) ||
                    complexity.equals("complex")) {
                return true;
            } else {

                wrapperClass.addField(streamImplField);
                //TODO
                FieldAdaptation streamImplFieldAdapt = new FieldAdaptation();
                streamImplFieldAdapt.setSurfaceName(streamImplField.getName());
                streamImplFieldAdapt.setFieldType(streamImplField.getType().toString());
                taskHelper.getFieldAdaptations().add(streamImplFieldAdapt);

                SootMethod wrapMethod = wrapper.createStreamAugmentationMethod(cipherImplClass, wrapperClass);
                augmentedMethods.add(wrapMethod.getSignature());
                
                SootMethod getStreamImplMethod = wrapper.createGetStreamImplMethod(wrapperClass);
                augmentedMethods.add(getStreamImplMethod.getSignature());
                
                SootMethod construct = null;
                List<SootMethod> searchForConstruct = wrapperClass.getMethods();
                for (SootMethod possConstruct : searchForConstruct) {
                    if (possConstruct.getName().equals("<init>")) {
                        List<Type> paramTypes = possConstruct.getParameterTypes();
                        if (paramTypes.get(0).equals(wrappedClass.getType())) {
                            construct = possConstruct;
                        }
                    }
                }
                
                SootMethod cipherInitMethod = wrapperClass.getMethodByName("initCipherImpl");
                wrapper.expandConstructorSurface(construct, streamImplField, getStreamImplMethod, cipherInitMethod,
                        wrapMethod);

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
                
                AnalysisFrameAssessment.expandAdaptationSurface(taskHelper, Optional.empty()
                        , Optional.of("Wrapper" + wrapper.getWrappedClass().getShortName() + ".java"),
                        producedFileSource, false);
            }
            return true;
        }
        
        wrapperClass = new SootClass(wrapperClassName, modifiers);
        wrapper.setWrapperClass(wrapperClass);
        Scene.v().addClass(wrapperClass);
        wrapperClass.setSuperclass(wrappedClass);
        
        SootField newField = new SootField(wrappedClass.getShortName().toLowerCase(), wrappedClass.getType(), Modifier.PRIVATE);
        wrapperClass.addField(newField);
        //TODO
        FieldAdaptation fieldAdaptation = new FieldAdaptation();
        fieldAdaptation.setFieldType(newField.getType().toString());
        fieldAdaptation.setSurfaceName(newField.getName());
        taskHelper.getFieldAdaptations().add(fieldAdaptation);
        
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
        
        if (complexity.equals("simple")) {
            
            SootField streamImplField = new SootField(wrapper.getStreamType().replace("java.io.", "").toLowerCase(),
                    Scene.v().getRefType(wrapper.getStreamType()), Modifier.PRIVATE);
            wrapperClass.addField(streamImplField);
            //TODO
            FieldAdaptation streamImplFieldAdapt = new FieldAdaptation();
            streamImplFieldAdapt.setSurfaceName(streamImplField.getName());
            streamImplFieldAdapt.setFieldType(streamImplField.getType().toString());
            taskHelper.getFieldAdaptations().add(streamImplFieldAdapt);
            
            SootMethod wrapMethod = wrapper.createStreamAugmentationMethod(cipherImplClass, wrapperClass);
            
            SootMethod getStreamImplMethod = wrapper.createGetStreamImplMethod(wrapperClass);
            
            SootMethod newConstructor = wrapper.createCipherWrapperConstructorSimple(wrapperClass, newField,
                    cipherImplClass, initMethod, wrapMethod, getStreamImplMethod, streamImplField);
            
            ConstructorAdaptation newConstructorAdapt = new ConstructorAdaptation();
            newConstructorAdapt.setSurfaceName(newConstructor.getName());
            argTypes = new String[newConstructor.getParameterTypes().size()];
            int i = 0;
            for (Type argType : newConstructor.getParameterTypes()) {
                argTypes[i] = argType.toString();
                i++;
            }
            newConstructorAdapt.setArgTypes(argTypes);
            taskHelper.getConstructorAdaptations().add(newConstructorAdapt);
        } else {

            SootField cipherField = new SootField(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, cipherImplClass.getShortName()),
                    cipherImplClass.getType(), Modifier.PRIVATE);
            wrapperClass.addField(cipherField);
            
            FieldAdaptation cipherFieldAdapt = new FieldAdaptation();
            cipherFieldAdapt.setSurfaceName(cipherField.getName());
            cipherFieldAdapt.setFieldType(cipherField.getType().toString());
            
            SootMethod newConstructor = wrapper.createCipherWrapperConstructorComplex(wrapperClass, newField,
                    constructors.get(0), initMethod, cipherField);
            augmentedMethods.add(newConstructor.getSignature());
            
            ConstructorAdaptation newConstructorAdapt = new ConstructorAdaptation();
            newConstructorAdapt.setSurfaceName(newConstructor.getName());
            argTypes = new String[newConstructor.getParameterTypes().size()];
            int i = 0;
            for (Type argType : newConstructor.getParameterTypes()) {
                argTypes[i] = argType.toString();
                i++;
            }
            newConstructorAdapt.setArgTypes(argTypes);
            taskHelper.getConstructorAdaptations().add(newConstructorAdapt);
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
        
        return false;
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
