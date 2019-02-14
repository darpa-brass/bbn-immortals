package com.securboration.immortals.constraint;

public enum TransformationState {

    PRE_ANALYSIS,
    PRE_TRANSFORMATION,
    POST_TRANSFORMATION;

    public TransformationState next() {
        return values()[ordinal() + 1];
    }
}
