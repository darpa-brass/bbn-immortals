package com.securboration.immortals.bcas.block;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import com.securboration.immortals.bcad.transformers.helpers.OpcodeHelper;
import com.securboration.immortals.bcas.printer.MethodPrinter;

/**
 * An instruction graph is a sequence of single-instruction nodes where each
 * node is connected to its plausible successors. Each node maintains a
 * reference to a map object (shared by nodes) that allows it to lookup any
 * instruction in the graph in O(1) time.
 * 
 * @author jstaples
 *
 */
public class InstructionGraphNode
{
  //this node's instruction
  private final AbstractInsnNode node;
  
  //the instruction's successors that can be reached via normal execution
  private final Set<InstructionGraphNode> successors = 
      new LinkedHashSet<>();
  
  //the instruction's successors that can be reached via normal execution
  private final Set<InstructionGraphNode> predecessors = 
      new LinkedHashSet<>();
  
  //the instruction's successors that can only be reached if an exception is
  // thrown and subsequently caught in this method
  private final Set<InstructionGraphNode> exceptionalSuccessors = 
      new LinkedHashSet<>();
  
  //the instruction's successors that can be reached via normal execution
  private final Set<InstructionGraphNode> exceptionalPredecessors = 
      new LinkedHashSet<>();
  
  //a mapping shared between all nodes in the graph that allows each node to
  // quickly lookup other graph nodes
  private final Map<AbstractInsnNode,InstructionGraphNode> instructionMap;
  
  private InstructionGraphNode(
      AbstractInsnNode i,
      Map<AbstractInsnNode,InstructionGraphNode> instructionMap
      )
  {
    this.node = i;
    this.instructionMap = instructionMap;
  }
  
  private static InstructionGraphNode buildGraph(
      MethodNode mn,
      AbstractInsnNode start,
      Map<AbstractInsnNode,InstructionGraphNode> mapping
      )
  {
    if(mapping.containsKey(start))
    {
      return mapping.get(start);
    }
    
    InstructionGraphNode root = 
        new InstructionGraphNode(start,mapping);
    
    mapping.put(start, root);
    
    if(start.getNext() == null)
    {
      return root;
    }
    
    buildInstructionGraphNode(
        mn,
        root,
        start.getNext(),
        mapping,
        new LinkedHashMap<>());
    
    return root;
  }
  
  private static Map<AbstractInsnNode,Set<TryCatchBlockNode>> buildExceptionMap(
      MethodNode mn
      )
  {
    //a  map from normal control flow instructions to exception structures
    
    Map<LabelNode,Set<TryCatchBlockNode>> beginnings = new LinkedHashMap<>();
    Map<LabelNode,Set<TryCatchBlockNode>> endings = new LinkedHashMap<>();
    
    for(TryCatchBlockNode t:mn.tryCatchBlocks)
    {
      LabelNode start = t.start;
      LabelNode end = t.end;
      
      Set<TryCatchBlockNode> scopes = beginnings.get(start);
      
      if(scopes == null)
      {
        scopes = new LinkedHashSet<>();
        beginnings.put(start,scopes);
      }
      
      scopes.add(t);
      
      scopes = endings.get(end);
      
      if(scopes == null)
      {
        scopes = new LinkedHashSet<>();
        endings.put(end,scopes);
      }
      
      scopes.add(t);
    }
    
    Set<TryCatchBlockNode> blocksInScope = new LinkedHashSet<>();
    Map<AbstractInsnNode,Set<TryCatchBlockNode>> map = new LinkedHashMap<>();
    
    for(AbstractInsnNode i:mn.instructions.toArray())
    {
      Set<TryCatchBlockNode> newScopes = beginnings.get(i);
      if(newScopes != null)
      {
        blocksInScope.addAll(newScopes);
      }
      
      Set<TryCatchBlockNode> endScopes = endings.get(i);
      if(endScopes != null)
      {
        blocksInScope.removeAll(endScopes);
      }
      
      if(blocksInScope.size() > 0)
      {
        map.put(i,new LinkedHashSet<>(blocksInScope));
      }
    }
    
    return map;
  }
  
