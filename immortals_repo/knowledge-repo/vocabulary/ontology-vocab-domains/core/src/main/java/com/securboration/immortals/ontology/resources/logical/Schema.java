package com.securboration.immortals.ontology.resources.logical;

/**
 * A schema describing how data is ordered
 *
 *
 * @author cendicott
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
        "A schema describing how data is ordered   @author cendicott ")
public class Schema extends LogicalResource {
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
