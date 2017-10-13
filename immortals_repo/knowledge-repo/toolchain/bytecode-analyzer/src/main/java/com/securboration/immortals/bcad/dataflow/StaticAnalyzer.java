package com.securboration.immortals.bcad.dataflow;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.securboration.immortals.bca.AnalysisClassloader;
import com.securboration.immortals.bcad.dataflow.DataflowHelper.LocalVariableSpec;
import com.securboration.immortals.bcad.dataflow.DataflowHelper.ReturnValueSpec;
import com.securboration.immortals.bcad.dataflow.value.JavaValue;
import com.securboration.immortals.bcad.transformers.helpers.OpcodeHelper;
import com.securboration.immortals.bcas.block.BasicBlock;
import com.securboration.immortals.bcas.printer.MethodPrinter;
import com.securboration.immortals.helpers.ImmortalsPointerHelper;
import com.securboration.immortals.instantiation.annotationparser.bytecode.BytecodeHelper;
import com.securboration.immortals.ontology.functionality.datatype.DataType;

public class StaticAnalyzer {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(StaticAnalyzer.class);
    
    private final AnalysisClassloader classloader;
    
    public StaticAnalyzer(AnalysisClassloader classloader) {
        this.classloader = classloader;
    }
    
    public void analyze(
            final AnalysisFilter filter
            ) throws ClassNotFoundException, SecurityException, IOException, AnalyzerException{
        for(Class<?> c:classloader.loadEverythingPassingFilter(filter)){
            
            System.out.printf("considering %s\n", c.getName());//TODO
            
            final byte[] bytecode = classloader.getBytecode(c);
            final ClassNode cn = BytecodeHelper.getClassNode(bytecode);
            final String classPointer = 
                    ImmortalsPointerHelper.pointerForClass(bytecode);
            
            analyze(classPointer,filter,c,cn);
        }
    }
    
    private void analyze(
            final String classPointer,
            AnalysisFilter filter,
            Class<?> c,
            ClassNode cn
            ) throws AnalyzerException{
        for(Method m:c.getDeclaredMethods()){
            if(!filter.shouldAnalyzeMethodInClass(c,m)){
                continue;
            }
            
            final String desc = Type.getType(m).getDescriptor();
            
            final String methodPointer = 
                    ImmortalsPointerHelper.pointerForMethod(
                        classPointer, 
                        m.getName(), 
                        desc
                        );
            
            final MethodNode mn = 
                    BytecodeHelper.getDeclaredMethod(cn, m.getName(), desc);
            
            analyze(c,m,cn,mn,methodPointer);
        }
    }
    
    private static boolean isSafeToAnalyze(MethodNode mn){
        for(AbstractInsnNode i:mn.instructions.toArray()){
            if(i.getOpcode() == Opcodes.JSR){
                return false;
            }
            
            if(i.getOpcode() == Opcodes.RET){
                return false;
            }
        }
        
        return true;
    }
    
