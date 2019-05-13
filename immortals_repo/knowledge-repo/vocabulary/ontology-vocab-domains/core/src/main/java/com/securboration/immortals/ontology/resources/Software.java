package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

/**
 * A device
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A device  @author jstaples ")
public class Software extends PlatformResource {
    
    private String applicationName;

    private DataType[] dataInSoftware;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public DataType[] getDataInSoftware() {
        return dataInSoftware;
    }

    public void setDataInSoftware(DataType[] dataInSoftware) {
        this.dataInSoftware = dataInSoftware;
    }
}
