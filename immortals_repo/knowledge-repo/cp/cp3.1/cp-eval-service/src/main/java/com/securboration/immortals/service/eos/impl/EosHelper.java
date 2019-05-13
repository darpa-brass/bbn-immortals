package com.securboration.immortals.service.eos.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import org.apache.commons.lang3.SystemUtils;

import com.securboration.immortals.service.eos.api.types.Document;
import com.securboration.immortals.service.eos.api.types.EvaluationConfiguration;
import com.securboration.immortals.service.eos.api.types.EvaluationRunCommand;
import com.securboration.immortals.service.eos.api.types.EvaluationRunConfiguration;
import com.securboration.immortals.service.eos.api.types.LambdaCommand;
import com.securboration.immortals.service.eos.api.types.WaitForEndpointCommand;
import com.securboration.immortals.service.eos.impl.ZipHelper.ZipArchiveTransformer;
import com.securboration.immortals.swri.EvaluationProperties;
import com.securboration.immortals.swri.EvaluationProperties.EvaluationPropertyKey;
import com.securboration.immortals.swri.eval.FusekiDatasetCudgel;

public class EosHelper {
    
    private static LambdaCommand fail(final String name){
        
        LambdaCommand l = new LambdaCommand();
        l.setAsync(false);
        l.setName("fail (" + name + ")");
        l.setR(new Runnable(){

            @Override
            public void run() {
                throw new RuntimeException(name);
            }
            
        });
        
        return l;
    }
    
    private static LambdaCommand lambda(final String name,final Runnable r){
        
        LambdaCommand l = new LambdaCommand();
        l.setAsync(false);
        l.setName("lambda " + name);
        l.setR(r);
        
        return l;
    }
    
    private static WaitForEndpointCommand waitFor(final String endpoint){
        
        WaitForEndpointCommand c = new WaitForEndpointCommand();
        c.setAsync(false);
        c.setName("wait for " + endpoint);
        c.setEndpointUrl(endpoint);
        
        return c;
    }
    
    private static EvaluationRunCommand cmd(
            final String name,
            final String workingDirRelativeToExtracted, 
            final boolean workingDirAbsolute,
            final boolean async,
            final String... parts
            ) {
        EvaluationRunCommand c = new EvaluationRunCommand();
        c.setAsync(async);
        c.setName(name);
        c.setCommandParts(parts);
        c.setWorkingDir(workingDirRelativeToExtracted);
        c.setWorkingDirAbsolute(workingDirAbsolute);

        return c;
    }
    
    private static EvaluationRunCommand cmd(
            final String name,
            final String workingDirRelativeToExtracted, 
            final boolean async,
            final String... parts
            ) {
        return cmd(name,workingDirRelativeToExtracted,false,async,parts);
    }
    
    private static EvaluationRunCommand cmdCleanup(
            final String name,
            final String workingDirRelativeToExtracted, 
            final boolean async,
            final String... parts
            ) {
        EvaluationRunCommand cmd = cmd(name,workingDirRelativeToExtracted,false,async,parts);
        cmd.setCleanupCommand(true);
        return cmd;
    }
    
    private static EvaluationRunCommand cmdAbsoluteWorkDir(
            final String name,
            final String workingDirRelativeToExtracted, 
            final boolean async,
            final String... parts
            ) {
        return cmd(name,workingDirRelativeToExtracted,true,async,parts);
    }
    
