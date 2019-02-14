package com.securboration.immortals.bcad.dataflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
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
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import com.securboration.immortals.bca.tools.MethodPrinter;
import com.securboration.immortals.bcad.dataflow.DataflowHelper.LocalVariableSpec;
import com.securboration.immortals.bcad.dataflow.StackEmulator.Entry;
import com.securboration.immortals.bcad.dataflow.value.JavaValue;
import com.securboration.immortals.bcad.dataflow.value.JavaValueObject;
import com.securboration.immortals.bcad.dataflow.value.JavaValues;
import com.securboration.immortals.bcad.transformers.helpers.OpcodeHelper;
import com.securboration.immortals.bcas.block.BasicBlock;

public class BytecodeSimulator {
    
    private final ClassNode cn;
    private final MethodNode mn;
    
    private final BasicBlock bbg;
    
    private final StackSimulator stackSimulator;
    
    private final StackEmulator stackEmulator;
    
    private final LocalVars locals;
    
    private final JavaValues valueEmulation;
    
    private final List<Action> actions = new ArrayList<>();
    
    private static <C> void visit(
            List<AbstractInsnNode> sequence,
            C context, 
            IInstructionVisitor<C> visitor
            ){
        visitor.pre(context);
        
        for(AbstractInsnNode instruction:sequence){
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
        }
        
        visitor.post(context);
    }
    
    public BytecodeSimulator(
            ClassLoader cl,
            ClassNode cn,
            MethodNode mn,
            BasicBlock bbg,
            LocalVars locals,
            List<AbstractInsnNode> simulationSequence
            ) throws AnalyzerException{
        
        this.cn = cn;
        this.mn = mn;
        this.bbg = bbg;
        this.stackSimulator = new StackSimulator(cl,cn,mn);
        this.stackEmulator = new StackEmulator(this.bbg,true);
        this.locals = locals;
        
        this.valueEmulation = new JavaValues();
        
        AnalysisVisitor visitor = new AnalysisVisitor();
        
        visit(
            simulationSequence,
            this, 
            visitor
            );
    }
    
    final int[] alltypes = {
            AbstractInsnNode.FIELD_INSN,
            AbstractInsnNode.FRAME,
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
            AbstractInsnNode.VAR_INSN
    };
    
    public static class AnalysisVisitor implements IInstructionVisitor<BytecodeSimulator>{
        
        private void setAction(
                BytecodeSimulator context, 
                AbstractInsnNode i, 
                Action a
                ){
            context.actions.add(a);
        }
        
        private void debug(
                AbstractInsnNode instruction,
                BytecodeSimulator context,
                String format,
                Object...args
                ){
            
            if(true){
                return;//TODO
            }
            
            if(instruction != null){
                System.out.printf(
                    "b%-2d i%-4d: %s",
                    context.bbg.getBlock(instruction).getBlockId(),
                    context.mn.instructions.indexOf(instruction),
                    String.format(format, args)
                    );
            } else {
                System.out.printf(format,args);
            }
        }
        
        @Override
        public void visitAnyInstruction(
                AbstractInsnNode instruction,
                BytecodeSimulator context
                ){
            debug(
                instruction,context,
                "%s\n", 
                MethodPrinter.print(context.mn,instruction)
                );
        }
        
        @Override
        public void visitIincInsnNode(
                IincInsnNode instruction,
                BytecodeSimulator context
                ){
            //IINC: increment local var X by Y
            //READ value from local
            //READ increment from instruction
            //WRITE value to local
            
            debug(
                instruction,context,
                "increments local %d by %d\n", 
                instruction.var,
                instruction.incr
                );
            
            {//stack emulation
                //none required
            }//stack emulation
            
            {//local emulation
//                context.stackEmulator.
            }//local emulation
        }
        
        @Override
        public void visitLdcNode(
                LdcInsnNode instruction,
                BytecodeSimulator context
                ){
            //LDC
            //WRITE constant to stack
            
            final Type type = context.getStackTypeAfter(instruction, 0);
            
            debug(
                instruction,context,
                "pushes a constant (of type %s) onto the stack\n",
                type.getDescriptor()
                );
            
            {//stack emulation
                JavaValue newValue = context.valueEmulation.newValue(type);
                
                context.stackEmulator.produceAndPushOntoStack(
                    instruction, 
                    type,
                    newValue
                    );
            }
        }
        
        @Override
        public void visitLookupSwitchNode(
                LookupSwitchInsnNode instruction,
                BytecodeSimulator context
                ){
            //LOOKUPSWITCH
            
            debug(
                instruction,context,
                "pops a key off of the stack and does a SWITCH\n"
                );
            
            {//stack emulation
                //pop key off of stack
                Entry key = context.stackEmulator.popEntry(instruction);
                
                setAction(
                    context,
                    instruction,
                    new ActionControlFlow(
                        MethodPrinter.print(context.mn,instruction),
                        new JavaValue[]{key.getValue()},
                        instruction.labels.toArray(new AbstractInsnNode[]{})
                        )
                    );
            }//stack emulation
        }
        
        @Override
        public void visitMethodInsnNode(
                MethodInsnNode instruction,
                BytecodeSimulator context
                ){
            //INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC or INVOKEINTERFACE:
            //read args (including object ref if non-static) from stack
            //write return value (if any) to stack
            
            final int opcode = instruction.getOpcode();
            final boolean isStatic = OpcodeHelper.isOpcodeAnyOf(
                opcode, 
                Opcodes.INVOKESTATIC
                );
            
            final Type desc = Type.getMethodType(instruction.desc);
            
            final boolean isVoid = desc.getReturnType() == Type.VOID_TYPE;
            final int argCount = desc.getArgumentTypes().length;
            
            final int numPops = isStatic ? argCount : argCount + 1;
            
            debug(
                instruction,context,
                "pops off the top %d stack values (%d are args) %s\n", 
                numPops,
                argCount,
                isVoid ? "and pushes nothing" : "and pushes the returned value of type " + desc.getReturnType().getDescriptor() + " onto the stack"
                );
            
            {//stack emulation
                Entry objectRef = null;
                List<Entry> args = new ArrayList<>();
                
                //pop object ref + args off the stack
                for(int i=0;i<numPops;i++){
                    Entry e = context.stackEmulator.popEntry(instruction);
                    
                    args.add(e);
                }
                
                //the object ref, if any, will be the last thing popped
                if(!isStatic){
                    objectRef = args.remove(args.size()-1);
                }
                
                //now reverse the args since we got them from a stack
                args = Lists.reverse(args);
                
                Entry returnValue = null;
                if(!isVoid){
                    //TODO: this value should come from a recursive analysis of 
                    // the called method
                    JavaValue returnedValue = 
                            context.valueEmulation.newValue(desc.getReturnType());
                    
                    //push a value onto the stack
                    returnValue = context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        desc.getReturnType(),
                        returnedValue
                        );
                }
                
                {//action tracking
                    DataLocationStack[] argsC = 
                            new DataLocationStack[args.size()];
                    JavaValue[] argValues = 
                            new JavaValue[args.size()];
                    for(int i=0;i<args.size();i++){
                        argsC[i] = new DataLocationStack(args.get(i));
                        argValues[i] = args.get(i).getValue();
                    }
                    
                    //move a value from local to stack
                    setAction(
                        context,
                        instruction,
                        new ActionInvoke(
                            returnValue == null ? null : new DataLocationStack(returnValue),
                            objectRef == null ? null : new DataLocationStack(objectRef),
                            objectRef == null ? null : objectRef.getValue(),
                            argsC,
                            argValues
                            )
                        );
                }//action tracking
                
            }//stack emulation
        }
        
