package com.securboration.immortals.ontology.resources.memory;

import com.securboration.immortals.ontology.resources.PlatformResource;

/**
 * Memory
 * 
 * 
 * @author Securboration
 *
 */
public class MemoryResource extends PlatformResource {
    
    /**
     * True iff we can read this device
     */
    private boolean canRead;
    
    /**
     * True iff we can write this device
     */
    private boolean canWrite;
    
    /**
     * The space available for writing
     */
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
