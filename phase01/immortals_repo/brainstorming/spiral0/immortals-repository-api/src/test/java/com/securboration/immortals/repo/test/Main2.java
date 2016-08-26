package com.securboration.immortals.repo.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;

import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.core.DeploymentModel;
import com.securboration.immortals.ontology.dataset.ImmortalsDataset;
import com.securboration.immortals.ontology.resources.environment.OperatingEnvironment;
import com.securboration.immortals.ontology.resources.gps.GpsEnvironment;
import com.securboration.immortals.repo.api.WritableRepository;
import com.securboration.immortals.repo.api.RepositoryConfiguration;
import com.securboration.immortals.repo.api.QueryableRepository;
import com.securboration.immortals.repo.etc.ExceptionWrapper;
import com.securboration.immortals.repo.ontology.FusekiClient;

public class Main2 {
    
    public static void main(String[] args){
        
        RepositoryConfiguration c = new RepositoryConfiguration();
        c.setRepositoryBaseUrl("http://localhost:3030/ds");
        
        WritableRepository r = new QueryableRepository(c);
        
        System.out.printf("%d graphs\n", ((QueryableRepository)r).getGraphs().size());
        ((QueryableRepository)r).deleteEverything();
        System.out.printf("%d graphs\n", ((QueryableRepository)r).getGraphs().size());
        
        List<String> graphNames = new ArrayList<>();
        for(int i=0;i<2;i++){
            graphNames.add(r.createDeploymentModel(generateModel(i)));
        }
        
        System.out.printf("%d graphs\n", ((QueryableRepository)r).getGraphs().size());
        
        ImmortalsDataset dataset = new ImmortalsDataset();
        dataset.setDatasetTag("test");
        dataset.setGraphNames(graphNames.toArray(new String[]{}));
        
        final String datasetName = r.createDataset(dataset);
        
        
        {
            FusekiClient client = new FusekiClient("http://localhost:3030/ds");
            
            for(String graphName:graphNames){
                Model graph = ((QueryableRepository)r).getGraph(graphName);
                
                printModel(graphName,graph);
            }
            
            Model graph = client.getModel(datasetName);
            
            printModel(datasetName,graph);
        }
    }
    
    private static DeploymentModel generateModel(int version){
        DeploymentModel d = new DeploymentModel();
        d.setOperatingEnvironment(new OperatingEnvironment());
        d.getOperatingEnvironment().setGps(new GpsEnvironment());
        d.getOperatingEnvironment().getGps().setNumberOfVisibleSatellites(version);
        
        return d;
    }
    

    
    private static void printModel(String graphName,Model model){
        
        ExceptionWrapper.wrap(()->{
            System.out.printf("printing graph [%s]\n", graphName);
            System.out.println("----------------------------------");
            System.out.println(OntologyHelper.serializeModel(model, "Turtle"));
            System.out.println("----------------------------------\n");
        });
    }
    
    

}
