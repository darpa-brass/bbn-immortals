package com.securboration.immortals.ontology.resources.memory;

import com.securboration.immortals.ontology.resources.PlatformResource;

/**
 * A model of memory
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A model of memory  @author jstaples ")
public class MemoryResource extends PlatformResource {
    
    /**
     * True iff we can read from a memory device
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "True iff we can read from a memory device")
    private boolean canRead;
    
    /**
     * True iff we can write to a memory device
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "True iff we can write to a memory device")
    private boolean canWrite;
    
    /**
     * The unused space available on a memory device
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The unused space available on a memory device")
    private long maxAvailableBytes;

    public boolean isCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean isCanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public long getMaxAvailableBytes() {
        return maxAvailableBytes;
    }

    public void setMaxAvailableBytes(long maxAvailableBytes) {
        this.maxAvailableBytes = maxAvailableBytes;
    }
}
