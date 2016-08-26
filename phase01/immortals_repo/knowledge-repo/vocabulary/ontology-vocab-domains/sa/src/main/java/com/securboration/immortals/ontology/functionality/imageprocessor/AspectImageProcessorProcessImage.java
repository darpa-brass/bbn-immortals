package com.securboration.immortals.ontology.functionality.imageprocessor;

import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.datatype.Image;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class AspectImageProcessorProcessImage extends DefaultAspectBase{
    public AspectImageProcessorProcessImage(){
        super("processImage");
        super.setOutputs(new Output[]{imageOut()});
        super.setInputs(new Input[]{imageIn()});
    }
    
    private static Output imageOut(){
        Output o = new Output();
        o.setType(Image.class);
        return o;
    }
    
    private static Input imageIn(){
        Input i = new Input();
        i.setType(Image.class);
        return i;
    }
}