  public static InstructionGraphNode buildInstructionGraph(MethodNode mn)
  {
    Map<AbstractInsnNode,InstructionGraphNode> instructionMap = 
        new LinkedHashMap<AbstractInsnNode,InstructionGraphNode>();
    
    //first build the normal flow graph starting at the first instruction
    InstructionGraphNode root = 
        buildGraph(
            mn,
            mn.instructions.getFirst(),
            instructionMap);
    
    //build a mapping from catch blocks to their exception handler graphs
    {
      Map<TryCatchBlockNode,InstructionGraphNode> exceptionHandlers = 
          new LinkedHashMap<>();
      for(TryCatchBlockNode t:mn.tryCatchBlocks)
      {
        InstructionGraphNode node = 
            buildGraph(
                mn,
                t.handler,
                instructionMap);
        
        exceptionHandlers.put(t,node);
      }
      
      Map<AbstractInsnNode,Set<TryCatchBlockNode>> exceptionScopeMap = 
          buildExceptionMap(mn);
      for(AbstractInsnNode i:exceptionScopeMap.keySet())
      {
        Set<TryCatchBlockNode> blocksInScope = exceptionScopeMap.get(i);
        
        for(TryCatchBlockNode blockInScope:blocksInScope)
        {
          InstructionGraphNode from = instructionMap.get(i);
          //TODO: from might not be included in the graph but its predecessor 
          //       must
          if(from == null)
          {
            from = instructionMap.get(i.getPrevious());
          }
          
          InstructionGraphNode to = instructionMap.get(blockInScope.handler);
          
          from.exceptionalSuccessors.add(to);
        }
      }
    }
    
    //build the predecessor links
    {
      for(InstructionGraphNode n:instructionMap.values())
      {
        for(InstructionGraphNode successor:n.getNormalSuccessors())
        {
          successor.predecessors.add(n);
        }
        
        for(InstructionGraphNode successor:n.getExceptionalSuccessors())
        {
          successor.exceptionalPredecessors.add(n);
        }
      }
    }
    
    return root;
  }
  
  private String printSuccessors(MethodNode mn)
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append(String.format("\t%d normal successors:\n",successors.size()));
    for(InstructionGraphNode successor:successors)
    {
      sb.append(String.format(
          "\t\t%s\n",
          MethodPrinter.print(
              mn,
              successor.getNode())));
    }
    
    sb.append(
        String.format(
            "\t%d exceptional successors:\n",exceptionalSuccessors.size()));
    for(InstructionGraphNode successor:exceptionalSuccessors)
    {
      sb.append(String.format(
          "\t\t%s\n",
          MethodPrinter.print(
              mn,
              successor.getNode())));
    }
    
