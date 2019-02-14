package com.securboration.immortals.ontology;

import com.securboration.immortals.ontology.annotations.RdfsComment;
import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

@GenerateAnnotation
public class FunctionalityExemplarDriver {
    
    @RdfsComment(value = "What function are we being taught?")
    private String templateTaught;
    
    private String[] instructions;
    
    private String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String[] getInstructions() {
        return instructions;
    }

    public void setInstructions(String[] instructions) {
        this.instructions = instructions;
    }

    public String getTemplateTaught() {
        return templateTaught;
    }

    public void setTemplateTaught(String templateTaught) {
        this.templateTaught = templateTaught;
    }
}
