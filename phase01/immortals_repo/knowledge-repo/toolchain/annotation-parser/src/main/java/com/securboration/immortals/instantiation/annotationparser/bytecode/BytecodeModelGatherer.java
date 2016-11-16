package com.securboration.immortals.instantiation.annotationparser.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.objectweb.asm.tree.ClassNode;

import com.securboration.immortals.instantiation.annotationparser.traversal.BytecodeArtifactVisitor;

public class BytecodeModelGatherer implements BytecodeArtifactVisitor {

    private final Map<String,ClassNode> map = new HashMap<>();
    
    @Override
    public void visitClass(String classHash, byte[] bytecode) {
        map.put(classHash, BytecodeHelper.getClassNode(bytecode));
    }
    
    public JarBytecodeModel getBytecodeModel(){
        List<ClassNode> classes = new ArrayList<>();
        
        new TreeSet<>(map.keySet()).forEach(k->{
            classes.add(map.get(k));
        });
        
        JarBytecodeModel m = new JarBytecodeModel();
        m.classNodes = classes.toArray(new ClassNode[]{});
        
        return m;
    }
    
    public static class JarBytecodeModel{
        private ClassNode[] classNodes;
    }

}
