/*******************************************************************************
 * Copyright (c) 2014, All rights reserved, Securboration Inc.
 * 
 * Securboration Inc. 
 * http://www.securboration.com/
 * 1050 W NASA Blvd, Melbourne FL, 32901
 * 
 * The source code or information contained in this file may be used only in 
 * applications directly related to the Robust Software Modeling Tool (RSMT), 
 * ONR contract N00014-14-1-0462.  All other rights reserved by Securboration.
 * 
 * This code is provided "as is" without warranty of any kind, either expressed 
 * or implied, including but not limited to the implied warranties of 
 * merchantability and/or fitness for a particular purpose.
 ******************************************************************************/
package com.securboration.immortals.bcas.block;

import java.util.Set;

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
public interface ITraversableBasicBlock extends IBasicBlock
{
  
  /**
   * All traversable basic blocks have a pointer to the root for convenience.
   * @return
   */
  public IBasicBlock getRoot();
  
  /**
   * 
   * @return the successors of the basic block
   */
  public Set<ITraversableBasicBlock> getSuccessors();
  
  /**
   * 
   * @return the predecessors of the basic block
   */
  public Set<ITraversableBasicBlock> getPredecessors();
  
  /**
   * The head is the only instruction in the basic block that may be the target
   * of a jump
   * @return the head of the block or null if this is the first block in the
   * method
   */
  public AbstractInsnNode getHead();
  
  /**
   * The tail is the only instruction in the basic block that may be a jump or
   * return statement
   * 
   * @return the tail of the block
   */
  public AbstractInsnNode getTail();
  
}
