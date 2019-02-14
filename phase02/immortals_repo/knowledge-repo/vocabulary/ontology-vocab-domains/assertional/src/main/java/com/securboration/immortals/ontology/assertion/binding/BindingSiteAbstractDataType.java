package com.securboration.immortals.ontology.assertion.binding;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

/**
 * A binding site that is an abstract data type. I.e., a corresponding assertion
 * should bind to all data of that type.
 * 
 * @author jstaples
 *
 */
public class BindingSiteAbstractDataType extends BindingSiteBase {
    
    /**
     * The type of data for this binding site
     */
    private Class<? extends DataType> assertionalBindingSite;
    
}
