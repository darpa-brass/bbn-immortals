package com.securboration.immortals.ontology.bytecode.analysis;

import java.util.ArrayList;
import java.util.List;

public class BasicBlockDecomposition {
    
    private BasicBlock root;
    
    private final List<BasicBlock> nodes = new ArrayList<>();

    
    public BasicBlock getRoot() {
        return root;
    }

    
    public void setRoot(BasicBlock root) {
        this.root = root;
    }

    
    public List<BasicBlock> getNodes() {
        return nodes;
    }

}
