package com.securboration.immortals.ontology.functionality.imagecapture;

import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.functionality.datatype.Image;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class AspectWriteImage extends DefaultAspectBase{
    public AspectWriteImage(){
        super("writeImage");
        super.setOutputs(null);
        super.setInputs(new Input[]{image(),outFile()});
    }
    
    private static Input outFile(){
        Input i = new Input();
        i.setType(FileHandle.class);
        i.setProperties(new DataProperty[]{});
        return i;
    }
    
    private static Input image(){
        Input i = new Input();
        i.setType(Image.class);
        i.setProperties(new DataProperty[]{});
        return i;
    }
}