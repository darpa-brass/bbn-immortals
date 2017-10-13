package com.securboration.immortals.ontology.condition;

import com.securboration.immortals.ontology.resources.PlatformResource;

/**
 * A condition that occurs on a platform (e.g., battery dies)
 * 
 * @author Securboration
 *
 */
public class PlatformCondition extends LogicalCondition {

    /**
     * The platform to which the condition applies
     */
    private PlatformResource platform;

    public PlatformResource getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformResource platform) {
        this.platform = platform;
    }

}
