package com.securboration.immortals.ontology.functionality.compression;

import com.securboration.immortals.ontology.constraint.PropertyImpactType;
import com.securboration.immortals.ontology.core.TruthConstraint;
import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.dataproperties.Compressed;
import com.securboration.immortals.ontology.functionality.dataproperties.DataPropertyImpact;
import com.securboration.immortals.ontology.functionality.dataproperties.Entropy;
import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.impact.ImpactStatement;
import com.securboration.immortals.ontology.property.impact.PropertyImpact;

@ConceptInstance
public class AspectDeflate extends DefaultAspectBase{
    public AspectDeflate(){
        super("deflate");
        super.setOutputs(new Output[]{out()});
        super.setInputs(new Input[]{in()});
        super.setInverseAspect(
            AspectInflate.class
            );
        super.setImpactStatements(new ImpactStatement[] {deflateImpact()});
    }
    
    private static DataProperty addCompressedProperty(){
        DataPropertyImpact p = new DataPropertyImpact();
        
        p.setImpact(ImpactType.ADDS);
        p.setPropertyType(Compressed.class);
        p.setTruthConstraint(TruthConstraint.ALWAYS_TRUE);
        
        return p;
    }
    
    private static ImpactStatement deflateImpact() {
        PropertyImpact p = new PropertyImpact();

        p.setImpactOnProperty(PropertyImpactType.ADDS);
        p.setImpactedProperty(Compressed.class);
        return p;
    }
    
    private static DataProperty addEntropyProperty(){
        DataPropertyImpact p = new DataPropertyImpact();
        
        p.setImpact(ImpactType.INCREASES);
        p.setPropertyType(Entropy.class);
        p.setTruthConstraint(TruthConstraint.ALWAYS_TRUE);
        
        return p;
    }
    
    private static DataProperty deflated(){
        Compressed c = new Compressed();
        c.setTruthConstraint(TruthConstraint.ALWAYS_TRUE);
        
        return c;
    }
    
    private static Output out(){
        Output o = new Output();
        o.setType(BinaryData.class);
        o.setProperties(new DataProperty[]{
                deflated()
        });
        return o;
    }
    
    private static Input in(){
        Input i = new Input();
        i.setType(BinaryData.class);
        i.setProperties(new DataProperty[]{
            addCompressedProperty(),
            addEntropyProperty()
        });
        return i;
    }
}