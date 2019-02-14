package com.securboration.immortals.ontology.gmei;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.cp2.ClientServerEnvironment;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.resource.containment.ConcreteResourceNode;
import com.securboration.immortals.ontology.resource.containment.ResourceContainmentModel;
import com.securboration.immortals.ontology.resource.containment.ResourceContainmentModelNode;
import com.securboration.immortals.ontology.resources.ResourceMigrationTarget;
import com.securboration.immortals.ontology.resources.SoftwareLibrary;
import com.securboration.immortals.ontology.resources.logical.Version;

@ConceptInstance
public class DeploymentModelCP3 extends DeploymentModel {
    
    @Ignore
    private static class Instances {
        static final ClientServerEnvironment.ClientDevice1 clientDevice1 = new ClientServerEnvironment.ClientDevice1();
        static final ClientServerEnvironment.ClientDevice2 clientDevice2 = new ClientServerEnvironment.ClientDevice2();
        static final ClientServerEnvironment.ClientDevice3 clientDevice3 = new ClientServerEnvironment.ClientDevice3();
        
        static final SoftwareLibrary badSoftwareLibrary = new SoftwareLibrary();
        static final SoftwareLibrary goodSoftwareLibrary = new SoftwareLibrary();

        static final ResourceMigrationTarget targetLibrary = new ResourceMigrationTarget();
    } 
    
    public DeploymentModelCP3() {
        Instances.badSoftwareLibrary.setApplicationName("oldCommonsIO");
        Version oldVersion = new Version();
        oldVersion.setMajor(1);
        oldVersion.setMinor(4);
        oldVersion.setPatch(9);
        Instances.badSoftwareLibrary.setVersion(oldVersion);
        
        Instances.goodSoftwareLibrary.setApplicationName("currentCommonsIO");
        Version currentVersion = new Version();
        currentVersion.setMajor(2);
        currentVersion.setMinor(0);
        currentVersion.setPatch(0);
        Instances.goodSoftwareLibrary.setVersion(currentVersion);
        
        Instances.targetLibrary.setTargetResource(Instances.goodSoftwareLibrary);
        Instances.targetLibrary.setRationale("Need to upgrade library version to avoid discovered exploit");

        ResourceContainmentModel resourceContainmentModel = new ResourceContainmentModel();

        ConcreteResourceNode clientDevice1Node = new ConcreteResourceNode();
        clientDevice1Node.setResource(Instances.clientDevice1);
        clientDevice1Node.setHumanReadableDesc("Resource node representing ATAK client device instance #1. Also contains a software library resource node.");
        ConcreteResourceNode badLibNode = new ConcreteResourceNode();
        badLibNode.setHumanReadableDesc("Resource node representing the bad library instance.");
        badLibNode.setResource(Instances.badSoftwareLibrary);
        ConcreteResourceNode clientDevice2Node = new ConcreteResourceNode();
        clientDevice2Node.setHumanReadableDesc("Resource node representing ATAK client device instance #2. Also contains a software library resource node.");
        clientDevice2Node.setResource(Instances.clientDevice2);
        ConcreteResourceNode goodLibNode = new ConcreteResourceNode();
        goodLibNode.setHumanReadableDesc("Resource node representing the good library instance.");
        goodLibNode.setResource(Instances.goodSoftwareLibrary);
        ConcreteResourceNode clientDevice3Node = new ConcreteResourceNode();
        clientDevice3Node.setHumanReadableDesc("Resource node representing ATAK client device instance #3. Also contains a software library resource node.");
        clientDevice3Node.setResource(Instances.clientDevice3);
        
        resourceContainmentModel.setResourceModel(new ResourceContainmentModelNode[] {clientDevice1Node, clientDevice2Node, clientDevice3Node});
        clientDevice1Node.setContainedNode(new ResourceContainmentModelNode[]{goodLibNode});
        clientDevice2Node.setContainedNode(new ResourceContainmentModelNode[]{goodLibNode});
        clientDevice3Node.setContainedNode(new ResourceContainmentModelNode[]{badLibNode});
        
        this.setHumanReadableDescription("Describes ATAK mobile devices utilizing various versions of a library, and a target version that should upgraded to.");
        this.setResourceMigrationTargets(new ResourceMigrationTarget[]{Instances.targetLibrary});
        this.setResourceContainmentModel(resourceContainmentModel);
        this.setAvailableResources(new Resource[]{Instances.clientDevice1, Instances.clientDevice2, Instances.clientDevice3,
        Instances.badSoftwareLibrary, Instances.goodSoftwareLibrary});
        this.setSessionIdentifier("CP3ATAKDevicesLibraries");
    }
    
}
