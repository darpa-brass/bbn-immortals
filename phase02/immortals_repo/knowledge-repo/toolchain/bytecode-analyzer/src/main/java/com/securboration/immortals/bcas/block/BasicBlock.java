package com.securboration.immortals.bcas.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import com.securboration.immortals.bca.tools.MethodPrinter;


/**
 * A basic block is a sequence of instructions where, assuming the first
 * instruction is executed, all subsequent instructions will be executed
 * (barring any exception throws). A basic block meets the following additional
 * requirements:
 * 
 * <ol>
 * <li>
 * Only the first instruction in a basic block may be the target of a jump
 * </li>
 * <li>
 * Only the last instruction in a basic block may be a jump or return
 * statement
 * </li>
 * </ol>
 * 
 * @author jstaples
 *
 */
public class BasicBlock
{
  private final BasicBlock root;
  private final Map<AbstractInsnNode,BasicBlock> instructionMapping;
  
  private final List<AbstractInsnNode> blockInstructions = new ArrayList<>();
  private final Set<BasicBlock> successors = new LinkedHashSet<>();
  private final Set<BasicBlock> predecessors = new LinkedHashSet<>();
  private int blockId;
  
  private String getNodeDetails(MethodNode mn)
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append("\"");
    
    sb.append(String.format("node %d\\n",this.getBlockId()));
    
    for(AbstractInsnNode i:blockInstructions)
    {
      sb.append(String.format("%s\\l",MethodPrinter.print(mn, i)));
    }
    sb.append("\"");
    
