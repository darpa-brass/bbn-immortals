package com.securboration.immortals.ontology.inference.instances;

import com.securboration.immortals.ontology.annotations.RdfsComment;
import com.securboration.immortals.ontology.inference.ConstructQuery;
import com.securboration.immortals.ontology.inference.InferenceRule;
import com.securboration.immortals.ontology.inference.InferenceRules;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
@RdfsComment(
    "Kludge to link ATAK/MARTI structural analysis (produced by machines) " +
    "to dataflow analysis (currently produced by humans)"
    )
public class AtakMartiDataflowLinker extends InferenceRules {
    
    public AtakMartiDataflowLinker(){
        this.setIterateUntilNoNewTriples(false);
        this.setMaxIterations(1);
        
        this.getRules().add(getAtakLinkageRule());
        this.getRules().add(getMartiLinkageRule());
    }

    private InferenceRule getMartiLinkageRule(){
        ConstructQuery q = new ConstructQuery();
        q.setQueryText(
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" +
                "PREFIX IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\r\n" +
                "PREFIX IMMoRTALS_bytecode_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode/analysis#>\r\n" +
                "PREFIX IMMoRTALS_com_securboration_immortals_exampleDataflows: <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/exampleDataflows#>\r\n" +
                "CONSTRUCT { \r\n" +
                "    ?methodInvoke IMMoRTALS:hasSemanticLink IMMoRTALS_com_securboration_immortals_exampleDataflows:AtakMartiDuplexFlows.CreateSocket \r\n" +
                "} WHERE {\r\n" +
                "    GRAPH <?GRAPH?> { \r\n" +
                "        ?invoker a IMMoRTALS_bytecode:AMethod .\r\n" +
                "        ?invoker IMMoRTALS:hasMethodName \"run\" .\r\n" +
                "        ?invoker IMMoRTALS:hasMethodDesc \"()V\" .\r\n" +
                "        ?invoker IMMoRTALS:hasOwner ?invokerOwner .\r\n" +
                "        ?invokerOwner a IMMoRTALS_bytecode:AClass .\r\n" +
                "        ?invokerOwner IMMoRTALS:hasClassName \"com/bbn/marti/immortals/net/tcp/TcpSocketServer$1\" .\r\n" +
                "        ?invoker IMMoRTALS:hasInterestingInstructions ?methodInvoke .\r\n" +
                "        ?methodInvoke a IMMoRTALS_bytecode_analysis:MethodCall .\r\n" +
                "        ?methodInvoke IMMoRTALS:hasCalledMethodName \"accept\" .\r\n" +
                "        ?methodInvoke IMMoRTALS:hasCalledMethodDesc \"()Ljava/net/Socket;\" .\r\n" +
                "    } .\r\n" +
                "}"
                );
        
        InferenceRule rule = new InferenceRule();
        
        rule.setHumanReadableDesc(
            "Anchors nodes in a structural decomposition of ATAK/MARTI to " +
            "semantically interesting entrypoints in a dataflow betweeen " +
            "them.  This is necessary because for now the dataflows are " +
            "generated manually (tooling would be able to perform this " +
            "linkage automatically)."
            );
        
        rule.setForwardInferenceRule(q);
        
        return rule;
    }
    
    
    private InferenceRule getAtakLinkageRule(){
        ConstructQuery q = new ConstructQuery();
        q.setQueryText(
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" +
                "PREFIX IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\r\n" +
                "PREFIX IMMoRTALS_bytecode_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode/analysis#>\r\n" +
                "PREFIX IMMoRTALS_com_securboration_immortals_exampleDataflows: <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/exampleDataflows#>\r\n" +
                "CONSTRUCT { \r\n" +
                "    ?methodInvoke IMMoRTALS:hasSemanticLink IMMoRTALS_com_securboration_immortals_exampleDataflows:AtakMartiDuplexFlows.CreateAtakStreams \r\n" +
                "} WHERE {\r\n" +
                "    GRAPH <?GRAPH?> { \r\n" +
                "        ?invoker a IMMoRTALS_bytecode:AMethod .\r\n" +
                "        ?invoker IMMoRTALS:hasMethodName \"run\" .\r\n" +
                "        ?invoker IMMoRTALS:hasMethodDesc \"()V\" .\r\n" +
                "        ?invoker IMMoRTALS:hasOwner ?invokerOwner .\r\n" +
                "        ?invokerOwner a IMMoRTALS_bytecode:AClass .\r\n" +
                "        ?invokerOwner IMMoRTALS:hasClassName \"com/bbn/ataklite/net/Dispatcher\" .\r\n" +
                "        ?invoker IMMoRTALS:hasInterestingInstructions ?methodInvoke .\r\n" +
                "        ?methodInvoke a IMMoRTALS_bytecode_analysis:MethodCall .\r\n" +
                "        ?methodInvoke IMMoRTALS:hasCalledMethodName \"open\" .\r\n" +
                "        ?methodInvoke IMMoRTALS:hasCalledMethodDesc \"()Ljava/nio/channels/SocketChannel;\" .\r\n" +
                "    } .\r\n" +
                "}"
                );
        
        InferenceRule rule = new InferenceRule();
        
        rule.setHumanReadableDesc(
            "Anchors nodes in a structural decomposition of ATAK/MARTI to " +
            "semantically interesting entrypoints in a dataflow betweeen " +
            "them.  This is necessary because for now the dataflows are " +
            "generated manually (tooling would be able to perform this " +
            "linkage automatically)."
            );
        
        rule.setForwardInferenceRule(q);
        
        return rule;
    }
    
}


