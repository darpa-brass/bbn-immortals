package com.securboration.immortals.bcad.transformers;



import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.securboration.immortals.bcad.instrument.IBytecodeTransformerClass;
import com.securboration.immortals.bcad.instrument.IBytecodeTransformerMethod;
import com.securboration.immortals.bcad.runtime.EventBroadcaster;
import com.securboration.immortals.bcad.runtime.TrackingFilter;
import com.securboration.immortals.bcad.transformers.helpers.OpcodeHelper;
import com.securboration.immortals.bcad.transformers.helpers.TransformationHelper;
import com.securboration.immortals.bcas.block.BasicBlock;
import com.securboration.immortals.bcas.block.InstructionGraphNode;


/**
 * Adds instructions for tracking control flow events
 * 
 * @author jstaples
 *
 */
public class TraceTransformer implements IBytecodeTransformerClass{
  private static class LocalVariables
  {
    int shouldFilterOffset = 0;
    int newOffset = 0;
    
    LocalVariables(int startingOffset)
    {
      newOffset = startingOffset;
    }
  }

  private LabelNode injectInsertionPoint(ClassNode cn, MethodNode mn)
  {
    final AbstractInsnNode firstInstruction = 
        TransformationHelper.getInstructionInsertionPoint(cn,mn);
    
    final LabelNode insertionPoint = new LabelNode();
    
    mn.instructions.insertBefore(
        firstInstruction,
        insertionPoint
        );
    
    return insertionPoint;
  }
  
  /**
   * Adjusts all of the local variable references within the indicated method
   * such that those whose value is >= startingOffsetInclusive will be offset
   * by offsetDelta
   * 
   * Added handling for IINC which is an implicit read-modify-write of a local
   * 
   * @param mn
   * @param startingOffsetInclusive
   * @param offsetDelta
   */
  private void updateLocalVariableOffsets(
      MethodNode mn, 
      final int startingOffsetInclusive,
      final int offsetDelta)
  {
    for(AbstractInsnNode i:mn.instructions.toArray())
    {
      if(i.getType() == AbstractInsnNode.VAR_INSN)
      {
        VarInsnNode v = (VarInsnNode)i;
        
        if(v.var >= startingOffsetInclusive)
        {
          mn.instructions.insertBefore(
              v,
              new VarInsnNode(v.getOpcode(),v.var+offsetDelta));
          
          mn.instructions.remove(v);
        }
      }
      else if(i.getType() == AbstractInsnNode.IINC_INSN)
      {
        IincInsnNode inc = (IincInsnNode)i;
        
        if(inc.var >= startingOffsetInclusive)
        {
          mn.instructions.insertBefore(
              inc,
              new IincInsnNode(inc.var+offsetDelta,inc.incr));
          
          mn.instructions.remove(inc);
        }
      }
    }
  }
  
  private int getStartingOffset(MethodNode mn)
  {
    int offset = 1;
    if((mn.access & Opcodes.ACC_STATIC) > 0)
    {
      offset--;
    }
    
    for(Type t:Type.getMethodType(mn.desc).getArgumentTypes())
    {
      offset += t.getSize();
    }
    
    return offset;
  }
  
  