    return sb.toString();
  }
  
  private String printPredecessors(MethodNode mn)
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append(String.format("\t%d normal predecessors:\n",predecessors.size()));
    for(InstructionGraphNode predecessor:predecessors)
    {
      sb.append(String.format(
          "\t\t%s\n",
          MethodPrinter.print(
              mn,
              predecessor.getNode())));
    }
    
    sb.append(
        String.format(
            "\t%d exceptional predecessors:\n",exceptionalPredecessors.size()));
    for(InstructionGraphNode predecessor:exceptionalPredecessors)
    {
      sb.append(String.format(
          "\t\t%s\n",
          MethodPrinter.print(
              mn,
              predecessor.getNode())));
    }
    
    return sb.toString();
  }
  
  public String print(MethodNode mn)
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append(String.format("%d try/catch blocks:\n",mn.tryCatchBlocks.size()));
    for(TryCatchBlockNode t:mn.tryCatchBlocks)
    {
      sb.append(
          String.format(
              "\t[%s,%s]->[%s]\n",
              MethodPrinter.print(mn,t.start),
              MethodPrinter.print(mn,t.end),
              MethodPrinter.print(mn,t.handler)));
    }
    for(AbstractInsnNode i:mn.instructions.toArray())
    {
      sb.append(String.format(
          "%s\n",
          MethodPrinter.print(
              mn,
              i)));
      
      if(instructionMap.get(i) == null)
      {
        sb.append("\tno graph structure backing this node\n");
      }
      else
      {
        sb.append(instructionMap.get(i).printSuccessors(mn));
        sb.append(instructionMap.get(i).printPredecessors(mn));
      }
    }
    
    return String.format(
        "instruction graph for %s:{\n%s}\n",
        mn.name,
        sb.toString());
  }
  
  private static Set<AbstractInsnNode> getSuccessors(
      AbstractInsnNode instruction
      )
  {
    Set<AbstractInsnNode> successors = new LinkedHashSet<>();
    
    if(instruction.getType() == AbstractInsnNode.JUMP_INSN)
    {
      JumpInsnNode j = (JumpInsnNode)instruction;
      
      if(j.getOpcode() == Opcodes.GOTO)
      {
        //it's an unconditional GOTO
        successors.add(j.label);
      }
      else if(j.getOpcode() == Opcodes.JSR)
      {
        //it's a JSR (similar to a method call)
        successors.add(j.getNext());
      }
      else
      {
        //It's a conditional jump with two possible outcomes
        successors.add(j.getNext());
        successors.add(j.label);
      }
    }
    else if(instruction.getType() == AbstractInsnNode.LOOKUPSWITCH_INSN)
    {
      LookupSwitchInsnNode l = (LookupSwitchInsnNode)instruction;
      
      successors.addAll(l.labels);
      successors.add(l.dflt);
    }
    else if(instruction.getType() == AbstractInsnNode.TABLESWITCH_INSN)
    {
      TableSwitchInsnNode t = (TableSwitchInsnNode)instruction;
      
      successors.addAll(t.labels);
      successors.add(t.dflt);
    }
    else
    {
      //it isn't a branch instruction so its successors are simply its next
      // instructions
      successors.add(instruction.getNext());
    }
    
    return successors;
  }
  
  
  /**
   * Builds a simple graph that does not track exception behavior (an ATHROW
   * is interpreted as the end of an instruction chain and no catch blocks are
   * examined).
   * 
   * @param predecessor
   * @param currentInstruction
   * @param visited
   * @param backtrackingCandidates
   */
  private static void buildInstructionGraphNode(
      MethodNode mn,
      InstructionGraphNode predecessor,
      AbstractInsnNode currentInstruction,
      Map<AbstractInsnNode,InstructionGraphNode> visited,
      Map<AbstractInsnNode,Set<InstructionGraphNode>> backtrackingCandidates
      )
  {
    boolean stop = false;
    
    while(!stop)
    {
      boolean isEndOfPath = false;
      boolean isRevisit = false;
      
      //figure out what kind of instruction this is
      final InstructionGraphNode currentNode;
      
      if(visited.containsKey(currentInstruction))
      {
        isRevisit = true;
        currentNode = visited.get(currentInstruction);
      }
      else
      {
        currentNode = new InstructionGraphNode(currentInstruction,visited);
        visited.put(currentInstruction, currentNode);
        
        if(OpcodeHelper.isExitOpcode(currentInstruction.getOpcode()))
        {
          isEndOfPath = true;
        }
      }
      
      //add link from old->new
      predecessor.successors.add(currentNode);
      
      //get the next entry
      AbstractInsnNode next;
      
      if(isEndOfPath || isRevisit)
      {
        //no next instruction
        next = null;
      }
      else
      {
        Set<AbstractInsnNode> successorsToThisInstruction = 
            getSuccessors(currentInstruction);
        
        //find a valid successor
        next = successorsToThisInstruction.iterator().next();
        
        successorsToThisInstruction.remove(next);
        
        for(AbstractInsnNode successor:successorsToThisInstruction)
        {
          Set<InstructionGraphNode> s = backtrackingCandidates.get(successor);
          if(s == null)
          {
            s = new LinkedHashSet<>();
            backtrackingCandidates.put(successor, s);
          }
          
          s.add(currentNode);
        }
      }
      
      if(next == null)
      {
        //if we didn't find a successor the easy way, get an entry from the 
        // backtracking candidates if one exists
        
        if(backtrackingCandidates.isEmpty())
        {
          //if no more backtracking candidates, we've reached the end of the
          // control flow path and have exhausted all other possible branches
          // so we stop looking
          stop = true;
        }
        else
        {
          next = backtrackingCandidates.keySet().iterator().next();
          currentInstruction = next;
          
          Set<InstructionGraphNode> backtrackValues = 
              backtrackingCandidates.get(next);
          
          predecessor = backtrackValues.iterator().next();
          backtrackValues.remove(predecessor);
          
          if(backtrackValues.size() == 0)
          {
            backtrackingCandidates.remove(next);
          }
        }
      }
      else
      {
        //we did find a successor the easy way so simply update predecessor and
        //  currentInstruction
        predecessor = currentNode;
        currentInstruction = next;
      }
    }
  }
  
  public Set<MethodInsnNode> getFirstConstructorCalls(
      final String internalClassName,
      final String internalSuperClassName
      )
  {
    Set<MethodInsnNode> superInitCalls = new LinkedHashSet<>();
    
    getFirstConstructorCalls(
        internalClassName,
        internalSuperClassName,
        this,
        new LinkedHashSet<>(),
        superInitCalls);
    
    return superInitCalls;
  }
  
  private static void getFirstConstructorCalls(
      final String internalClassName,
      final String internalSuperClassName,
      InstructionGraphNode current,
      Set<InstructionGraphNode> visited,
      Set<MethodInsnNode> superInitCalls
      )
  {
    if(visited.contains(current))
    {
      return;
    }
    
    visited.add(current);
    
    AbstractInsnNode i = current.getNode();
    
    if(i.getType() == AbstractInsnNode.METHOD_INSN)
    {
      MethodInsnNode m = (MethodInsnNode)i;
      
      final boolean methodNameMatches = 
          m.name.equals("<init>");
      final boolean classNameMatches = 
          m.owner.equals(internalClassName) 
          || 
          m.owner.equals(internalSuperClassName);
      
      if(methodNameMatches && classNameMatches)
      {
        superInitCalls.add(m);
        return;
      }
    }
    
    for(InstructionGraphNode next:current.getNormalSuccessors())
    {
      getFirstConstructorCalls(
          internalClassName,
          internalSuperClassName,
          next,
          current.getNormalSuccessors().size() > 1 ? 
              new LinkedHashSet<>(visited) : visited,
          superInitCalls);
    }
  }
  
  public boolean isJump()
  {
    if(this.successors.size() > 1)
    {
      return true;
    }
    
    if(this.node.getType() == AbstractInsnNode.JUMP_INSN)
    {
      return true;
    }
    
    return false;
  }
  
  public InstructionGraphNode getNode(AbstractInsnNode n)
  {
    return instructionMap.get(n);
  }

  public AbstractInsnNode getNode()
  {
    return node;
  }
  
  public Set<InstructionGraphNode> getAllPredecessors()
  {
    Set<InstructionGraphNode> s = new LinkedHashSet<>();
    s.addAll(getNormalPredecessors());
    s.addAll(getExceptionalPredecessors());
    return s;
  }
  
  public Set<InstructionGraphNode> getAllSuccessors()
  {
    Set<InstructionGraphNode> s = new LinkedHashSet<>();
    s.addAll(getNormalSuccessors());
    s.addAll(getExceptionalSuccessors());
    return s;
  }

  public Set<InstructionGraphNode> getNormalSuccessors()
  {
    return successors;
  }
  
  public Set<InstructionGraphNode> getExceptionalSuccessors()
  {
    return exceptionalSuccessors;
  }
  
  public Set<InstructionGraphNode> getNormalPredecessors()
  {
    return predecessors;
  }
  
  public Set<InstructionGraphNode> getExceptionalPredecessors()
  {
    return exceptionalPredecessors;
  }
}
