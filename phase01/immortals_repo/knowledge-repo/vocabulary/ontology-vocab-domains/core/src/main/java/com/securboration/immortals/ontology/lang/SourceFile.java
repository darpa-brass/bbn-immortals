package com.securboration.immortals.ontology.lang;

/**
 * Model of a source file that exists on a file system
 * 
 * @author Securboration
 *
 */
public class SourceFile extends CodeUnit {

    /**
     * The path to the source code on this machine
     */
    private String fileSystemPath;
    
    /**
     * A URL for accessing the source code in a repository like SVN
     */
    private String repositoryPath;
    
    /**
     * The contents of the source file
     */
    private String source;
    
    /**
     * A language model for the source file
     */
    private ProgrammingLanguage languageModel;
    

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

    public ProgrammingLanguage getLanguageModel() {
        return languageModel;
    }

    public void setLanguageModel(ProgrammingLanguage languageModel) {
        this.languageModel = languageModel;
    }
    
}
