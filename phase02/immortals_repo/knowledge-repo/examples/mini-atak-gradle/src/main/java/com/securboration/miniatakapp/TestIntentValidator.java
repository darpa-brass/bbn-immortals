package com.securboration.miniatakapp;

import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherDecrypt;
import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherEncrypt;
import mil.darpa.immortals.annotation.dsl.ontology.java.testing.annotation.ProvidedFunctionalityValidationAnnotation;

@ProvidedFunctionalityValidationAnnotation(validatedFunctionality = com.securboration.immortals.ontology.functionality.alg.encryption.Cipher.class,
        validatedAspects = {AspectCipherEncrypt.class, AspectCipherDecrypt.class},
        intents = {"intent-1", "intent-2"})
public class TestIntentValidator {
}