    private void analyze(
            final Class<?> c, 
            final Method m, 
            final ClassNode cn, 
            final MethodNode mn,
            final String methodPointer
            ) throws AnalyzerException{
        if(OpcodeHelper.isAnyFlagSet(mn.access, 
            Opcodes.ACC_ABSTRACT,
            Opcodes.ACC_NATIVE
            )){
            return;
        }
        
        if(!isSafeToAnalyze(mn)){
            return;
        }
        
        System.out.printf(
            "%s %s %s passed filtering\n", 
            cn.name, 
            mn.name, 
            mn.desc
            );
        
//        System.out.printf(
//            "%s\n", 
//            MethodPrinter.print(mn)
//            );//TODO
        
        //TODO
        
        //decompose control flow into a basic block view
        BasicBlock bbg = BasicBlock.decompose(mn);
        
        //determine reachability
        ReachabilityAnalysis reachability = new ReachabilityAnalysis(mn,bbg);
        
        //get local var specs
        LocalVars locals = LocalVars.analyze(
            m, 
            mn, 
            bbg, 
            reachability
            );
        
        if(!locals.isLocalMappingComplete()){
            logger.warn(
                String.format(
                    "%s %s %s does not have sufficient local table info to " +
                    "analyze and will be skipped", 
                    cn.name,
                    mn.name,
                    mn.desc
                    )
                );
            
            return;
        }
        
        //create a dataflow tracker
//        DataflowTracker tracker = new DataflowTracker(
//            classloader,
//            cn,
//            mn,
//            bbg,
//            locals
//            );
        
        //identify terminal instructions of normal control flows
        Set<AbstractInsnNode> terminals = 
                reachability.getTerminalInstructions();
        
        System.out.printf("%d terminal instructions\n", terminals.size());
        for(AbstractInsnNode terminal:terminals){
            List<List<AbstractInsnNode>> paths = reachability.getPathsToInstruction(terminal);
            System.out.printf(
                "\t%d paths to i%d: %s\n", 
                paths.size(),
                mn.instructions.indexOf(terminal),
                MethodPrinter.print(mn,terminal)
                );
            for(List<AbstractInsnNode> path:paths){
                BytecodeSimulator tracker = new BytecodeSimulator(
                    classloader,
                    cn,
                    mn,
                    bbg,
                    locals,
                    path
                    );
                
                List<Action> actions = tracker.getActions();
                
                System.out.printf(
                    "\t%d instructions in chain, %d actions\n", 
                    path.size(), 
                    actions.size()
                    );
                
                for(Action a:actions){
                    System.out.printf("\t\t%s\n", a.toString());
                    
                    if(a instanceof ActionInvoke){
                        ActionInvoke i = (ActionInvoke)a;
                        
                        int argCount = 0;
                        for(JavaValue v:i.getArgsValues()){
                            
                            LocalVariableSpec local = 
                                    tracker.getLocalVariableSpecForValue(v);
                            
                            System.out.printf(
                                "\t\t\targ %d: %s\n", 
                                argCount,
                                local == null ? "?" : local.getSemanticType()
                                );
                            argCount++;
                        }
                    }
                }
            }
        }
        
        
        
        
        
        
        
        {
//            {//TODO
//                System.out.printf("starting traversal\n");
//                
//                bbg.traverse((i)->{
//                    Action a = tracker.getInstructionsToActions().get(i);
//                    
//                    if(a == null){
//                        return;
//                    }
//                    
//                    System.out.printf(
//                        "i%d %s\n\t%s\n",
//                        mn.instructions.indexOf(i),
//                        MethodPrinter.print(mn,i),
//                        a
//                        );
//                });
//            }
//            
//            {
//                System.out.printf("path analysis\n");
//                //TODO
//                for(AbstractInsnNode i:mn.instructions.toArray()){
//                    List<List<AbstractInsnNode>> paths = 
//                            reachability.getPathsToInstruction(i);
//                    
//                    System.out.printf(
//                        "\tfound %d paths to i%d %s\n",
//                        paths.size(),
//                        mn.instructions.indexOf(i),
//                        MethodPrinter.print(mn,i)
//                        );
//                    
//                    for(List<AbstractInsnNode> path:paths){
//                        
//                        System.out.printf("\t\tpath\n");
//                        
//                        for(AbstractInsnNode instruction:path){
//                            System.out.printf(
//                                "\t\t\ti%d %s\n",
//                                mn.instructions.indexOf(instruction),
//                                MethodPrinter.print(mn,instruction)
//                                );
//                        }
//                    }
//                }
//            }
//            
//            {
//                System.out.printf("path analysis2\n");
//                //TODO
//                for(AbstractInsnNode i:reachability.getTerminalInstructions()){
//                    List<List<AbstractInsnNode>> paths = 
//                            reachability.getPathsToInstruction(i);
//                    
//                    System.out.printf(
//                        "\tfound %d paths to i%d %s\n",
//                        paths.size(),
//                        mn.instructions.indexOf(i),
//                        MethodPrinter.print(mn,i)
//                        );
//                    
//                    for(List<AbstractInsnNode> path:paths){
//                        
//                        System.out.printf("\t\tpath\n");
//                        
//                        for(AbstractInsnNode instruction:path){
//                            System.out.printf(
//                                "\t\t\ti%d %s\n",
//                                mn.instructions.indexOf(instruction),
//                                MethodPrinter.print(mn,instruction)
//                                );
//                        }
//                    }
//                }
//            }
            
//            if(false)
//            {
//                System.out.printf("path analysis2 over %d terminals\n",terminals.size());
//                //TODO
//                for(AbstractInsnNode i:terminals){
//                    List<List<AbstractInsnNode>> paths = 
//                            reachability.getPathsToInstruction(i);
//                    
//                    System.out.printf(
//                        "\tfound %d paths to i%d %s\n",
//                        paths.size(),
//                        mn.instructions.indexOf(i),
//                        MethodPrinter.print(mn,i)
//                        );
//                    
//                    for(List<AbstractInsnNode> path:paths){
//                        
//                        System.out.printf("\t\tpath\n");
//                        
//                        for(AbstractInsnNode instruction:path){
//                            Action a = tracker.getInstructionsToActions().get(instruction);
//                            
//                            System.out.printf(
//                                "\t\t\ti%d %s\n",
//                                mn.instructions.indexOf(instruction),
//                                MethodPrinter.print(mn,instruction)
//                                );
//                            
//                            if(a == null){
//                                continue;
//                            }
//                            
//                            System.out.printf(
//                                "\t\t\t\t%s\n",
//                                a
//                                );
//                        }
//                    }
//                }
//            }
//            
//            if(false)
//            {
//                System.out.printf("action analysis3 over %d terminals\n",terminals.size());
//                //TODO
//                for(AbstractInsnNode i:terminals){
//                    List<List<Action>> actions = 
//                            getActionRoutesToTerminal(i,tracker,reachability);
//                    
//                    System.out.printf(
//                        "\tfound %d paths to i%d %s\n",
//                        actions.size(),
//                        mn.instructions.indexOf(i),
//                        MethodPrinter.print(mn,i)
//                        );
//                    
//                    for(List<Action> path:actions){
//                        
//                        System.out.printf("\t\tpath\n");
//                        
//                        List<ActionInvoke> invokes = getInvocationsAlongPath(path);
//                        
//                        for(ActionInvoke a:invokes){
//                            System.out.printf(
//                                "\t\t\t%s\n",
//                                a
//                                );
//                            
//                            JavaValue[] args = a.getArgsValues();
//                            for(int index=0;index<args.length;index++){
//                                JavaValue arg = args[index];
//                                
//                                LocalVariableSpec s = 
//                                        tracker.getLocalVariableSpecForValue(arg);
//                                
//                                System.out.printf(
//                                    "\t\t\t\targ %d: %s\n",
//                                    index,
//                                    s == null ? "?" : s.getSemanticType() == null ? "none" : s.getSemanticType().getSimpleName()
//                                    );
//                            }
//                        }
//                    }
//                }
//            }
            
            
//            if(mn.name.equals("test4")){
//                throw new RuntimeException("intentional");//TODO
//            }
        }
        
        
        
    }
    
    
    private List<List<Action>> getActionRoutesToTerminal(
            AbstractInsnNode terminal,
            DataflowTracker tracker,
            ReachabilityAnalysis reachability
            ){
        List<List<Action>> actionsForPaths = new ArrayList<>();
        
        for(List<AbstractInsnNode> path:reachability.getPathsToInstruction(terminal)){
            
            List<Action> actionsForPath = new ArrayList<>();
            
            for(AbstractInsnNode instruction:path){
                Action a = tracker.getInstructionsToActions().get(instruction);

                if(a == null){
                    continue;
                }
                
                actionsForPath.add(a);
            }
            
            actionsForPaths.add(actionsForPath);
        }
        
        return actionsForPaths;
    }
    
