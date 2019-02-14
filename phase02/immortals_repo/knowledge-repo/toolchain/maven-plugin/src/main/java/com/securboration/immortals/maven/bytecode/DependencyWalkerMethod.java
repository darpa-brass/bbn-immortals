package com.securboration.immortals.maven.bytecode;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

public class DependencyWalkerMethod extends MethodVisitor{

    public DependencyWalkerMethod() {
        super(Opcodes.ASM5);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        // TODO Auto-generated method stub
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        // TODO Auto-generated method stub
        return super.visitAnnotationDefault();
    }

    @Override
    public void visitAttribute(Attribute attr) {
        // TODO Auto-generated method stub
        super.visitAttribute(attr);
    }

    @Override
    public void visitCode() {
        System.out.println("code:");
        super.visitCode();
    }

    @Override
    public void visitEnd() {
        System.out.println("END");
        super.visitEnd();
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name,
            String desc) {
        System.out.printf("\tFIELD ACCESS: owner=%s, name=%s, desc=%s\n", owner,name,desc);
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack,
            Object[] stack) {
        // TODO Auto-generated method stub
        super.visitFrame(type, nLocal, local, nStack, stack);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        System.out.printf("\tIINC: var=%d\n",var);
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitInsn(int opcode) {
        System.out.printf("\tINSN: opcode=%d\n",opcode);
        super.visitInsn(opcode);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath,
            String desc, boolean visible) {
        // TODO Auto-generated method stub
        return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        // TODO Auto-generated method stub
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
            Object... bsmArgs) {
        // TODO Auto-generated method stub
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        // TODO Auto-generated method stub
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(Label label) {
        // TODO Auto-generated method stub
        super.visitLabel(label);
    }

    @Override
    public void visitLdcInsn(Object cst) {
        // TODO Auto-generated method stub
        super.visitLdcInsn(cst);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        // TODO Auto-generated method stub
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature,
            Label start, Label end, int index) {
        // TODO Auto-generated method stub
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
            TypePath typePath, Label[] start, Label[] end, int[] index,
            String desc, boolean visible) {
        // TODO Auto-generated method stub
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index,
                desc, visible);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        // TODO Auto-generated method stub
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // TODO Auto-generated method stub
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
            String desc, boolean itf) {
        // TODO Auto-generated method stub
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        // TODO Auto-generated method stub
        super.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitParameter(String name, int access) {
        // TODO Auto-generated method stub
        super.visitParameter(name, access);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter,
            String desc, boolean visible) {
        // TODO Auto-generated method stub
        return super.visitParameterAnnotation(parameter, desc, visible);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt,
            Label... labels) {
        // TODO Auto-generated method stub
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef,
            TypePath typePath, String desc, boolean visible) {
        // TODO Auto-generated method stub
        return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler,
            String type) {
        // TODO Auto-generated method stub
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath,
            String desc, boolean visible) {
        // TODO Auto-generated method stub
        return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        // TODO Auto-generated method stub
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        // TODO Auto-generated method stub
        super.visitVarInsn(opcode, var);
    }
    
    

}
