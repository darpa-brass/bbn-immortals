package com.securboration.immortals.ontology.resources.logical;

import com.securboration.immortals.ontology.resources.FileSystemResource;
import com.securboration.immortals.ontology.resources.PartitionedDiskResource;

/**
 * Simple model of an operating system
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Simple model of an operating system  @author jstaples ")
public class OperatingSystem extends LogicalResource {
    
    /**
     * The type of an operating system.  E.g., Android
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The type of an operating system.  E.g., Android")
    private String osType;
    
    /**
     * The version of an operating system.  E.g., 6.0_Marshmallow
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The version of an operating system.  E.g., 6.0_Marshmallow")
    private String versionTag;

    /**
     * The file system of an operating system
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The file system of an operating system")
    private FileSystemResource fileSystem;
    
    /**
     * Libraries available to an operating system
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Libraries available to an operating system")
    private LogicalResource[] systemLibraries;
    
    /**
     * The disk storage available to an operating system
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The disk storage available to an operating system")
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
