package com.securboration.immortals.bcd.util;

import java.io.File;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

public class BytecodeUtils {

    public static ClassNode getClassNode(byte[] bytecode) {
        ClassReader cr = new ClassReader(bytecode);
        ClassNode cn = new ClassNode();

        cr.accept(cn, 0);// 0 = Don't expand frames or compute stack/local
                         // mappings

        return cn;
    }
    
    
    public static SootMethod getSootMethod(
            final SootClass sootClass, 
            final MethodNode mn
            ){
        final String targetDesc = mn.name + mn.desc;
        
        for(SootMethod m:sootClass.getMethods()){
            final String bcs = m.getBytecodeSignature();
            final String desc = bcs.substring(bcs.indexOf(": ") + 2, bcs.length()-1);
            
            if(desc.equals(targetDesc)){
                return m;
            }
        }
        
        throw new RuntimeException("could not find " + targetDesc + " in " + sootClass.getName());
    }
    
    public static SootClass getSootClass(
            final File jar, 
            final String internalName,
            final File...otherJars
            ) throws IOException{
        final StringBuilder cp = new StringBuilder();
        {
            cp.append(Scene.v().defaultClassPath());
            cp.append(File.pathSeparator);
            cp.append(jar.getCanonicalPath());
            
            for(File other:otherJars){
                cp.append(File.pathSeparator);
                cp.append(other.getCanonicalPath());
            }
        }
        
        final String classpath = cp.toString();
        
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_soot_classpath(classpath);
        
        Scene.v().loadBasicClasses();
        Scene.v().loadNecessaryClasses();
        
        SootClass c = Scene.v().forceResolve(
            internalName.replace("/", "."),
            SootClass.BODIES
            );
        
        return c;
    }

}
