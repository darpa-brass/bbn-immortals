package com.securboration.immortals.swri;

import java.util.HashMap;
import java.util.Map;


public class EvaluationProperties{
    
    public static enum EvaluationPropertyKey{
        
        evalType(
            "testSanity",
            "The sort of evaluation to perform (one of 'testSanity', 'testCanned', 'testSimple', 'testCustom', or 'live').  " +
            "\nThe default value is \"testSanity\" with the other types exposing increasing degrees of freedom (and complexity) to the evaluator.  " +
            "\nA 'sanity' run will never experience evolutionary pressure and " +
            "is therefore useful for testing whether the harness can run " +
            "at all in your environment (a 'testSanity' run requires the bare minimum of configuration parameters).  " +
            "\nA 'testCanned' run accepts a JSON document " +
            "as input that specifies the various schema versions at play--" +
            "the benefit being that there is no chance of " +
            "accidentally misconfiguring the CP because it will have been " +
            "vetted a priori by the performer.  " +
            "\nA 'testSimple' run exposes more freedom to the evaluator in that " +
            "the evaluator can select from several predefined schemas and " +
            "document sets to use for the client, server, and datasource.  " +
            "As in the 'testCanned' case, these configurations should all be valid " +
            "out of the box (the difference is that it takes more work to configure a simple run than a canned one).  " +
            "\nA 'testCustom' run exposes the full evaluation space to evaluators and " +
            "care must therefore be taken to ensure that the configuration is sensible (e.g., " +
            "providing a datasource document set that does not comply with the " +
            "specified datasource schema would be nonsensical).  " +
            "\nA 'live' run is similar to 'testCustom' with the notable " +
            "difference that the harness will retrieve the evaluation " +
            "configuration from OrientDB instead of from configuration " +
            "properties."
            ),
        
        cannedInputJson(
            null,
            "For a 'testCanned' evaluation type, the path to an approved JSON " +
            "configuration file. This value is ignored for other evaluation " +
            "types."
            ),
        
        simpleClientSchemaVersion(
            null,
            "For a 'testSimple' evaluation type, a tag that uniquely identifies " +
            "the schema version used by the client. If evalType is not 'testSimple', " +
            "this value is ignored. " +
            "'v1' = MDL 17, 'v2' = MDL 19."
            ),
        simpleServerSchemaVersion(
            null,
            "For a 'testSimple' evaluation type, a tag that uniquely identifies " +
            "the schema version used by the server. If evalType is not 'testSimple', this value is ignored. " +
            "'v1' = MDL 17, 'v2' = MDL 19."
            ),
        simpleDatasourceSchemaVersion(
            null,
            "For a 'testSimple' evaluation type, a tag that uniquely identifies " +
            "the schema version used by the datasource. If evalType is not 'testSimple', this value is ignored. " +
            "'v1' = MDL 17, 'v2' = MDL 19."
            ),
        essTemplateDir(
            null,
            "For a 'testSimple' or 'testSanity' evaluation type, the path to a template " +
            "cp-ess-min module.  Ignored for other evaluation types."
            ),
        
        customDirContainingDatasourceXsds(
            null,
            "For a 'testCustom' evaluation type, a directory containing .xsd " +
            "files that collectively define the schema version used by the " +
            "datasource. If evalType is not 'testCustom', this value is ignored."
            ),
        customDirContainingClientXsds(
            null,
            "For a 'testCustom' evaluation type, a directory containing .xsd " +
            "files that collectively define the schema version used by the " +
            "client. If evalType is not 'testCustom', this value is ignored."
            ),
        customDirContainingServerXsds(
            null,
            "For a 'testCustom' evaluation type, a directory containing .xsd " +
            "files that collectively define the schema version used by the " +
            "server. If evalType is not 'testCustom', this value is ignored."
            ),
        customDirContainingDatasourceXml(
            null,
            "For a 'testCustom' evaluation type, a directory containing XML " +
            "documents conformant to the schema version used by the " +
            "datasource. If evalType is not 'testCustom', this value is ignored."
            ),
        
        cheatDir(
            null,
            "The path to a directory that is merged into the working eval directory after " +
            "creation.  This can be a powerful way to override certain " +
            "aspects of the evaluation workflow, but use with extreme " +
            "caution."
            ),
        
