package com.securboration.immortals.ontology.lang;

/**
 * Model of a source file that exists on a file system
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Model of a source file that exists on a file system  @author jstaples ")
public class SourceFile extends CodeUnit {

    /**
     * The path to the source code on this machine
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The path to the source code on this machine")
    private String fileSystemPath;
    
    /**
     * A URL for accessing the source code in a repository like SVN
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A URL for accessing the source code in a repository like SVN")
    private String repositoryPath;
    
    /**
     * The contents of the source file
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The contents of the source file")
    private String source;
    
    /**
     * A language model for the source file
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A language model for the source file")
    private ProgrammingLanguage languageModel;

    /**
     * The name of the source file
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
            "The name of the source file")
    private String fileName;
    
    private String fullyQualifiedName;
    
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullQualifiedName) {
        this.fullyQualifiedName = fullQualifiedName;
    }
}
