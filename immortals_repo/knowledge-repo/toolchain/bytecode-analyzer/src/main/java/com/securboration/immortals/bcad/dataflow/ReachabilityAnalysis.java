package com.securboration.immortals.bcad.dataflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.securboration.immortals.bcas.block.BasicBlock;

public class ReachabilityAnalysis {
    
    private final MethodNode mn;
    private final BasicBlock bbg;

    /* a full connectivity graph */
    private final Map<BasicBlock,Set<BasicBlock>> connectivity = new HashMap<>();
    
    /* an edge connectivity graph */
    private final Map<BasicBlock,Set<BasicBlock>> edges = new HashMap<>();
    
    private final Set<BasicBlock> terminalBlocks = new HashSet<>();
    
    public ReachabilityAnalysis(MethodNode mn, BasicBlock bbg) {
        this.mn = mn;
        this.bbg = bbg;
        this.terminalBlocks.addAll(findTerminalNodes(bbg));
        
        analyzeRecursive(bbg.getRoot());
        
        buildPaths();
        
        
    }
    
    public Set<AbstractInsnNode> getTerminalInstructions(){
        Set<AbstractInsnNode> terminal = new HashSet<>();
        for(BasicBlock b:terminalBlocks){
            terminal.add(b.getTail());
        }
        return terminal;
    }
    
    public List<List<AbstractInsnNode>> getPathsToInstruction(AbstractInsnNode i){
        return getPaths(bbg.getHead(),i);
    }
    
    public boolean isWaypoint(
            AbstractInsnNode start, 
            AbstractInsnNode waypoint, 
            AbstractInsnNode end
            ){//TODO: horrendously inefficient
        for(List<AbstractInsnNode> path:getPaths(start,end)){
            if(path.contains(waypoint)){
                return true;
            }
        }
        
        return false;
    }
    
    private static Set<BasicBlock> findTerminalNodes(BasicBlock bbg){
        
        Set<BasicBlock> terminal = new HashSet<>();
        bbg.traverseEdges((path,src,dst)->{
            if(dst.getSuccessors().size() == 0){
                terminal.add(dst);
            }
        });
        
        return terminal;
    }
    
    private void buildPaths(){
        
        bbg.traverseEdges((path,src,dest)->{
            
            Set<BasicBlock> edgesForSrc = edges.get(src);
            
            if(edgesForSrc == null){
                edgesForSrc = new HashSet<>();
                edges.put(src, edgesForSrc);
            }
            
            edgesForSrc.add(dest);
            
            if(false){
                System.out.printf(
                    "b%s -> b%s\n",
                    src == null ? "?" : src.getBlockId(),
                    dest.getBlockId()
                    );//TODO
                
                if(path.size() > 0){
                    System.out.printf("\tedge reached along %d-path: ", path.size());
                    for(BasicBlock b:path){
                        System.out.printf(
                            "[%sb%d] ", 
                            b==src ? "*": b==dest ? "**" : "",
                            b.getBlockId()
                            );
                    }
                    System.out.println();
                }
            }//TODO
        });
        
    }
    
    private Set<BasicBlock> getConnectedTo(BasicBlock b){
        Set<BasicBlock> connected = new LinkedHashSet<>();
        
        collectConnected(b,connected);
        
        return connected;
    }
    
    private void collectConnected(
            BasicBlock current,
            Set<BasicBlock> connected
            ){
        if(connected.contains(current)){
            return;
        }
        
        for(BasicBlock connectedToCurrent:current.getSuccessors()){
            connected.add(connectedToCurrent);
            collectConnected(connectedToCurrent,connected);
        }
    }
    
    private void analyzeRecursive(
            BasicBlock current
            ){
        if(connectivity.containsKey(current)){
            return;
        }
        
        Set<BasicBlock> connected = getConnectedTo(current);
        connectivity.put(current, connected);
        
        for(BasicBlock successor:current.getSuccessors()){
            analyzeRecursive(successor);
            //TODO: inefficient, should use dynamic programming solution
        }
    }
    
