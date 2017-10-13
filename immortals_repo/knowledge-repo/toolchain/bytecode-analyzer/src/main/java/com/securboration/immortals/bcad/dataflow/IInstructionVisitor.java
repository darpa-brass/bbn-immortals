package com.securboration.immortals.bcad.dataflow;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.securboration.immortals.bcas.block.BasicBlock;

/**
 * To be implemented by a visitor of a method
 * 
 * @author jstaples
 *
 * @param <C> an analysis context to be provided with each visit call
 */
public interface IInstructionVisitor<C> {
    
    /**
     * Applies a visitor to the provided method
     * 
     * @param mn
     *            the method whose instructions will be visited
     * @param bbg
     *            a basic block decomposition of the method's control flow
     * @param context
     *            the context for visiting instructions
     * @param visitor
     *            a visitor that operates on the provided context
     */
    public static <C> void visit(
            MethodNode mn,
            BasicBlock bbg,
            C context, 
            IInstructionVisitor<C> visitor
            ){
        visitor.pre(context);
        
        //follow the normal control flow paths
        bbg.traverse((instruction)->{
            
            visitor.visitAnyInstruction(instruction, context);
            
            final int instructionType = instruction.getType();
            
            if(instructionType == AbstractInsnNode.FIELD_INSN){
                visitor.visitFieldInsnNode((FieldInsnNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.IINC_INSN){
                visitor.visitIincInsnNode((IincInsnNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.INSN){
                visitor.visitInsnNode((InsnNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.INT_INSN){
                visitor.visitIntInsnNode((IntInsnNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.INVOKE_DYNAMIC_INSN){
                visitor.visitInvokeDynamicInsnNode((InvokeDynamicInsnNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.LDC_INSN){
                visitor.visitLdcNode((LdcInsnNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.LOOKUPSWITCH_INSN){
                visitor.visitLookupSwitchNode((LookupSwitchInsnNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.METHOD_INSN){
                visitor.visitMethodInsnNode((MethodInsnNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.MULTIANEWARRAY_INSN){
                visitor.visitMultiANewArrayNode((MultiANewArrayInsnNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.TABLESWITCH_INSN){
                visitor.visitTableSwitchNode((TableSwitchInsnNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.TYPE_INSN){
                visitor.visitTypeInsnNode((TypeInsnNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.VAR_INSN){
                visitor.visitVarInsnNode((VarInsnNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.JUMP_INSN){
                visitor.visitJumpInsnNode((JumpInsnNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.FRAME){
                visitor.visitFrameInsnNode((FrameNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.LABEL){
                visitor.visitLabelInsnNode((LabelNode)instruction,context);
            } else if(instructionType == AbstractInsnNode.LINE){
                visitor.visitLineInsnNode((LineNumberNode)instruction,context);
            } else {
                throw new RuntimeException("unhandled instruction type");
            }
            
            visitor.postAnyInstruction(instruction, context);
        });
        
        //TODO: follow exceptional entry points
        
        //now visit everything not reachable along a CFG, things like 
        //unreachable labels
        
        visitor.post(context);
    }
    
    /**
     * IINC: increment local var X by Y
     * READ value from local
     * READ increment from instruction
     * WRITE value to local
     * 
     * @param field
     * @param before
     * @param after
     */
    public default void visitIincInsnNode(
            IincInsnNode field,
            C analysisContext
            ){}
    
    /**
     * LDC
     * WRITE constant to stack
     * 
     * @param instruction
     * @param before
     * @param after
     */
    public default void visitLdcNode(
            LdcInsnNode instruction,
            C analysisContext
            ){}
    
    /**
     * LOOKUPSWITCH
     * TODO
     * 
     * @param instruction
     * @param before
     * @param after
     */
    public default void visitLookupSwitchNode(
            LookupSwitchInsnNode instruction,
            C analysisContext
            ){}
    
    /**
     * INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC or INVOKEINTERFACE:
     * read args (including object ref if non-static) from stack
     * write return value (if any) to stack
     * 
     * @param instruction
     * @param before
     * @param after
     */
    public default void visitMethodInsnNode(
            MethodInsnNode instruction,
            C analysisContext
            ){}
    
    /**
     * MULTIANEWARRAY
     * 
     * read n from instruction
     * read type from instruction
     * read n values from the stack (these are the dimensions of the array)
     * write the new array reference to the stack
     * 
     * @param instruction
     */
    public default void visitMultiANewArrayNode(
            MultiANewArrayInsnNode instruction,
            C analysisContext
            ){}
    
    /**
     * TABLESWITCH
     * TODO
     *
     * @param instruction
     * @param before
     * @param after
     */
    public default void visitTableSwitchNode(
            TableSwitchInsnNode instruction,
            C analysisContext
            ){}
    
    /**
     * NEW, ANEWARRAY, CHECKCAST or INSTANCEOF
     * 
     * NEW:
     * produces objectref
     * 
     * ANEWARRAY:
     * produces arrayref
     * 
     * CHECKCAST:
     * consumes objectref
     * produces objectref
     * 
     * INSTANCEOF:
     * consumes objectref
     * produces boolean
     *  
     * @param instruction
     * @param before
     * @param after
     */
    public default void visitTypeInsnNode(
            TypeInsnNode instruction,
            C analysisContext
            ){}
    
    /**
     * BIPUSH, SIPUSH or NEWARRAY
     * 
     * BIPUSH:
     * produce a byte as an int
     * 
     * SIPUSH:
     * produce a short as an int
     * 
     * NEWARRAY:
     * consume count (# of elements in array)
     * produce objectref
     * 
     * @param field
     * @param before
     * @param after
     */
    public default void visitIntInsnNode(
            IntInsnNode field,
            C analysisContext
            ){}
    
    /**
     * INVOKEDYNAMIC
     * consume args
     * produce return value, if any
     *  
     * @param field
     * @param before
     * @param after
     */
    public default void visitInvokeDynamicInsnNode(
            InvokeDynamicInsnNode field,
            C analysisContext
            ){}
    
    /**
     * NOP, ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3,
     * ICONST_4, ICONST_5, LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2,
     * DCONST_0, DCONST_1, IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD,
     * CALOAD, SALOAD, IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE,
     * CASTORE, SASTORE, POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2,
     * SWAP, IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL,
     * DMUL, IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG, FNEG,
     * DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR, LOR, IXOR,
     * LXOR, I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B,
     * I2C, I2S, LCMP, FCMPL, FCMPG, DCMPL, DCMPG, IRETURN, LRETURN, FRETURN,
     * DRETURN, ARETURN, RETURN, ARRAYLENGTH, ATHROW, MONITORENTER, or
     * MONITOREXIT.
     * 
     * TODO
     * 
     * @param field
     * @param before
     * @param after
     */
    public default void visitInsnNode(
            InsnNode field,
            C analysisContext
            ){}
    
    /**
     * GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD
     * 
     * @param field
     * @param before
     * @param after
     */
    public default void visitFieldInsnNode(
            FieldInsnNode field,
            C analysisContext
            ){}
    
    /**
     * ILOAD, LLOAD, FLOAD, DLOAD, ALOAD
     * consume value from local
     * produce value to stack
     * 
     * ISTORE, LSTORE, FSTORE, DSTORE, ASTORE
     * consume value from stack
     * produce value to local
     * 
     * RET
     * none
     * 
     * @param instruction
     * @param before
     * @param after
     */
    public default void visitVarInsnNode(
            VarInsnNode instruction,
            C analysisContext
            ){}
    
    /**
     * IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IFNULL, IFNONNULL
     * consume value from stack (control flow depends on this)
     * 
     * IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE,
     * consume top two values from stack (control flow depends on these)
     * 
     * GOTO, JSR
     * unhandled
     * 
     * @param instruction
     * @param before
     * @param after
     */
    public default void visitJumpInsnNode(
            JumpInsnNode instruction,
            C analysisContext
            ){}
    
    /**
     * 
     * @param frame
     * @param analysisContext
     */
    public default void visitFrameInsnNode(
            FrameNode frame,
            C analysisContext
            ){}

    /**
     * 
     * @param label
     * @param analysisContext
     */
    public default void visitLabelInsnNode(
            LabelNode label,
            C analysisContext
            ){}
    
    /**
     * 
     * @param line
     * @param analysisContext
     */
    public default void visitLineInsnNode(
            LineNumberNode line,
            C analysisContext
            ){}
    
    /**
     * Called before a more specific visitation call
     * 
     * @param instruction
     * @param analysisContext
     */
    public default void visitAnyInstruction(
            AbstractInsnNode instruction,
            C analysisContext
            ){}
    
    /**
     * Called before any visit call is invoked
     * 
     * @param analysisContext
     */
    public default void pre(
            C analysisContext
            ){}
    
    /**
     * Called after the last visit call is invoked
     * 
     * @param analysisContext
     */
    public default void post(
            C analysisContext
            ){}
    
    /**
     * Called after visiting any instruction
     * 
     * @param instruction
     */
    public default void postAnyInstruction(
            AbstractInsnNode instruction,
            C analysisContext
            ){}
    
    
}
