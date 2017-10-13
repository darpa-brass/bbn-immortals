package com.securboration.immortals.ontology.sourcecode;

/**
 * Model of a source file that exists on a file system
 * 
 * @author Securboration
 *
 */
public class SourceFile {

    /**
     * The path to the source code on this machine
     */
    private String fileSystemPath;
    
    /**
     * A URL for accessing the source code in a repository
     */
    private String repositoryPath;
    
    /**
     * The contents of the source file
     */
    private String source;
    

    public String getFileSystemPath() {
        return fileSystemPath;
    }

    public void setFileSystemPath(String fileSystemPath) {
        this.fileSystemPath = fileSystemPath;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
}
