package com.securboration.immortals.ontology.resources;

/**
 * A partitioned disk resource is a physical disk that has been partitioned into
 * logical segments
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A partitioned disk resource is a physical disk that has been" +
    " partitioned into logical segments  @author jstaples ")
public class PartitionedDiskResource extends IOResource {

    /**
     * The partitions on the disk
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The partitions on the disk")
    private Partition[] partitions;

    public Partition[] getPartitions() {
        return partitions;
    }

    public void setPartitions(Partition[] partitions) {
        this.partitions = partitions;
    }

}
