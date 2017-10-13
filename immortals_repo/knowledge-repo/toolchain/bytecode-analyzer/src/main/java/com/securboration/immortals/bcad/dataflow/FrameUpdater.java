//package com.securboration.immortals.bcad.dataflow;
//
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.objectweb.asm.ClassReader;
//import org.objectweb.asm.ClassWriter;
//import org.objectweb.asm.Opcodes;
//import org.objectweb.asm.tree.AbstractInsnNode;
//import org.objectweb.asm.tree.ClassNode;
//import org.objectweb.asm.tree.FrameNode;
//import org.objectweb.asm.tree.InsnList;
//import org.objectweb.asm.tree.InsnNode;
//import org.objectweb.asm.tree.LabelNode;
//import org.objectweb.asm.tree.LineNumberNode;
//import org.objectweb.asm.tree.MethodNode;
//
//import com.securboration.immortals.bcad.transformers.helpers.OpcodeHelper;
//
//
///**
// *
// * @author Jacob
// */
//public class FrameUpdater 
//{
//  private final ClassNode originalClass;
//  private final MethodNode originalMethod;
//  
//  public static void updateFrames(ClassNode classNode, MethodNode methodNode)
//  {
//    FrameUpdater updater = new FrameUpdater(classNode,methodNode);
//    updater.updateFrames();
//  }
//  
//  private void updateFrames()
//  {
//    List<MethodNode> methods = originalClass.methods;
//    
//    for(MethodNode method:methods)
//    {
//      boolean descEquals = method.desc.equals(originalMethod.desc);
//      boolean nameEquals = method.name.equals(originalMethod.name);
//      
//      if(nameEquals && descEquals)
//      {
//        updateFrames(method);
//      }
//    }
//  }
//  
//  private String printFrame(FrameNode frame)
//  {
//    return String.format(
//        "%d locals = %s\n" +
//        "%d stack = %s\n",
//        frame.local==null?-1:frame.local.size(),frame.local,
//        frame.stack==null?-1:frame.stack.size(),frame.stack);
//  }
//  
//  private void updateFrames(MethodNode method)
//  {
//    boolean added = false;
//    if(method.instructions.getLast().getType() != AbstractInsnNode.LABEL)
//    {
//      added = true;
//      method.instructions.add(new LabelNode());
//    }
//    
//    addLabelsBeforeAllNew(method.instructions);
//    addLabelsAfterFrames(method.instructions);
//    addLineNumbersToLabels(method.instructions);
//    
//    if(method.instructions.getLast().getType() == AbstractInsnNode.LABEL && added)
//    {
//      method.instructions.remove(method.instructions.getLast());
//    }
//    
//    ClassWriter initialOutput = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//    originalClass.accept(initialOutput);
//    
//    ClassReader reader = new ClassReader(initialOutput.toByteArray());
//    ClassNode newClass = new ClassNode(Opcodes.ASM4);
//    reader.accept(newClass, ClassReader.EXPAND_FRAMES);
//    
//    ClassWriter midOut = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//    newClass.accept(midOut);
//    
//    
//    List<MethodNode> methods = newClass.methods;
//    MethodNode updatedMethod = null;
//    
//    for(MethodNode newMethod:methods)
//    {
//      boolean nameEquals = newMethod.name.equals(method.name);
//      boolean descEquals = newMethod.desc.equals(method.desc);
//      if(nameEquals && descEquals)
//      {
//        updatedMethod = newMethod;
//      }
//    }
//    
//    InsnList oldInstructions = method.instructions;
//    InsnList newInstructions = updatedMethod.instructions;
//    List<FrameNode> oldFrames = getFrames(oldInstructions);
//    List<FrameNode> newFrames = getFrames(newInstructions);
//    Map<LabelNode,LabelNode> labelMap = 
//            buildLabelMap(oldInstructions,newInstructions);
//    
//    if(oldFrames.size() != newFrames.size())
//    {
//      System.out.printf("method %s %s\n",method.name, method.desc);
//      
//      for(int i=0; i<oldFrames.size(); i++)
//      {
//        System.out.printf(
//            "\t%d %d\n",
//            oldInstructions.indexOf(oldFrames.get(i)),
//            newInstructions.indexOf(newFrames.get(i)));
//        
//        System.out.printf(
//            "old:\n%s\nnew:\n%s\n",
//            printFrame(oldFrames.get(i)),
//            printFrame(newFrames.get(i)));
//      }
//      
//      System.out.printf(
//              "%d,%d\n", 
//              oldFrames.size(), 
//              newFrames.size());
//      throw new RuntimeException("expected sizes to be the same");
//    }
//    
//    for(int i=0; i<oldFrames.size(); i++)
//    {
//      FrameNode oldFrame = oldFrames.get(i);
//      FrameNode newFrame = newFrames.get(i);
//      
//      newFrame = (FrameNode) newFrame.clone(labelMap);
//      oldInstructions.insertBefore(oldFrame, newFrame);
//      oldInstructions.remove(oldFrame);
//    }
//    
//    
////    try
////    {
////      ASMHelper.printBinaryToLogDir(originalClass);
////    }
////    catch(Exception e)
////    {
////      throw new RuntimeException(e);
////    }
//    
//    
//    removeNoops(oldInstructions);
//    removeAllLineNumbers(oldInstructions);
//  }
//  
//  private FrameUpdater(ClassNode classNode, MethodNode methodNode)
//  {
//    this.originalClass = classNode;
//    this.originalMethod = methodNode;
//  }
//  
//  
//  
//  
//  
//  private void addLineNumbersToLabels(InsnList instructions)
//  {
//    removeAllLineNumbers(instructions);
//    
//    int count = 999;
//    List<LabelNode> labels = getLabels(instructions);
//    for(LabelNode label:labels)
//    {
//      instructions.insert(label,new InsnNode(Opcodes.NOP));
//      instructions.insert(label,new LineNumberNode(count,label));
//      count++;
//    }
//    
//    instructions.remove(labels.get(labels.size()-1).getNext());
//    instructions.remove(labels.get(labels.size()-1).getNext());
//  }
//  
//  private void removeAllLineNumbers(InsnList instructions)
//  {
//    List<AbstractInsnNode> removeThese = new ArrayList<AbstractInsnNode>();
//    
//    for(AbstractInsnNode instruction:instructions.toArray())
//    {
//      if(instruction.getType() == AbstractInsnNode.LINE)
//      {
//        removeThese.add(instruction);
//      }
//    }
//    
//    for(AbstractInsnNode remove:removeThese)
//    {
//      instructions.remove(remove);
//    }
//  }
//  
//  private void addLabelsAfterFrames(InsnList instructions)
//  {
//    List<FrameNode> frames = getFrames(instructions);
//    
//    for(FrameNode frame:frames)
//    {
//      AbstractInsnNode next = frame.getNext();
//      if(next.getType() == AbstractInsnNode.LABEL)
//      {
//        //do nothing
//      }
//      else
//      {
//        LabelNode newLabel = new LabelNode();
//        instructions.insert(frame,newLabel);
//      }
//    }
//  }
//  
//  private List<AbstractInsnNode> getAllNewInstructions(InsnList instructions)
//  {
//    List<AbstractInsnNode> newInstructions = new ArrayList<AbstractInsnNode>();
//    
//    for(AbstractInsnNode instruction:instructions.toArray())
//    {
//      int opcode = instruction.getOpcode();
//      if(OpcodeHelper.isNewOpcode(opcode))
//      {
//        newInstructions.add(instruction);
//      }
//    }
//    
//    return newInstructions;
//  }
//  
//  private void addLabelsBeforeAllNew(InsnList instructions)
//  {
//    List<AbstractInsnNode> newInstructions = 
//        getAllNewInstructions(instructions);
//    
//    for(AbstractInsnNode newInstruction:newInstructions)
//    {
//      if(newInstruction.getPrevious().getType() != AbstractInsnNode.LABEL)
//      {
//        instructions.insertBefore(newInstruction, new LabelNode());
//      }
//    }
//  }
//  
//  private void removeNoops(InsnList instructions)
//  {
//    for(AbstractInsnNode instruction:instructions.toArray())
//    {
//      if(instruction.getOpcode() == Opcodes.NOP)
//      {
//        instructions.remove(instruction);
//      }
//    }
//  }
//  
//  private List<FrameNode> getFrames(InsnList instructions)
//  {
//    List<FrameNode> frames = new ArrayList<FrameNode>();
//    
//    for(AbstractInsnNode instruction:instructions.toArray())
//    {
//      if(instruction.getType() == AbstractInsnNode.FRAME)
//      {
//        frames.add((FrameNode) instruction);
//      }
//    }
//    return frames;
//  }
//  
//  private List<LabelNode> getLabels(InsnList instructions)
//  {
//    List<LabelNode> labels = new ArrayList<LabelNode>();
//    
//    for(AbstractInsnNode instruction:instructions.toArray())
//    {
//      if(instruction.getType() == AbstractInsnNode.LABEL)
//      {
//        //System.out.printf("\tL@ %d\n", instructions.indexOf(instruction));
//        labels.add((LabelNode) instruction);
//      }
//    }
//    //System.out.printf("\n");
//    return labels;
//  }
//  
//  private Map<LabelNode,LabelNode> buildLabelMap(
//          InsnList before, 
//          InsnList after
//          )
//  {
//    Map<LabelNode,LabelNode> map = new HashMap<LabelNode,LabelNode>();
//    
//    List<LabelNode> oldLabels = getLabels(before);
//    List<LabelNode> newLabels = getLabels(after);
//    
//    if(oldLabels.size() != newLabels.size())
//    {
//      throw new RuntimeException(
//              String.format(
//                "expected # labels to be the same (%d != %d)",
//                oldLabels.size(),
//                newLabels.size()));
//    }
//    
//    for(int i=0; i<oldLabels.size(); i++)
//    {
//      map.put(oldLabels.get(i), newLabels.get(i));
//    }
//    
//    return map;
//  }
//  
//  
//  
//}