package com.securboration.immortals.ontology.functionality.alg.encryption;

import com.securboration.immortals.ontology.core.TruthConstraint;
import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.dataproperties.DataPropertyImpact;
import com.securboration.immortals.ontology.functionality.dataproperties.Encrypted;
import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class AspectCipherDecrypt extends DefaultAspectBase {
    public AspectCipherDecrypt(){
        super("cipherDecrypt");
        super.setInputs(new Input[]{
                in()});
        super.setOutputs(new Output[]{
                out()});
        super.setInverseAspect(
            AspectCipherEncrypt.class
            );
    }
    
    private static DataProperty decrypt(){
        DataPropertyImpact p = new DataPropertyImpact();
        
        p.setImpact(ImpactType.REMOVES);
        p.setPropertyType(Encrypted.class);
        p.setTruthConstraint(TruthConstraint.ALWAYS_TRUE);
        
        return p;
    }
    
    private static Output out(){
        Output o = new Output();
        o.setType(BinaryData.class);
        return o;
    }
    
    private static Input in(){
        Input i = new Input();
        i.setType(BinaryData.class);
        i.setProperties(new DataProperty[]{
            decrypt()
        });
        return i;
    }
}