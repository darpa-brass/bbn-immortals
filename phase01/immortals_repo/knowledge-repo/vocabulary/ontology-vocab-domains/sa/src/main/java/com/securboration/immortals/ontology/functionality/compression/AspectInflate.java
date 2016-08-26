package com.securboration.immortals.ontology.functionality.compression;

import com.securboration.immortals.ontology.core.TruthConstraint;
import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.dataproperties.Compressed;
import com.securboration.immortals.ontology.functionality.dataproperties.DataPropertyImpact;
import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class AspectInflate extends DefaultAspectBase{
    public AspectInflate(){
        super("inflate");
        super.setOutputs(new Output[]{out()});
        super.setInputs(new Input[]{in()});
        super.setInverseAspect(
            AspectDeflate.class
            );
    }
    
    private static DataProperty inflate(){
        DataPropertyImpact p = new DataPropertyImpact();
        
        p.setImpact(ImpactType.REMOVES);
        p.setPropertyType(Compressed.class);
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
                inflate()
        });
        return i;
    }
}