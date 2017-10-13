package com.securboration.immortals.ontology.bytecode;

/**
 * Anything concrete a class might contain (e.g., a field). Note that this is a
 * recursive definition because classes can contain nested classes
 * 
 * @author Securboration
 *
 */
public class ClassStructure {
    
    /**
     * The class containing this feature
     */
    private AClass owner;
    
    /**
     * The modifiers for this structure. For example: public, private,
     * synchronized, volatile...
     */
    private Modifier[] modifiers;
    
    /**
     * Annotations on this structure visible after compilation
     */
    private AnAnnotation[] annotations;

    public AClass getOwner() {
        return owner;
    }

    public void setOwner(AClass owner) {
        this.owner = owner;
    }

    public Modifier[] getModifiers() {
        return modifiers;
    }

    public void setModifiers(Modifier[] modifiers) {
        this.modifiers = modifiers;
    }

    public AnAnnotation[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(AnAnnotation[] annotations) {
        this.annotations = annotations;
    }
    
}