        @Override
        public void visitMultiANewArrayNode(
                MultiANewArrayInsnNode instruction,
                BytecodeSimulator context
                ){
            //MULTIANEWARRAY
            
            //read n from instruction
            //read type from instruction
            //read n values from the stack (these are the dimensions of the array)
            //write the new array reference to the stack
            
            debug(
                instruction,context,
                "pops off the top %d stack values (which are array dims), and pushes a reference to a new array with those dims onto the stack\n", 
                instruction.dims
                );
            
            {//stack emulation
                final Type type = context.getStackTypeAfter(instruction, 0);
                
                //pop object ref + args off the stack
                for(int i=0;i<instruction.dims;i++){
                    context.stackEmulator.popEntry(instruction);
                }
                
                //push array ref onto stack
                context.stackEmulator.produceAndPushOntoStack(
                    instruction, 
                    type,
                    context.valueEmulation.newValue(type)
                    );
                
            }//stack emulation
        }
        
        @Override
        public void visitTableSwitchNode(
                TableSwitchInsnNode instruction,
                BytecodeSimulator context
                ){
            //TABLESWITCH
            
            debug(
                instruction,context,
                "pops an address off of the stack and does a SWITCH\n"
                );
            
            {//stack emulation
                //pop off index
                Entry index = context.stackEmulator.popEntry(instruction);
                
                setAction(
                    context,
                    instruction,
                    new ActionControlFlow(
                        MethodPrinter.print(context.mn,instruction),
                        new JavaValue[]{index.getValue()},
                        instruction.labels.toArray(new AbstractInsnNode[]{})
                        )
                    );
            }//stack emulation
        }
        
