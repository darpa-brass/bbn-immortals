package com.securboration.immortals.bcas.block;

import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * A basic block is a sequence of instructions where, assuming the first
 * instruction is executed, all subsequent instructions will be executed 
 * (barring any exception throws).  A basic block meets the following additional
 * requirements:
 * 
 * <ol>
 * <li>Only the first instruction in a basic block may be the target of a 
 * jump</li>
 * <li>Only the last instruction in a basic block may be a jump</li>
 * </ol>
 * 
 * @author jstaples
 *
 */
public interface IBasicBlock
{
  
  /**
   * The block ID is an integer that identifies the block uniquely within the
   * control flow graph in which it appears. Decomposing the same method
   * multiple times will always produce the same block IDs.
   * 
   * @return an integer that identifies the block uniquely within the control
   *         flow graph in which it appears.
   */
  public int getBlockId();
  
  /**
   * 
   * @return a list of the instructions in this basic block
   */
  public List<AbstractInsnNode> getBlockInstructions();
  
  /**
   * 
   * @param instruction
   *          an instruction in the method on which the basic block
   *          decomposition was performed
   * @return the corresponding basic block, or null if the instruction does not
   *         belong to a basic block
   */
  public IBasicBlock getBlock(AbstractInsnNode instruction);
  
}
