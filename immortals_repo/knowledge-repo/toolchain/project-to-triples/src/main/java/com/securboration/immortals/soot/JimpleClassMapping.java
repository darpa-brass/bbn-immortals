package com.securboration.immortals.soot;

import com.github.javaparser.utils.Pair;

public class JimpleClassMapping {
    
    public JimpleClassMapping(Pair<String, String> _jimplePathToClassName) {
        jimplePathToClassName = _jimplePathToClassName;
    }
    
    private Pair<String, String> jimplePathToClassName;

    public String getPathToJimple() {
        return jimplePathToClassName.a;
    }
    
    public String getClassName() {
        return jimplePathToClassName.b;
    }
}

