package com.securboration.immortals.ontology.lang;

/**
 * The result of compiling and then packaging source code.
 * 
 * An aggregate compiled code unit is a single compiled artifact derived from 
 * multiple source code files (e.g., a .jar or .dll).  It may stand alone or
 * be used as a dependency by other artifacts.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "The result of compiling and then packaging source code.  An aggregate" +
    " compiled code unit is a single compiled artifact derived from " +
    " multiple source code files (e.g., a .jar or .dll).  It may stand" +
    " alone or be used as a dependency by other artifacts.  @author" +
    " jstaples ")
public class AggregateCompiledCodeUnit extends CompiledCodeUnit {

    /**
     * The source files from which the aggregate code unit was compiled
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The source files from which the aggregate code unit was compiled")
    private SourceFile[] sourceFiles;

    /**
     * The binary form of the aggregate code unit
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The binary form of the aggregate code unit")
    private byte[] compiledForm;

    public AggregateCompiledCodeUnit() {
    }

    public SourceFile[] getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(SourceFile[] sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public byte[] getCompiledForm() {
        return compiledForm;
    }

    public void setCompiledForm(byte[] compiledForm) {
        this.compiledForm = compiledForm;
    }

}
