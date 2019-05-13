package com.securboration.client.utility;


import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.datatype.Text;
import com.securboration.immortals.ontology.functionality.imagecapture.FileHandle;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.impact.ImpactStatement;

@ConceptInstance
public class RetrieveFileResourceAspect extends FunctionalAspect {
    public RetrieveFileResourceAspect() {
        super();
        this.setAspectId("applyXsltTransformation");
        this.setImpactStatements(new ImpactStatement[] {});
        this.setInputs(new Input[] {getFileInput()});
        this.setOutputs(new Output[] {getStringOutput()});
    }

    public Input getFileInput() {
       Input fileInput = new Input();
       fileInput.setType(FileHandle.class);
       return fileInput;
    }

    public Output getStringOutput() {
        Output stringOutput = new Output();
        stringOutput.setType(Text.class);
        return stringOutput;
    }
}