    @Override
    public IBytecodeTransformerMethod acquireMethodTransformer(
            String classHash,
            ClassNode cn
            ) {
        return new IBytecodeTransformerMethod(){

            @Override
            public boolean transformMethod(
                    String classHash, 
                    String methodId,
                    ClassNode methodOwner, 
                    MethodNode mn
                    ) {
                final Map<AbstractInsnNode,Integer> originalInstructions = 
                        new HashMap<>();
                originalInstructions.putAll(decompose(mn));
                
                    FieldNode methodIdField = 
                        Helper.acquireFieldForMethod(
                            cn,
                            methodId,
                            "methodId",
                            Type.getDescriptor(String.class),
                            Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC+Opcodes.ACC_FINAL,
                            methodId
                            );
                    
                    FieldNode filterField = 
                        Helper.acquireFieldForMethod(
                            cn,
                            methodId,
                            "filterField",
                            Type.getDescriptor(TrackingFilter.class),
                            Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC,
                            null
                            );
                    
                    final LabelNode insertionPoint = injectInsertionPoint(cn,mn);
                    
                    LocalVariables localVars = new LocalVariables(getStartingOffset(mn));
                    
                    initShouldFilterLocalVar(
                        cn,
                        mn,
                        insertionPoint,
                        localVars,
                        filterField
                        );
                    
                    addVisitMethodEnterCall(
                        cn,
                        mn,
                        methodIdField,
                        insertionPoint,
                        localVars
                        );
                    
                    addControlFlowTracking(
                        originalInstructions,
                        classHash,
                        cn,
                        methodId,
                        mn,
                        localVars
                        );
                    
                    addVisitMethodExitInstructions(
                        cn,
                        mn,
                        methodIdField,
                        localVars
                        );
                    
                    addVisitCatchBlockInstructions(
                        cn,
                        mn,
                        methodIdField,
                        localVars
                        );
                    
                    addInitFilterToClinit(
                        cn,
                        filterField,
                        methodIdField
                        );
                    
                    wrapMethodInTryCatchBlock(
                        cn,
                        mn,
                        methodIdField,
                        localVars,
                        filterField
                        );
                    
                    return true;
            }
            
        };
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
  private static AbstractInsnNode getTryCatchWrapperInsertionPoint(
      ClassNode cn,
      MethodNode mn
      )
  {
    final boolean isConstructor = 
        mn.name.equals("<init>");
    
    if(isConstructor)
    {
      //Due to constraints placed on the bytecode format starting in JVM spec 
      // 1.8, we cannot safely inject instructions before a call to this.<init> 
      // or super.<init>.  There should only be one such call in a method.
      InstructionGraphNode instructionGraph = 
          InstructionGraphNode.buildInstructionGraph(mn);
      
      Set<MethodInsnNode> firstReachableConstructorCalls =
          instructionGraph.getFirstConstructorCalls(
              cn.name,
              cn.superName
              );
      
      if(firstReachableConstructorCalls.size() != 1)
      {
        throw new RuntimeException(
            "expected exactly one call to this.<init> or super.<init> "
            + "but found " + firstReachableConstructorCalls.size());
      }
      
      return firstReachableConstructorCalls.iterator().next().getNext();
    }
    
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
    
    throw new RuntimeException("no valid start instruction found");
  }
  
  private void wrapMethodInTryCatchBlock(
      ClassNode cn,
      MethodNode mn,
      FieldNode methodIdField,
      LocalVariables localVars,
      FieldNode filterField
      )
  {
    final AbstractInsnNode tryInsertionPoint = 
        getTryCatchWrapperInsertionPoint(cn,mn);
    
    //TODO: filter
    
    LabelNode tryBegin = new LabelNode();
    LabelNode tryEnd = new LabelNode();
    LabelNode handlerBegin = new LabelNode();
    
    LabelNode insertionPoint = new LabelNode();
    LabelNode skipTarget = new LabelNode();
    LabelNode dontSkipTarget = new LabelNode();
    
    mn.instructions.insertBefore(
        tryInsertionPoint,
        tryBegin
        );
    mn.instructions.add(tryEnd);
    mn.instructions.add(handlerBegin);
    mn.instructions.add(insertionPoint);
    
    TryCatchBlockNode tryCatch = 
        new TryCatchBlockNode(
            tryBegin,
            tryEnd,
            handlerBegin,
            Type.getInternalName(Throwable.class));
    
    mn.tryCatchBlocks.add(tryCatch);
    
    //@insertionPoint, just after the exception gets created
    //stack looks like [...][exception]
    
    //TODO: no way to currently filter uncaught exceptions in <clinit>
    //      (this may actually be okay since the clinit only ever gets called 
    //       once)
    
    //lies. filter uncaught exception calls
    {
      mn.instructions.insertBefore(
          insertionPoint, 
          new FieldInsnNode(
              Opcodes.GETSTATIC,
              cn.name,
              filterField.name,
              filterField.desc
              )
          );
      
      mn.instructions.insertBefore(
          insertionPoint, 
          new JumpInsnNode(
              Opcodes.IFNULL,
              dontSkipTarget
              )
          );//jump to dontSkipTarget if the filter is null
      
      mn.instructions.insertBefore(
          insertionPoint, 
          new FieldInsnNode(
              Opcodes.GETSTATIC,
              cn.name,
              filterField.name,
              filterField.desc
              )
          );
      
      mn.instructions.insertBefore(
          insertionPoint, 
          new FieldInsnNode(
              Opcodes.GETFIELD,
              Type.getInternalName(TrackingFilter.class),
              "filterTraceCalls",
              Type.BOOLEAN_TYPE.getDescriptor()
              )
          );
    
      mn.instructions.insertBefore(
          insertionPoint, 
          new JumpInsnNode(
              Opcodes.IFNE,
              skipTarget
              )
          );//jump to skipTarget if top of stack is true
    }
    
    mn.instructions.insertBefore(
        insertionPoint,
        dontSkipTarget
        );//SKIP HERE if the filter is not initialized
    
    mn.instructions.insertBefore(
        insertionPoint,
        new InsnNode(Opcodes.DUP)
        );
    
    //stack looks like 
    // [...][exception][exception]
    
    //invoke visitMethodExitPoint since the method is about to throw
    mn.instructions.insertBefore(
        insertionPoint,
        new FieldInsnNode(
            Opcodes.GETSTATIC,
            cn.name,
            methodIdField.name,
            methodIdField.desc
            )
        );
    
    //stack looks like 
    // [...][exception][exception][STRING method ID]
    
    mn.instructions.insertBefore(
        insertionPoint,
        new InsnNode(Opcodes.SWAP)
        );
    
    //stack looks like 
    // [...][exception][STRING method ID][exception]
    
    mn.instructions.insertBefore(
        insertionPoint,
        new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            Type.getInternalName(EventBroadcaster.class),
            "uncaught",
            Type.getMethodDescriptor(
                Type.VOID_TYPE,
                Type.getType(String.class),
                Type.getType(Throwable.class)),
            false));
    
    //stack looks like 
    // [...][exception]
    
    mn.instructions.insertBefore(
        insertionPoint, 
        skipTarget
        );//SKIP HERE if the filter says to ignore uncaught events
    
    mn.instructions.insertBefore(
        insertionPoint, 
        new InsnNode(Opcodes.ATHROW)
        );
  }
  
