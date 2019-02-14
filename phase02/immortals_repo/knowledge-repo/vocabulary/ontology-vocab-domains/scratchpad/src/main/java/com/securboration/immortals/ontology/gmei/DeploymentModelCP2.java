package com.securboration.immortals.ontology.gmei;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.cp2.Analysis;
import com.securboration.immortals.ontology.cp2.ClientServerEnvironment;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.property.impact.CauseEffectAssertion;
import com.securboration.immortals.ontology.resource.containment.ConcreteResourceNode;
import com.securboration.immortals.ontology.resource.containment.ResourceContainmentModel;
import com.securboration.immortals.ontology.resource.containment.ResourceContainmentModelNode;


@ConceptInstance
public class DeploymentModelCP2 extends DeploymentModel {
    
    @Ignore
    private static class Instances{
        static final Analysis.Marti.MartiServer martiServer = new ClientServerEnvironment.MartiServer();
        static final Analysis.Atak.AtakPhone atakPhone = new ClientServerEnvironment.ClientDevice1();
        static final ClientServerEnvironment.DataSafetyConstraint dataSafetyConstraint = new ClientServerEnvironment.DataSafetyConstraint();
    }
    
    public DeploymentModelCP2() {
        this.setSessionIdentifier("CP2DataflowManagement");
        this.setAvailableResources(new Resource[] {Instances.atakPhone, Instances.martiServer});
        this.setCauseEffectAssertions(new CauseEffectAssertion[] {Instances.dataSafetyConstraint});
        
        ResourceContainmentModel resourceContainmentModel = new ResourceContainmentModel();
        
        ConcreteResourceNode client1Node = new ConcreteResourceNode();
        client1Node.setHumanReadableDesc("Resource node representing ATAK client device instance");
        client1Node.setResource(Instances.atakPhone);
        
        ConcreteResourceNode serverNode = new ConcreteResourceNode();
        serverNode.setResource(Instances.martiServer);
        
        resourceContainmentModel.setResourceModel(new ResourceContainmentModelNode[]{client1Node});
        this.setResourceContainmentModel(resourceContainmentModel);
        this.setHumanReadableDescription("This describes a server and several client devices, along with some file systems and software, on the devices." +
                "There is a constraint placed on any data transmitted between client and server devices mandating it be confidential somehow.");
    }
}
