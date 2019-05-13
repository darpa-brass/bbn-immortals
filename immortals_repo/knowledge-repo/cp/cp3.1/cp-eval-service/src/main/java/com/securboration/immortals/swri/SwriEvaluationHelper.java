package com.securboration.immortals.swri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securboration.immortals.bcd.BytecodeDiff;
import com.securboration.immortals.bridge.BridgePojo;
import com.securboration.immortals.service.eos.api.types.EosType;
import com.securboration.immortals.service.eos.api.types.EvaluationConfiguration;
import com.securboration.immortals.service.eos.api.types.EvaluationMetricCategory;
import com.securboration.immortals.service.eos.api.types.EvaluationMetricCostOfAdaptation;
import com.securboration.immortals.service.eos.api.types.EvaluationReport;
import com.securboration.immortals.service.eos.api.types.EvaluationRunCommand;
import com.securboration.immortals.service.eos.api.types.EvaluationRunCommandResult;
import com.securboration.immortals.service.eos.api.types.EvaluationRunStatus;
import com.securboration.immortals.service.eos.api.types.EvaluationStatusReport;
import com.securboration.immortals.service.eos.client.EosClient;
import com.securboration.immortals.service.eos.impl.ZipHelper;
import com.securboration.immortals.swri.EvaluationPackageBuilder.MdlSchemaVersion;
import com.securboration.immortals.swri.EvaluationProperties.EvaluationPropertyKey;
import com.securboration.immortals.swri.eval.EvaluationReportDiff;
import com.securboration.immortals.swri.eval.Out;

import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;
import mil.darpa.immortals.schemaevolution.TerminalStatus;

public class SwriEvaluationHelper {
    
    public static void evaluate(
            final EvaluationProperties p,
            final ChallengeProblemBridge b
            ) throws Exception{
        final String endpoint;{
            final String localhost = InetAddress.getLocalHost().getHostName();
            
            final String portProperty = System.getProperty("server.port");
            final int port = portProperty == null ? 8080 : Integer.parseInt(portProperty);
            
            endpoint = String.format("http://%s:%d/eos",localhost,port);
        }
        
        final EosClient client = new EosClient(endpoint);
        
        final SwriEvaluationHelper eval = new SwriEvaluationHelper(p);
        
        eval.evaluateInternal(
            b,
            client
            );
        
        eval.shutdown();
    }
    
    
    
    
    private final String uuid = UUID.randomUUID().toString();
    
    private final long startTime = System.currentTimeMillis();
    
    private final EvaluationProperties props;
    
    private SwriEvaluationHelper(final EvaluationProperties props){
        this.props = props;
    }
    
    private void shutdown(){
        System.exit(0);
    }
    
    private void println(String format, Object...args){
        Out.println(System.out,"EVAL", format, args);
    }
    
