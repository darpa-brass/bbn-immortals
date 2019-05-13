package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.resources.xml.StructuredDocument;

public class StructuredDocumentVersionCriterion extends CriterionStatement {

    private Class<? extends StructuredDocument> structuredDocument;
    private StructuredDocumentCriterionType structuredDocumentCriterionType;


    public Class<? extends StructuredDocument> getStructuredDocument() {
        return structuredDocument;
    }

    public void setStructuredDocument(Class<? extends StructuredDocument> structuredDocument) {
        this.structuredDocument = structuredDocument;
    }

    public StructuredDocumentCriterionType getStructuredDocumentCriterionType() {
        return structuredDocumentCriterionType;
    }

    public void setStructuredDocumentCriterionType(StructuredDocumentCriterionType structuredDocumentCriterionType) {
        this.structuredDocumentCriterionType = structuredDocumentCriterionType;
    }
}
