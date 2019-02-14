package com.securboration.immortals.service.eos.impl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

import com.securboration.immortals.adapt.engine.AdaptationEngine;
import com.securboration.immortals.service.eos.api.types.EvaluationRunCommand;
import com.securboration.immortals.service.eos.api.types.EvaluationRunCommandResult;
import com.securboration.immortals.service.eos.api.types.EvaluationRunConfiguration;
import com.securboration.immortals.service.eos.api.types.EvaluationRunStatus;
import com.securboration.immortals.service.eos.api.types.LambdaCommand;
import com.securboration.immortals.service.eos.api.types.WaitForEndpointCommand;
import com.securboration.immortals.service.eos.nonapi.types.EvaluationContext;
import com.securboration.immortals.swri.eval.EndpointHelper;
import com.securboration.immortals.swri.eval.Out;

public class Evaluator {
    private final EvaluationContext context;
    
    private final EvaluationRunConfiguration config;
    
    private final PrintStream stdout;
    private final PrintStream stderr;
    
    public Evaluator(
            EvaluationContext evalContext
            ){
        this.context = evalContext;
        this.config = evalContext.getEvaluationConfiguration();
        
        this.stdout = new PrintStream(evalContext.getStdout());
        this.stderr = new PrintStream(evalContext.getStderr());
    }
    
    private void stdout(String format, Object...args){
        Out.println(stdout,"EVAL-ENGINE", format, args);
    }
    
    private void stderr(String format, Object...args){
        Out.println(stderr,"EVAL-ENGINE", format, args);
    }
    
    public void evaluate(final Object contextLock) throws Exception {
        
        stdout("beginning evaluation of %s",context.getContextId());
        
        final File workingDir = extractPackage();
        context.setEvaluationWorkingDir(workingDir);
        
        stdout("about to step through %d commands",config.getEvaluationCommands().size());
        
        boolean errorsEncountered = false;
        
        try{
            int count = 1;
            for(EvaluationRunCommand command:config.getEvaluationCommands()){
                
                final String tag = 
                        String.format(
                            "command-%02d-of-%02d (%s)", 
                            count, 
                            config.getEvaluationCommands().size(),
                            command.getName()
                            );
                
                evaluate(contextLock,workingDir,tag,command);
                
                count++;
            }
            
            stdout("evaluation completed with no errors");
        } catch(Exception e){
            stdout("evaluation completed WITH errors");
            
            e.printStackTrace(stderr);
            errorsEncountered = true;
        } finally{
            
            try{
                synchronized(contextLock){
                    
                    {//kill all processes in the context
                        for(Process p:context.getKillAfterCompletion()){
                            EosHelper.slayProcess(p, 100L, 20, 2000L);
                            
                            if(p.isAlive()){
                                throw new RuntimeException("unable to slay process");
                            }
                        }
                    }
                    
                    //set the evaluated package
                    context.setEvaluatedPackageZip(
                        createEvaluationReportZip(workingDir)
                        );
                }
                
                Thread.sleep(5000L);//give the process streams a chance to quiesce
                
                FileUtils.deleteDirectory(workingDir);
            } catch(Exception ee){
                stdout("errors during post-evaluation cleanup");
                
                ee.printStackTrace();
                
                errorsEncountered = true;
                
                throw ee;
            } finally {
                synchronized(contextLock){
                    if(errorsEncountered){
                        context.setCurrentStatus(EvaluationRunStatus.COMPLETED_ERROR);
                    } else {
                        context.setCurrentStatus(EvaluationRunStatus.COMPLETED_OK);
                    }
                }
            }
        }
    }
    
    private File extractPackage() throws IOException{
        
        final File packageDir = new File(
            "./eosData/tmp/",
            context.getContextId()
            );
        
        stdout(
            "Extracting %dB to working dir %s", 
            config.getCodePackageZipped().length, 
            packageDir.getAbsolutePath()
            );
        
        ZipHelper.unzip(config.getCodePackageZipped(), packageDir);
        
        {
            final byte[] cheatZip = context.getHighLevelConfiguartion().getCheatZip();
            
            if(cheatZip != null){
                ZipHelper.unzip(cheatZip, packageDir);
            }
        }
        
        stdout("done with extraction");
        
        return packageDir;
    }
    
    private static String getInfo(EvaluationRunCommand c){
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format(
            "in working dir %s\n",new File(c.getWorkingDir()).getAbsolutePath()
            ));
        
        sb.append(
            String.format(
                "%s\n" +
                "%s\n", 
                c.getName(),
                Arrays.asList(c.getCommandParts())
                )
            );
        
