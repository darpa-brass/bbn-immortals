package com.securboration.immortals.wrapper;

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
    
    public Wrapper createWrapper(String className, List<String> dependencies) {

        Options.v().set_keep_line_number(true);
        PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");
        SootClass wrappedClass = null;

        try {
            StringBuilder sootCP = new StringBuilder(Scene.v().getSootClassPath());
            
            Scene.v().loadBasicClasses();
            for (String dependency : dependencies) {
                sootCP.append(File.pathSeparatorChar + dependency);
            }
            
            Scene.v().setSootClassPath(sootCP.toString());
            Scene.v().extendSootClassPath(System.getProperty("java.home") + File.separatorChar + "lib" + File.separatorChar + "jce.jar");
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

    public void wrapWithCipher(Wrapper wrapper, String cipherImpl) {

        int modifiers = wrapper.getWrappedClass().getModifiers();
        if (Modifier.toString(modifiers).contains("abstract")) {
            modifiers = modifiers & ~Modifier.ABSTRACT;
        }

        SootClass wrappedClass = wrapper.getWrappedClass();
        SootClass wrapperClass = new SootClass("Wrapper" + wrapper.getWrappedClass().getShortName(), modifiers);
        Scene.v().addClass(wrapperClass);
        wrapperClass.setSuperclass(wrappedClass);

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

        // Doesn't matter which super constructor to call, as long as one gets called
        SootMethod initMethod = wrapper.createCipherInitializationMethod(wrapperClass, cipherImplClass);
        wrapper.createCipherWrapperConstructor(wrapperClass, newField,
                cipherImplClass, constructors.get(0), initMethod);

        wrapper.setWrapperClass(wrapperClass);
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
        
        return fileName;
    } 
    
    public static void main(String[] args) {
    }
}