  private void addInitFilterToClinit(
      ClassNode cn,
      FieldNode filterField,
      FieldNode methodIdField
      ){
    MethodNode clinit = getOrCreateClinit(cn);
    
    InsnList injectThese = new InsnList();
    
    injectThese.add(
        new TypeInsnNode(
            Opcodes.NEW,
            Type.getInternalName(TrackingFilter.class)
            )
        );
    
    injectThese.add(
        new InsnNode(Opcodes.DUP)
        );
    
    injectThese.add(
        new FieldInsnNode(
            Opcodes.GETSTATIC,
            cn.name,
            methodIdField.name,
            methodIdField.desc
            )
        );
    
    injectThese.add(
        new MethodInsnNode(
            Opcodes.INVOKESPECIAL,
            Type.getType(TrackingFilter.class).getInternalName(),
            "<init>",
            Type.getMethodDescriptor(
                Type.VOID_TYPE,
                Type.getType(String.class)
                ),
            false
            )
        );
    
    injectThese.add(
        new FieldInsnNode(
            Opcodes.PUTSTATIC,
            cn.name,
            filterField.name,
            filterField.desc
            )
        );
    
    clinit.instructions.insert(injectThese);
  }
  
  private void initShouldFilterLocalVar(
      ClassNode cn,
      MethodNode mn,
      LabelNode insertionPoint,
      LocalVariables localVars,
      FieldNode filterField
      )
  {
    final int newLocalOffset = localVars.newOffset;
    localVars.shouldFilterOffset = newLocalOffset;
    localVars.newOffset = newLocalOffset + Type.INT_TYPE.getSize();
        
    updateLocalVariableOffsets(
        mn,
        newLocalOffset,
        Type.INT_TYPE.getSize()
        );
    
    mn.instructions.insertBefore(
        insertionPoint, 
        new InsnNode(Opcodes.ICONST_0)
        );
    
    mn.instructions.insertBefore(
        insertionPoint, 
        new VarInsnNode(
            Opcodes.ISTORE,
            newLocalOffset
            )
        );
    
    final LabelNode skipTarget = new LabelNode();//go here if we skip
    
    //If the filter is null, it hasn't yet been initialized.  This is a weird
    // corner case but it does happen.  Assume we shouldn't filter.
    {
      mn.instructions.insertBefore(
          insertionPoint, 
          new FieldInsnNode(
              Opcodes.GETSTATIC,
              cn.name,
              filterField.name,
              filterField.desc
              )
          );
      
      mn.instructions.insertBefore(
          insertionPoint, 
          new JumpInsnNode(
              Opcodes.IFNULL,
              skipTarget
              )
          );//if the filter is null, dont bother checking it
    }
    
    //Now we know the filter field isn't null so we check its value to determine
    // whether or not filtering should occur.  If it should occur, jump to the
    // skip target.
    {
      mn.instructions.insertBefore(
          insertionPoint, 
          new FieldInsnNode(
              Opcodes.GETSTATIC,
              cn.name,
              filterField.name,
              filterField.desc
              )
          );
      
      mn.instructions.insertBefore(
          insertionPoint, 
          new FieldInsnNode(
              Opcodes.GETFIELD,
              Type.getInternalName(TrackingFilter.class),
              "filterTraceCalls",
              Type.BOOLEAN_TYPE.getDescriptor()
              )
          );
      
      mn.instructions.insertBefore(
          insertionPoint, 
          new VarInsnNode(
              Opcodes.ISTORE,
              newLocalOffset
              )
          );
    }
    
    mn.instructions.insertBefore(
        insertionPoint, 
        skipTarget
        );
  }

