package com.securboration.immortals.ontology.profiling;

/**
 * An (incomplete) enumeration of the types of metrics that may be gathered
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An (incomplete) enumeration of the types of metrics that may be" +
    " gathered  @author jstaples ")
public enum MetricType {
    
    CPU_CYCLES_WALL,
    CPU_CYCLES_USER,
    CPU_CYCLES_SYSTEM,
    
    BYTES_READ,
    BYTES_WRITTEN,

}
