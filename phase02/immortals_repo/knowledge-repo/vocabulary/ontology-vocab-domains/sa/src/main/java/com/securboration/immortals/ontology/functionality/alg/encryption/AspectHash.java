package com.securboration.immortals.ontology.functionality.alg.encryption;

import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class AspectHash extends DefaultAspectBase {
    public AspectHash(){
        super("hash");
        
        Input in = new Input();
        in.setType(BinaryData.class);
        
        Output out = new Output();
        out.setType(BinaryData.class);
        
        super.setInputs(new Input[]{
                in
                });
        super.setOutputs(new Output[]{
                out
                });
    }
}