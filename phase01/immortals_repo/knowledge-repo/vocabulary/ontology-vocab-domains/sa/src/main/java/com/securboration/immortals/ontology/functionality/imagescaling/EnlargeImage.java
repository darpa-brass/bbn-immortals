package com.securboration.immortals.ontology.functionality.imagescaling;

import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.compression.LossyTransformation;
import com.securboration.immortals.ontology.functionality.dataproperties.DataPropertyImpact;
import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.functionality.datatype.Image;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.Property;

@ConceptInstance
public class EnlargeImage extends DefaultAspectBase{
    public EnlargeImage(){
        super("enlarge");
        super.setOutputs(new Output[]{imageOut()});
        super.setInputs(new Input[]{imageIn(),desiredSizeIn()});
    }
    
    private static DataProperty getImpactOfShrinkOnNumPixels(){
        DataPropertyImpact p = new DataPropertyImpact();
        
        p.setImpact(ImpactType.INCREASES);
        p.setPropertyType(NumberOfPixels.class);
        
        return p;
    }
    
    private static Output imageOut(){
        Output o = new Output();
        o.setType(Image.class);
        o.setProperties(new Property[]{
                getImpactOfShrinkOnNumPixels(),
                new LossyTransformation()
                }
        );
        return o;
    }
    
    private static Input imageIn(){
        Input i = new Input();
        i.setType(Image.class);
        return i;
    }
    
    private static Input desiredSizeIn(){
        Input i = new Input();
        i.setType(ImageScalingFactorType.class);
        return i;
    }
}