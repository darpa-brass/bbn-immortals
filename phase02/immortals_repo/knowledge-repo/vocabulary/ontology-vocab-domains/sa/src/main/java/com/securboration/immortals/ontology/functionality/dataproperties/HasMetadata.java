package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.functionality.dataformat.DataFormat;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.functionality.datatype.DataType;

/**
 * Indicates that a piece of data contains embedded metadata (e.g., an image
 * might contain an embedded GPS location and timestamp)
 * 
 * @author Securboration
 *
 */
public class HasMetadata extends DataProperty {
    
    private Class<? extends DataType>[] metadataContent;
    private Class<? extends DataFormat> metadataFormat;

    public Class<? extends DataType>[] getMetadataContent() {
        return metadataContent;
    }

    public void setMetadataContent(Class<? extends DataType>[] metadataContent) {
        this.metadataContent = metadataContent;
    }

    public Class<? extends DataFormat> getMetadataFormat() {
        return metadataFormat;
    }

    public void setMetadataFormat(Class<? extends DataFormat> metadataFormat) {
        this.metadataFormat = metadataFormat;
    }
    
}