    private void evaluateInternal(
            final ChallengeProblemBridge bridge,
            final EosClient client
            ) throws Exception{
        
        EvaluationStatusReport lastEvalStatus = null;
        try{
            {
                bridge.init();
            }
            
            {
                bridge.storeLargeBinaryData(
                    uuid, 
                    "evaluation-config.json", 
                    bridge.getConfigurationJson(uuid).getBytes(StandardCharsets.UTF_8)
                    );
            }
            
            println("verifying connectivity to EOS endpoint @ " + client.getServerUrl());
            {
                final long serverTime = client.ping();
                final long delta = System.currentTimeMillis() - serverTime;
                println("\tping delta = %dms", delta);
            }
    
            
            println("executing evaluation workflow");
            {
                final String json = 
//                        FileUtils.readFileToString(new File("test2.json"));
                        bridge.getConfigurationJson(uuid);
                
                final EvaluationConfiguration config;
                if(props.get(EvaluationPropertyKey.evalType).equals("live")){
                    System.out.println("retrieved the following configuration from OrientDB: " + json);//TODO
                    
                    final BridgePojo pojo = EosType.fromJson(json, BridgePojo.class);
                    
                    config = translate(pojo);
                } else {
                    config = EosType.fromJson(json, EvaluationConfiguration.class);
                }
                
                try{
                    println(
                        "Evaluating with config:\n\tdatasource: %s\n\tclient: %s\n\tserver: %s", 
                        config.getDatasourceSchemaDefinition().getSchemaId(),
                        config.getClientSchemaDefinition().getSchemaId(),
                        config.getServerSchemaDefinition().getSchemaId()
                        );
                } catch(Exception e){
                    throw new RuntimeException(
                        "malformed evaluation configuration",
                        e
                        );
                }
    
                
                // kick off an evaluation run
                final String contextId = client.evaluate(config);
                println("\tcreated context %s", contextId);
                
                
                {// poll until status is complete or error
                    String currentTask = "<none>";
                    String currentName = "";
                    String lastStatus = null;
                    Long lastStatusReportTime = null;
                    
                    final Set<Integer> reported = new HashSet<>();
                    
                    boolean stop = false;
                    while (!stop) {
                        final EvaluationStatusReport status =
                            client.status(contextId);
                        lastEvalStatus = status;
                        
                        for(int i=0;i<status.getCommandResults().size()-1;i++){//print those status changes that occur faster than the refresh interval
                            final EvaluationRunCommandResult r = status.getCommandResults().get(i);
                            final EvaluationRunCommand c = r.getCommand();
                            if(r.getReturnValue() != null && r.getReturnValue() != 0){
                                throw new RuntimeException(
                                    "nonzero return value from " + c.getName()
                                    );
                            }
                            
                            if(reported.contains(i)){
                                continue;
                            }reported.add(i);
                            
                            println(String.format(
                                "\t\tstatus is %s (step %d): \"%20s\" %s", 
                                status.getStatus(),
                                i+1,
                                c.getName(),
                                c.getCommandParts() == null ? "" : Arrays.asList(c.getCommandParts())
                                ));
                        }
                        if(status.getCommandResults().size() > 0){
                            
                            EvaluationRunCommandResult current = 
                                    status.getCommandResults().get(
                                        status.getCommandResults().size() - 1
                                        );
                            
                            if(current.getCommand() == null || current.getCommand().getCommandParts() == null){
                                currentTask = "";
                            } else {
                                currentTask = Arrays.asList(
                                    current.getCommand().getCommandParts()
                                    ).toString();
                            }
                            
                            currentName = current.getCommand().getName();
                        }
    
                        final String statusString = String.format(
                            "\t\tstatus is %s (step %d): \"%20s\" %s", 
                            status.getStatus(),
                            status.getCommandResults().size(),
                            currentName,
                            currentTask
                            );
                        
                        {//print the current status, but not too often
                            final boolean longSinceLastReport = 
                                    lastStatusReportTime == null 
                                    || 
                                    (System.currentTimeMillis() - lastStatusReportTime) > 10000L;
                                    
                            final boolean reportChanged = 
                                    lastStatus == null 
                                    || 
                                    !statusString.equals(lastStatus);
                            
                            if(longSinceLastReport || reportChanged){
                                println(statusString);
                                lastStatus = statusString;
                                lastStatusReportTime = System.currentTimeMillis();
                            }
                        }
    
                        if (status.getStatus() == EvaluationRunStatus.UNKNOWN) {
                            throw new RuntimeException("did not expect status " + status.getStatus());
                        } else if (status.getStatus() == EvaluationRunStatus.IN_PROGRESS) {
                            // do nothing
                        } else {
                            stop = true;
                            
                            postProcess(
                                bridge,
                                lastEvalStatus,
                                null
                                );
                        }
    
                        Thread.sleep(1000L);//poll sleep time
                    }
                } 
            }
        } catch(Throwable t) {
            postProcess(
                bridge,
                lastEvalStatus,
                t
                );
            
            throw t;
        }
    }
    
    private final String trace(Throwable t){
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        
        t.printStackTrace(new PrintWriter(new PrintStream(o)));
        
        return new String(o.toByteArray(),StandardCharsets.UTF_8);
    }
    