  private static LabelNode addSkipIfFilteredInstructions(
      MethodNode mn,
      AbstractInsnNode insertionPoint,
      LocalVariables localVars
      )
  {
    final LabelNode skipTarget = new LabelNode();//go here if we skip
    
    mn.instructions.insertBefore(
        insertionPoint, 
        new VarInsnNode(
            Opcodes.ILOAD,
            localVars.shouldFilterOffset
            )
        );
    
    mn.instructions.insertBefore(
        insertionPoint, 
        new JumpInsnNode(
            Opcodes.IFNE,
            skipTarget
            )
        );//jump to skipTarget if top of stack is true
    
    return skipTarget;
  }

  /**
   * Returns the offset to the variable that stores the method start time
   * @param resourceUrl
   * @param cn
   * @param mn
   * @param insertionPoint
   * @param localVars
   */
  private void addVisitMethodEnterCall(
      ClassNode cn,
      MethodNode mn,
      FieldNode methodIdField,
      LabelNode insertionPoint,
      LocalVariables localVars
      )
  {
    final LabelNode skipTarget = 
        addSkipIfFilteredInstructions(
            mn,
            insertionPoint,
            localVars
            );

    mn.instructions.insertBefore(
        insertionPoint,
        new FieldInsnNode(
            Opcodes.GETSTATIC,
            cn.name,
            methodIdField.name,
            methodIdField.desc
            )
        );
    
    mn.instructions.insertBefore(
        insertionPoint,
        new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            Type.getInternalName(EventBroadcaster.class),
            "postEntry",
            Type.getMethodDescriptor(
                Type.VOID_TYPE,
                Type.getType(String.class)
                ),
            false
            )
        );
    
