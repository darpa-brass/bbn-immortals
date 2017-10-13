package com.securboration.immortals.ontology.bytecode;

public enum InvocationType {
    
    INTERFACE(185),
    SPECIAL(183),
    STATIC(184),
    VIRTUAL(182),
    ;
    
    private final int type;
    
    private InvocationType(int _type) {type = _type;}

    public static InvocationType getType(int invokeType) {
        for (InvocationType type : InvocationType.values()) {
            if (type.type == invokeType) {
                return type;
            }
        }
        return null;
    }
}