    private void postProcessError(
            final ChallengeProblemBridge bridge,
            final byte[] evalZip,
            final EvaluationStatusReport status,
            final Throwable caughtDuringEval
            ) throws Exception{
        
        if(caughtDuringEval != null){
            bridge.postError(
                uuid, 
                "an exception was caught during evaluation " + uuid + ": " + caughtDuringEval.getMessage(), 
                trace(caughtDuringEval)
                );
            
            return;
        }
        
        if(status == null){
            bridge.postError(
                uuid, 
                "the status was null after evaluation " + uuid + " but no exception was caught",
                "[not specified]"
                );
            
            return;
        }
        
        if(status.getCommandResults() == null || status.getCommandResults().isEmpty()){
            bridge.postError(
                uuid, 
                "no commands were evaluated and no exception was caught during evaluation " + uuid, 
                "[not specified]"
                );
            return;
        }
        
        boolean foundError = false;
        for(EvaluationRunCommandResult r:status.getCommandResults()){
            foundError = true;
            final String statusString = String.format(
                "%s during evaluation step %d: %s", 
                status.getStatus(),
                status.getCommandResults().size(),
                r.getCommand().getName()
                );
            
            bridge.postError(
                uuid, 
                "an error occurred during evaluation run " + uuid + ": " + statusString, 
                "[consult the evaluation archive for more details]"
                );
        }
        
        if(foundError == false){
            bridge.postError(
                uuid, 
                "unable to determine the command(s) that caused the error during evaluation run " + uuid,
                "[consult the evaluation archive for more details]"
                );
        }
    }
    
