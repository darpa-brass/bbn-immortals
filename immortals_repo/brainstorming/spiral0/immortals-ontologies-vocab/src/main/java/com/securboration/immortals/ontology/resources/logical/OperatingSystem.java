package com.securboration.immortals.ontology.resources.logical;

import com.securboration.immortals.ontology.resources.FileSystemResource;
import com.securboration.immortals.ontology.resources.PartitionedDiskResource;

/**
 * Simple model of an operating system
 * 
 * @author Securboration
 *
 */
public class OperatingSystem extends LogicalResource {
    
    /**
     * E.g., Android
     */
    private String osType;
    
    /**
     * E.g., 6.0_Marshmallow
     */
    private String versionTag;

    /**
     * The OS's file system
     */
    private FileSystemResource fileSystem;
    
    /**
     * Libraries made available by the OS
     */
    private LogicalResource[] systemLibraries;
    
    /**
     * The device's disk storage
     */
    private PartitionedDiskResource osPartition;

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getVersionTag() {
        return versionTag;
    }

    public void setVersionTag(String versionTag) {
        this.versionTag = versionTag;
    }

    public FileSystemResource getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(FileSystemResource fileSystem) {
        this.fileSystem = fileSystem;
    }

    public LogicalResource[] getSystemLibraries() {
        return systemLibraries;
    }

    public void setSystemLibraries(LogicalResource[] systemLibraries) {
        this.systemLibraries = systemLibraries;
    }

    public PartitionedDiskResource getOsPartition() {
        return osPartition;
    }

    public void setOsPartition(PartitionedDiskResource osPartition) {
        this.osPartition = osPartition;
    }
    
    
}
