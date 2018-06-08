package com.securboration.immortals.ontology.gmei;

import com.securboration.immortals.ontology.core.Resource;
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
        static final ClientServerEnvironment.MartiServer martiServer = new ClientServerEnvironment.MartiServer();
        static final ClientServerEnvironment.ClientDevice1 clientDevice1 = new ClientServerEnvironment.ClientDevice1();
        static final ClientServerEnvironment.ClientDevice2 clientDevice2 = new ClientServerEnvironment.ClientDevice2();
        static final ClientServerEnvironment.ClientDevice3 clientDevice3 = new ClientServerEnvironment.ClientDevice3();
        static final ClientServerEnvironment.FileSystem1 fileSystem1 = new ClientServerEnvironment.FileSystem1();
        static final ClientServerEnvironment.FileSystem2 fileSystem2 = new ClientServerEnvironment.FileSystem2();
        
        static final ClientServerEnvironment.DataSafetyConstraint dataSafetyConstraint = new ClientServerEnvironment.DataSafetyConstraint();
    }
    
    public DeploymentModelCP2() {
        this.setSessionIdentifier("CP2DataflowManagement");
        this.setAvailableResources(new Resource[] {Instances.clientDevice1, Instances.clientDevice2, Instances.clientDevice3, Instances.martiServer,
        Instances.fileSystem1, Instances.fileSystem2});
        this.setCauseEffectAssertions(new CauseEffectAssertion[] {Instances.dataSafetyConstraint});
        
        ResourceContainmentModel resourceContainmentModel = new ResourceContainmentModel();
        
        ConcreteResourceNode client1Node = new ConcreteResourceNode();
        client1Node.setHumanReadableDesc("Resource node representing ATAK client device instance #1. Also contains a file system resource node.");
        client1Node.setResource(Instances.clientDevice1);
        ConcreteResourceNode fileSystem1Node = new ConcreteResourceNode();
        fileSystem1Node.setHumanReadableDesc("Resource node representing file system instance #1.");
        fileSystem1Node.setResource(Instances.fileSystem1);
        client1Node.setContainedNode(new ResourceContainmentModelNode[]{fileSystem1Node});
        
        ConcreteResourceNode client2Node = new ConcreteResourceNode();
        client2Node.setHumanReadableDesc("Resource node representing ATAK client device instance #2.");
        client2Node.setResource(Instances.clientDevice2);

        ConcreteResourceNode client3Node = new ConcreteResourceNode();
        client3Node.setHumanReadableDesc("Resource node representing ATAK client device instance #3. Also contains a file system resource node.");
        client3Node.setResource(Instances.clientDevice3);
        ConcreteResourceNode fileSystem2Node = new ConcreteResourceNode();
        fileSystem2Node.setHumanReadableDesc("Resource node representing file system instance #2.");
        fileSystem2Node.setResource(Instances.fileSystem2);
        client3Node.setContainedNode(new ResourceContainmentModelNode[]{fileSystem2Node});
        
        resourceContainmentModel.setResourceModel(new ResourceContainmentModelNode[]{client1Node, client2Node, client3Node});
        this.setResourceContainmentModel(resourceContainmentModel);
        this.setHumanReadableDescription("This describes a server and several client devices, along with some file systems and software, on the devices." +
                "There is a constraint placed on any data transmitted between client and server devices mandating it be confidential somehow.");
    }
    
    
}