    mn.instructions.insertBefore(
        insertionPoint,
        skipTarget
        );
  }
  
  private void addVisitMethodExitInstructions(
      ClassNode cn,
      MethodNode mn,
      FieldNode methodIdField,
      LocalVariables localVars
      )
  {
    List<AbstractInsnNode> exitInstructions = 
        new ArrayList<>();
    
    for(AbstractInsnNode instruction:mn.instructions.toArray())
    {
      if(OpcodeHelper.isReturnOpcode(instruction.getOpcode()))
      {
        exitInstructions.add(instruction);
      }
    }
    
    for(AbstractInsnNode instruction:exitInstructions)
    {
      final LabelNode skipTarget = 
          addSkipIfFilteredInstructions(
              mn,
              instruction,
              localVars
              );
      
      mn.instructions.insertBefore(
          instruction,
          new FieldInsnNode(
              Opcodes.GETSTATIC,
              cn.name,
              methodIdField.name,
              methodIdField.desc
              )
          );

      mn.instructions.insertBefore(
          instruction,
          new MethodInsnNode(
              Opcodes.INVOKESTATIC,
              Type.getInternalName(EventBroadcaster.class),
              "preReturn",
              Type.getMethodDescriptor(
                  Type.VOID_TYPE,
                  Type.getType(String.class)// methodId
                  ),  
              false
              )
          );
      
      mn.instructions.insertBefore(
          instruction, 
          skipTarget
          );
    }
  }
  
  private void addVisitCatchBlockInstructions(
      ClassNode cn,
      MethodNode mn,
      FieldNode methodIdField,
      LocalVariables localVars
      )
  {
    List<TryCatchBlockNode> tryCatchBlocks = mn.tryCatchBlocks;
    if(tryCatchBlocks == null)
    {
      tryCatchBlocks = new ArrayList<>();
    }
    
    Set<LabelNode> encounteredLabels = new HashSet<>();
    
    for(TryCatchBlockNode tryCatchBlock:tryCatchBlocks)
    {
      if(encounteredLabels.contains(tryCatchBlock.handler))
      {
        break;
      }
      
      encounteredLabels.add(tryCatchBlock.handler);
      
      AbstractInsnNode catchBlockBegin = tryCatchBlock.handler.getNext();
      
      LabelNode insertionPoint = new LabelNode();
      
      mn.instructions.insertBefore(
          catchBlockBegin,
          insertionPoint);
      
      final LabelNode skipTarget = 
          addSkipIfFilteredInstructions(
              mn,
              insertionPoint,
              localVars
              );
      
      //stack looks like [...][exception ref]
      
      //copy the exception
      mn.instructions.insertBefore(
          insertionPoint, 
          new InsnNode(Opcodes.DUP)
          );
      
      //stack looks like [...][exception ref][exception ref]
      
      mn.instructions.insertBefore(
          insertionPoint,
          new FieldInsnNode(
              Opcodes.GETSTATIC,
              cn.name,
              methodIdField.name,
              methodIdField.desc
              )
          );
      
      //stack looks like 
      // [...][exception][exception][method UID]
      
      mn.instructions.insertBefore(
          insertionPoint,
          new InsnNode(Opcodes.DUP2_X1)
          );
      
      //stack looks like 
      // [...][exception][method UID][exception][method UID]
      
      mn.instructions.insertBefore(
          insertionPoint,
          new InsnNode(Opcodes.POP2)
          );
      
      //stack looks like 
      // [...][exception][method UID][exception]
      
      mn.instructions.insertBefore(
          insertionPoint,
          new MethodInsnNode(
              Opcodes.INVOKESTATIC,
              Type.getInternalName(EventBroadcaster.class),
              "postCatch",
              Type.getMethodDescriptor(
                  Type.VOID_TYPE,
                  Type.getType(String.class),
                  Type.getType(Throwable.class)
                  ),
              false
              )
          );
      
      //stack looks like [...][exception ref]
      
      mn.instructions.insertBefore(
          insertionPoint, 
          skipTarget
          );
    }
  }
  
  

  
  private static MethodNode getOrCreateClinit(ClassNode cn){
    
    if(cn.methods == null){
      return addClinit(cn);
    }
    
    for(MethodNode mn:cn.methods){
      if(mn.name.equals("<clinit>")){
        return mn;
      }
    }
    
    return addClinit(cn);
  }
  
  private static MethodNode addClinit(ClassNode cn){
    
    MethodNode mn = 
        new MethodNode(
            Opcodes.ACC_STATIC,
            "<clinit>", 
            Type.getMethodDescriptor(Type.VOID_TYPE), 
            null, 
            null
            );
    
    mn.instructions = new InsnList();
    
    mn.instructions.add(new InsnNode(Opcodes.RETURN));
    
    return mn;
  }
  
  
  
  
  
  
  private static class Helper{
    
    private static FieldNode getExisting(
        final ClassNode cn, 
        final String fieldName
        ){
      List<FieldNode> fields = cn.fields;
      
      if(fields == null){
        return null;
      }
      
      for(FieldNode f:fields){
        if(f.name.equals(fieldName)){
          return f;
        }
      }
      
      return null;
    }
    
    private static String getFieldNameForMethod(
        final String methodId,
        final String purpose
        ){
        final String name = Base64.getEncoder().encodeToString(methodId.getBytes());
        
      return purpose+"$$$"+sanitizeMethodId(name);
    }
    
    private static FieldNode acquireFieldForMethod(
        final ClassNode cn,
        final String methodId,
        final String purpose,
        final String desc,
        final int access,
        final Object initialValue
        ){
      final String fieldName = 
          getFieldNameForMethod(methodId,purpose);
      
      FieldNode existing = getExisting(cn,fieldName);
      
      if(existing != null){
        throw new RuntimeException(
            "class " + cn.name + 
            " already contains a field with name " + fieldName
            );
      }
      
      FieldNode f = 
          new FieldNode(
              access, 
              fieldName, 
              desc, 
              null, 
              initialValue
              );
      
      if(cn.fields == null){
        cn.fields = new ArrayList<>();
      }
      
      cn.fields.add(f);
      
      return f;
    }
    
    /**
     * 
     * @param methodId a method identifier to sanitize
     * @return a sanitized id (ie one that can be used as a field name)
     */
    private static String sanitizeMethodId(
        final String methodId
        ){
      return methodId.
          replace("+", "pl").
          replace("/", "fs").
          replace("=", "eq").
          replace("-", "_")
          ;
    }
  }
  
  private static void addControlFlowTracking(
          Map<AbstractInsnNode,Integer> originalInstructions,
          final String classId,
          ClassNode cn,
          final String methodId,
          MethodNode mn,
          LocalVariables localVars
          ){
        BasicBlock root = BasicBlock.decompose(mn);
        
        Set<BasicBlock> allBlocks = new HashSet<>();
        for(AbstractInsnNode i:mn.instructions.toArray()){
          if(originalInstructions.containsKey(i) && root.getBlock(i) != null){
            allBlocks.add(root.getBlock(i));
          }
        }
        
        Set<Integer> alreadyVisited = new HashSet<>();
        for(BasicBlock b:allBlocks){
          final int blockId = getRealBlockId(originalInstructions,b);
          
          if(alreadyVisited.contains(blockId)){
            continue;
          }
          alreadyVisited.add(blockId);
          
          //TODO
          final String blockHash = 
                  methodId + "/block/" + blockId 
                  //+ b.printBlock(mn)
                  ;
          
          final String fieldName = 
              Helper.getFieldNameForMethod(
                  methodId,
                  "_block"+blockId
                  );
          
          final FieldNode f = Helper.acquireFieldForMethod(
              cn,
              methodId,
              fieldName,
              Type.getDescriptor(String.class),
              Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
              blockHash
              );
          
          addControlFlowTrackingForBlock(f,b,cn,mn,localVars);
        }
      }
  
  private static void addControlFlowTrackingForBlock(
          FieldNode blockHash,
          BasicBlock b,
          ClassNode cn,
          MethodNode mn,
          LocalVariables localVars
          ){
        
        final AbstractInsnNode insertionPoint = 
            getFirstInstructionAfterBlockBegins(b,mn);
        LabelNode jumpHereIfFiltered = 
            addSkipIfFilteredInstructions(
                mn,
                insertionPoint,
                localVars
                );
        
        mn.instructions.insertBefore(
            insertionPoint,
            new FieldInsnNode(
                Opcodes.GETSTATIC,
                cn.name,
                blockHash.name,
                blockHash.desc
                )
            );
        
        mn.instructions.insertBefore(
            insertionPoint,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(EventBroadcaster.class),
                "postControlFlowPathTaken",
                Type.getMethodDescriptor(
                    Type.VOID_TYPE,
                    Type.getType(String.class)
                    ),
                false
                )
            );
        
        mn.instructions.insertBefore(
            insertionPoint, 
            jumpHereIfFiltered
            );
      }
  
  private static AbstractInsnNode getFirstInstructionAfterBlockBegins(
          BasicBlock b,
          MethodNode mn
          ){
        AbstractInsnNode first = b.getBlockInstructions().get(0);
        
        final AbstractInsnNode insertionPoint;
        
        if(first.getType() == AbstractInsnNode.LABEL){
          insertionPoint = 
              new InsnNode(Opcodes.NOP);
          mn.instructions.insert(
              first,
              insertionPoint);
        } else {
          insertionPoint = first;
        }
        
        return insertionPoint;
      }
  
  private static int getRealBlockId(
          Map<AbstractInsnNode,Integer> original,
          BasicBlock current
          ){
        
        Set<Integer> values = new HashSet<>();
        
        for(AbstractInsnNode i:current.getBlockInstructions()){
          if(original.containsKey(i)){
            values.add(original.get(i));
          }
        }
        
        if(values.size() != 1){
          throw new RuntimeException(
              "expected exactly one matching block but found " + values.size()
              );
        }
        
        return values.iterator().next();
      }
  
  private static Map<AbstractInsnNode,Integer> decompose(MethodNode mn){
      BasicBlock b = BasicBlock.decompose(mn);
      
      Map<AbstractInsnNode,Integer> map = new HashMap<>();
      
      for(AbstractInsnNode i:mn.instructions.toArray()){
        BasicBlock block = b.getBlock(i);
        
        if(block != null){
          map.put(i, block.getBlockId());
        }
      }
      
      return map;
    }
  
}

