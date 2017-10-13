package com.securboration.immortals.ontology.resources.gps;

/**
 * Enumerates mechanisms for encoding bits in a carrier signal
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Enumerates mechanisms for encoding bits in a carrier signal  @author" +
    " jstaples ")
public enum SpectrumKeying {
    
    /**
     * Bi-Phase Shift Keying
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Bi-Phase Shift Keying")
    BPSK,
    
    /**
     * Quad-Phase Shift Keying
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Quad-Phase Shift Keying")
    QPSK,
    
    /**
     * Multiplexed Binary Offset Carrier ,
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Multiplexed Binary Offset Carrier ,")
    MBOC,
    
    /**
     * Frequency Shift Keying
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Frequency Shift Keying")
    FSK,
    
    //...
    
}