    private static byte[] createCodePackageForEvaluation(
            EvaluationProperties properties,
            EvaluationConfiguration config
            ) throws IOException{
        if(properties.get(EvaluationPropertyKey.essTemplateDir) == null){
            throw new RuntimeException("no ESS template dir property defined");
        }
        
        final File templateDir = new File(properties.get(EvaluationPropertyKey.essTemplateDir));
        
        if(!templateDir.exists()){
            throw new RuntimeException(
                "specified ESS template does not exist: " + templateDir.getAbsolutePath()
                );
        }
        
        final byte[] initialArchive = ZipHelper.zip(templateDir);
        
        final byte[] transformedArchive = ZipHelper.transform(initialArchive, new ZipArchiveTransformer(){
            
            private final Map<String,byte[]> addThese = new HashMap<>();
            
            private byte[] utf8(String s){
                try{
                    return s.getBytes("UTF-8");
                } catch(UnsupportedEncodingException e){
                    throw new RuntimeException(e);
                }
            }
            
            private void add(Collection<Document> docs){
                
                for(Document d:docs){
                    addThese.put(
                        d.getDocumentName(), 
                        utf8(d.getDocumentContent())
                        );
                }
            }

            @Override
            public boolean shouldExclude(ZipEntry e) {
                
                final String name = e.getName().replace("\\", "/");
                
                if(config.getClientSchemaDefinition() != null){//client schema version
                    if(name.startsWith("ess/schema/client")){
                        add(config.getClientSchemaDefinition().getXsds());
                        return true;
                    }
                }
                
                if(config.getServerSchemaDefinition() != null){//server schema version
                    if(name.startsWith("ess/schema/server")){
                        add(config.getServerSchemaDefinition().getXsds());
                        return true;
                    }
                }
                
                if(config.getDatasourceSchemaDefinition() != null){//datasource schema version
                    if(name.startsWith("ess/schema/datasource")){
                        add(config.getDatasourceSchemaDefinition().getXsds());
                        return true;
                    }
                }
                
                if(config.getDatasourceXmls().size() > 0){//datasource documents
                    if(name.startsWith("ess/datasource")){
                        add(config.getDatasourceXmls());
                        return true;
                    }
                }
                
                return false;
            }

            @Override
            public Map<String, byte[]> getNewData() {
                return addThese;
            }
            
        });
        
        return transformedArchive;
    }
    
