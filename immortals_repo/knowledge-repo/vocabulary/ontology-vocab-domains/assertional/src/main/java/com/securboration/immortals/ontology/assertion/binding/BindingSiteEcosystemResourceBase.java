package com.securboration.immortals.ontology.assertion.binding;

import com.securboration.immortals.ontology.property.Property;

/**
 * A binding site that is a resource type
 * 
 * @author jstaples
 *
 */
public class BindingSiteEcosystemResourceBase extends BindingSiteBase {
    
    /**
     * The properties of the resource binding site, (whether that site is 
     * abstract or concrete).
     */
    private Property[] propertiesOfResourceBindingSite;
    
    /**
     * The resource binding site supports arbitrary nesting (e.g., a constraint
     * can bind to any MobileDevice containing a GpsReceiver with the Trusted
     * property).
     */
    private BindingSiteEcosystemResourceBase nestedAbstractResource;
    
}