    private void postProcess(
            final ChallengeProblemBridge bridge,
            final EvaluationStatusReport status,
            final Throwable caughtDuringEval
            ) throws Exception{
        
        final byte[] evalZip = status == null ? null : status.getEvaluationReportZip();
        
        if(status != null && evalZip != null){//store the evaluation data
            bridge.storeLargeBinaryData(
                uuid, 
                "evaluationArchive.zip", 
                evalZip
                );
            
            FileUtils.writeByteArrayToFile(
                new File(props.get(EvaluationPropertyKey.evalOutputDir),"evaluationArchive.zip"), 
                evalZip
                );
        }
        
        final boolean errorCondition = 
                caughtDuringEval != null 
                || 
                status == null 
                || 
                status.getStatus() == null 
                || 
                status.getStatus() != EvaluationRunStatus.COMPLETED_OK
                ;
        if(errorCondition){
            postProcessError(bridge,evalZip,status,caughtDuringEval);
            return;
        }
        
        //if we're here, we know the evaluation run completed successfully
        
        {
            //immortals-bytecode-diff
            final byte[] original = ZipHelper.getZipEntry(evalZip, "ess/ess/client/target/immortals-cp3.1-client-1.0.0.jar");
            final byte[] modified = ZipHelper.getZipEntry(evalZip, "ess/ess/client/target/immortals-cp3.1-client-1.0.0MODIFIED.jar");
            
            final File originalJar = new File("./tmp/immortals-cp3.1-ess.jar");
            final File adaptedJar = new File("./tmp/immortals-cp3.1-ess.REPAIRED.jar");
            try{
                FileUtils.writeByteArrayToFile(originalJar, original);
                FileUtils.writeByteArrayToFile(adaptedJar, modified);
                
                final String diff = BytecodeDiff.diffJars(originalJar, adaptedJar);
                
                bridge.storeLargeBinaryData(
                    uuid, 
                    "bytecode.diff", 
                    diff.getBytes(StandardCharsets.UTF_8)
                    );
            } finally {
                FileUtils.forceDelete(originalJar);
                FileUtils.forceDelete(adaptedJar);
            }
        }
        
        final EvaluationReport report = new EvaluationReport();
        
        {//status rollup section
            report.setEvaluationStatus(status.getStatus());
        }
        
        if(Boolean.parseBoolean(props.get(EvaluationPropertyKey.includeWorkflowDetailsInReport))){//details section
            report.setEvaluationDetails((EvaluationStatusReport)status.cp());
            
            report.getEvaluationDetails().setEvaluationReportZip(null);//this can be quite large
            for(EvaluationRunCommandResult r:report.getEvaluationDetails().getCommandResults()){
                r.getCommand().setCommandParts(null);
                r.getCommand().setWorkingDir(null);
                r.setStderrPath(null);
                r.setStdoutPath(null);
            }
        }
        
        {//evaluation metrics section
            EvaluationMetricCategory category = new EvaluationMetricCategory();
            category.setCategoryDesc("metrics relevant to evaluation workflow");
            
            category.getMetricsForCategory().add(
                new EvaluationMetricCostOfAdaptation(
                    "eval context ID", 
                    "a unique identifier for the evaluation context",
                    uuid
                    )
                );
            
            category.getMetricsForCategory().add(
                new EvaluationMetricCostOfAdaptation(
                    "eval start time", 
                    "the starting time of the evaluation run",
                    new Date(startTime).toString()
                    )
                );
            
            final long endTime = System.currentTimeMillis();
            
            category.getMetricsForCategory().add(
                new EvaluationMetricCostOfAdaptation(
                    "eval end time", 
                    "the ending time of the evaluation run",
                    new Date(endTime).toString()
                    )
                );
            
            final long elapsed = endTime - startTime;
            
            category.getMetricsForCategory().add(
                new EvaluationMetricCostOfAdaptation(
                    "eval elapsed millis", 
                    "the duration of the evaluation run in milliseconds",
                    ""+elapsed
                    )
                );
            
            category.getMetricsForCategory().add(
                new EvaluationMetricCostOfAdaptation(
                    "eval # cores", 
                    "the number of cores available to the JVM performing evaluation",
                    ""+Runtime.getRuntime().availableProcessors()
                    )
                );
            
            category.getMetricsForCategory().add(
                new EvaluationMetricCostOfAdaptation(
                    "eval max memory", 
                    "the memory available to the JVM performing evaluation",
                    ""+Runtime.getRuntime().maxMemory()
                    )
                );
            
            report.getCategories().add(category);
        }
        
        {//add a diff showing the qualitative impacts of adaptation
            final EvaluationMetricCategory diff = EvaluationReportDiff.diff(
                ZipHelper.getZipEntry(
                    evalZip, 
                    "ess/ess/reports/baselineRun/report.dat"
                    ), 
                ZipHelper.getZipEntry(
                    evalZip, 
                    "ess/ess/reports/evaluationRun/report.dat"
                    )
                );
            
            report.getCategories().add(diff);
        }
        
        {//add analysis metrics
            final byte[] entry = ZipHelper.getZipEntry(
                evalZip, 
                "ess/ess/immortals-gradle-plugin-output/AnalysisReport.txt"
                );
            
            report.getCategories().add(
                getAnalysisMetrics(
                    new String(entry,StandardCharsets.UTF_8)
                    )
                );
        }
        
        if(false){//add mock sections
            
            report.getCategories().add(getMockEvalSection(
                "# triples",
                "# triples provided",
                "# triples derived after analysis",
                "# triples derived after mining",
                "# triples derived after constraint analysis",
                "# triples derived through inference rule execution",
                "# triples derived elsewhere during eval workflow",
                "# triples in adaptation graph (should equal sum of above)"
                ));
            
            
            report.getCategories().add(getMockEvalSection(
                "timings",
                "# millis required to perform bytecode task",
                "# millis required to perform mine task",
                "# millis required to perform ingest task",
                "# millis required to perform adapt task"
                ));
            
            report.getCategories().add(getMockEvalSection(
                "adaptation process",
                "# problematic dataflows with discovered constraint violations",
                "# problematic dataflows resolved"
                ));
            
            report.getCategories().add(getMockEvalSection(
                "code impact of adaptation",
                "# existing classes modified",
                "# existing methods modified",
                "% of anterior code surface modified",
                "# new classes synthesized",
                "# new methods synthesized",
                "% of posterior code surface synthesized",
                "# new libraries added"
                ));
        }
        
        final String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(report);
        
        System.out.println(json);
        
        bridge.postResultsJson(
            uuid,
            getTerminalStatus(status,evalZip),
            json
            );
    }
    