    private static int randomFreePort(){
        try(ServerSocket s = new ServerSocket(0)){
            return s.getLocalPort();
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }
    
    private static String getGradleExecutable(){
        if(SystemUtils.IS_OS_WINDOWS){
            return "gradle.bat";
        } else {
            return "gradle";
        }
    }
    
    private static String getGradlewExecutable(){
        if(SystemUtils.IS_OS_WINDOWS){
            return "gradlew.bat";
        } else {
            return "gradlew";
        }
    }
    
    private static String[] getBuildArgs(){
        
        final String gradlew = getGradlewExecutable();
        
        if(SystemUtils.IS_OS_WINDOWS){
            return new String[]{"cmd.exe","/C",gradlew,"clean","build","--no-daemon"};
        } else {
            return new String[]{"bash",gradlew,"clean","build","--no-daemon"};
        }
    }
    
    private static String[] getBuildPublishArgs(){
        return merge(getBuildArgs(),new String[]{"publish"});
    }
    
    private static String[] getGradleStopArgs(){
        
        final String gradlew = getGradlewExecutable();
        
        if(SystemUtils.IS_OS_WINDOWS){
            return new String[]{"cmd.exe","/C",gradlew,"--stop"};
        } else {
            return new String[]{"bash",gradlew,"--stop"};
        }
    }
    
    private static String[] getAnalyzeProjectArgs(EvaluationProperties properties){
        
        final String gradlew = getGradlewExecutable();
        
        if(SystemUtils.IS_OS_WINDOWS){
            return merge(
                new String[]{"cmd.exe","/C",gradlew,"--debug","bytecode","--no-daemon"},
                getPluginProperties(properties)
                );
        } else {
            return merge(
                new String[]{"bash",gradlew,"--debug","bytecode","--no-daemon"},
                getPluginProperties(properties)
                );
        }
    }
    
    private static String[] getAnalyzeMineArgs(EvaluationProperties properties){
        
        final String gradlew = getGradlewExecutable();
        
        if(SystemUtils.IS_OS_WINDOWS){
            return merge(
                new String[]{"cmd.exe","/C",gradlew,"--debug","mine","--no-daemon"},
                getPluginProperties(properties)
                );
        } else {
            return merge(
                new String[]{"bash",gradlew,"--debug","mine","--no-daemon"},
                getPluginProperties(properties)
                );
        }
    }
    
    private static String[] getTouchArgs(final String path){
        if(SystemUtils.IS_OS_WINDOWS){
            return new String[]{"cmd.exe","/C","echo asdf >" + path};
        } else {
            return new String[]{"touch",path};
        }
    }
    
    private static String[] getAnalyzeIngestArgs(EvaluationProperties properties){
        
        final String gradlew = getGradlewExecutable();
        
        if(SystemUtils.IS_OS_WINDOWS){
            return merge(
                new String[]{"cmd.exe","/C",gradlew,"--debug","--stacktrace","ingest","--no-daemon"},
                getPluginProperties(properties)
                );
        } else {
            return merge(
                new String[]{"bash",gradlew,"--debug","--stacktrace","ingest","--no-daemon"},
                getPluginProperties(properties)
                );
        }
    }
    
    private static String[] getAnalyzeAdaptArgs(EvaluationProperties properties){
        
        final String gradlew = getGradlewExecutable();
        
        if(SystemUtils.IS_OS_WINDOWS){
            return merge(
                new String[]{"cmd.exe","/C",gradlew,"--debug","--stacktrace","adapt","--no-daemon"},
                getPluginProperties(properties)
                );
        } else {
            return merge(
                new String[]{"bash",gradlew,"--debug","--stacktrace","adapt","--no-daemon"},
                getPluginProperties(properties)
                );
        }
    }
    
    private static String[] getSleepArgs(final int seconds){
        if(SystemUtils.IS_OS_WINDOWS){
//            return new String[]{"timeout","/t",""+duration};//ugh.  Windows.
            
            return new String[]{"ping","-n",""+seconds,"127.0.0.1"};
        } else {
            return new String[]{"sleep",""+seconds};
        }
    }
    
    private static String[] getPluginProperties(
            EvaluationProperties properties
            ){
        final String localhost = properties.get(EvaluationPropertyKey.hostName);
        
        String domainKnowledge;
        try {
            domainKnowledge = new File(properties.get(EvaluationPropertyKey.domainKnowledge)).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        final int xsdstsPort = Integer.parseInt(properties.get(EvaluationPropertyKey.xsdTranslationServicePort));
        
        return new String[]{
                "-PfusekiEndpoint=http://" + localhost + ":3030/ds",
                "-PxsdTranslationEndpoint=http://" + localhost + ":" + xsdstsPort + "/xsdsts",
                "-PgraphDumpFile=./evaluation.ttl",
                "-PdomainKnowledge=" + domainKnowledge
        };
    }
    
    private static String[] merge(String[] a1, String[] a2){
        String[] m = new String[a1.length + a2.length];
        
        System.arraycopy(a1, 0, m, 0, a1.length);
        System.arraycopy(a2, 0, m, a1.length, a2.length);
        
        return m;
    }
    
    public static EvaluationRunConfiguration getEvaluatableConfiguration(
            EvaluationProperties properties,
            EvaluationConfiguration highLevelConfig
            ) throws IOException{
        final int freePortForTesting = randomFreePort();
        
        final String relativePathToCompiledServer = 
                "./server/target/immortals-cp3.1-server-v1-1.0.0.jar";
        
        final String relativePathToCompiledClient = 
                "./client/target/immortals-cp3.1-client-1.0.0.jar";
        final String relativePathToModifiedClient = 
                "./client/target/immortals-cp3.1-client-1.0.0MODIFIED.jar";
        
        final String relativePathToInstrumentedClient = 
                "./client/target/immortals-cp3.1-client-1.0.0-INST.jar";
        
        final String relativePathToClientInputDir = 
                "./datasource/";
        
        final int xsdstsPort = Integer.parseInt(properties.get(EvaluationPropertyKey.xsdTranslationServicePort));
        
        final String localhost = properties.get(EvaluationPropertyKey.hostName);
        
        final String python = properties.get(EvaluationPropertyKey.pythonExecutable);
        final String java = properties.get(EvaluationPropertyKey.javaExecutable);
        
        final String pathToInstrumentationJar = 
                new File(properties.get(EvaluationPropertyKey.pathToInstrumentationJar)).getAbsolutePath();
        
        if(!new File(pathToInstrumentationJar).exists()){
            throw new RuntimeException(
                "dynamic analysis instrumentation JAR \"" + pathToInstrumentationJar + "\" does not exist"
                );
        }
        
        final EvaluationRunConfiguration config = 
                new EvaluationRunConfiguration();
        
        {//create a code package
            config.setCodePackageZipped(
                createCodePackageForEvaluation(
                    properties,
                    highLevelConfig
                    )
                );
        }
            
        {// create an evaluation config
            final String workingDir = "ess";
            
//            {//dump working dir and its contents
//                config.getEvaluationCommands().add(cmd(
//                    "print working dir",
//                    workingDir,
//                    false,
//                    
//                    "pwd"
//                    )
//                );
//                
//                config.getEvaluationCommands().add(cmd(
//                    "dump contents of working dir",
//                    workingDir,
//                    false,
//                    
//                    "ls","-lia"
//                    )
//                );
//            }
            
            if(false){//sanity check the test environment
                config.getEvaluationCommands().add(cmd(
                    "verify gradle is on path",
                    workingDir,
                    false,
                    
                    getGradleExecutable(),"-version"
                    )
                );
                
                config.getEvaluationCommands().add(cmd(
                    "verify java is on path",
                    workingDir,
                    false,
                    
                    java,"-version"
                    )
                );
                
                config.getEvaluationCommands().add(cmd(
                    "verify python is on path",
                    workingDir,
                    false,
                    
                    python,"--version"
                    )
                );
                
//                config.getEvaluationCommands().add(cmd(
//                    "verify flask is on path",
//                    workingDir,
//                    false,
//                    
//                    "flask","--version"
//                    )
//                );
            }
            
            {//start fuseki
                config.getEvaluationCommands().add(cmdAbsoluteWorkDir(
                    "start fuseki",
                    new File(properties.get(EvaluationPropertyKey.fusekiHome)).getAbsolutePath(),
                    true,//NOTE: background process
                    
                    java,"-Xmx8000M","-jar","fuseki-server.jar"
                    )
                );
                
                config.getEvaluationCommands().add(waitFor(
                    "http://" + localhost + ":3030"
                    )
                );
            }
            
//            {//TODO
//                config.getEvaluationCommands().add(fail("intentional"));
//            }//TODO
            
            
            
            {//configure fuseki dataset
                config.getEvaluationCommands().add(
                    lambda("set up fuseki datastore", new Runnable(){

                        @Override
                        public void run() {
                            
                            try {
                                FusekiDatasetCudgel.createDataset(
                                    "http://" + localhost + ":3030", 
                                    "ds"
                                    );
                                
//                                FusekiDatasetCudgel.createDataset(
//                                    "http://" + InetAddress.getLocalHost().getHostName() + ":3030", 
//                                    "ds"
//                                    );
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        
                    })
                    );
            }
            
            
            {//start the XSD translation service
                if(properties.isSet(EvaluationPropertyKey.pathToXsdTranslationServiceJar)){//start the schema translation service (java)
                    config.getEvaluationCommands().add(cmd(
                        "start schema translation service (java)",
                        workingDir,
                        true,//NOTE: background process
                        
                        java,"-Xmx1000M","-jar",new File(properties.get(EvaluationPropertyKey.pathToXsdTranslationServiceJar)).getCanonicalPath()
                        )
                    );
                }
                
                if(properties.isSet(EvaluationPropertyKey.pathToXsdTranslationServicePy)){//start the schema translation service (python)
                    config.getEvaluationCommands().add(cmd(
                        "start schema translation service (python)",
                        workingDir,
                        true,//NOTE: background process
                        
                        python,new File(properties.get(EvaluationPropertyKey.pathToXsdTranslationServicePy)).getCanonicalPath()
                        )
                    );
                }
            }
            
            {//wait for the xsd translation service to start up
                config.getEvaluationCommands().add(waitFor(
                    "http://" + localhost + ":" + xsdstsPort + "/xsdsts/ping"
                    )
                );
            }
            
            {//build the exemplar system
                config.getEvaluationCommands().add(cmd(
                    "build exemplar software system",
                    workingDir,
                    false,
                    
                    getBuildArgs()
                    )
                );
            }
            
            {//build the DFU code module
                config.getEvaluationCommands().add(cmd(
                    "build DFUs",
                    workingDir + "/dfus/all",
                    false,
                    
                    getBuildPublishArgs()
                    )//TODO
                );
            }
            
            {//instrument the exemplar system (client only)
                config.getEvaluationCommands().add(cmd(
                    "instrument client (dynamic analysis I)",
                    workingDir,
                    false,
                    
                    getInstrumentationParts(
                        java,
                        pathToInstrumentationJar,
                        relativePathToCompiledClient,
                        relativePathToInstrumentedClient
                        )
                    )
                );
            }
            
            {//perform dynamic analysis run
                config.getEvaluationCommands().add(cmd(
                    "dynamic analysis (start server)",
                    workingDir,
                    true,//NOTE: async
                    
                    java,
                    "-Dserver.port=" + freePortForTesting,
                    "-jar",relativePathToCompiledServer
                    )
                );
                
                config.getEvaluationCommands().add(waitFor(
                    "http://" + localhost + ":" + freePortForTesting + "/ws/messageListener.wsdl"
                    )
                );
                
                config.getEvaluationCommands().add(cmd(
                    "dynamic analysis (execute client)",
                    workingDir,
                    false,
                    
                    java,
                    "-DREPORT_DIR=reports/dynamicAnalysisRun",
                    "-DMESSAGES_TO_SEND_DIR="+relativePathToClientInputDir,
                    "-DSERVER_ENDPOINT_URL=http://localhost:" + freePortForTesting + "/ws",
                    "-jar",relativePathToInstrumentedClient
                    )
                );
            
                config.getEvaluationCommands().add(cmd(
                    "dynamic analysis (kill server)",
                    workingDir,
                    false,
                    
                    getTouchArgs("killServer.dat")
                    )
                );
                
                config.getEvaluationCommands().add(cmd(
                    "dynamic analysis (sleep for a few seconds...)",
                    workingDir,
                    false,
                    
                    getSleepArgs(2)
                    )
                );
                
                config.getEvaluationCommands().add(cmd(
                    "dynamic analysis (archive dynamic analysis data)",
                    workingDir,
                    false,
                    
                    "tar","-czvf","analysis.tar.gz","./rampartData"
                    )
                );
            }
            
            {//perform baseline run
                config.getEvaluationCommands().add(cmd(
                    "baseline (start server)",
                    workingDir,
                    true,//NOTE: async
                    
                    java,
                    "-Dserver.port=" + freePortForTesting,
                    "-jar",relativePathToCompiledServer
                    )
                );
                
                config.getEvaluationCommands().add(waitFor(
                    "http://" + localhost + ":" + freePortForTesting + "/ws/messageListener.wsdl"
                    )
                );
                
                config.getEvaluationCommands().add(cmd(
                    "baseline (evaluate)",
                    workingDir,
                    false,
                    
                    java,
                    "-DREPORT_DIR=reports/baselineRun",
                    "-DMESSAGES_TO_SEND_DIR="+relativePathToClientInputDir,
                    "-DSERVER_ENDPOINT_URL=http://localhost:" + freePortForTesting + "/ws",
                    "-jar",relativePathToCompiledClient
                    )
                );
            
                config.getEvaluationCommands().add(cmd(
                    "baseline (kill server)",
                    workingDir,
                    false,
                    
                    getTouchArgs("killServer.dat")
                    )
                );
                
                config.getEvaluationCommands().add(cmd(
                    "baseline (sleep for a few seconds...)",
                    workingDir,
                    false,
                    
                    getSleepArgs(2)
                    )
                );
            }
            
            {//analyze
                //analyze bytecode structure
                config.getEvaluationCommands().add(cmd(
                    "analyze project structure",
                    workingDir,
                    false,
                    
                    getAnalyzeProjectArgs(properties)
                    )
                );
                
                //mine for relevant DFUs
                config.getEvaluationCommands().add(cmd(
                    "mine for relevant DFUs",
                    workingDir,
                    false,
                    
                    getAnalyzeMineArgs(properties)
                    )
                );
                
                config.getEvaluationCommands().add(cmd(
                    "ingest analysis artifacts",
                    workingDir,
                    false,
                    
                    getAnalyzeIngestArgs(properties)
                    )
                );
            }
            
            {//adapt
                config.getEvaluationCommands().add(cmd(
                    "adapt (identify problematic conditions and synthesize repair)",
                    workingDir,
                    false,
                    
                    getAnalyzeAdaptArgs(properties)
                    )
                );
            }
            
            {//perform evaluation run
                config.getEvaluationCommands().add(cmd(
                    "evaluate (start server)",
                    workingDir,
                    true,//NOTE: async
                    
                    java,
                    "-Dserver.port=" + freePortForTesting,
                    "-jar",relativePathToCompiledServer
                    )
                );
                
                config.getEvaluationCommands().add(waitFor(
                    "http://" + localhost + ":" + freePortForTesting + "/ws/messageListener.wsdl"
                    )
                );
                
                config.getEvaluationCommands().add(cmd(
                    "evaluate (evaluate)",
                    workingDir,
                    false,
                    
                    java,
                    "-DREPORT_DIR=reports/evaluationRun",
                    "-DMESSAGES_TO_SEND_DIR="+relativePathToClientInputDir,
                    "-DSERVER_ENDPOINT_URL=http://localhost:" + freePortForTesting + "/ws",
                    "-jar",relativePathToModifiedClient
                    )
                );
            
                config.getEvaluationCommands().add(cmd(
                    "evaluate (kill server)",
                    workingDir,
                    false,
                    
                    getTouchArgs("killServer.dat")
                    )
                );
                
                config.getEvaluationCommands().add(cmd(
                    "evaluate (sleep for a few seconds...)",
                    workingDir,
                    false,
                    
                    getSleepArgs(2)
                    )
                );
            }
            
            {//sleep
                config.getEvaluationCommands().add(cmd(
                    "wait a few seconds before cleanup...",
                    workingDir,
                    false,
                    
                    getSleepArgs(5)
                    )
                );
            }
        }
        
        addEarlyTermination(properties,config);
        
        return config;
    }
    
    private static void addEarlyTermination(
            EvaluationProperties properties,
            EvaluationRunConfiguration config
            ){
        final int earlyTerminationIndex = Integer.parseInt(
            properties.get(EvaluationPropertyKey.endEvaluationBeforeStep)
            ) - 1;
        
        if(earlyTerminationIndex < 0){
            return;
        }
        
        //step 1 (index 0
        //step 2 (index 1)
        //...
        //step N (index N-1)
        
        List<EvaluationRunCommand> commands = new ArrayList<>();
        for(int i=0;i<earlyTerminationIndex && i<config.getEvaluationCommands().size();i++){
            commands.add(config.getEvaluationCommands().get(i));
        }
        
        config.getEvaluationCommands().clear();
        config.getEvaluationCommands().addAll(commands);
    }
    
    private static String[] getInstrumentationParts(
            final String java,
            final String pathToInstrumentationJar,
            final String inputJarPath,
            final String instrumentedJarPath
            ){
        return new String[]{
                java,
                
                //begin properties
                "-DInstrumentationConfig.addEventFilterLogic=false",
                "-DInstrumentationConfig.instrumentorClassNames=com.securboration.rampart.inst.transformers.DynamicAnalysisTransformer",
                "-DInstrumentationConfig.inputJarPath=" + inputJarPath,
                "-DInstrumentationConfig.classpathEntries=" + inputJarPath,
                "-DInstrumentationConfig.outputJarPath=" + instrumentedJarPath,
                "-DInstrumentationConfig.overwriteOutputArtifact=true",
                "-DInstrumentationConfig.includeControlFlowPaths=true",

                "-DRampartConfig.rampartDataDir=./rampartData",

                "-DDynamicAnalysisConfig.dumpDaDictionaryTo=client.dict",
                "-DDynamicAnalysisConfig.daBlacklist=net/sf/saxon,org/springframework/boot/,org/springframework/util/,org/springframework/asm/,org/springframework/cglib/,org/springframework/core/annotation/,org/springframework/core/ResolvableType,org/apache/commons/logging,org/apache/logging,org/slf4j,ch/qos",
                "-DDynamicAnalysisConfig.entrypointPrefix=enter@ com/securboration/client/ClientRunner clientAction",
                //end properties
                
                "-jar",pathToInstrumentationJar,"console"
        };
    }
    
    public static void slayProcess(
            final Process p,
            final Long delayBetweenAttemptsMillis,
            final Integer maxRetries,
            final Long maxTimeMillis
            ){
        final long start = System.currentTimeMillis();
        
        int count = 0;
        while(true){
            
            try{
                if(delayBetweenAttemptsMillis != null){
                    Thread.sleep(delayBetweenAttemptsMillis);
                }
                
                p.destroyForcibly();
            } catch(Exception e){
                e.printStackTrace();
            }
            
            {//loop post updates
                count++;
            }
            
            {//exit checks
                if(!p.isAlive()){
                    break;
                }
                
                final long elapsed = System.currentTimeMillis() - start;
                
                if(maxTimeMillis != null && elapsed > maxTimeMillis){
                    break;
                }
                
                if(maxRetries != null && count > maxRetries){
                    break;
                }
            }
        }
    }

}


