package com.securboration.immortals.ontology.assertion.types.descriptive;

import com.securboration.immortals.ontology.assertion.AssertionalStatement;
import com.securboration.immortals.ontology.assertion.binding.BindingSiteBase;

/**
 * An assertional statement about the state of things as they actually are.
 * 
 * E.g.,
 * <ul>
 * <li>A location update message was observed to have an SEP-95 of 30 meters
 * </li>
 * <li>3 location update messages per minute were sent from client99 to server1
 * </li>
 * <li>A SymmetricCipher DFU was initialized with a 128-bit key</li>
 * <li>RSA is not a post-quantum secure algorithm</li>
 * <li>ECC is not a post-quantum secure algorithm</li>
 * <li>SupersingularECC is a post-quantum secure algorithm</li>
 * <li>AsymmetricCipher1995-as3-zmiuox is an AsymmetricCipher implementation
 * based on the SupersingularECC algorithm</li>
 * </ul>
 * 
 * @author jstaples
 *
 */
public class DescriptiveAssertionalStatement extends AssertionalStatement {
    
    /**
     * Describes the subject of a descriptive assertion. E.g., In the assertion
     * "A SymmetricCipher DFU was initialized with a 128-bit key",
     * "SymmetricCipher DFU" is the subject.
     */
    private BindingSiteBase subjectOfDescriptiveAssertion;
    
    /**
     * Describes the object of a descriptive assertion. E.g., In the assertion
     * "A SymmetricCipher DFU was initialized with a 128-bit key",
     * "key_length = 128-bits" is the object.  
     */
    private DescriptiveRValue objectOfDescriptiveAssertion;
    
}
