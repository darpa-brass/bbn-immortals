package com.securboration.immortals.ontology.functionality;

/**
 * Where should this aspect be injected in order to be applied correctly?
 * e.g. AspectCipherEncrypt should be injected just before it is communicated
 * over the network
 */
public enum AspectInjectionRelation {
    JUST_BEFORE,
    JUST_AFTER,
    DURING
}
