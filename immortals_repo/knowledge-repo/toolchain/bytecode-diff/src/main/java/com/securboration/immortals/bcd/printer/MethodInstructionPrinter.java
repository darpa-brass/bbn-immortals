package com.securboration.immortals.bcd.printer;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceMethodVisitor;

public class MethodInstructionPrinter extends MethodVisitor
{
  //the index of the instruction to print
  private final Integer instructionIndex;
  
  //the current instruction index
  private int currentInstructionIndex = 0;
  
  private final LabelMappableTextifier labelMappableTextifier;
  private final TraceMethodVisitor traceVisitor;
  
  
  public MethodInstructionPrinter(
      LabelMappableTextifier labelMappableTextifier,
      final Integer instructionIndex
      )
  {
    super(Opcodes.ASM5);
    
    this.labelMappableTextifier=labelMappableTextifier;
    this.traceVisitor = new TraceMethodVisitor(null,labelMappableTextifier);
    this.instructionIndex = instructionIndex;
  }
  
  /**
   * 
   * @return true iff the call should be propagated
   */
  private boolean visitInstruction()
  {
    if(instructionIndex == null)
    {
      return true;
    }
    else
    {
      final boolean match = currentInstructionIndex == instructionIndex;
      currentInstructionIndex++;
      
      return match;
    }
  }
  
  @Override
  public void visitFieldInsn(int opcode,String owner,String name,String desc)
  {
    if(visitInstruction())
    {
      traceVisitor.visitFieldInsn(opcode, owner, name, desc);
    }
  }
  
  @Override
  public void visitFrame(
      int type, 
      int nLocal, 
      Object[] local, 
      int nStack, 
      Object[] stack
      )
  {
    if(visitInstruction())
    {
      traceVisitor.visitFrame(type, nLocal, local, nStack, stack);
    }
  }
  
  @Override
  public void visitIincInsn(int var,int increment)
  {
    if(visitInstruction())
    {
      traceVisitor.visitIincInsn(var, increment);
    }
  }
  
  @Override
  public void visitInsn(int opcode)
  {
    if(visitInstruction())
    {
      traceVisitor.visitInsn(opcode);
    }
  }
  
  @Override
  public void visitIntInsn(int opcode,int operand)
  {
    if(visitInstruction())
    {
      traceVisitor.visitIntInsn(opcode, operand);
    }
  }
  
  @Override
  public void visitInvokeDynamicInsn(
      String name,
      String desc, 
      Handle handle, 
      Object...args
      )
  {
    if(visitInstruction())
    {
      traceVisitor.visitInvokeDynamicInsn(name, desc, handle, args);
    }
  }
  
  @Override
  public void visitJumpInsn(int opcode,Label label)
  {
    labelMappableTextifier.registerLabel(label);
    
    if(visitInstruction())
    {
      traceVisitor.visitJumpInsn(opcode, label);
    }
  }
  
  @Override
  public void visitLabel(Label label)
  {
    labelMappableTextifier.registerLabel(label);
    
    if(visitInstruction())
    {
      traceVisitor.visitLabel(label);
    }
  }
  
  @Override
  public void visitLdcInsn(Object o)
  {
    if(visitInstruction())
    {
      traceVisitor.visitLdcInsn(o);
    }
  }
  
  @Override
  public void visitLineNumber(int line,Label start)
  {
    labelMappableTextifier.registerLabel(start);
    
    if(visitInstruction())
    {
      traceVisitor.visitLineNumber(line, start);
    }
  }
  
  @Override 
  public void visitLookupSwitchInsn(Label d,int[] keys,Label[] labels)
  {
    labelMappableTextifier.registerLabel(d);
    for(Label l:labels)
    {
      labelMappableTextifier.registerLabel(l);
    }
    
    if(visitInstruction())
    {
      traceVisitor.visitLookupSwitchInsn(d, keys, labels);
    }
  }
  
  @Override
  public void visitMethodInsn(
      int opcode,
      String owner,
      String name,
      String desc,
      boolean isInterface
      )
  {
    if(visitInstruction())
    {
      traceVisitor.visitMethodInsn(opcode, owner, name, desc, isInterface);
    }
  }
  
  @Override
  public void visitMultiANewArrayInsn(String desc,int dim)
  {
    if(visitInstruction())
    {
      traceVisitor.visitMultiANewArrayInsn(desc, dim);
    }
  }
  
  @Override
  public void visitTableSwitchInsn(int min,int max,Label d,Label...labels)
  {
    labelMappableTextifier.registerLabel(d);
    for(Label l:labels)
    {
      labelMappableTextifier.registerLabel(l);
    }
    
    if(visitInstruction())
    {
      traceVisitor.visitTableSwitchInsn(min, max, d, labels);
    }
  }
  
  @Override
  public void visitTypeInsn(int opcode,String type)
  {
    if(visitInstruction())
    {
      traceVisitor.visitTypeInsn(opcode, type);
    }
  }
  
  @Override
  public void visitVarInsn(int opcode,int var)
  {
    if(visitInstruction())
    {
      traceVisitor.visitVarInsn(opcode, var);
    }
  }
  
  @Override
  public void visitTryCatchBlock(
      Label start,
      Label end,
      Label handler,
      String type
      )
  {
    labelMappableTextifier.registerLabel(start);
    labelMappableTextifier.registerLabel(end);
    labelMappableTextifier.registerLabel(handler);
  }

  /*
   * visitCode ( 
   *  visitFrame 
   *  | 
   *  visitXInsn 
   *  | 
   *  visitLabel 
   *  | 
   *  visitInsnAnnotation 
   *  | 
   *  visitTryCatchBlock 
   *  | 
   *  visitTryCatchBlockAnnotation 
   *  | 
   *  visitLocalVariable 
   *  | 
   *  visitLocalVariableAnnotation 
   *  | 
   *  visitLineNumber 
   *  )
   */
  
//  {
//    super.visitFieldInsn(opcode, owner, name, desc);
//    super.visitFrame(type, nLocal, local, nStack, stack);
//    super.visitIincInsn(var, increment);
//    super.visitInsn(opcode);
//    super.visitIntInsn(opcode, operand);
//    super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
//    super.visitJumpInsn(opcode, label);
//    super.visitLabel(label);
//    super.visitLdcInsn(cst);
//    super.visitLineNumber(line, start);
//    super.visitLookupSwitchInsn(dflt, keys, labels);
//    super.visitMethodInsn(opcode, owner, name, desc, itf);
//    super.visitMultiANewArrayInsn(desc, dims);
//    super.visitTableSwitchInsn(min, max, dflt, labels);
//    super.visitTypeInsn(opcode, type);
//    super.visitVarInsn(opcode, var);
//  }
  
}
