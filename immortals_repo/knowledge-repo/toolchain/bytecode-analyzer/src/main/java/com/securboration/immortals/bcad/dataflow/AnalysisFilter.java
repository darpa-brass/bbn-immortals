package com.securboration.immortals.bcad.dataflow;

import java.lang.reflect.Method;

public interface AnalysisFilter{
    
    public boolean shouldAnalyzeClass(
            final String className
            );
    
    public boolean shouldAnalyzeMethodInClass(
            final Class<?> c, 
            final Method m
            );
}