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
public class AspectCipherEncrypt extends DefaultAspectBase {
    public AspectCipherEncrypt(){
        super("cipherEncrypt");
        super.setInputs(new Input[]{
                in()});
        super.setOutputs(new Output[]{
                out()});
        super.setInverseAspect(
            AspectCipherDecrypt.class
            );
    }
    
    private static DataProperty encrypt(){
        DataPropertyImpact p = new DataPropertyImpact();
        
        p.setImpact(ImpactType.ADDS);
        p.setPropertyType(Encrypted.class);
        p.setTruthConstraint(TruthConstraint.ALWAYS_TRUE);
        
        return p;
    }
    
    private static DataProperty encrypted(){
        Encrypted c = new Encrypted();
        c.setTruthConstraint(TruthConstraint.ALWAYS_TRUE);
        
        return c;
    }
    
    private static Output out(){
        Output o = new Output();
        o.setType(BinaryData.class);
        o.setProperties(new DataProperty[]{
                encrypted()
        });
        return o;
    }
    
    private static Input in(){
        Input i = new Input();
        i.setType(BinaryData.class);
        i.setProperties(new DataProperty[]{
            encrypt()
        });
        return i;
    }
}