package com.securboration.immortals.ontology.java.source;

import com.securboration.immortals.ontology.bytecode.ClassArtifact;
import com.securboration.immortals.ontology.bytecode.ClasspathElement;
import com.securboration.immortals.ontology.java.vcs.VcsCoordinate;
/**
 * A model of a Java source file
 * 
 * @author jstaples
 *
 */
public class CompiledJavaSourceFile extends ClasspathElement {
    
    /**
     * The encoding used in a source file
     */
    private String sourceEncoding;
    
    /**
     * The resultant classes emitted after compilation of this file. Note that
     * there can be more than one if, for example, this file contains nested or
     * anonymous classes.
     */
    private ClassArtifact correspondingClass[];
    
    /**
     * The Version Control System coordinate for a source file
     */
    private VcsCoordinate vcsInfo;
    
    public String getSourceEncoding() {
        return sourceEncoding;
    }

    
    public void setSourceEncoding(String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }


    
    public VcsCoordinate getVcsInfo() {
        return vcsInfo;
    }


    
    public void setVcsInfo(VcsCoordinate vcsInfo) {
        this.vcsInfo = vcsInfo;
    }


    
    public ClassArtifact[] getCorrespondingClass() {
        return correspondingClass;
    }


    
    public void setCorrespondingClass(ClassArtifact[] correspondingClass) {
        this.correspondingClass = correspondingClass;
    }

}
