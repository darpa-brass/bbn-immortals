package com.securboration.immortals.ontology.markers;

import com.securboration.immortals.ontology.pojos.markup.Ignore;

@Ignore
public final class Defaults {
    
    public static final Class<?> defaultClass = DefaultClass.class;
    public static final String defaultString = "";
    
    public static final int defaultInt = Integer.MIN_VALUE;
    
    public static final long defaultLong = Long.MIN_VALUE;
    
    
    private static final class DefaultClass{
        
    }

}