        @Override
        public void visitTypeInsnNode(
                TypeInsnNode instruction,
                BytecodeSimulator context
                ){
            //NEW, ANEWARRAY, CHECKCAST or INSTANCEOF
            
            final int opcode = instruction.getOpcode();
            
            if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.NEW
                )){
                //NEW:
                //produces objectref
                
                debug(
                    instruction,context,
                    "pushes a new object ref onto the stack\n"
                    );
                
                {//stack emulation
                    final Type type = context.getStackTypeAfter(instruction, 0);
                    
                    //push new object
                    context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        type,
                        context.valueEmulation.newValue(type)
                        );
                }//stack emulation
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.ANEWARRAY
                )){
                //ANEWARRAY:
                //produces arrayref
                
                debug(
                    instruction,context,
                    "pops a count off of the stack and pushes a new array of that length onto the stack\n"
                    );
                
                {//stack emulation
                    final Type type = context.getStackTypeAfter(instruction, 0);
                    
                    //consume count
                    final Entry size = context.stackEmulator.popEntry(instruction);
                    
                    //push new array
                    final Entry newArray = context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        type,
                        context.valueEmulation.newValue(type)
                        );
                }//stack emulation
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.CHECKCAST
                )){
                //CHECKCAST:
                //consumes objectref
                //produces objectref
                
                debug(
                    instruction,context,
                    "pops an objectref off of the stack and pushes an objectref back onto the stack\n", 
                    context.indexOf(instruction)
                    );
                
                {//stack emulation
                    //TODO: this is tricky because the stack entry is consumed 
                    //in-place
                    
                    //pop off the entry
                    Entry e = context.stackEmulator.popEntry(instruction);
                    
                    //clone it with new type
                    Entry n = e.cloneWithNewType(Type.getObjectType(instruction.desc));
                    
                    //push the cloned value, now with the proper type
                    context.stackEmulator.pushEntry(instruction,n);
                }//stack emulation
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.INSTANCEOF
                )){
                //INSTANCEOF:
                //consumes objectref
                //produces boolean
                
                debug(
                    instruction,context,
                    "pops an objectref off of the stack and pushes a boolean onto the stack\n"
                    );
                
                {//stack emulation
                    final Type type = context.getStackTypeAfter(instruction, 0);
                    
                    //consume objectref
                    context.stackEmulator.popEntry(instruction);
                    
                    //push boolean
                    context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        type,
                        context.valueEmulation.newValue(type)
                        );
                }//stack emulation
            } else {
                throw new RuntimeException(
                    "unhandled instruction: " + context.print(instruction)
                    );
            }
            
        }
        
        @Override
        public void visitIntInsnNode(
                IntInsnNode instruction,
                BytecodeSimulator context
                ){
            //BIPUSH, SIPUSH or NEWARRAY
            
            final int opcode = instruction.getOpcode();
            
            if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.BIPUSH
                )){
                //BIPUSH:
                //produce a byte as an int
                
                debug(
                    instruction,context,
                    "pushes a byte constant onto the stack\n"
                    );
                
                {//stack emulation
                    final Type type = context.getStackTypeAfter(instruction, 0);
                    
                    //push a value onto the stack
                    context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        type,
                        context.valueEmulation.newValue(type)
                        );
                }//stack emulation
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.SIPUSH
                )){
                //SIPUSH:
                //produce a short as an int
                
                debug(
                    instruction,context,
                    "pushes a short constant onto the stack\n"
                    );
                
                {//stack emulation
                    final Type type = context.getStackTypeAfter(instruction, 0);
                    
                    //push a value onto the stack
                    context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        type,
                        context.valueEmulation.newValue(type)
                        );
                }//stack emulation
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.NEWARRAY
                )){
                //NEWARRAY:
                //consume count (# of elements in array)
                //produce objectref
                
                debug(
                    instruction,context,
                    "pops a count off of the stack and pushes a new array ref onto the stack\n"
                    );
                
                {//stack emulation
                    final Type type = context.getStackTypeAfter(instruction, 0);
                    
                    //pop off the count
                    context.stackEmulator.popEntry(instruction);
                    
                    //push a value onto the stack
                    context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        type,
                        context.valueEmulation.newValue(type)
                        );
                }//stack emulation
            } else {
                throw new RuntimeException(
                    "unhandled instruction: " + context.print(instruction)
                    );
            }
        }
        
        @Override
        public void visitInvokeDynamicInsnNode(
                InvokeDynamicInsnNode instruction,
                BytecodeSimulator context
                ){
            //INVOKEDYNAMIC
            
            //consume args
            //produce return value, if any
            
            final Type desc = Type.getMethodType(instruction.desc);
            
            final boolean isVoid = desc.getReturnType() == Type.VOID_TYPE;
            final int argCount = desc.getArgumentTypes().length;
            
            debug(
                instruction,context,
                "pops off the top %d stack values %s\n", 
                argCount,
                isVoid ? "and pushes nothing" : "and pushes the returned value of type " + desc.getReturnType().getDescriptor() + " onto the stack"
                );
            
            {//stack emulation
                //pop object ref + args off the stack
                for(int i=0;i<argCount;i++){
                    context.stackEmulator.popEntry(instruction);
                }
                
                if(!isVoid){
                    //TODO: this value should come from recursive analysis
                    // but that's hard because invokedynamic binding is delayed
                    // until runtime
                    
                    //push return value onto the stack
                    context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        desc.getReturnType(),
                        context.valueEmulation.newValue(desc.getReturnType())
                        );
                }
                
            }//stack emulation
        }
        
        @Override
        public void visitInsnNode(
                InsnNode instruction,
                BytecodeSimulator context
                ){
           /*           NOP, ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1,
            *            ICONST_2, ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1,
            *            FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1, IALOAD,
            *            LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD,
            *            IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE,
            *            SASTORE, POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1,
            *            DUP2_X2, SWAP, IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB,
            *            IMUL, LMUL, FMUL, DMUL, IDIV, LDIV, FDIV, DDIV, IREM, LREM,
            *            FREM, DREM, INEG, LNEG, FNEG, DNEG, ISHL, LSHL, ISHR, LSHR,
            *            IUSHR, LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR, I2L, I2F, I2D,
            *            L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B, I2C, I2S,
            *            LCMP, FCMPL, FCMPG, DCMPL, DCMPG, IRETURN, LRETURN, FRETURN,
            *            DRETURN, ARETURN, RETURN, ARRAYLENGTH, ATHROW, MONITORENTER,
            *            or MONITOREXIT.
            */
            
            final int opcode = instruction.getOpcode();
            
            if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.NOP
                )){
                visitUninterestingInstruction(instruction,context);
                
                {//stack emulation
                    //none required
                }//stack emulation
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.ACONST_NULL,
                Opcodes.ICONST_M1,Opcodes.ICONST_0,Opcodes.ICONST_1,Opcodes.ICONST_2,Opcodes.ICONST_3,Opcodes.ICONST_4,Opcodes.ICONST_5,
                Opcodes.LCONST_0,Opcodes.LCONST_1,
                Opcodes.FCONST_0,Opcodes.FCONST_1,Opcodes.FCONST_2,
                Opcodes.DCONST_0,Opcodes.DCONST_1
                )){
                
                final Type value = context.getStackTypeAfter(instruction, 0);
                
                debug(
                    instruction,context,
                    "pushes a const (%s) onto the stack\n",
                    value.getDescriptor()
                    );
                
                {//stack emulation
                    //push a value onto the stack
                    context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        value,
                        context.valueEmulation.newValue(value)
                        );
                }//stack emulation
                
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.IALOAD,Opcodes.LALOAD,Opcodes.FALOAD,Opcodes.DALOAD,Opcodes.AALOAD,Opcodes.BALOAD,Opcodes.CALOAD,Opcodes.SALOAD,
                Opcodes.IASTORE,Opcodes.LASTORE,Opcodes.FASTORE,Opcodes.DASTORE,Opcodes.AASTORE,Opcodes.BASTORE,Opcodes.CASTORE,Opcodes.SASTORE)){
                
                final boolean isLoad = OpcodeHelper.isOpcodeAnyOf(
                    opcode, 
                    Opcodes.IALOAD,Opcodes.LALOAD,Opcodes.FALOAD,Opcodes.DALOAD,
                    Opcodes.AALOAD,Opcodes.BALOAD,Opcodes.CALOAD,Opcodes.SALOAD
                    );
                
                if(isLoad){
                    final Type arrayRef = context.getStackTypeBefore(instruction, -1);
                    final Type index = context.getStackTypeBefore(instruction, 0);
                    final Type value = context.getStackTypeAfter(instruction, 0);
                    
                    debug(
                        instruction,context,
                        "pops an array ref (%s) and an index (%s) and pushes the array value (%s) onto the stack\n",
                        arrayRef.getDescriptor(),
                        index.getDescriptor(),
                        value.getDescriptor()
                        );
                    
                    {//stack emulation
                        //pop array ref and index
                        context.stackEmulator.popEntry(instruction);
                        context.stackEmulator.popEntry(instruction);
                        
                        //push a value onto the stack
                        context.stackEmulator.produceAndPushOntoStack(
                            instruction, 
                            value,
                            context.valueEmulation.newValue(value)
                            );
                    }//stack emulation
                } else {
                    final Type arrayRef = context.getStackTypeBefore(instruction, -2);
                    final Type index = context.getStackTypeBefore(instruction, -1);
                    final Type value = context.getStackTypeBefore(instruction,0);
                    
                    debug(
                        instruction,context,
                        "pops an array ref (%s), an index (%s), and a value (%s) from the stack and inserts the value into the array\n",
                        arrayRef.getDescriptor(),
                        index.getDescriptor(),
                        value.getDescriptor()
                        );
                    
                    {//stack emulation
                        //pop array ref, index and value
                        context.stackEmulator.popEntry(instruction);
                        context.stackEmulator.popEntry(instruction);
                        context.stackEmulator.popEntry(instruction);
                    }//stack emulation
                }
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.POP,Opcodes.POP2,
                Opcodes.DUP,Opcodes.DUP_X1,Opcodes.DUP_X2,
                Opcodes.DUP2,Opcodes.DUP2_X1,Opcodes.DUP2_X2,
                Opcodes.SWAP)){
                
                Frame<BasicValue> frame = context.getFrameBefore(instruction);
                
                if(opcode == Opcodes.POP){
                    //pops 1 value
                    debug(
                        instruction,context,
                        "pops %d values from the stack\n",
                        frame.getStack(frame.getStackSize()-1).getSize() == 1 ? 1 : 2
                        );
                    
                    {//stack emulation
                        context.stackEmulator.popEntry(instruction);
                    }//stack emulation
                } else if(opcode == Opcodes.POP2){
                    //pops 1 value if long or double
                    //pops 2 values if not long or double
                    final int numPops = frame.getStack(
                        frame.getStackSize()-1
                        ).getSize() == 1 ? 2 : 1;
                    debug(
                        instruction,context,
                        "pops %d values from the stack for type %s\n",
                        numPops,
                        frame.getStack(frame.getStackSize()-1).getType()
                        );
                    
                    {//stack emulation
                        for(int i=0;i<numPops;i++){
                            context.stackEmulator.popEntry(instruction);
                        }
                    }//stack emulation
                } else if(opcode == Opcodes.DUP){
                    debug(
                        instruction,context,
                        "copies the top of stack value\n"
                        );
                    
                    {//stack emulation
                        Entry e = context.stackEmulator.popEntry(instruction);
                        
                        context.stackEmulator.pushEntry(instruction,e);
                        context.stackEmulator.pushEntry(instruction,e);
                    }//stack emulation
                } else if(opcode == Opcodes.DUP_X1){
                    debug(
                        instruction,context,
                        "copies the top of stack value A: {B, A} -> {A, B, A}\n"
                        );
                    
                    {//stack emulation
                        Entry a = context.stackEmulator.popEntry(instruction);
                        Entry b = context.stackEmulator.popEntry(instruction);
                        
                        context.stackEmulator.pushEntry(instruction,a);
                        context.stackEmulator.pushEntry(instruction,b);
                        context.stackEmulator.pushEntry(instruction,a);
                    }
                } else if(opcode == Opcodes.DUP_X2){
                    debug(
                        instruction,context,
                        "copies the top of stack value A: {C, B, A} -> {A, C, B, A}\n"
                        );
                    
                    {//stack emulation
                        Entry a = context.stackEmulator.popEntry(instruction);
                        Entry b = context.stackEmulator.popEntry(instruction);
                        Entry c = context.stackEmulator.popEntry(instruction);
                        
                        context.stackEmulator.pushEntry(instruction,a);
                        context.stackEmulator.pushEntry(instruction,c);
                        context.stackEmulator.pushEntry(instruction,b);
                        context.stackEmulator.pushEntry(instruction,a);
                    }
                } else if(opcode == Opcodes.DUP2){
                    debug(
                        instruction,context,
                        "copies the top two stack words: {B,A} -> {B, A, B, A}\n"
                        );
                    
                    {//stack emulation
                        Entry top = context.stackEmulator.popEntry(instruction);
                        
                        if(top.getType().getSize() == 2){
                            //single, double-word dup
                            context.stackEmulator.pushEntry(instruction,top);
                            context.stackEmulator.pushEntry(instruction,top);
                        } else {
                            //2-value, single-word dup
                            Entry next = context.stackEmulator.popEntry(instruction);
                            
                            context.stackEmulator.pushEntry(instruction,next);
                            context.stackEmulator.pushEntry(instruction,top);
                            context.stackEmulator.pushEntry(instruction,next);
                            context.stackEmulator.pushEntry(instruction,top);
                        }
                    }
                } else if(opcode == Opcodes.DUP2_X1){
                    debug(
                        instruction,context,
                        "copies the top two stack words: {B,A} -> {B,A}, {B,A}\n"
                        );
                    
                    {//stack emulation
                        Entry top = context.stackEmulator.popEntry(instruction);
                        
                        if(top.getType().getSize() == 2){
                            // value2, value1 -> value1, value2, value1
                            // where value1 is a value of a category 2 
                            // computational type and value2 is a value of a 
                            // category 1 computational type
                            Entry value1 = top;
                            Entry value2 = context.stackEmulator.popEntry(instruction);
                            
                            context.stackEmulator.pushEntry(instruction,value1);
                            context.stackEmulator.pushEntry(instruction,value2);
                            context.stackEmulator.pushEntry(instruction,value1);
                        } else {
                            // value3, value2, value1 
                            //   -> 
                            // value2, value1, value3, value2, value1
                            // where value1, value2, and value3 are all values 
                            // of a category 1 computational type 
                            Entry value1 = top;
                            Entry value2 = context.stackEmulator.popEntry(instruction);
                            Entry value3 = context.stackEmulator.popEntry(instruction);
                            
                            context.stackEmulator.pushEntry(instruction,value2);
                            context.stackEmulator.pushEntry(instruction,value1);
                            context.stackEmulator.pushEntry(instruction,value3);
                            context.stackEmulator.pushEntry(instruction,value2);
                            context.stackEmulator.pushEntry(instruction,value1);
                        }
                    }
                } else if(opcode == Opcodes.DUP2_X2){
                    debug(
                        instruction,context,
                        "copies the top two stack words: C,{B,A} -> {B, A},C,{B, A}\n"
                        );
                    
                    {//stack emulation
                        /*
                         Form 1:
                        ..., value4, value3, value2, value1 ->
                        ..., value2, value1, value4, value3, value2, value1
                        where value1, value2, value3, and value4 are all values 
                        of a category 1 computational type.
                        
                        Form 2:
                        ..., value3, value2, value1 ->
                        ..., value1, value3, value2, value1
                        where value1 is a value of a category 2 computational 
                        type and value2 and value3 are both values of a 
                        category 1 computational type.
                        
                        Form 3:
                        ..., value3, value2, value1 ->
                        ..., value2, value1, value3, value2, value1
                        where value1 and value2 are both values of a category 1 
                        computational type and value3 is a value of a category 
                        2 computational type.
                        
                        Form 4:
                        ..., value2, value1 ->
                        ..., value1, value2, value1                        
                        where value1 and value2 are both values of a category 
                        2 computational type. 
                         */
                        
                        Entry value1 = context.stackEmulator.popEntry(instruction);
                        Entry value2 = context.stackEmulator.popEntry(instruction);
                        
                        final int v1s = value1.getType().getSize();
                        final int v2s = value2.getType().getSize();
                        
                        if(v1s == 2 && v2s == 2){
                            //form 4
                            context.stackEmulator.pushEntry(instruction,value1);
                            context.stackEmulator.pushEntry(instruction,value2);
                            context.stackEmulator.pushEntry(instruction,value1);
                        } else {
                            Entry value3 = context.stackEmulator.popEntry(instruction);
                            final int v3s = value3.getType().getSize();
                            
                            if(v1s == 1 && v2s == 1 && v3s == 2){
                                //form 3
                                context.stackEmulator.pushEntry(instruction,value2);
                                context.stackEmulator.pushEntry(instruction,value1);
                                context.stackEmulator.pushEntry(instruction,value3);
                                context.stackEmulator.pushEntry(instruction,value2);
                                context.stackEmulator.pushEntry(instruction,value1);
                            } else if(v1s == 2 && v2s == 1 && v3s == 1){
                                //form 2
                                context.stackEmulator.pushEntry(instruction,value1);
                                context.stackEmulator.pushEntry(instruction,value3);
                                context.stackEmulator.pushEntry(instruction,value2);
                                context.stackEmulator.pushEntry(instruction,value1);
                            } else if(v1s == 1 && v2s == 1 && v3s == 1){
                                //form 1
                                Entry value4 = context.stackEmulator.popEntry(instruction);
                                
                                context.stackEmulator.pushEntry(instruction,value2);
                                context.stackEmulator.pushEntry(instruction,value1);
                                context.stackEmulator.pushEntry(instruction,value4);
                                context.stackEmulator.pushEntry(instruction,value3);
                                context.stackEmulator.pushEntry(instruction,value2);
                                context.stackEmulator.pushEntry(instruction,value1);
                            } else {
                                throw new RuntimeException("unhandled case");
                            }
                        }
                    }//stack emulation
                } else if(opcode == Opcodes.SWAP){
                    debug(
                        instruction,context,
                        "swaps the top two stack words: B,A -> A,B\n"
                        );
                    
                    {//stack emulation
                        Entry a = context.stackEmulator.popEntry(instruction);
                        Entry b = context.stackEmulator.popEntry(instruction);
                        
                        context.stackEmulator.pushEntry(instruction,a);
                        context.stackEmulator.pushEntry(instruction,b);
                    }
                } else {
                    throw new RuntimeException("unhandled case: " + context.print(instruction));
                }
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.IADD,Opcodes.LADD,Opcodes.FADD,Opcodes.DADD,
                Opcodes.ISUB,Opcodes.LSUB,Opcodes.FSUB,Opcodes.DSUB,
                Opcodes.IMUL,Opcodes.LMUL,Opcodes.FMUL,Opcodes.DMUL,
                Opcodes.IDIV,Opcodes.LDIV,Opcodes.FDIV,Opcodes.DDIV,
                Opcodes.IREM,Opcodes.LREM,Opcodes.FREM,Opcodes.DREM,
                Opcodes.ISHL,Opcodes.LSHL,Opcodes.ISHR,Opcodes.LSHR,
                Opcodes.IUSHR,Opcodes.LUSHR,
                Opcodes.IAND,Opcodes.LAND,
                Opcodes.IOR,Opcodes.LOR,
                Opcodes.IXOR,Opcodes.LXOR,
                Opcodes.LCMP,Opcodes.FCMPL,Opcodes.FCMPG,Opcodes.DCMPL,Opcodes.DCMPG
                )){
                final Type arg1 = context.getStackTypeBefore(instruction, -1);
                final Type arg2 = context.getStackTypeBefore(instruction, 0);
                
                debug(
                    instruction,context,
                    "pops off two values (%s and %s), does arithmetic, and pushes a result\n",
                    arg1.getDescriptor(),
                    arg2.getDescriptor()
                    );
                
                {//stack emulation
                    final Type type = context.getStackTypeAfter(instruction, 0);
                    
                    //pop two values
                    context.stackEmulator.popEntry(instruction);
                    context.stackEmulator.popEntry(instruction);
                    
                    //push result
                    context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        type,
                        context.valueEmulation.newValue(type)
                        );
                }//stack emulation
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.INEG,Opcodes.LNEG,Opcodes.FNEG,Opcodes.DNEG,
                Opcodes.L2I,Opcodes.L2F,Opcodes.L2D,
                Opcodes.F2I,Opcodes.F2L,Opcodes.F2D,
                Opcodes.D2I,Opcodes.D2L,Opcodes.D2F,
                Opcodes.I2B,Opcodes.I2C,Opcodes.I2D,Opcodes.I2F,Opcodes.I2L,Opcodes.I2S
                )){
                final Type type = context.getStackTypeAfter(instruction, 0);
                
                debug(
                    instruction,context,
                    "pops off one value (a %s), does arithmetic, and pushes a result\n",
                    type.getDescriptor()
                    );
                
                {//stack emulation
                    //pop one value
                    context.stackEmulator.popEntry(instruction);
                    
                    //push result
                    context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        type,
                        context.valueEmulation.newValue(type)
                        );
                }//stack emulation
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.IRETURN,Opcodes.LRETURN,Opcodes.FRETURN,Opcodes.DRETURN,Opcodes.ARETURN
                )){
                final Type type = context.getStackTypeBefore(instruction, 0);
                
                debug(
                    instruction,context,
                    "pops off the return value (a %s) from the stack\n",
                    type.getDescriptor()
                    );
                
                {//stack emulation
                    //pop return value
                    context.stackEmulator.popEntry(instruction);
                }//stack emulation
            } else if(opcode ==  Opcodes.RETURN){
                debug(
                    instruction,context,
                    "returns\n"
                    );
                
                {//stack emulation
                    //nothing to emulate
                }//stack emulation
            } else if(opcode == Opcodes.ATHROW){
                final Type type = context.getStackTypeBefore(instruction, 0);
                
                debug(
                    instruction,context,
                    "pops off the top of stack value (a %s), which is an exception to throw\n",
                    type.getDescriptor()
                    );
                
                {//stack emulation
                    //pop thrown exception
                    context.stackEmulator.popEntry(instruction);
                }//stack emulation
            } else if(opcode == Opcodes.ARRAYLENGTH){
                final Type type = context.getStackTypeBefore(instruction, 0);
                
                debug(
                    instruction,context,
                    "pops off the top of stack value (a %s), which is an array reference, and pushes the length of the array back onto the stack\n",
                    type.getDescriptor()
                    );
                
                {//stack emulation
                    final Type afterType = context.getStackTypeAfter(instruction, 0);
                    
                    //pop top value
                    context.stackEmulator.popEntry(instruction);
                    
                    //push length
                    context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        afterType,
                        context.valueEmulation.newValue(afterType)
                        );
                }//stack emulation
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.MONITORENTER,Opcodes.MONITOREXIT
                )){
                final Type type = context.getStackTypeBefore(instruction, 0);
                
                debug(
                    instruction,context,
                    "pops off the top of stack value (a %s), which is an object for which a monitor will be acquired\n",
                    type.getDescriptor()
                    );
                
                {//stack emulation
                    //pop top value
                    context.stackEmulator.popEntry(instruction);
                }//stack emulation
            } else {
                throw new RuntimeException("not yet implemented: " + context.print(instruction));
            }
        }
        
        @Override
        public void visitFieldInsnNode(
                FieldInsnNode instruction,
                BytecodeSimulator context
                ){
            //GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD
            
            final boolean consumesObjectRef;
            final boolean isRead;
            
            if(instruction.getOpcode() == Opcodes.GETSTATIC){
                //consume field value from field
                //produce field value in stack
                isRead = true;
                consumesObjectRef = false;
            } else if(instruction.getOpcode() == Opcodes.PUTSTATIC){
                //consume value from stack
                //produce value in field
                isRead = false;
                consumesObjectRef = false;
            } else if(instruction.getOpcode() == Opcodes.GETFIELD){
                //consume value from stack
                //produce value in field
                isRead = true;
                consumesObjectRef = true;
            } else if(instruction.getOpcode() == Opcodes.PUTFIELD){
                //consume value from stack
                //produce value in field
                isRead = false;
                consumesObjectRef = true;
            } else {
                throw new RuntimeException("unhandled case");
            }
            
            final Type fieldType = 
                    isRead ? context.getStackTypeAfter(instruction, 0) : context.getStackTypeBefore(instruction, 0);
            final Type objectRef = consumesObjectRef ? context.getStackTypeBefore(instruction, isRead ? 0 : -1) : null;
            
            debug(
                instruction,context,
                "is %s field %s\n", 
                consumesObjectRef ? "an instance" : "a static",
                isRead ? "read" : "write"
                );
            debug(instruction,context,
                "\tobjectRef = %s\n\tvarType=%s\n",
                objectRef == null ? null : objectRef.getDescriptor(),
                fieldType.getDescriptor()
                );
            
            {//stack emulation
                Entry referenceConsumed = null;
                
                Entry pushedValue = null;
                Entry poppedValue = null;
                
                if(isRead){
                    //handle read case
                    //[objectRef ?] -> [value]
                    
                    if(consumesObjectRef){
                        //pop off the object ref
                        referenceConsumed = context.stackEmulator.popEntry(instruction);
                    }
                    
                    final Type type = context.getStackTypeAfter(instruction, 0);
                    
                    JavaValue pushedValueValue = null;
                    if(consumesObjectRef){
                        pushedValueValue = context.valueEmulation.getField(
                            (JavaValueObject)referenceConsumed.getValue(), 
                            instruction.name,
                            Type.getType(instruction.desc)
                            );
                    } else {
                        pushedValueValue = context.valueEmulation.getStatic(
                            instruction.owner,
                            instruction.name,
                            Type.getType(instruction.desc)
                            );
                    }
                    
                    //push the value read from the field
                    pushedValue = context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        type,
                        pushedValueValue
                        );
                } else {
                    //handle write case
                    //[objectRef ?],[value] ->
                    
                    //pop off the value to write to the field
                    poppedValue = context.stackEmulator.popEntry(instruction);
                    
                    if(consumesObjectRef){
                        //pop off the object ref
                        referenceConsumed = context.stackEmulator.popEntry(instruction);
                    }
                    
                    if(consumesObjectRef){
                        context.valueEmulation.setField(
                            (JavaValueObject)referenceConsumed.getValue(), 
                            instruction.name, 
                            poppedValue.getValue()
                            );
                    } else {
                        context.valueEmulation.putStatic(
                            instruction.owner, 
                            instruction.name, 
                            poppedValue.getValue()
                            );
                    }
                }
                
                
                {//action emulation
                    if(isRead){
                        //move a value from field to stack
                        setAction(
                            context,
                            instruction,
                            new ActionMove(
                                pushedValue.getValue(),
                                new DataLocationField(instruction.owner,instruction.name,referenceConsumed == null ? null:(JavaValueObject)referenceConsumed.getValue()),
                                new DataLocationStack(pushedValue)
                                )
                            );
                    } else {
                        //move a value from stack to field
                        setAction(
                            context,
                            instruction,
                            new ActionMove(
                                poppedValue.getValue(),
                                new DataLocationStack(poppedValue),
                                new DataLocationField(instruction.owner,instruction.name,referenceConsumed == null ? null:(JavaValueObject)referenceConsumed.getValue())
                                )
                            );
                    }
                    
                    //TODO
                }
            }
        }
        
        @Override
        public void visitVarInsnNode(
                VarInsnNode instruction,
                BytecodeSimulator context
                ){
            final int opcode = instruction.getOpcode();
            final int var = instruction.var;
            
            if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.ILOAD, 
                Opcodes.LLOAD, 
                Opcodes.FLOAD, 
                Opcodes.DLOAD, 
                Opcodes.ALOAD
                )){
                //ILOAD, LLOAD, FLOAD, DLOAD, ALOAD
                //consume value from local
                //produce value to stack
                
                final Type varType = context.getLocalTypeBefore(instruction, var);
                
                debug(
                    instruction,context,
                    "reads local %d (a %s) and places it on the stack\n",
                    instruction.var,
                    varType.getDescriptor()
                    );
                
                {//stack emulation
                    LocalVariableSpec local = 
                            context.locals.getLocal(instruction);
                    
                    JavaValue value = 
                            context.stackEmulator.getLocal(instruction.var);
                    
                    //push a value onto the stack
                    Entry e = context.stackEmulator.produceAndPushOntoStack(
                        instruction, 
                        varType,
                        value
                        );
                    
                    {//action tracking
                        //move a value from local to stack
                        setAction(
                            context,
                            instruction,
                            new ActionMove(
                                e.getValue(),
                                new DataLocationLocal(local),
                                new DataLocationStack(e)
                                )
                            );
                        //TODO
                    }//action tracking
                }//stack emulation
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.ISTORE, 
                Opcodes.LSTORE, 
                Opcodes.FSTORE, 
                Opcodes.DSTORE, 
                Opcodes.ASTORE
                )){
                //ISTORE, LSTORE, FSTORE, DSTORE, ASTORE
                //consume value from stack
                //produce value to local
                
                final Type varType = context.getLocalTypeAfter(instruction, var);
                
                debug(
                    instruction,context,
                    "pops off the top of stack value (a %s) and writes it to local %d\n", 
                    varType,
                    instruction.var
                    );
                
                {//stack emulation
                    //pop a value off of the stack
                    Entry e = context.stackEmulator.popEntry(instruction);
                    
                    LocalVariableSpec local = 
                            context.locals.getLocal(instruction);
                    
                    context.stackEmulator.storeLocal(
                        instruction.var, 
                        e.getValue()
                        );
                    
                    {//action tracking
                        //move a value from stack into a local
                        setAction(
                            context,
                            instruction,
                            new ActionMove(
                                e.getValue(),
                                new DataLocationStack(e),
                                new DataLocationLocal(local)
                                )
                            );
                        //TODO
                    }//action tracking
                }//stack emulation
                
                
            } else {
                //RET
                //none
                
                throw new RuntimeException(
                    "unsupported instruction: " + MethodPrinter.print(
                        context.mn,
                        instruction
                        )
                    );
            }
        }
        
        @Override
        public void visitJumpInsnNode(
                JumpInsnNode instruction,
                BytecodeSimulator context
                ){
            final int opcode = instruction.getOpcode();
            
            List<AbstractInsnNode> successors = 
                    context.getSuccessors(instruction);
            
            if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.IFEQ,
                Opcodes.IFNE,
                Opcodes.IFLT,
                Opcodes.IFGE,
                Opcodes.IFGT,
                Opcodes.IFLE,
                Opcodes.IFNULL,
                Opcodes.IFNONNULL
                )){
                //believe it or not, this "sanity" check doesn't actually work
                //the compiler sometimes does crazy things:
//                org/apache/jena/atlas/lib/PoolBase put (Ljava/lang/Object;)V passed filtering
//                  L0
//                    LINENUMBER 42 L0
//                    ALOAD 0
//                    GETFIELD org/apache/jena/atlas/lib/PoolBase.maxSize : I
//                    IFLT L1
//                    ALOAD 0
//                    GETFIELD org/apache/jena/atlas/lib/PoolBase.pool : Ljava/util/ArrayDeque;
//                    INVOKEVIRTUAL java/util/ArrayDeque.size ()I
//                    IFNE L1 //uh...ok?
//                   L1
                
//                if(successors.size() != 2){
//                    throw new RuntimeException(
//                        "sanity check failed, expected 2 but got " + successors.size()
//                        );
//                }
                
                //IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IFNULL, IFNONNULL
                //consume value from stack (control flow depends on this)
                
                debug(
                    instruction,context,
                    "pops off the top of stack value and jumps to either %s or %s\n", 
                    context.print(successors.get(0)),
                    successors.size() > 1 ? context.print(successors.get(1)) : "[no other jump target, which is weird]"
                    );
                
                {//stack emulation
                    //pop a value from the stack
                    Entry poppedValue = 
                            context.stackEmulator.popEntry(instruction);
                    
                    setAction(
                        context,
                        instruction,
                        new ActionControlFlow(
                            MethodPrinter.print(context.mn,instruction),
                            new JavaValue[]{poppedValue.getValue()},
                            new AbstractInsnNode[]{instruction.getNext(),instruction.label}
                            )
                        );
                    
                }//stack emulation
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.IF_ICMPEQ, 
                Opcodes.IF_ICMPNE,
                Opcodes.IF_ICMPLT,
                Opcodes.IF_ICMPGE,
                Opcodes.IF_ICMPGT,
                Opcodes.IF_ICMPLE,
                Opcodes.IF_ACMPEQ,
                Opcodes.IF_ACMPNE
                )){
//                if(successors.size() != 2){
//                    throw new RuntimeException(
//                        "sanity check failed, expected 2 but got " + successors.size()
//                        );
//                }
                
                //IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE,
                //consume top two values from stack (control flow depends on these)
                
                debug(
                    instruction,context,
                    "pops off the top two stack values and jumps to either %s or %s\n", 
                    context.print(successors.get(0)),
                    successors.size() > 1 ? context.print(successors.get(1)) : "[no other jump target, which is weird]"
                    );
                
                {//stack emulation
                    //pop two values from the stack
                    Entry v1 = context.stackEmulator.popEntry(instruction);
                    Entry v2 = context.stackEmulator.popEntry(instruction);
                    
                    setAction(
                        context,
                        instruction,
                        new ActionControlFlow(
                            MethodPrinter.print(context.mn,instruction),
                            new JavaValue[]{v1.getValue(),v2.getValue()},
                            new AbstractInsnNode[]{instruction.getNext(),instruction.label}
                            )
                        );
                }//stack emulation
            } else if(OpcodeHelper.isOpcodeAnyOf(opcode, 
                Opcodes.GOTO
                )){
                if(successors.size() != 1){
                    throw new RuntimeException(
                        "sanity check failed, expected 1 but got " + successors.size()
                        );
                }
                
                //GOTO
                
                debug(
                    instruction,context,
                    "jumps to %s\n", 
                    context.print(successors.get(0))
                    );
            } else {
                //RET
                //none
                
                throw new RuntimeException(
                    "unsupported instruction: " + context.print(instruction)
                    );
            }
        }
        
        private void visitUninterestingInstruction(
                AbstractInsnNode instruction,
                BytecodeSimulator context
                ){
            debug(
                instruction,context,
                "is not interesting\n"
                );
        }

        @Override
        public void visitFrameInsnNode(
                FrameNode instruction,
                BytecodeSimulator analysisContext
                ) {
            visitUninterestingInstruction(instruction,analysisContext);
        }

        @Override
        public void visitLabelInsnNode(
                LabelNode label,
                BytecodeSimulator analysisContext
                ) {
            visitUninterestingInstruction(label,analysisContext);
        }

        @Override
        public void visitLineInsnNode(
                LineNumberNode line,
                BytecodeSimulator analysisContext
                ) {
            visitUninterestingInstruction(line,analysisContext);
        }

        @Override
        public void pre(BytecodeSimulator context) {
            if(false)
            {
                System.out.println(MethodPrinter.print(context.mn));//TODO
            }
            
            debug(
                null,context,
                "about to visit instructions in %s %s %s\n",
                context.cn.name,
                context.mn.name,
                context.mn.desc
                );
            
            {
                //store all arguments in local vars
                Type t = Type.getMethodType(context.mn.desc);
                final boolean isStatic = 
                        (context.mn.access & Opcodes.ACC_STATIC) > 0;
                
                int index = 0;
                
                if(isStatic){
                    //no this arg
                } else {
                    JavaValue value = context.valueEmulation.newValue(
                        Type.getObjectType(context.cn.name)
                        );
                    
                    LocalVariableSpec thisRef = 
                            getArgParameterSpec(context,index);
                    
                    context.stackEmulator.storeLocal(
                        index, 
                        value
                        );
                    
                    context.stackEmulator.getParametersToInitialValues().put(
                        thisRef, 
                        value
                        );
                    
                    index++;
                }
                
                for(Type arg:t.getArgumentTypes()){
                    JavaValue value = context.valueEmulation.newValue(arg);
                    
                    LocalVariableSpec local = getArgParameterSpec(context,index);
                    
                    context.stackEmulator.storeLocal(
                        index, 
                        value
                        );
                    
                    context.stackEmulator.getParametersToInitialValues().put(
                        local, 
                        value
                        );
                    
                    index+=arg.getSize();
                }
                
            }
        }
        
        private LocalVariableSpec getArgParameterSpec(
                BytecodeSimulator context,
                int i
                ){
            return context.locals.getParameterSpec(i);
        }

        @Override
        public void post(BytecodeSimulator analysisContext) {
            // sanity check to make sure we didn't miss handling of an
            // instruction
            // (this would otherwise cause the dataflow analysis to self
            // destruct in a non-obvious manner)
            
            
            //verifyVisitation(analysisContext.mn);
        }
        
        private static final Set<String> integerTypes = new HashSet<>(Arrays.asList(
            "B",
            "C",
//            "D",
//            "F",
            "I",
//            "J",
            "S",
            "Z"
            ));

        @Override
        public void postAnyInstruction(
                AbstractInsnNode instruction,
                BytecodeSimulator analysisContext
                ) {
            analysisContext.stackEmulator.recordState(instruction);
            
//            if(false)
            {
                Frame<BasicValue> frame1 = 
                        analysisContext.getFrameAfter(instruction);
                
                List<Entry> frame2 = analysisContext.stackEmulator.getStackStatesAfter().get(instruction);
                
                if(frame1 != null){
                    
                    if(frame1.getStackSize() != frame2.size()){
                        System.out.printf("expected:\n");
                        for(int i=0;i<frame1.getStackSize();i++){
                            System.out.printf("\t%d: %s\n", i, frame1.getStack(i).getType());
                        }
                        System.out.printf("actual:\n");
                        for(int i=0;i<frame2.size();i++){
                            System.out.printf("\t%d: %s\n", i, frame2.get(i));
                        }
                        
                        
                        throw new RuntimeException("stack inconsistency detected, expected " + frame1.getStackSize() + " but got " + frame2.size());//TODO
                    }
                    
                    if(false){
                    for(int i=0;i<frame1.getStackSize();i++){
                        String t1 = frame1.getStack(i).getType().getDescriptor();
                        String t2 = frame2.get(i).getType().getDescriptor();
                        
                        if(t1.equals(t2)){
                            //do nothing
                        } else {
                            
                            if(integerTypes.contains(t1) && integerTypes.contains(t2)){
                                continue;//TODO
                            }
                            
                            if(t2.equals("Lnull;")){
                                continue;//TODO
                            }
                            
                            System.out.printf("expected:\n");
                            for(int j=0;j<frame1.getStackSize();j++){
                                System.out.printf("\t%d: %s\n", j, frame1.getStack(j).getType());
                            }
                            System.out.printf("actual:\n");
                            for(int j=0;j<frame2.size();j++){
                                System.out.printf("\t%d: %s\n", j, frame2.get(j));
                            }
                            
                            //TODO
                            //throw new RuntimeException("stack inconsistency detected, expected " + t1 + " but got " + t2);//TODO
                        }
                    }}
                }
            }

            if(false)
            {
                List<Entry> stackStates = 
                        analysisContext.stackEmulator.getStackStatesAfter().get(instruction);
                
                if(stackStates == null){
                    return;
                }
                
                System.out.printf("\t\t%d stack entries:\n", stackStates.size());
                for(Entry e:stackStates){
                    System.out.printf("\t\t\t%s %s %s\n", e.getId(), e.getType(), e.getValue());
                }
            }
        }
    }
    
    private int indexOf(AbstractInsnNode instruction){
        return mn.instructions.indexOf(instruction);
    }
    
    private String print(AbstractInsnNode instruction){
        return MethodPrinter.print(mn,instruction);
    }
    
    private Frame<BasicValue> getFrameBefore(AbstractInsnNode instruction){
        return stackSimulator.getFrameBefore(instruction);
    }
    
    private Type getStackType(
            Frame<BasicValue> frame, 
            int offset
            ){
        final int index = frame.getStackSize() - 1 + offset;
        
        return frame.getStack(index).getType();
    }
    
    private Type getLocalTypeBefore(
            AbstractInsnNode instruction, 
            int index
            ){
        return getLocalType(getFrameBefore(instruction),index);
    }
    
    private Type getLocalTypeAfter(
            AbstractInsnNode instruction, 
            int index
            ){
        return getLocalType(getFrameAfter(instruction),index);
    }
    
    private Type getStackTypeBefore(
            AbstractInsnNode instruction, 
            int offset
            ){
        return getStackType(getFrameBefore(instruction),offset);
    }
    
    private Type getStackTypeAfter(
            AbstractInsnNode instruction, 
            int offset
            ){
        return getStackType(getFrameAfter(instruction),offset);
    }
    
    private Type getLocalType(
            Frame<BasicValue> frame, 
            int index
            ){
        return frame.getLocal(index).getType();
    }
    
    private Frame<BasicValue> getFrameAfter(AbstractInsnNode instruction){
        return stackSimulator.getFrameAfter(instruction);
    }
    
    private List<AbstractInsnNode> getSuccessors(
            AbstractInsnNode i
            ){
        BasicBlock b = bbg.getBlock(i);
        
        if(b.isLastInBlock(i)){
            List<AbstractInsnNode> successors = new ArrayList<>();
            
            for(BasicBlock s:b.getSuccessors()){
                successors.add(s.getHead());
            }
            return successors;
            
        } else {
            return Arrays.asList(i.getNext());
        }
        
    }

    
    public List<Action> getActions() {
        return actions;
    }
    
    public LocalVariableSpec getLocalVariableSpecForValue(JavaValue j){
        Map<LocalVariableSpec,JavaValue> map = 
                stackEmulator.getParametersToInitialValues();
        
        for(LocalVariableSpec s:map.keySet()){
            JavaValue v = map.get(s);
            
            if(v == j){
                return s;
            }
        }
        
        return null;
    }

}
