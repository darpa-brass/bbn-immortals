package com.securboration.immortals.ontology.resources;

/**
 * A logical volume may span multiple partitions
 * 
 * @author Securboration
 *
 */
public class Volume extends DiskResource {

    /**
     * The partitions the volume spans
     */
    private Partition[] partitionsSpanned;

    public Partition[] getPartitionsSpanned() {
        return partitionsSpanned;
    }

    public void setPartitionsSpanned(Partition[] partitionsSpanned) {
        this.partitionsSpanned = partitionsSpanned;
    }

}
