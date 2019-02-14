package com.securboration.immortals.ontology.analysis;

/**
 *  What impact does performing a specific functionality aspect have on data should it be
 *  transmitted to a separate process? e.g. Encrypting an image has a simple impact on whatever
 *  data being transmitted, the receiving process simply has to decrypt the data. Shrinking an image
 *  doesn't have an impact on the data, the receiving process can still perform operations on it freely.
 */
public enum InterProcessAspectImpact {
    SIMPLE_IMPACT,
    NO_IMPACT,
    LARGE_IMPACT
}
