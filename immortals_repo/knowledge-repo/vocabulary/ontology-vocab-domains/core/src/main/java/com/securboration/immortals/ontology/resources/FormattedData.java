package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

public class FormattedData extends DataType {

    private String format;

    private String formatVersion;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormatVersion() {
        return formatVersion;
    }

    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }
}
