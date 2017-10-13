package com.securboration.immortals.ontology.condition;

import com.securboration.immortals.ontology.bytecode.Library;

/**
 * A condition where some library is unavailable
 * 
 * @author Securboration
 *
 */
public class LibraryUnavailable extends PlatformCondition {

    /**
     * The library that is not available
     */
    private Library library;

    public Library getLibrary() {
        return library;
    }

    public void setLibrary(Library library) {
        this.library = library;
    }
    
}
