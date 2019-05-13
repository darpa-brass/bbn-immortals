package com.securboration.dfus.loader.resource;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;

import com.securboration.immortals.ontology.functionality.datatype.Text;
import com.securboration.immortals.ontology.functionality.imagecapture.FileHandle;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.Property;


@ConceptInstance
public class ApplyFileResourceLoaderAspect extends FunctionalAspect {

    public ApplyFileResourceLoaderAspect() {
        super();
        this.setAspectId("applyFileResourceLoaderAspect");
        this.setInputs(new Input[] {in()});
        this.setOutputs(new Output[] {out()});


    }

    private static Input in() {
        Input i = new Input();

        i.setFlowName("name of resource");
        i.setType(FileHandle.class);

        return i;
    }

    private static Output out() {
        Output o = new Output();
        o.setFlowName("string file");
        o.setType(Text.class);

        return o;
    }

}
