package com.securboration.immortals.ontology.functionality.imageprocessor;

import com.securboration.immortals.ontology.constraint.PropertyImpactType;
import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.dataproperties.GrayScale;
import com.securboration.immortals.ontology.functionality.dataproperties.Pixelated;
import com.securboration.immortals.ontology.functionality.datatype.Image;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.impact.ImpactStatement;
import com.securboration.immortals.ontology.property.impact.PropertyImpact;

@ConceptInstance
public class AspectImageSuperAspect extends DefaultAspectBase {
    
    public AspectImageSuperAspect() {
        super("segment");
        super.setImpactStatements(new ImpactStatement[] {removePixelationImpact(), removeMonochromeImpact()});
        super.setInputs(new Input[] {in()});
        super.setOutputs(new Output[] {out()});
    }

    private static ImpactStatement removePixelationImpact() {
        PropertyImpact p = new PropertyImpact();

        p.setImpactOnProperty(PropertyImpactType.REMOVES);
        p.setImpactedProperty(Pixelated.class);
        return p;
    }

    private static ImpactStatement removeMonochromeImpact() {
        PropertyImpact p = new PropertyImpact();

        p.setImpactOnProperty(PropertyImpactType.REMOVES);
        p.setImpactedProperty(GrayScale.class);
        return p;
    }
    private static Output out(){
        Output o = new Output();
        o.setType(Image.class);

        return o;
    }

    private static Input in(){
        Input i = new Input();
        i.setType(Image.class);

        return i;
    }
    
}
