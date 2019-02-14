package com.securboration.immortals.ontology.functionality.logger;

import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.datatype.Text;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class AspectLog extends DefaultAspectBase{
    public AspectLog(){
        super("log");
        super.setOutputs(new Output[]{});
        super.setInputs(new Input[]{in()});
    }
    
    private static Input in(){
        Input i = new Input();
        i.setType(Text.class);
        return i;
    }
}