package com.securboration.immortals.ontology.resources;

/**
 * A file on a file system
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A file on a file system  @author jstaples ")
public class FileResource extends FileSystemResource {
    
    /**
     * The path to the file
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The path to the file")
    private String absolutePath;
    
    /**
     * The file's access permissions
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The file's access permissions")
    private FilePermission[] permission;

    
    public String getAbsolutePath() {
        return absolutePath;
    }

    
    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    
    public FilePermission[] getPermission() {
        return permission;
    }

    
    public void setPermission(FilePermission[] permission) {
        this.permission = permission;
    }
    
}
