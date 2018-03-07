package com.securboration.immortals.ontology.gmei;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.cp2.ClientServerEnvironment;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.resource.containment.ConcreteResourceNode;
import com.securboration.immortals.ontology.resource.containment.ResourceContainmentModel;
import com.securboration.immortals.ontology.resource.containment.ResourceContainmentModelNode;
import com.securboration.immortals.ontology.resources.DataBase;
import com.securboration.immortals.ontology.resources.ResourceMigrationTarget;
import com.securboration.immortals.ontology.resources.logical.*;



@ConceptInstance
public class DeploymentModelCP1 extends DeploymentModel {

    @Ignore
    private static class Instances {
        static final ClientServerEnvironment.MartiServer martiServer = new ClientServerEnvironment.MartiServer();
        static final DataBase martiDatabase = new DataBase();
        static final DBSchema oldSchema = new DBSchema();
        static final DBSchema newSchema = new DBSchema();
        static final ResourceMigrationTarget targetSchema = new ResourceMigrationTarget();
    }
    
    public DeploymentModelCP1() {
        Table[] oldTables = new Table[2];
        Column[] oldColumns = new Column[2];
        
        Table[] newTables = new Table[2];
        Column[] newColumns = new Column[2];
        
        Column oldColumn1 = new Column();
        oldColumn1.setName("oldStrings");
        oldColumns[0] = oldColumn1;
        Table oldTable1 = new Table();
        oldTable1.setColumns(new Column[]{oldColumns[0]});
        oldTables[0] = oldTable1;
        
        Column oldColumn2 = new Column();
        oldColumn2.setName("oldInts");
        oldColumns[1] = oldColumn2;
        Table oldTable2 = new Table();
        oldTable2.setColumns(new Column[]{oldColumns[1]});
        oldTables[1] = oldTable2;
        
        Instances.oldSchema.setName("OldSchema");
        Instances.oldSchema.setTables(oldTables);
        Instances.oldSchema.setVersion("oldVersion");
        
        Column newColumn1 = new Column();
        newColumn1.setName("newBooleans");
        newColumns[0] = newColumn1;
        Table newTable1 = new Table();
        newTable1.setColumns(new Column[]{newColumns[0]});
        newTables[0] = newTable1;
        
        Column newColumn2 = new Column();
        newColumn2.setName("newReals");
        newColumns[1] = newColumn2;
        Table newTable2 = new Table();
        newTable2.setColumns(new Column[]{newColumns[1]});
        newTables[1] = newTable2;
        
        Instances.newSchema.setName("CurrentSchema");
        Instances.newSchema.setTables(newTables);
        Instances.newSchema.setVersion("currentVersion");
        
        Instances.martiDatabase.setDataBaseSchemas(new DBSchema[]{Instances.oldSchema, Instances.newSchema});

        Instances.targetSchema.setTargetResource(Instances.newSchema);
        Instances.targetSchema.setRationale("Need to upgrade schema to support new operations");

        ResourceContainmentModel resourceContainmentModel = new ResourceContainmentModel();

        ConcreteResourceNode martiServerNode = new ConcreteResourceNode();
        martiServerNode.setHumanReadableDesc("Resource node representing the marti server instance. Also contains a database resource node.");
        martiServerNode.setResource(Instances.martiServer);
        ConcreteResourceNode martiDBNode = new ConcreteResourceNode();
        martiDBNode.setHumanReadableDesc("Resource node representing the marti database instance. Also contains two schema resource nodes.");
        martiDBNode.setResource(Instances.martiDatabase);
        ConcreteResourceNode oldSchemaNode = new ConcreteResourceNode();
        oldSchemaNode.setHumanReadableDesc("Resource node representing the old schema instance.");
        oldSchemaNode.setResource(Instances.oldSchema);
        ConcreteResourceNode newSchemaNode = new ConcreteResourceNode();
        newSchemaNode.setHumanReadableDesc("Resource node representing the new schema instance.");
        newSchemaNode.setResource(Instances.newSchema);
        
        martiServerNode.setContainedNode(new ResourceContainmentModelNode[]{martiDBNode});
        martiDBNode.setContainedNode(new ResourceContainmentModelNode[]{oldSchemaNode, newSchemaNode});
        resourceContainmentModel.setResourceModel(new ResourceContainmentModelNode[]{martiServerNode});
        
        this.setHumanReadableDescription("Describes a marti database in need of upgrading schema versions to the specified target.");
        this.setResourceContainmentModel(resourceContainmentModel);
        this.setSessionIdentifier("CP1MartiDatabaseSchemas");
        this.setAvailableResources(new Resource[]{Instances.martiServer, Instances.martiDatabase, Instances.oldSchema, Instances.newSchema});
        this.setResourceMigrationTargets(new ResourceMigrationTarget[]{Instances.targetSchema});
        
    }
    
    
    public static void main(String[] args) {
        DeploymentModelCP1 deploymentModelCP1 = new DeploymentModelCP1();
    }
}

















