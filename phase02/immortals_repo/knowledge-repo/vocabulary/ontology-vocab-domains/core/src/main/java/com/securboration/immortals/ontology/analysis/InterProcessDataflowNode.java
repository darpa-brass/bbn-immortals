package com.securboration.immortals.ontology.analysis;

/**
 * A dataflow node that involves the transfer of data between processes (rather
 * than within the same process). For example: IPC, network IO, disk IO
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A dataflow node that involves the transfer of data between processes" +
    " (rather than within the same process). For example: IPC, network IO," +
    " disk IO  @author jstaples ")
public class InterProcessDataflowNode extends DataflowNode {
    
}
