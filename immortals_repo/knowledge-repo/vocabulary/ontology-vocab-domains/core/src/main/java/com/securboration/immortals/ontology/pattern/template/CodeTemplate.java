package com.securboration.immortals.ontology.pattern.template;

import com.securboration.immortals.ontology.pattern.spec.LibraryFunctionalAspectSpec;

/**
 * A code template is an artifact that contains code chunks that should be 
 * injected into an application in order to fulfill some functional role.
 * 
 * @author jstaples
 *
 */
public class CodeTemplate {
    
    /**
     * The unparsed surface form of the template
     */
    private String templateSurfaceForm;
    
    /**
     * A logical decomposition of the template derived from parsing its surface
     * form
     */
    private LibraryFunctionalAspectSpec templateSpec;
    
    public String getTemplateSurfaceForm() {
        return templateSurfaceForm;
    }
    
    public void setTemplateSurfaceForm(String templateSurfaceForm) {
        this.templateSurfaceForm = templateSurfaceForm;
    }

    
    public LibraryFunctionalAspectSpec getTemplateSpec() {
        return templateSpec;
    }

    
    public void setTemplateSpec(LibraryFunctionalAspectSpec templateSpec) {
        this.templateSpec = templateSpec;
    }

}
