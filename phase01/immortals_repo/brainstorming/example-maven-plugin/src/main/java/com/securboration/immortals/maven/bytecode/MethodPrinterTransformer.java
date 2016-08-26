package com.securboration.immortals.maven.bytecode;

import java.io.PrintStream;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Illustrative transformation that instruments a class such that when its
 * methods are called, a brief message is printed.
 * 
 * @author jstaples
 *
 */
public class MethodPrinterTransformer {
    public MethodPrinterTransformer() {
    }
    
    public void transformClass(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            transformMethod(cn, mn);
        }
    }

    public void transformMethod(ClassNode cn, MethodNode mn) {
        AbstractInsnNode insertionPoint = mn.instructions.getFirst();

        // stack: []
        
        mn.instructions.insertBefore(
                insertionPoint,
                new FieldInsnNode(
                        Opcodes.GETSTATIC,
                        Type.getInternalName(System.class),
                        "out",
                        Type.getDescriptor(PrintStream.class)));
        
        // stack: [System.out]
        
        mn.instructions.insertBefore(
                insertionPoint,
                new LdcInsnNode(
                        String.format(
                                "> invoked method [%s] [%s] [%s]",
                                cn.name, 
                                mn.name, 
                                mn.desc)));

        // stack: [System.out][message]

        mn.instructions.insertBefore(
                insertionPoint,
                new MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(PrintStream.class),
                        "println", 
                        Type.getMethodDescriptor(
                                Type.VOID_TYPE,
                                Type.getType(String.class)
                                ),
                        false));

        // stack: []
    }
}