    private List<List<AbstractInsnNode>> getPaths(
            AbstractInsnNode src, 
            AbstractInsnNode dst
            ){
        List<List<AbstractInsnNode>> paths = new ArrayList<>();
        
        List<List<BasicBlock>> blockPaths = 
                getPaths(bbg.getBlock(src),bbg.getBlock(dst));
        
        for(List<BasicBlock> path:blockPaths){
            List<AbstractInsnNode> p = new ArrayList<>();
            
            for(int i=0;i<path.size();i++){
                final boolean isLast = i == path.size() -1;
                
                BasicBlock b = path.get(i);
                
                for(AbstractInsnNode instructionInBlock:b.getBlockInstructions()){
                    p.add(instructionInBlock);
                    
                    if(isLast && instructionInBlock == dst){
                        break;
                    }
                }
            }
            
            paths.add(p);
        }
        
        return paths;
    }
    
    private List<List<BasicBlock>> getPaths(BasicBlock src, BasicBlock dest){
        if(src == dest){
            return Arrays.asList(Arrays.asList(src));
        }
        
        List<List<BasicBlock>> paths = new ArrayList<>();
        
        findPath(src,new ArrayList<>(),dest,paths);
        
//        List<List<BasicBlock>> reduced = new ArrayList<>();
//        for(List<BasicBlock> path:paths){
//            reduced.add(reduce(path));
//        }
        
        return paths;
    }
    
    private List<BasicBlock> reduce(
            List<BasicBlock> path, 
            BasicBlock src, 
            BasicBlock dst
            ){
        if(path.size() == 0 || path.size() == 1){
            return path;//irreducible
        }
        
        List<BasicBlock> reduced = new ArrayList<>();
        
        //search for a cycle in the path up to n-1
        int currentIndex = 0;
        boolean wasReduced = false;
        boolean stop = false;
        while(!stop){
            BasicBlock currentValue = path.get(currentIndex);
            reduced.add(currentValue);
            
            //search from here to the end for a match
            int matchIndex = -1;
            for(int i=currentIndex+1;i<path.size()-1;i++){
                
                if(path.get(i) == currentValue){
                    matchIndex = i;
                }
            }
            
            if(matchIndex != -1){
                //found a match, skip to it
                currentIndex = matchIndex + 1;
                wasReduced = true;
            } else {
                currentIndex++;
            }
            
            
            if(currentIndex == path.size() - 1){
                stop = true;
            }
        }
        
        //irreducible
        //[b1]  [b3]  [b4]  [b6]  [b4]  [b5]  [b1]
        
        // ------------------------------------>
        //[b1]  [b3]  [b4]  [b6]  [b4]  [b5]  [b1]  [b2]
        //[b1]  [b2]
        
        //             ------------>
        //[b1]  [b3]  [b4]  [b6]  [b4]  [b5]
        //[b1]  [b3]  [b4]  [b5]
        
        
        // ------------>
        //[b4]  [b6]  [b4]  [b5]  [b1]  [b3]  [b4]
        //[b4]  [b5]  [b1]  [b3]  [b4]
        
        if(wasReduced){
            reduced.add(path.get(path.size()-1));
            return reduced;
        }
        
        return path;
    }
    
    private void findPath(
            BasicBlock current, 
            List<BasicBlock> currentPath,
            BasicBlock target,
            List<List<BasicBlock>> solutions
            ){
        currentPath.add(current);
        
        //terminate if hypercyclic condition is reached
        //(any node visited more than twice)
        {
            for(BasicBlock countThis:currentPath){
                
                int count = 0;
                for(BasicBlock b:currentPath){
                    if(b == countThis){
                        count ++;
                    }
                }
                
                if(count > 2){
                    return;
                }
                
            }
            
        }
        
        if(current == target && currentPath.size() > 1){
            solutions.add(new ArrayList<>(currentPath));
        }
        
        for(BasicBlock successor:current.getSuccessors()){
            List<BasicBlock> newPath = new ArrayList<>(currentPath);
            
            findPath(
                successor,
                newPath,
                target,
                solutions
                );
        }
    }
    
//    private void findPath(
//            BasicBlock current, 
//            List<BasicBlock> currentPath, 
//            BasicBlock target, 
//            Map<BasicBlock,Set<BasicBlock>> visitedEdges, 
//            List<List<BasicBlock>> solutions
//            ){
//        currentPath.add(current);
//        if(current == target){
//            solutions.add(new ArrayList<>(currentPath));
//        }
//        
//        Set<BasicBlock> visitedEdgesForCurrent = visitedEdges.get(current);
//        if(visitedEdgesForCurrent == null){
//            visitedEdgesForCurrent = new HashSet<>();
//            visitedEdges.put(current, visitedEdgesForCurrent);
//        }
//        
//        for(BasicBlock successor:current.getSuccessors()){
//            if(visitedEdgesForCurrent.contains(successor)){
//                System.out.printf("skipping %d->%d\n", current.getBlockId(), successor.getBlockId());//TODO
//                continue;
//            }
//            visitedEdgesForCurrent.add(successor);
//            
//            findPath(
//                successor,
//                new ArrayList<>(currentPath),
//                target,
//                visitedEdges,
//                solutions
//                );
//        }
//    }
    
