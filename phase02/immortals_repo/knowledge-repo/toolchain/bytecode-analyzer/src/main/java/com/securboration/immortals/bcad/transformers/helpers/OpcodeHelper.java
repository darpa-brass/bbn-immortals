package com.securboration.immortals.bcad.transformers.helpers;


import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;


/**
 * Utility methods related to opcodes 
 * 
 * @author jstaples
 *
 */
public class OpcodeHelper
{
  protected byte[] inputClassBytes;
  
  private OpcodeHelper(){}
  
  /**
   * These instruction types result in the instantiation of a new object on
   * the heap
   */
  public static final int[] NEW_OPCODES = new int[]{
    Opcodes.ANEWARRAY,
    Opcodes.NEWARRAY,
    Opcodes.MULTIANEWARRAY,
    Opcodes.NEW
  };
  
  public static final int[] INVOKE_OPCODES = new int[]{
    Opcodes.INVOKEDYNAMIC,
    Opcodes.INVOKEINTERFACE,
    Opcodes.INVOKESPECIAL,
    Opcodes.INVOKESTATIC,
    Opcodes.INVOKEVIRTUAL
  };
  
  public static final int[] RETURN_OPCODES = new int[]{
    Opcodes.RETURN,
    
    Opcodes.ARETURN,
    Opcodes.IRETURN,
    Opcodes.FRETURN,
    Opcodes.LRETURN,
    Opcodes.DRETURN,
  };
  
  public static final int[] THROW_OPCODES = new int[]{
    Opcodes.ATHROW,
  };
  
  public static final int[] METHOD_EXIT_OPCODES = 
      combineArrays(
          RETURN_OPCODES,
          THROW_OPCODES);
  
  private static final int[] READ_VAR_OPCODES = new int[]{
          Opcodes.ALOAD,
          Opcodes.ILOAD,
          Opcodes.FLOAD,
          Opcodes.LLOAD,
          Opcodes.DLOAD,
        };
  
  private static final int[] WRITE_VAR_OPCODES = new int[]{
          Opcodes.ASTORE,
          Opcodes.ISTORE,
          Opcodes.FSTORE,
          Opcodes.LSTORE,
          Opcodes.DSTORE,
        };
  
    public static boolean isReadInstruction(VarInsnNode instruction) {
        boolean isRead = isReadVarOpcode(instruction.getOpcode());
        boolean isWrite = isWriteVarOpcode(instruction.getOpcode());

        if (!isRead && !isWrite) {
            throw new RuntimeException("not a read or a write (what is it?)");
        } else if (isRead && isWrite) {
            throw new RuntimeException(
                    "instruction cannot be a read and a write");
        }

        return isRead;
    }
  
  public static int[] combineArrays(int[]...arrays)
  {
    Set<Integer> list = new TreeSet<>();
    for(int[] array:arrays)
    {
      for(int value:array)
      {
        list.add(value);
      }
    }
    
    int[] newArray = new int[list.size()];
    int index = 0;
    for(int value:list)
    {
      newArray[index] = value;
      index++;
    }
    
    return newArray;
  }
  
  public static boolean isAnyFlagSet(int value,int...flags){
      for(int flag:flags){
          if((value & flag) > 0){
              return true;
          }
      }
      
      return false;
  }
  
  public static boolean isOpcodeAnyOf(int opcode,int...values)
  {
    for(int value:values)
    {
      if(opcode == value)
      {
        return true;
      }
    }
    return false;
  }

  /**
   * stack before: ...]
   * stack after:  ...]
   * @param cn
   * @param mn
   * @param insertionPoint
   * @param msg 
   */
  public void addSystemOutPrintln(
      ClassNode cn,
      MethodNode mn,
      AbstractInsnNode insertionPoint,
      String msg
      )
  {
    // stack: ...]

    mn.instructions.insertBefore(
        insertionPoint, 
        new FieldInsnNode(
            Opcodes.GETSTATIC,
            Type.getInternalName(System.class),
            "out",
            Type.getDescriptor(PrintStream.class)));

    // stack: ...]PrintStream

    mn.instructions.insertBefore(
        insertionPoint, 
        new LdcInsnNode(msg));
    
    // stack: ...]PrintStream,String
    
    mn.instructions.insertBefore(
        insertionPoint,
        new MethodInsnNode(
            Opcodes.INVOKEVIRTUAL,
            Type.getInternalName(PrintStream.class),
            "println",
            Type.getMethodDescriptor(
                Type.VOID_TYPE,
                Type.getType(String.class)),
            false));

    // NOTE: PrintStream & String are popped by PrintStream.println()
    // stack: ...]
  }
  
  public static boolean isReturnOpcode(int opcode){
    return isOpcodeAnyOf(
        opcode,
        RETURN_OPCODES
        );
  }
  
  public static boolean isExitOpcode(int opcode)
  {
    return OpcodeHelper.isOpcodeAnyOf(
        opcode,
        METHOD_EXIT_OPCODES
        );
  }
  
  public static boolean isReadVarOpcode(int opcode){
      return isOpcodeAnyOf(opcode,READ_VAR_OPCODES);
  }
  
  public static boolean isWriteVarOpcode(int opcode){
      return isOpcodeAnyOf(opcode,WRITE_VAR_OPCODES);
  }
  
  public static boolean isNewOpcode(int opcode){
      return isOpcodeAnyOf(opcode,NEW_OPCODES);
  }
}