        fusekiHome(
            System.getenv("FUSEKI_HOME"),
            "A directory containing fuseki 2.3.1.  " +
            "The default value of this property is derived from the " +
            "${FUSEKI_HOME} environment variable, though it may be " +
            "overridden."
            ),
        
        pathToXsdTranslationServiceJar(
            null,
            "The path to the XSD translation service, if it is implemented as a standalone executable JAR.  The default is null."
            ),
        pathToXsdTranslationServicePy(
            null,
            "The path to the XSD translation service, if it is implemented in python/flask.  The default is null."
            ),
        xsdTranslationServicePort(
            "8080",
            "The port used by the XSD translation service."
            ),
        
        pathToInstrumentationJar(
            null,
            "The path to a JAR that instruments bytecode as part of a " +
            "dynamic analysis workflow.  The default is null."
            ),
        
        evalOutputDir(
            "./eval-out",
            "The path to a directory where values emitted during evaluation " +
            "will be dumped."
            ),
        
        uniqueDirPerEval(
            "false",
            "A boolean that if true will force a unique output " +
            "directory (rooted in the eval output dir) to be used per " +
            "evaluation run.  " +
            "This may result in excessive disk use for a large number of runs."
            ),
        
        includeWorkflowDetailsInReport(
            "false",
            "A boolean that if true will result in fine-grained information " +
            "about the evaluation workflow being inserted into the " +
            "evaluation report."
            ),
        
        hostName(
            "localhost",
            "If for some reason you need to refer to this machine by a " +
            "name other than \"localhost\" in an HTTP request, specify it here"
            ),
        
        pythonExecutable(
            "python3.5",
            "The name of a Python interpreter on the current path OR the " +
            "path to a Python interpreter not on the current path."
            ),
        
        javaExecutable(
            "java",
            "The name of a JVM executable on the current path OR the path " +
            "to a JVM executable not on the path."
            ),
        
        domainKnowledge(
            null,
            "A TTL file containing the human-generated knowledge needed to " +
            "perform adaptation."
            ),
        
        ;
        
        final String defaultValue;
        final String id = this.name();
        final String desc;
        
        private EvaluationPropertyKey(
                final String defaultValue,
                final String desc
                ){
            this.defaultValue = defaultValue;
            this.desc = desc;
        }
    }
    
    
    final Map<String,String> map = new HashMap<>();
    
    public EvaluationProperties(String[] args){
        for(int i=0;i<args.length;i+=2){
            map.put(args[i], args[i+1]);
        }
        
        System.out.printf(
            "%d configuration keys defined in %s:\n",
            EvaluationPropertyKey.values().length,
            this.getClass().getName()
            );
        for(EvaluationPropertyKey key:EvaluationPropertyKey.values()){
            String value = map.get(key.id);
            String src = "KV pairs passed to constructor";
            
            if(value == null){
                value = System.getProperty(key.id);
                src = "system property";
            }
            
            if(value == null){
                value = key.defaultValue;
                src = "default value";
            }
            System.out.printf(
                "\t%s = %s (derived from %s) \"%s\"\n", 
                key.id, 
                value,src,
                key.desc
                );
        }
    }
    
    public String get(EvaluationPropertyKey key){
        final String id = key.id;
        
        String value = map.get(id);
        
        if(value == null){
            value = System.getProperty(key.id);
        }
        
        if(value == null){
            value = key.defaultValue;
        }
        
        return value;
    }
    
    public boolean isSet(EvaluationPropertyKey key){
        return get(key) != null;
    }
    
    
    public static void main(String[] args){
//        EvaluationProperties p = new EvaluationProperties(args);
        
        System.out.printf("|key|default|desc|\n");
        System.out.printf("|:---|:---|:---|\n");
        for(EvaluationPropertyKey p:EvaluationPropertyKey.values()){
            String defaultValue = p.defaultValue;
            if(defaultValue != null && defaultValue.length() > 64){
                defaultValue = "[too long to show]";
            }
            
            String desc = p.desc;
            desc = desc.replace("\n", " ");
            
            System.out.printf("|%s|%s|%s|\n", p.name(), defaultValue, desc);
        }
    }
}
