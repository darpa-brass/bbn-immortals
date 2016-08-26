package com.securboration.immortals.ontology.lang;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.core.org.Standard;
import com.securboration.immortals.uris.Uris.rdfs;

/**
 * An abstraction of a programming language
 * 
 * @author Securboration
 *
 */
@Triple(
    predicateUri = rdfs.comment$,
    objectLiteral = @Literal("an abstraction of a programming language"))
public class ProgrammingLanguage {
    
    @Triple(
        predicateUri = rdfs.comment$,
        objectLiteral = @Literal("The name of the language.  E.g., Java"))
    private String languageName;
    
    @Triple(
        predicateUri = rdfs.comment$,
        objectLiteral = @Literal("The version of the language.  E.g., " +
                "Java 8"))
    private String versionTag;
    
    @Triple(
        predicateUri = rdfs.comment$,
        objectLiteral = @Literal("The standard for the language.  " +
                "E.g., C11 aka ISO/IEC 9899:2011"))
    private Standard programmingLanguageStandard;

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getVersionTag() {
        return versionTag;
    }

    public void setVersionTag(String versionTag) {
        this.versionTag = versionTag;
    }

    
    public Standard getProgrammingLanguageStandard() {
        return programmingLanguageStandard;
    }

    
    public void setProgrammingLanguageStandard(
            Standard programmingLanguageStandard) {
        this.programmingLanguageStandard = programmingLanguageStandard;
    }

}
