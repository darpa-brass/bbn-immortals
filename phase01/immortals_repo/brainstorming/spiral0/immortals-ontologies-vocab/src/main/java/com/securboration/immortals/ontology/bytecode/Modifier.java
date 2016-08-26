package com.securboration.immortals.ontology.bytecode;

import java.util.ArrayList;
import java.util.List;

/**
 * A modifier for a class structure. E.g., an access modifier like public or
 * private.
 * 
 * @author Securboration
 *
 */
public enum Modifier {
    
    //access modifier flags (values taken from JVM spec)
    ABSTRACT(1024),
    FINAL(16),
    INTERFACE(512),
    NATIVE(256),
    PRIVATE(2),
    PROTECTED(4),
    PUBLIC(1),
    STATIC(8),
    STRICT(2048),
    SYNCHRONIZED(32),
    TRANSIENT(128),
    VOLATILE(64),
    
    //additional modifier flags (values taken from JVM spec)
    ANNOTATION(8192),
    BRIDGE(64),
    DEPRECATED(131072),
    ENUM(16384),
    MANDATED(32768),
    SUPER(32),
    SYNTHETIC(4096),
    VARARGS(128),
    ;
    
    private final int access;
    
    private Modifier(int access){
        this.access = access;
    }
    
    public static Modifier[] getModifiers(int access){
        
        List<Modifier> modifiers = new ArrayList<>();
        
        for(Modifier modifier:Modifier.values()){
            if((modifier.access & access) > 0){
                modifiers.add(modifier);
            }
        }
        
        return modifiers.toArray(new Modifier[]{});
    }
    
}
