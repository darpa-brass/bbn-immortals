package com.securboration.immortals.ontology.dfu;

/**
 * A container for an array of many DFUs
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A container for an array of many DFUs  @author jstaples ")
public class Dfus {

    /**
     * The dfus
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment("The dfus")
    private Dfu[] dfus;

    public Dfu[] getDfus() {
        return dfus;
    }

    public void setDfus(Dfu[] dfus) {
        this.dfus = dfus;
    }

}