    public boolean areConnected(AbstractInsnNode i1, AbstractInsnNode i2){
        if(i1 == i2){
            return true;
        }
        
        BasicBlock b1 = bbg.getBlock(i1);
        BasicBlock b2 = bbg.getBlock(i2);
        
        Set<BasicBlock> connectedToB1 = connectivity.get(b1);
        
        if(connectedToB1 == null){
            return false;
        }
        
        //if they're in different basic blocks, test if b2 is reachable from b1
        if(b1 != b2){
            return connectedToB1.contains(b2);
        }
        
        //if they're in the same block, test if i1 comes before i2 OR if b1 
        // is connected to itself
        
        final boolean i1Beforei2 = 
                mn.instructions.indexOf(i1) < mn.instructions.indexOf(i2);
        
        return i1Beforei2 | connectedToB1.contains(b2);
    }
    
    public String printConnectivity(MethodNode mn){
        StringBuilder sb = new StringBuilder();
        
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        
        sb.append("            ");
        for(AbstractInsnNode i:instructions){
            String label = 
                    String.format(
                        "b%s i%d", 
                        bbg.getBlock(i) == null ? "?" : bbg.getBlock(i).getBlockId(), 
                        mn.instructions.indexOf(i)
                        );
            
            sb.append(String.format("[%8s]  ", label));
        }
        sb.append("\n");
        
        for(AbstractInsnNode i:instructions){
            String label = 
                    String.format(
                        "b%s i%d", 
                        bbg.getBlock(i) == null ? "?" : bbg.getBlock(i).getBlockId(), 
                        mn.instructions.indexOf(i)
                        );
            
            sb.append(String.format("[%8s]  ", label));
            
            for(AbstractInsnNode j:instructions){
                final String connected = areConnected(i,j) ? "    XXXX    " : "            ";
                sb.append(connected);
            }
            
            sb.append("\n");
        }
        
        {
            Set<BasicBlock> blocks = new HashSet<>();
            for(AbstractInsnNode i:mn.instructions.toArray()){
                BasicBlock b = bbg.getBlock(i);
                
                if(b != null){
                    blocks.add(b);
                }
            }
            
            for(BasicBlock i:blocks){
                for(BasicBlock j:blocks){
                    List<List<BasicBlock>> paths = getPaths(i,j);
                    
                    sb.append(String.format(
                        "found %d paths between b%d and b%d\n", 
                        paths.size(),
                        i.getBlockId(), 
                        j.getBlockId()
                        ));
                    
                    for(List<BasicBlock> path:paths){
                        sb.append(String.format("\t%d-path:  ", path.size()));
                        
                        for(BasicBlock b:path){
                            sb.append(String.format("[b%d]  ", b.getBlockId()));
                        }
                        sb.append("\n");
                        
//                        List<BasicBlock> reduced = reduce(path,i,j);
//                        
//                        if(reduced != path){
//                            sb.append(String.format("\treduces to a %d-path:  ", reduced.size()));
//                            
//                            for(BasicBlock b:reduced){
//                                sb.append(String.format("[b%d]  ", b.getBlockId()));
//                            }
//                            sb.append("\n");
//                        }
                        
                    }
                }
            }
        }
        
        return sb.toString();
    }
    
//    private List<BasicBlock> getPath(BasicBlock b1, BasicBlock b2){
//        if(!areConnected(b1,b2)){
//            throw new RuntimeException(
//                "attempted to retrieve path for two blocks that are not " +
//                "connected by control flow"
//                        );
//        }
//        
//        
//    }
    

}
