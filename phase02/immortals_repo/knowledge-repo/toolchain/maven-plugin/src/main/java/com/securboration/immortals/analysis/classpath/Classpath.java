package com.securboration.immortals.analysis.classpath;

import java.util.ArrayList;
import java.util.List;

import com.securboration.immortals.analysis.AnalysisBaseType;

public class Classpath extends AnalysisBaseType {
    
    public Classpath(){super();}
    public Classpath(String s){super(s);}
    
    private final List<ClasspathItem> classpath = new ArrayList<>();

    public List<ClasspathItem> getClasspath() {
        return classpath;
    }
    
}
