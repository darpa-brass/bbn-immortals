package com.securboration.immortals.maven.bytecode;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

public class DependencyWalkerClass extends ClassVisitor{
    
    private String className = null;

    public DependencyWalkerClass() {
        super(Opcodes.ASM5);
        
        
        
//        super.visitMethod(access, name, desc, signature, exceptions)
    }
    
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        // TODO Auto-generated method stub
        super.visit(version, access, name, signature, superName, interfaces);
        
        this.className = name;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        // TODO Auto-generated method stub
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitAttribute(Attribute attr) {
        // TODO Auto-generated method stub
        super.visitAttribute(attr);
    }

    @Override
    public void visitEnd() {
        // TODO Auto-generated method stub
        super.visitEnd();
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        // TODO Auto-generated method stub
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName,
            int access) {
        // TODO Auto-generated method stub
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        // TODO Auto-generated method stub
        super.visitOuterClass(owner, name, desc);
    }

    @Override
    public void visitSource(String source, String debug) {
        // TODO Auto-generated method stub
        super.visitSource(source, debug);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath,
            String desc, boolean visible) {
        // TODO Auto-generated method stub
        return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("visited method [%s] [%s] [%s]\n", className,name,desc));
        
        if(exceptions != null){
            for(String exception:exceptions){
                sb.append(String.format("\tthrows %s\n", exception));
            }
        }
        
        System.out.println(sb.toString());
        return new DependencyWalkerMethod();
    }
    
    

}