    private Class<? extends DataType> getReturnValueType(
            final String methodOwner,
            final String methodName,
            final String methodDesc
            ) throws ClassNotFoundException{
        Class<?> c = classloader.loadClass(methodOwner.replace("/", "."));
        
        Method m = ReflectionHelper.findMethod(c, methodName, methodDesc);
        
        ReturnValueSpec r = DataflowHelper.getReturnValue(m);
        
        return (Class<? extends DataType>) r.getSemanticType();
    }
    
    private List<ActionInvoke> getInvocationsAlongPath(List<Action> actions){
        List<ActionInvoke> invokes = new ArrayList<>();

        for(Action a:actions){
            if(a instanceof ActionInvoke){
                invokes.add((ActionInvoke)a);
            }
        }
        
        return invokes;
    }
    
    private Map<MethodInsnNode,ActionInvoke> getCallActionsAlongPath(
            List<AbstractInsnNode> path,
            Map<AbstractInsnNode,Action> actions
            ){
        final List<MethodInsnNode> calls = BytecodeHelper.getInstructionsOfType(
            path, 
            AbstractInsnNode.METHOD_INSN
            );
        
        Map<MethodInsnNode,ActionInvoke> map = new HashMap<>();
        for(MethodInsnNode call:calls){
            ActionInvoke a = (ActionInvoke)actions.get(call);
            
            map.put(call, a);
        }
        
        return map;
    }

}
