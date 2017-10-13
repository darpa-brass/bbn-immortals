package com.securboration.immortals.repo.api;

import java.util.UUID;

import org.apache.jena.rdf.model.Model;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.ontology.core.DeploymentModel;
import com.securboration.immortals.ontology.dataset.ImmortalsDataset;

/**
 * Safely exposes various semantic operations on a Fuseki repository using a
 * POJO-based API.
 * 
 * Note: Operations are thread safe but may not be sequentially consistent.
 * 
 * @author jstaples
 *
 */
public class WritableRepository extends Repository {

    public WritableRepository(RepositoryConfiguration configuration) {
        super(configuration);
    }
    
    private ObjectToTriplesConfiguration getObjectToTriplesConfig(){
        
        ObjectToTriplesConfiguration c = new ObjectToTriplesConfiguration();
        
        c.setOutputFile(null);
        
        return c;
    }
    
    protected static String generateRandomUuid(){
        return UUID.randomUUID().toString();
    }
    
    /**
     * CREATE a deployment model.
     * 
     * @param d
     *            the deployment model definition
     * @return a unique name for the graph created
     */
    @Override
    public String createDeploymentModel(DeploymentModel d){
        return pushImmortalsObject(d);
    }
    
    /**
     * CREATE an IMMoRTALS dataset.
     * 
     * @param dataset
     *            a dataset definition which includes graph names for the
     *            complete set of models to be included in this dataset
     * @return a unique name for the graph created
     */
    @Override
    public String createDataset(
            ImmortalsDataset dataset
            ){
        return pushImmortalsObject(dataset);
    }
    
    private <T> String pushImmortalsObject(Object o){
        Model m = getModel(o);
        
        final String uuid = generateRandomUuid();
        
        getFusekiClient().setModel(m, uuid);
        
        return uuid;
    }
    
    /**
     * DELETE the indicated graph
     * 
     * @param graphName
     *            the graph to delete. This identifier is retrieved from a
     *            previous CREATE operation
     */
    @Override
    public void deleteGraph(String graphName){
        getFusekiClient().deleteModel(graphName);
    }
    
    @Override
    protected Model getModel(Object o){
        return ObjectToTriples.convert(
                getObjectToTriplesConfig(), 
                o);
    }

}
