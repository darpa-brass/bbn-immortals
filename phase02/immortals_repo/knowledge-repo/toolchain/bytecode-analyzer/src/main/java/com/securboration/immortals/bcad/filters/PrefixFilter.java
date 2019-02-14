package com.securboration.immortals.bcad.filters;

import java.lang.reflect.Method;

import org.objectweb.asm.Type;

import com.securboration.immortals.bcad.dataflow.AnalysisFilter;

public class PrefixFilter implements AnalysisFilter {
    
    private final String whitelistClassPrefix;
    private final String whitelistMethodPrefix;
    private final String[] blacklistedClasses;

    @Override
    public boolean shouldAnalyzeClass(String className) {
        if(!className.startsWith(whitelistClassPrefix)){
            return false;
        }
        
        for(String s:blacklistedClasses){
            if(className.startsWith(s)){
                return false;
            }
        }
        
        return true;
    }

    @Override
    public boolean shouldAnalyzeMethodInClass(Class<?> c, Method m) {
        
        final String desc = m.getName() + " " + Type.getMethodDescriptor(m);
        
        if(!desc.startsWith(whitelistMethodPrefix)){
            return false;
        }
        
        return true;
    }

    public PrefixFilter(
            String classPrefix, 
            String methodPrefix,
            String...avoidClassPrefixes
            ) {
        this.whitelistClassPrefix = classPrefix;
        this.whitelistMethodPrefix = methodPrefix;
        this.blacklistedClasses = avoidClassPrefixes;
    }

}