    return sb.toString();
  }
  
  private String getNodeName(MethodNode mn)
  {
    return getNodeDetails(mn);
  }
  
  public String printDot(MethodNode mn)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("digraph G {\n");
    sb.append("node [shape=box]\n");
    for(BasicBlock b:new LinkedHashSet<>(instructionMapping.values()))
    {
      String color = null;
      
      if(b.getPredecessors().size() == 0)
      {
        color = "green";
      }
      
      else if(b.getSuccessors().size() == 0)
      {
        color = "red";
      }
      
      else if(b.getSuccessors().size() > 1)
      {
        color = "yellow";
      }
      
      if(color != null)
      {
        sb.append(
            String.format(
                "%s [style=filled, fillcolor=%s]\n",
                b.getNodeName(mn),
                color));
      }
    }
    for(BasicBlock b:new LinkedHashSet<>(instructionMapping.values()))
    {
      for(BasicBlock successor:b.successors)
      {
        sb.append(
            String.format(
                "%s -> %s\n",
                b.getNodeName(mn),
                successor.getNodeName(mn)));
      }
    }
    sb.append("}");
    
    return sb.toString();
  }
  
  public String printBlock(MethodNode mn)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    for(AbstractInsnNode i:blockInstructions)
    {
      BasicBlock b = instructionMapping.get(i);
      
      if(b.isFirstInBlock(i))
      {
        if(b != null)
        {
          sb.append(String.format("block has %d predecessors: [",b.predecessors.size()));
          for(BasicBlock successor:b.predecessors)
          {
            sb.append(String.format("%3d,",successor.blockId));
          }
          sb.append(String.format("]\n"));
        }
      }
      
      sb.append(String.format(
          "\t[%s]\n",
          MethodPrinter.print(mn,i)));
      
      if(b.isLastInBlock(i))
      {
        if(b != null)
        {
          sb.append(String.format("block has %d successors:   [",b.successors.size()));
          for(BasicBlock successor:b.successors)
          {
            sb.append(String.format("%3d,",successor.blockId));
          }
          sb.append(String.format("]\n"));
        }
      }
    }
    
    return sb.toString();
  }
  
  public String print(MethodNode mn)
  {
    StringBuilder sb = new StringBuilder();
    
    BasicBlock last = null;
    for(AbstractInsnNode i:mn.instructions.toArray())
    {
      BasicBlock b = instructionMapping.get(i);
      
      if(last != b)
      {
        sb.append("\n");
        if(b != null)
        {
          sb.append(String.format("\t[%d successors: ",b.successors.size()));
          for(BasicBlock successor:b.successors)
          {
            sb.append(String.format("%d,",successor.blockId));
          }
          sb.append(String.format("]\n"));
          
          sb.append(String.format("\t[%d predecessors: ",b.predecessors.size()));
          for(BasicBlock successor:b.predecessors)
          {
            sb.append(String.format("%d,",successor.blockId));
          }
          sb.append(String.format("]\n"));
        }
      }
      
      last = b;
      
      sb.append(String.format(
          "[%d]: [%s]\n",
          b==null?-1:b.blockId,MethodPrinter.print(mn,i)));
    }
    
    return sb.toString();
  }
  
  public static BasicBlock decompose(MethodNode mn)
  {
    InstructionGraphNode graphRoot = 
        InstructionGraphNode.buildInstructionGraph(mn);
    
    //first, determine all terminal instructions
    Set<AbstractInsnNode> startingPoints = 
        new LinkedHashSet<>();
    
    Set<AbstractInsnNode> terminalInstructions = 
        new LinkedHashSet<>();
    Set<AbstractInsnNode> jumpInstructions = 
        new LinkedHashSet<>();
    Set<AbstractInsnNode> jumpTargets = 
        new LinkedHashSet<>();
    
    for(AbstractInsnNode instruction:mn.instructions.toArray())
    {
      InstructionGraphNode node = graphRoot.getNode(instruction);
      
      if(node == null)
      {
        continue;
      }
      
      Set<InstructionGraphNode> successors = node.getNormalSuccessors();
      Set<InstructionGraphNode> predecessors = node.getNormalPredecessors();
      
      if(successors.size() == 0)
      {
        //if it has no non-exceptional successors, then it's a terminal node
        terminalInstructions.add(instruction);
      }
      else if(predecessors.size() == 0)
      {
        //if it has no predecessors, then it's a starting point
        startingPoints.add(instruction);
      }
      else if(node.isJump())
      { 
        jumpInstructions.add(instruction);
        
        for(InstructionGraphNode successor:successors)
        {
          jumpTargets.add(successor.getNode());
        }
      }
    }
    
    int blockIdCounter = 0;
    
    //now build the basic blocks
    Map<AbstractInsnNode,BasicBlock> instructionMapping = 
        new LinkedHashMap<>();
    BasicBlock root = 
        new BasicBlock(
            null,
            instructionMapping,
            blockIdCounter++);
    
    
    Stack<AbstractInsnNode> backtrackCandidates = new Stack<>();
    for(TryCatchBlockNode t:mn.tryCatchBlocks)
    {
      backtrackCandidates.add(t.handler);
    }
    
    Set<AbstractInsnNode> visitedInstructions = new LinkedHashSet<>();
    AbstractInsnNode currentInstruction = mn.instructions.getFirst();
    BasicBlock currentBlock = root;
    
    
    boolean stop = false;
    while(!stop)
    {
      if(currentBlock == null){
        currentBlock = 
            new BasicBlock(root,instructionMapping,blockIdCounter++);
      }
      
      visitedInstructions.add(currentInstruction);
      
      if(terminalInstructions.contains(currentInstruction))
      {
        //this instruction is the last one in the current block
        
        //add this instruction to the current block
        currentBlock.addInstruction(currentInstruction);
        
        //attempt to backtrack
        if(backtrackCandidates.size() == 0)
        {
          stop = true;
        }
        else
        {
          currentInstruction = backtrackCandidates.pop();
          currentBlock = null;
        }
      }
      else if(jumpInstructions.contains(currentInstruction))
      {
        //this instruction is the end of the current block
        
        //add this instruction to the current block
        currentBlock.addInstruction(currentInstruction);
        
        //start a new block after this one
        currentBlock = 
            new BasicBlock(root,instructionMapping,blockIdCounter++);
        
        for(InstructionGraphNode successor:
          graphRoot.getNode(currentInstruction).getNormalSuccessors())
        {
          AbstractInsnNode i = successor.getNode();
          
          if(!visitedInstructions.contains(i))
          {
            backtrackCandidates.push(successor.getNode());
          }
        }
        
        if(backtrackCandidates.size() == 0)
        {
          stop = true;
        }
        else
        {
          currentInstruction = backtrackCandidates.pop();
          currentBlock = null;
        }
      }
      else if(jumpTargets.contains(currentInstruction) && (currentInstruction != mn.instructions.getFirst()))
      {
        //this instruction belongs to a new block
        
        //first, create the new block
        currentBlock = 
            new BasicBlock(root,instructionMapping,blockIdCounter++);
        
        //add this instruction to a new block
        currentBlock.addInstruction(currentInstruction);
        
        //the next instruction is simply the one following the jump target
        currentInstruction = currentInstruction.getNext();
      }
      else
      {
        //this instruction is part of the current block
        
        //add this instruction to the current block
        currentBlock.addInstruction(currentInstruction);
        
        //the next instruction is the one after this
        currentInstruction = currentInstruction.getNext();
      }
    }
    
    //add the links between blocks
    for(AbstractInsnNode jumpInstruction:jumpInstructions)
    {
      BasicBlock fromBlock = instructionMapping.get(jumpInstruction);
      
      InstructionGraphNode n = graphRoot.getNode(jumpInstruction);
      
      for(InstructionGraphNode successor:n.getNormalSuccessors())
      {
        AbstractInsnNode successorInstruction = successor.getNode();
        
        BasicBlock toBlock = instructionMapping.get(successorInstruction);
        
        fromBlock.successors.add(toBlock);
        toBlock.predecessors.add(fromBlock);
      }
    }
    
    for(BasicBlock fromBlock:instructionMapping.values())
    {
      AbstractInsnNode tail = fromBlock.getTail();
      
      if(graphRoot.getNode(tail) == null){
        System.out.println(root.print(mn));
        
        System.out.println("error @ " + MethodPrinter.print(mn,tail));
        
        continue;
      }
      
      final boolean isTerminal = terminalInstructions.contains(tail);
      final boolean isJump = jumpInstructions.contains(tail);
      final boolean isUnknownBlock = graphRoot.getNode(tail) == null;
      final boolean isInterruptedBlock = !(isTerminal||isJump);
      if(isInterruptedBlock)
      {
        InstructionGraphNode successorNode = graphRoot.getNode(tail);
        
        if(successorNode.getNormalSuccessors().size() != 1)
        {
          throw new RuntimeException("assumption violated");
        }
        
        AbstractInsnNode successor = 
            successorNode.getNormalSuccessors().iterator().next().getNode();
        
        BasicBlock toBlock = instructionMapping.get(successor);
        
        fromBlock.successors.add(toBlock);
        toBlock.predecessors.add(fromBlock);
      }
    }
    
    //ensure the numbering is monotonically increasing
    {
      Map<Integer,BasicBlock> map = new HashMap<>();
      for(BasicBlock fromBlock:instructionMapping.values())
      {
        map.put(fromBlock.blockId,fromBlock);
      }
      
      List<Integer> sorted = new ArrayList<>(map.keySet());
      Collections.sort(sorted);
      
      int counter = 0;
      
      for(int key:sorted){
        map.get(key).blockId = counter;
        counter++;
      }
    }
    
    return root;
  }
  
  private void addInstruction(AbstractInsnNode instruction)
  {
    instructionMapping.put(instruction, this);
    blockInstructions.add(instruction);
  }
  
  private BasicBlock(
      BasicBlock root,
      Map<AbstractInsnNode,BasicBlock> instructionMapping,
      final int blockId
      )
  {
    if(root == null)
    {
      this.root = this;
    }
    else
    {
      this.root = root;
    }
    
    this.blockId = blockId;
    
    this.instructionMapping = instructionMapping;
  }
  
  public BasicBlock getRoot()
  {
    return root;
  }
  
  public int getBlockId()
  {
    return blockId;
  }
  
  public List<AbstractInsnNode> getBlockInstructions()
  {
    return new ArrayList<>(blockInstructions);
  }
  
  public Set<BasicBlock> getSuccessors()
  {
    return new LinkedHashSet<>(successors);
  }

  public Set<BasicBlock> getPredecessors()
  {
    return new LinkedHashSet<>(predecessors);
  }

  public AbstractInsnNode getHead()
  {
    return blockInstructions.get(0);
  }

  public AbstractInsnNode getTail()
  {
    return blockInstructions.get(blockInstructions.size()-1);
  }

  public BasicBlock getBlock(AbstractInsnNode instruction)
  {
    return instructionMapping.get(instruction);
  }
  
  public boolean isFirstInBlock(AbstractInsnNode instruction){
    return instruction == blockInstructions.get(0);
  }
  
  public boolean isLastInBlock(AbstractInsnNode instruction){
    return instruction == blockInstructions.get(blockInstructions.size()-1);
  }

  public boolean isThrowableBlock() {
    for(AbstractInsnNode i:this.blockInstructions){
      if(i.getOpcode() == Opcodes.ATHROW){
        return true;
      }
    }
    
    return false;
  }
  
  public interface IBasicBlockTraverser{
      public void visit(AbstractInsnNode instruction);
  }
  
  public interface IBasicBlockEdgeTraverser{
      public void visitEdge(
              List<BasicBlock> pathSoFar,
              BasicBlock src, 
              BasicBlock dst
              );
  }
  
  
  
  public void traverseEdges(IBasicBlockEdgeTraverser traverser){
      traverseEdgesInternal(
          traverser,
          null,
          this,
          new ArrayList<>(Arrays.asList(this)),
          new HashMap<>()
          );
  }
  
  private void traverseEdgesInternal(
          IBasicBlockEdgeTraverser traverser, 
          BasicBlock previous,
          BasicBlock current,
          List<BasicBlock> pathSoFar,
          Map<BasicBlock,Set<BasicBlock>> visited
          ){
      Set<BasicBlock> visitedFromCurrent = visited.get(current);
      
      if(visitedFromCurrent == null){
          visitedFromCurrent = new HashSet<>();
          visited.put(current, visitedFromCurrent);
      }
      
      traverser.visitEdge(pathSoFar,previous, current);
      
      for(BasicBlock successor:current.getSuccessors()){
          
          List<BasicBlock> newPath = new ArrayList<>(pathSoFar);
          newPath.add(successor);
          
          if(visitedFromCurrent.contains(successor)){
              continue;
          }
          visitedFromCurrent.add(successor);
          
          traverseEdgesInternal(
              traverser,
              current,
              successor,
              newPath,
              visited
              );
      }
  }
  
  /**
   * Traverses a sequence of instructions by following control flow
   * 
   * @param traverser
   */
  public void traverse(IBasicBlockTraverser traverser){
      AbstractInsnNode head = this.getHead();
      
      Set<AbstractInsnNode> visited = new HashSet<>();
      traverseInternal(head,visited,traverser);
  }
  
  private void traverseInternal(
          AbstractInsnNode current, 
          Set<AbstractInsnNode> visited,
          IBasicBlockTraverser visitAction
          ){
      if(visited.contains(current)){
          return;
      }
      visited.add(current);
      
      visitAction.visit(current);
      
      BasicBlock b = this.getBlock(current);
      
      if(b==null){
          return;
      }
      
      if(!b.isLastInBlock(current)){
          traverseInternal(current.getNext(),visited,visitAction);
      }
      
      for(BasicBlock successor:b.getSuccessors()){
          traverseInternal(successor.getHead(),visited,visitAction);
      }
  }
  
}
