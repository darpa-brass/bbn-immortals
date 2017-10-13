package com.securboration.immortals.ontology.assertion.types.prescriptive;

import com.securboration.immortals.ontology.assertion.AssertionalStatement;

/**
 * Describes how to work around a condition that is explicitly prohibited (e.g.,
 * what can we do to mitigate using too much of a certain resource?).  E.g.,
 * 
 * <ul>
 * <li>If we exceed an upper location accuracy bound, fix by using a more
 * accurate location provider</li>
 * <li>If we undershoot a location update rate, fix by using a faster but less
 * accurate location provider</li>
 * <li>If network bandwidth constraint is exceeded, fix by reducing the
 * frequency of messages transmitted across the network</li>
 * <li>If network bandwidth constraint is exceeded, fix by reducing the fidelity
 * of messages transmitted across the network</li>
 * </ul>
 * 
 * @author jstaples
 *
 */
public class PrescriptiveAssertionalStatement extends AssertionalStatement {

}
