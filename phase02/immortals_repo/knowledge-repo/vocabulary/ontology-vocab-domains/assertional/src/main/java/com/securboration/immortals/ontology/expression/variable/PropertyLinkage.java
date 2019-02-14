package com.securboration.immortals.ontology.expression.variable;

import com.securboration.immortals.ontology.assertion.binding.BindingSiteBase;
import com.securboration.immortals.ontology.property.Property;

/**
 * Describes a linkage to a measurement 
 * 
 * @author jstaples
 *
 */
public class PropertyLinkage {
    
    /**
     * The type of property to which a linkage is specified
     */
    private Class<? extends Property> propertyType;
    
    /**
     * The path to a value in a property
     */
    private String propertyPath;
    
    /**
     * The owner of the indicated property type to which a linkage is specified
     */
    private BindingSiteBase propertyOwner;

}
