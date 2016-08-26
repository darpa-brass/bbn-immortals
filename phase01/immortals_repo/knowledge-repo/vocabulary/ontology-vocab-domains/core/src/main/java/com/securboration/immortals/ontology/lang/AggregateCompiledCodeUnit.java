package com.securboration.immortals.ontology.lang;

import com.securboration.immortals.ontology.annotations.RdfsComment;
import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

/**
 * The result of compiling source code
 * 
 * @author Securboration
 *
 */
@RdfsComment(
        "an aggregate compiled code unit is a single compiled " +
        "artifact derived from multiple source code files (e.g., a .jar or " +
        ".dll)"
        )
public class AggregateCompiledCodeUnit extends CompiledCodeUnit {

    @Triple(
        predicateUri = rdfs.comment$,
        objectLiteral = @Literal("the source files from which the" +
                "aggregate code unit was compiled"))
    private SourceFile[] sourceFiles;

    @Triple(
        predicateUri = rdfs.comment$,
        objectLiteral = @Literal("the binary form of the aggregate" +
                "code unit"))
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