    private static TerminalStatus getTerminalStatus(
            final EvaluationStatusReport evalStatus,
            final byte[] evalZip
            ){
        //TODO: dive into the emitted zip archive to determine the correct 
        //      fine-grained status
        
//        return TerminalStatus.AdaptationNotRequired;
//        return TerminalStatus.AdaptationPartiallySuccessful;
//        return TerminalStatus.AdaptationUnsuccessful;
//        return TerminalStatus.PerturbationInputInvalid;
        
        return TerminalStatus.AdaptationSuccessful;
    }
    
    private static EvaluationMetricCategory getMockEvalSection(String category,String...fields){
        EvaluationMetricCategory c = new EvaluationMetricCategory();
        
        c.setCategoryDesc(category);
        for(String field:fields){
            EvaluationMetricCostOfAdaptation metric = new EvaluationMetricCostOfAdaptation();
            metric.setMetricType(field);
            metric.setMetricDesc("*** this is currently a mocked metric ***");
            
            if(field.startsWith("#")){
                metric.setMetricValue(""+new Random().nextInt(1024*1024));
            } else {
                metric.setMetricValue(String.format("%1.2f", new Random().nextDouble()));
            }
            
            c.getMetricsForCategory().add(metric);
        }
        
        return c;
    }
    
    private EvaluationConfiguration translate(
            BridgePojo swriInput
            ) throws IOException{
        
        final MdlSchemaVersion clientVersion = 
                MdlSchemaVersion.get(swriInput.getInitialMdlVersion());
        final MdlSchemaVersion datasourceVersion = 
                clientVersion;//TODO: this is no longer a degree of freedom
        
        if(swriInput.getUpdatedMdlSchema() != null) {
            //the case where a custom schema is defined
            final String serverSchemaId = swriInput.getUpdatedMdlVersion();
            final String serverSchemaDefinition = swriInput.getUpdatedMdlSchema();
            
            return EvaluationPackageBuilder.createEvaluationPackageSimple(
                new File(props.get(EvaluationPropertyKey.essTemplateDir)), 
                clientVersion, 
                serverSchemaId,serverSchemaDefinition, 
                datasourceVersion,
                null
                );
        } else if(swriInput.getUpdatedMdlVersion() != null){
            //the case where a canned schema is used
            final MdlSchemaVersion serverVersion = 
                    MdlSchemaVersion.get(swriInput.getUpdatedMdlVersion());
            
            return EvaluationPackageBuilder.createEvaluationPackageSimple(
                new File(props.get(EvaluationPropertyKey.essTemplateDir)), 
                clientVersion, 
                serverVersion, 
                datasourceVersion, 
                null
                );
        } else {
            throw new RuntimeException("updatedMdlSchema and updatedMdlVersion fields cannot both be null (but are)");
        }
    }
    
    private static EvaluationMetricCategory getAnalysisMetrics(
            final String metricsFileContent
            ){
        final Map<String,String> map = new LinkedHashMap<>();
        
        {
            final String[] lines = metricsFileContent.split("\\r?\\n");
            for(String line:lines){
                if(!line.contains(" -> ")){
                    continue;
                }
                
                line = line.replace("===", "").trim();
                
                String[] parts = line.split(" -> ");
                
                final String key = parts[0].trim();
                final String value = parts.length == 1 ? "" : parts[1].trim();
                
                map.put(key, value);
            }
        }
        
        final EvaluationMetricCategory c = new EvaluationMetricCategory();
        c.setCategoryDesc("analysis metrics");
        for(final String field:map.keySet()){
            final String value = map.get(field);
            
            EvaluationMetricCostOfAdaptation metric = new EvaluationMetricCostOfAdaptation();
            metric.setMetricType(field);
            metric.setMetricValue(value);
            
            c.getMetricsForCategory().add(metric);
        }
        
        return c;
    }

}