        return sb.toString();
    }
    
    private void evaluate(
            final Object contextLock,
            final File extractedDir,
            final String commandTag,
            final EvaluationRunCommand command
            ) throws Exception{
        
        if(command instanceof LambdaCommand){
            EvaluationRunCommandResult result = new EvaluationRunCommandResult();
            
            LambdaCommand clone = new LambdaCommand();
            clone.setAsync(command.isAsync());
            clone.setName(command.getName());
            result.setCommand(clone);
            
            try{
                LambdaCommand l = (LambdaCommand)command;
                l.getR().run();//intentional abuse of Runnable functional interface
                
                result.setReturnValue(0);
            } catch(Throwable t){
                result.setReturnValue(-999);
                throw t;
            } finally {
                synchronized(contextLock){
                    context.getCommandResults().add(result);
                }
            }
            
            return;
        }
        
        if(command instanceof WaitForEndpointCommand){
            WaitForEndpointCommand wait = (WaitForEndpointCommand)command;
            
            final String endpoint = wait.getEndpointUrl();
            EndpointHelper check = new EndpointHelper();
            
            EvaluationRunCommandResult result = new EvaluationRunCommandResult();
            result.setCommand(command);
            
            try{
                check.waitFor200("GET",endpoint,1000L * 60);//1 minute
                result.setReturnValue(0);
            } catch(Exception e){
                result.setReturnValue(-999);
            } finally {
                synchronized(contextLock){
                    context.getCommandResults().add(result);
                }
            }
            
            return;
        }
        
        final File workingDir;
        if(command.getWorkingDir() != null){
            if(command.isWorkingDirAbsolute()){
                workingDir = new File(command.getWorkingDir());
            } else {
                workingDir = new File(extractedDir,command.getWorkingDir());
            }
        } else {
            workingDir = extractedDir;
        }
        
        if(command.getName().startsWith("adapt") && command.getCommandParts().length == 0){
            //TODO: hook for adapt entrypoint
            EvaluationRunCommandResult result = new EvaluationRunCommandResult();
            result.setCommand(command);
            result.setStderrPath(null);
            result.setStdoutPath(null);
            
            try{
                AdaptationEngine engine = context.getAdaptationEngine();
                engine.adapt(context);
                
                result.setReturnValue(0);
            } catch(Exception e){
                result.setReturnValue(-999);
                throw e;
            } finally {
                synchronized(contextLock){
                    context.getCommandResults().add(result);
                }
            }
            
            return;
        }//end special handling for adapt workflow
        
        stdout("\texecuting \"%s\" from %s: %s",command.getName(),workingDir.getAbsolutePath(),Arrays.asList(command.getCommandParts()));
        
        final File outputDir = new File(extractedDir,commandTag);
        final File stdoutFile = new File(outputDir,"stdout.out");
        final File stderrFile = new File(outputDir,"stderr.out");
        
        final File infoFile = new File(outputDir,"info.dat");
        
        {//force creation of stdout/stderr files
            FileUtils.writeStringToFile(stdoutFile, "");
            FileUtils.writeStringToFile(stderrFile, "");
        }
        
        {//write command info
            FileUtils.writeStringToFile(infoFile, getInfo(command));
        }
        
        final ProcessBuilder pb = new ProcessBuilder(command.getCommandParts());
        
        pb.directory(workingDir.getCanonicalFile());
        pb.redirectOutput(stdoutFile.getCanonicalFile());
        pb.redirectError(stderrFile.getCanonicalFile());
        
        final Process p = pb.start();
        
        EvaluationRunCommandResult result = new EvaluationRunCommandResult();
        result.setCommand(command);
        result.setStderrPath(stderrFile.getAbsolutePath());
        result.setStdoutPath(stdoutFile.getAbsolutePath());
        
        synchronized(contextLock){
            context.getCommandResults().add(result);
        }
        
        if(command.isAsync()){
            synchronized(contextLock){
                context.getKillAfterCompletion().add(p);
            }
            
            return;
        }
        
        final int exitCode = p.waitFor();
        
        synchronized(contextLock){
            result.setReturnValue(exitCode);
        }
        
        if(exitCode != 0){
            if(stderrFile.exists()){
                try{
                    final String stderr = FileUtils.readFileToString(stderrFile);
                    stderr(stderr);
//                    stderr = "> err >  " + stderr.replace("\n", "\n> err >  ");
//                    
//                    System.out.println(stderr);
                } catch(Exception ee){
                    System.err.println("unable to dump stderr, you'll have to dig for it in the archive");
                    //do nothing else
                }
            }
            
            throw new RuntimeException("nonzero exit code: " + exitCode);
        }
    }
    
    private byte[] createEvaluationReportZip(final File workingDir) throws IOException{
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        {
        
            final ZipOutputStream zos = 
                    new ZipOutputStream(new BufferedOutputStream(out));
            
            {//add a copy of the request
                ZipHelper.addEntry(zos,"eval.request.json",config.toJson().getBytes("UTF-8"));
            }
            
            {//add stdout/stderr from the service
                ZipHelper.addEntry(zos,"eval.stdout",context.getStdoutDump());
                ZipHelper.addEntry(zos,"eval.stderr",context.getStderrDump());
            }
            
            {//add a nested zipped copy of the input ESS
                ZipHelper.addEntry(zos, "ess-orig.zip", config.getCodePackageZipped());
            }
            
    //        {//add stdout/stderr from the various command IO streams
    //            int i=1;
    //            for(EvaluationRunCommandResult r:report.getCommandResults()){
    //                addEntry(zos,i+"-"+r.getCommand().getName()+".stdout",r.getStdoutPath());
    //                addEntry(zos,i+"-"+r.getCommand().getName()+".stderr",r.getStderrPath());
    //                i++;
    //            }
    //        }
            
            {//add the working dir
                if(workingDir != null && workingDir.exists()){
                    ZipHelper.addEntries(zos,workingDir);
                }
            }
            
            zos.close();
        }
        
        return out.toByteArray();
    }

}

