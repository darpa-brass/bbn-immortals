package com.securboration.immortals.ontology.functionality.imagecapture;

import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.functionality.datatype.Image;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class AspectReadImage extends DefaultAspectBase{
    public AspectReadImage(){
        super("readImage");
        super.setOutputs(new Output[]{out()});
        super.setInputs(new Input[]{in()});
    }
    
    private static Output out(){
        Output o = new Output();
        o.setType(Image.class);
        o.setProperties(new DataProperty[]{});
        return o;
    }
    
    private static Input in(){
        Input i = new Input();
        i.setType(FileHandle.class);
        i.setProperties(new DataProperty[]{});
        return i;
    }
}