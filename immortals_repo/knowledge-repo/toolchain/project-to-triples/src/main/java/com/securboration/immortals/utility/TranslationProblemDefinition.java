package com.securboration.immortals.utility;

public class TranslationProblemDefinition {

    private DocumentSet srcSchema;

    private DocumentSet dstSchema;

    public TranslationProblemDefinition(){

    }


    public DocumentSet getSrcSchema() {
        return srcSchema;
    }


    public void setSrcSchema(DocumentSet srcSchema) {
        this.srcSchema = srcSchema;
    }


    public DocumentSet getDstSchema() {
        return dstSchema;
    }


    public void setDstSchema(DocumentSet dstSchema) {
        this.dstSchema = dstSchema;
    }

}

