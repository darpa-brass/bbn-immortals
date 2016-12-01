package com.securboration.immortals.ontology.dfu.instance;

import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

/**
 * A recipe is a sequence of steps that are required to be performed to
 * configure some software component for use. For example, a cipher requires the
 * key size, algorithm, and padding scheme to be specified before it can be
 * used.
 * 
 * For now, a recipe is modeled as simply a string, but this could be exploded
 * into its own rich modeling space in the future.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A recipe is a sequence of steps that are required to be performed to" +
    " configure some software component for use. For example, a cipher" +
    " requires the key size, algorithm, and padding scheme to be specified" +
    " before it can be used.  For now, a recipe is modeled as simply a" +
    " string, but this could be exploded into its own rich modeling space" +
    " in the future.  @author jstaples ")
@GenerateAnnotation
public class Recipe {
    
    private String recipe;

    
    public String getRecipe() {
        return recipe;
    }
    
    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }
    
}
