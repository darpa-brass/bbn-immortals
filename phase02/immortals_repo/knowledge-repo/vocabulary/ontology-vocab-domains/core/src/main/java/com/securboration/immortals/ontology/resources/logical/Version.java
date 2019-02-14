package com.securboration.immortals.ontology.resources.logical;

import com.securboration.immortals.ontology.property.Property;

public class Version extends Property {
    
    private int major;
    
    private int minor;
    
    private int patch;
    
    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getPatch() {
        return patch;
    }

    public void setPatch(int patch) {
        this.patch = patch;
    }
}
