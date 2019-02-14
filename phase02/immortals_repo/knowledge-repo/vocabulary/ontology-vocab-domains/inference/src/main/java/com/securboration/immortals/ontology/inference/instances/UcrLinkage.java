package com.securboration.immortals.ontology.inference.instances;

import com.securboration.immortals.ontology.annotations.RdfsComment;
import com.securboration.immortals.ontology.inference.ConstructQuery;
import com.securboration.immortals.ontology.inference.InferenceRule;
import com.securboration.immortals.ontology.inference.InferenceRules;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
@RdfsComment("Links UCR analysis results to Securboration analysis results")
public class UcrLinkage extends InferenceRules {
    
    public UcrLinkage(){
        this.setIterateUntilNoNewTriples(false);
        this.setMaxIterations(1);
        
        this.getRules().add(getLinkageRule());
    }
    
    private InferenceRule getLinkageRule(){
        ConstructQuery q = new ConstructQuery();
        q.setQueryText(
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" +
                "CONSTRUCT { " +
                "    ?dfuInstance IMMoRTALS:hasResourceDependencies ?resourceType " +
                "} WHERE {\r\n" +
                "    GRAPH <?GRAPH?> { \r\n" +
                "        ?dfuInstance a <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance> .\r\n" +
                "        ?dfuInstance IMMoRTALS:hasFunctionalAspects ?functionalAspect .\r\n" +
                "        ?functionalAspect IMMoRTALS:hasMethodPointer ?pointer .\r\n" +
                "    } .\r\n" +
                "    GRAPH <?GRAPH?> { \r\n" +
                "        ?report a <http://darpa.mil/immortals/ontology/r2.0.0/analysis#AnalysisReport> .\r\n" +
                "        ?report IMMoRTALS:hasMeasurementProfile ?measurementProfile .\r\n" +
                "        ?measurementProfile IMMoRTALS:hasCodeUnit ?codeUnit .\r\n" +
                "        ?codeUnit IMMoRTALS:hasPointerString ?pointer . \r\n" +
                "        ?report IMMoRTALS:hasDiscoveredDependency ?dependencyAssertion .\r\n" +
                "        ?dependencyAssertion IMMoRTALS:hasDependency ?resourceType .\r\n" +
                "    } .\r\n" +
                "}"
                );
        
        InferenceRule rule = new InferenceRule();
        
        rule.setHumanReadableDesc(
            "links UCR's DFU-agnostic analysis to the DFU-aware analysis " +
            "performed by the IMMoRTALS Gradle plugin"
            );
        
        rule.setForwardInferenceRule(q);
        
        return rule;
    }
    
}


