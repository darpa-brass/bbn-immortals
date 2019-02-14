package com.securboration.immortals.ontology.lang;

import com.securboration.immortals.ontology.core.org.Standard;

/**
 * An abstraction of a programming language
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An abstraction of a programming language  @author jstaples ")
public class ProgrammingLanguage {
    
    /**
     * The name of the language.  E.g., "Java"
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The name of the language.  E.g., \"Java\"")
    private String languageName;
    
    /**
     * The version of the language.  E.g., "Java 8"
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The version of the language.  E.g., \"Java 8\"")
    private String versionTag;
    
    /**
     * The standard for the language.  E.g., C11 aka ISO/IEC 9899:2011
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The standard for the language.  E.g., C11 aka ISO/IEC 9899:2011")
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
