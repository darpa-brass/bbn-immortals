package com.securboration.immortals.service.eos.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securboration.immortals.service.eos.api.types.EvaluationConfiguration;
import com.securboration.immortals.service.eos.api.types.EvaluationStatusReport;
import com.securboration.immortals.service.eos.impl.EvaluationFsm;
import com.securboration.immortals.swri.EvaluationProperties;

/**
 * A REST API to be used during CP3.1 evaluation
 *
 * @author jstaples
 *
 */
@RestController
@RequestMapping("/eos")
public class EvaluationOrchestrationService {
    
    @Autowired(required = true)
    private EvaluationProperties properties;
    
    @Autowired(required = true)
    private EvaluationFsm fsm;
    
    
    
    
    /**
     * A simple method useful for approximating the client/server latency.
     * 
     * @return the server's current epoch time in milliseconds
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value="/ping",
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String ping() {
        return "" + System.currentTimeMillis();
    }
    
    /**
     * Asynchronously kicks off an evaluation run using the provided evaluation
     * configuration.
     * 
     * @param configuration
     *            contains the configuration for the evaluation run to be
     *            performed
     * @return a key (text/plain) useful for querying the status of evaluation
     *         and, upon completion, retrieving evaluation results
     * @throws Exception
     *             if an error occurs
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value="/evaluate",
            produces=MediaType.TEXT_PLAIN_VALUE
    )
    public String evaluate(
            @RequestBody
            EvaluationConfiguration configuration
            ) throws Exception {
        return fsm.evaluate(properties,configuration);
    }
    
    /**
     * Asynchronously kicks off a dynamic analysis run using the provided
     * evaluation configuration.
     * 
     * @param configuration
     *            contains the configuration for the evaluation run to be
     *            performed
     * @return a key (text/plain) useful for querying the status of evaluation
     *         and, upon completion, retrieving evaluation results
     * @throws Exception
     *             if an error occurs
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value="/dynamicAnalysis",
            produces=MediaType.TEXT_PLAIN_VALUE
    )
    public String performDynamicAnalysis(
            @RequestBody
            EvaluationConfiguration configuration
            ) throws Exception {
        return fsm.evaluate(properties,configuration);
    }
    
    /**
     * Returns the status of a provided context identifier.
     * 
     * @param contextId
     *            a unique identifier for a context previously created by a call
     *            to /evaluate
     * @return the status of the indicated context, or UNKNOWN if it is not a
     *         known context, as a json document
     * @throws Exception
     *             of an error occurs
     */
    @RequestMapping(
        method = RequestMethod.GET,
        value="/status",
        produces=MediaType.APPLICATION_JSON_VALUE
        )
    public EvaluationStatusReport status(
            @RequestParam("contextId")
            String contextId
            ) throws Exception{
        return fsm.statusOf(contextId);
    }
    
    
    /**
     * Lists all evaluation context IDs
     * 
     * @return an array of context identifiers
     */
    @RequestMapping(
        method = RequestMethod.GET,
        value="/context",
        produces=MediaType.APPLICATION_JSON_VALUE
        )
    public List<String> getContexts(){
        return fsm.getContextIds();
    }
    
    /**
     * Cleans up an evaluation context
     * 
     * @param contextId
     *            uniquely identifies an evaluation context.  If null, all 
     *            contexts will be purged.
     * @return true iff the cleanup was a success
     */
    @RequestMapping(
        method = RequestMethod.DELETE,
        value="/context",
        produces=MediaType.APPLICATION_JSON_VALUE
        )
    public boolean cleanup(
            @RequestParam(value="contextId",required=false)
            String contextId
            ){
        fsm.cleanup(contextId);
        
        return true;
    }

}


