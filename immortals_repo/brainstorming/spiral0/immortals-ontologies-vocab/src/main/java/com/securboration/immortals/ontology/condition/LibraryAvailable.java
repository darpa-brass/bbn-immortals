package com.securboration.immortals.ontology.condition;

import com.securboration.immortals.ontology.bytecode.Library;

/**
 * A condition where some library is available
 * 
 * @author Securboration
 *
 */
public class LibraryAvailable extends PlatformCondition {

    /**
     * The library that is available
     */
    private Library library;

    public Library getLibrary() {
        return library;
    }

    public void setLibrary(Library library) {
        this.library = library;
    }
    
}
