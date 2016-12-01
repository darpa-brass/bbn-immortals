package com.securboration.immortals.ontology.resources;

/**
 * A logical volume may span multiple partitions
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A logical volume may span multiple partitions  @author jstaples ")
public class Volume extends DiskResource {

    /**
     * The partitions the volume spans
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The partitions the volume spans")
    private Partition[] partitionsSpanned;

    public Partition[] getPartitionsSpanned() {
        return partitionsSpanned;
    }

    public void setPartitionsSpanned(Partition[] partitionsSpanned) {
        this.partitionsSpanned = partitionsSpanned;
    }

}
