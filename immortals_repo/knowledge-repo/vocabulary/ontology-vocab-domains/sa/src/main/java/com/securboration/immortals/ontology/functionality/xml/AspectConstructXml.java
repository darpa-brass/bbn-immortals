package com.securboration.immortals.ontology.functionality.xml;

import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.resources.xml.XmlInstance;

public class AspectConstructXml extends DefaultAspectBase {
    public AspectConstructXml(){
        super("xml-construct");
        super.setInputs(new Input[]{});
        super.setOutputs(new Output[]{
                Output.getOutput(XmlInstance.class)
        });
    }
}
