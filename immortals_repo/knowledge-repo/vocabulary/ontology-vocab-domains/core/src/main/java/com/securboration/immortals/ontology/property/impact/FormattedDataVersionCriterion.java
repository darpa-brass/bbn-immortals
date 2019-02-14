package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.resources.FormattedData;

public class FormattedDataVersionCriterion extends CriterionStatement {

    private Class<? extends FormattedData> formattedData;
    private FormattedDataCriterionType formattedDataCriterionType;

    public Class<? extends FormattedData> getFormattedData() {
        return formattedData;
    }

    public void setFormattedData(Class<? extends FormattedData> formattedData) {
        this.formattedData = formattedData;
    }

    public FormattedDataCriterionType getFormattedDataCriterionType() {
        return formattedDataCriterionType;
    }

    public void setFormattedDataCriterionType(FormattedDataCriterionType formattedDataCriterionType) {
        this.formattedDataCriterionType = formattedDataCriterionType;
    }
}
