package com.securboration.immortals.bcas.printer;


import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;

public class MethodPrinter extends ClassVisitor
{
  private final LabelMappableTextifier printer;
  
  private final ByteArrayOutputStream output = new ByteArrayOutputStream();
  private final PrintWriter writer = new PrintWriter(output);
  
  private final String methodName;
  private final String methodDesc;
  private final Integer instructionToPrint;
  
  
  public static String printClass(ClassNode classNode)
  {
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    PrintWriter printer = new PrintWriter(b);
    ClassWriter writer = new ClassWriter(0);
    TraceClassVisitor visitor = new TraceClassVisitor(writer,printer);
    classNode.accept(visitor);
    
    printer.flush();
    printer.close();
    
    return new String(b.toByteArray());
  }
  
  public static String print(
      MethodNode mn
      )
  {
    return print(mn,(AbstractInsnNode)null);
  }
  
  public static String print(
      MethodNode mn,
      AbstractInsnNode i
      )
  {
    return print(mn,i == null ? null : mn.instructions.indexOf(i));
  }
  
  private static String print(
      MethodNode mn,
      Integer instructionIndex
      )
  {
    ClassNode c = new ClassNode();
    {
      c.name = "dummyclass";
      c.methods = new ArrayList<MethodNode>();
      c.methods.add(mn);
    }
    
    LabelMappableTextifier labelAwareTextifier = 
        new LabelMappableTextifier();
    
    MethodPrinter visitor = 
        new MethodPrinter(
            mn.name,
            mn.desc,
            instructionIndex,
            labelAwareTextifier);
    c.accept(visitor);
    
    return visitor.output.toString().trim();
  }
  
  /**
   * 
   * @param methodName null = wildcard
   * @param methodDesc null = wildcard
   * @param instructionIndex null = wildcard
   */
  private MethodPrinter(
      String methodName,
      String methodDesc,
      Integer instructionIndex
      )
  {
    this(methodName,methodDesc,instructionIndex,new LabelMappableTextifier());
  }
  
  private MethodPrinter(
      String methodName,
      String methodDesc,
      Integer instructionIndex, 
      LabelMappableTextifier printer
      )
  {
    super(Opcodes.ASM5);
    
    this.methodName = methodName;
    this.methodDesc = methodDesc;
    this.instructionToPrint = instructionIndex;
    
    this.printer =  printer;
  }
  
  
  @Override
  public MethodVisitor visitMethod(
      final int access, 
      final String name, 
      final String desc, 
      final String signature, 
      final String[] exceptions
      )
  {
    final boolean nameMatches = 
        (name == null)
        ||
        (this.methodName.equals(name));
    
    final boolean descMatches = 
        (desc == null)
        ||
        (this.methodDesc.equals(desc));
    
    if(nameMatches && descMatches)
    {
      if(instructionToPrint != null)
      {
        //print only one instruction in the method
        return new MethodInstructionPrinter(
            printer,
            instructionToPrint);
      }
      else
      {
        //print everything in the method
        return new TraceMethodVisitor(printer);
      }
    }
    
    return new MethodVisitor(Opcodes.ASM5){};
  }
  
  @Override
  public void visitEnd()
  {
    this.printer.print(writer);
    this.writer.flush();
  }

}
