package com.securboration.immortals.ontology.resources;

/**
 * A partitioned disk resource is a physical disk that has been partitioned into
 * logical segments
 * 
 * @author Securboration
 *
 */
public class PartitionedDiskResource extends IOResource {

    /**
     * The partitions
     */
    private Partition[] partitions;

    public Partition[] getPartitions() {
        return partitions;
    }

    public void setPartitions(Partition[] partitions) {
        this.partitions = partitions;
    }

}
