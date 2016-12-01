package com.securboration.immortals.ontology.analysis;

/**
 * A dataflow node that performs Disk IO. This is true disk IO, not to be
 * confused with between generic file operations (which may or may not
 * correspond to actual disk writes)
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A dataflow node that performs Disk IO. This is true disk IO, not to be" +
    " confused with between generic file operations (which may or may not" +
    " correspond to actual disk writes)  @author jstaples ")
public class DiskIODataflowNode extends InterProcessDataflowNode {

}
