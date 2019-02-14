package com.securboration.immortals.service.eos.impl;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.securboration.immortals.adapt.engine.AdaptationEngine;
import com.securboration.immortals.service.eos.api.types.EvaluationConfiguration;
import com.securboration.immortals.service.eos.api.types.EvaluationRunConfiguration;
import com.securboration.immortals.service.eos.api.types.EvaluationStatusReport;
import com.securboration.immortals.service.eos.nonapi.types.EvaluationContext;
import com.securboration.immortals.swri.EvaluationProperties;

public class EvaluationFsm {
    
    private final Object lock = new Object();
    
    private final Map<String,EvaluationContext> dataMap = new HashMap<>();
    
    private AdaptationEngine adaptationEngine;
    
    public EvaluationFsm(AdaptationEngine adaptationEngine){
        this.adaptationEngine = adaptationEngine;
    }
    
    private void put(final EvaluationContext status){
        dataMap.put(status.getContextId(), status);
    }
    
    private EvaluationContext get(final String contextId){
        return dataMap.get(contextId);
    }
    
    public EvaluationStatusReport statusOf(final String contextId) throws IOException{
        synchronized(lock){
            final EvaluationContext context = get(contextId);
            
            final EvaluationStatusReport report = new EvaluationStatusReport();
            
            report.setContextId(contextId);
            report.setStatus(context.getCurrentStatus());
            report.getCommandResults().addAll(context.getCommandResults());
            report.setEvaluationReportZip(context.getEvaluatedPackageZip());
            
            return report;
        }
    }
    
    public void cleanup(final String contextId){
        synchronized(lock){
            if(contextId == null){
                dataMap.clear();
            } else {
                dataMap.remove(contextId);
            }
        }
    }
    
    public List<String> getContextIds(){
        synchronized(lock){
            return new ArrayList<>(dataMap.keySet());
        }
    }
    
    public String evaluate(
            final EvaluationProperties properties,
            final EvaluationConfiguration highLevelConfig
            ) throws IOException, InterruptedException{
        final String contextId = UUID.randomUUID().toString();
        
        final EvaluationRunConfiguration lowLevelConfig = 
                EosHelper.getEvaluatableConfiguration(
                    properties,
                    highLevelConfig
                    );
        
        final EvaluationContext d = new EvaluationContext(
            contextId,
            lowLevelConfig,
            highLevelConfig
            );
        d.setAdaptationEngine(adaptationEngine);
        
        synchronized(lock){
            if(dataMap.size() > 100){//TODO: this is very arbitrary
                throw new RuntimeException(
                    "there are " + dataMap.size() + 
                    " active contexts, which indicates a resource leak.  " +
                    "Try cleaning up using DELETE /eos/context"
                    );
            }
            
            put(d);
        }
        
        final Thread t = new Thread(()->{
            Evaluator evaluator = new Evaluator(d);
            
            try {
                evaluator.evaluate(lock);
            } catch (Exception e) {
                e.printStackTrace(new PrintStream(d.getStderr()));
                
                throw new RuntimeException(e);
            }
        });
        
        t.setDaemon(true);
        t.setName("evaluator-" + contextId);
        t.start();
        
        return contextId;
    }

}
