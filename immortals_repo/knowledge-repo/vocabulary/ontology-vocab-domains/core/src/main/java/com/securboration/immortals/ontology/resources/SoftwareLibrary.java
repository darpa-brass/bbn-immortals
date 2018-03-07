package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.resources.logical.Version;

public class SoftwareLibrary extends Software {
    
    private Version version;

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
}
