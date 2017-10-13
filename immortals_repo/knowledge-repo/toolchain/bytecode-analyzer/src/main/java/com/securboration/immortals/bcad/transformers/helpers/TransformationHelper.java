package com.securboration.immortals.bcad.transformers.helpers;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;


public class TransformationHelper {
  
  public static boolean isInterface(ClassNode cn){
    return (cn.access & Opcodes.ACC_INTERFACE) > 0;
  }
  
  public static boolean isAbstract(MethodNode mn){
    return (mn.access & Opcodes.ACC_ABSTRACT) > 0;
  }
  
  /**
   * 
   * @param cn
   *          the class containing the method to examine
   * @param mn
   *          the method to examine
   * @return the first instruction in a method that we can safely insert other
   *         instructions before (an example of an instruction that ISN'T safe
   *         to insert before is a frame instruction)
   */
  public static AbstractInsnNode getInstructionInsertionPoint(
      ClassNode cn,
      MethodNode mn
      )
  {
    final boolean isConstructor = mn.name.equals("<init>");
    
    for(AbstractInsnNode instruction:mn.instructions.toArray())
    {
      final boolean isValidFirstInstructionType = 
          OpcodeHelper.isOpcodeAnyOf(
              instruction.getType(),
              AbstractInsnNode.FIELD_INSN,
              AbstractInsnNode.IINC_INSN,
              AbstractInsnNode.INSN,
              AbstractInsnNode.INT_INSN,
              AbstractInsnNode.INVOKE_DYNAMIC_INSN,
              AbstractInsnNode.JUMP_INSN,
              AbstractInsnNode.LABEL,
              AbstractInsnNode.LDC_INSN,
              AbstractInsnNode.LINE,
              AbstractInsnNode.LOOKUPSWITCH_INSN,
              AbstractInsnNode.METHOD_INSN,
              AbstractInsnNode.MULTIANEWARRAY_INSN,
              AbstractInsnNode.TABLESWITCH_INSN,
              AbstractInsnNode.TYPE_INSN,
              AbstractInsnNode.VAR_INSN);
      
      if(isValidFirstInstructionType)
      {
        return instruction;
      }
    }
    
    throw new RuntimeException(
        "no valid start instruction found in method " + mn.name
        );
  }

}
