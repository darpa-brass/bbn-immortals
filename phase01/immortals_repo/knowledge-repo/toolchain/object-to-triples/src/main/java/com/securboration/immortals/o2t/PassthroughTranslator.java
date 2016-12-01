package com.securboration.immortals.o2t;

public class PassthroughTranslator implements ObjectTranslator{
    @Override
    public Object translate(Object input) {
        return input;
    }
}
