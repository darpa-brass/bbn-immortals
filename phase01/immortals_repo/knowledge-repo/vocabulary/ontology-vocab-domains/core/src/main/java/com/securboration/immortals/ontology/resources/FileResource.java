package com.securboration.immortals.ontology.resources;

/**
 * A file on a file system
 * 
 * 
 * @author Securboration
 *
 */
public class FileResource extends FileSystemResource {
    
    private String absolutePath;
    
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
