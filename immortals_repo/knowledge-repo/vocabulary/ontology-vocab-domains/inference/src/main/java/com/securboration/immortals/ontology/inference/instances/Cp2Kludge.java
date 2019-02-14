package com.securboration.immortals.ontology.inference.instances;

import com.securboration.immortals.ontology.annotations.RdfsComment;
import com.securboration.immortals.ontology.inference.ConstructQuery;
import com.securboration.immortals.ontology.inference.InferenceRule;
import com.securboration.immortals.ontology.inference.InferenceRules;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
@RdfsComment(
    "Kludge to get CP2 working"
    )
public class Cp2Kludge extends InferenceRules {
    
    public Cp2Kludge(){
        this.setIterateUntilNoNewTriples(false);
        this.setMaxIterations(1);
        
       // this.getRules().add(getMartiLinkageRule());
    }

    private InferenceRule getMartiLinkageRule(){
        ConstructQuery q = new ConstructQuery();
        q.setQueryText(
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" +
                "PREFIX IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode#>\r\n" +
                "PREFIX IMMoRTALS_bytecode_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/bytecode/analysis#>\r\n" +
                "PREFIX IMMoRTALS_com_securboration_immortals_exampleDataflows: <http://darpa.mil/immortals/ontology/r2.0.0/com/securboration/immortals/exampleDataflows#>\r\n" +
                "PREFIX IMMoRTALS_java_project: <http://darpa.mil/immortals/ontology/r2.0.0/java/project#>\r\n" +
                "CONSTRUCT { \r\n" +
                "    ?martiJavaProject IMMoRTALS:hasAndroidApp ?atakAndroidApp \r\n" +
                "} WHERE {\r\n" +
                "    GRAPH <?GRAPH?> { \r\n" +
                "        ?martiJavaProject a IMMoRTALS_java_project:JavaProject .\r\n" +
                "        ?martiJavaProject IMMoRTALS:hasCoordinate ?martiCoord .\r\n" +
                "        ?martiCoord IMMoRTALS:hasArtifactId  \"Marti\" .\r\n" +
                
                "        ?atakJavaProject a IMMoRTALS_java_project:JavaProject .\r\n" +
                "        ?atakJavaProject IMMoRTALS:hasCoordinate ?atakCoord .\r\n" +
                "        ?atakCoord IMMoRTALS:hasArtifactId  \"ATAKLite\" .\r\n" +
                "        ?atakJavaProject IMMoRTALS:hasAndroidApp ?atakAndroidApp .\r\n" +
                "    } .\r\n" +
                "}"
                );
        
        /*
        IMMoRTALS_java_project:JavaProject-c30d34bc-ad1a-46d4-8539-78572b1c0f4d IMMoRTALS:hasAndroidApp      IMMoRTALS_java_android:AndroidApp-bed1129f-e930-4444-a455-c211344a326c ;

        IMMoRTALS_java_android:AndroidApp-bed1129f-e930-4444-a455-c211344a326c
        a                           IMMoRTALS_java_android:AndroidApp ;
        IMMoRTALS:hasPathToUberJar  "C:\\Users\\CharlesEndicott\\AppData\\Local\\Android\\Sdk\\platforms\\android-21\\android.jar" .
         */
        
        InferenceRule rule = new InferenceRule();
        
        rule.setHumanReadableDesc(
            "Soot 'splodes when exposed to the JVM's rt.jar and Android's " +
            "android.jar simultaneously.  This is a janky workaround."
            );
        
        rule.setForwardInferenceRule(q);
        
        return rule;
    }
    
}


