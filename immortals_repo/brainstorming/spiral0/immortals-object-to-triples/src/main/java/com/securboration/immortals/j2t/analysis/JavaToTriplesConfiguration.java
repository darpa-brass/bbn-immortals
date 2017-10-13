package com.securboration.immortals.j2t.analysis;

import java.util.ArrayList;
import java.util.Collection;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;

/**
 * Configuration for the object-to-triples conversion process. All fields have
 * sane defaults.
 * 
 * @author jstaples
 *
 */
public class JavaToTriplesConfiguration extends ObjectToTriplesConfiguration {
    
    private Collection<String> skipPrefixes = new ArrayList<>();
    private Collection<String> sourcePaths = new ArrayList<>();
    private Collection<String> classPaths = new ArrayList<>();

    public Collection<String> getSkipPrefixes() {
        return skipPrefixes;
    }

    public void setSkipPrefixes(Collection<String> skipPrefixes) {
        this.skipPrefixes = skipPrefixes;
    }

    public Collection<String> getSourcePaths() {
        return sourcePaths;
    }

    public void setSourcePaths(Collection<String> sourcePaths) {
        this.sourcePaths = sourcePaths;
    }

    public Collection<String> getClassPaths() {
        return classPaths;
    }

    public void setClassPaths(Collection<String> classPaths) {
        this.classPaths = classPaths;
    }

}
