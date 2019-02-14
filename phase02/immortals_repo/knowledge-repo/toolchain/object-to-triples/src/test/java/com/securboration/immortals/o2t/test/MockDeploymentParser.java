package com.securboration.immortals.o2t.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;

import com.securboration.immortals.deployment.pojos.DeploymentParser;
import com.securboration.immortals.deployment.pojos.FieldValue;
import com.securboration.immortals.deployment.pojos.ObjectInstance;
import com.securboration.immortals.deployment.pojos.TypeAbstraction;
import com.securboration.immortals.deployment.pojos.values.ValuePrimitive;


public class MockDeploymentParser
{
    @BeforeClass
    public static DeploymentParser getParser()
    {
        // *** Types ***
        // NetworkTopology
        TypeAbstraction networkTopology = new TypeAbstraction();
        networkTopology.setUuid("aa2970a2-2df2-cd04-2bfb-a875cfa0478e");
        networkTopology.setName("NetworkTopology");

        // DeviceInstance
        TypeAbstraction deviceInstance = new TypeAbstraction();
        deviceInstance.setUuid("c5c61472-04ab-810f-cafc-2ab2bd269fb2");
        deviceInstance.setName("DeviceInstance");
        deviceInstance.setComments("In UML2 this is called 'Device'. This is the hardware which provides an " +
                "ExecutionPlatform. There will be certain types of ExecutionEnvironments which may be installed on " +
                "this device. In order to clarify the role but preserve the name from UML this will be called " +
                "ExecutionDevice.");

        FieldValue deviceInstance_level_field = new FieldValue("level", ValuePrimitive.instantiatePrimitive("instance"));
        FieldValue deviceInstance_fields[] = new FieldValue[] { deviceInstance_level_field };
        deviceInstance.setFieldValues(deviceInstance_fields);

        // DeploymentFrame
        TypeAbstraction deploymentFrame = new TypeAbstraction();
        deploymentFrame.setUuid("638b7d42-2e96-9811-f626-141d804e2bad");
        deploymentFrame.setName("DeploymentFrame");

        // *** Instances ***
        // SituationAwareMobile
        ObjectInstance situationAwareMobile = new ObjectInstance();
        situationAwareMobile.setUuid("8748321b-0a05-febe-96be-b043f3f990d5");
        situationAwareMobile.setName("SituationAwareMobile");
        situationAwareMobile.setInstanceType(deploymentFrame);
        situationAwareMobile.setInstanceParent(deploymentFrame.makeInstance());

        // SpecifiedNetworkTopology
        ObjectInstance specifiedNetworkTopology = new ObjectInstance();
        specifiedNetworkTopology.setUuid("b149902b-8970-f5a1-5fd3-b628c9be90d3");
        specifiedNetworkTopology.setName("SpecifiedNetworkTopology");
        specifiedNetworkTopology.setInstanceType(networkTopology);
        specifiedNetworkTopology.setInstanceParent(networkTopology.makeInstance());

        // Nexus 7 : A
        ObjectInstance nexus7A = new ObjectInstance();
        nexus7A.setUuid("5eca28a4-e759-5f83-4cc6-4ab3344406cf");
        nexus7A.setName("nexus7 : A");
        nexus7A.setInstanceType(deviceInstance);
        nexus7A.setInstanceParent(deviceInstance.makeInstance());

        // Nexus 7 : A1
        ObjectInstance nexus7A1 = new ObjectInstance();
        nexus7A1.setUuid("b950bbe9-a56f-457b-fcee-45ce80d0e7e5");
        nexus7A1.setName("nexus7 : A1");
        nexus7A1.setInstanceType(deviceInstance);
        nexus7A1.setInstanceParent(nexus7A);

        return new DeploymentParser()
        {
            private List<TypeAbstraction> types = Arrays.asList(
                    networkTopology, deviceInstance, deploymentFrame
            );

            private List<ObjectInstance> instances = Arrays.asList(
                    situationAwareMobile, specifiedNetworkTopology, nexus7A, nexus7A1
            );

            @Override
            public void parse(String deploymentJson)
            {
                return;
            }

            @Override
            public Collection<TypeAbstraction> getTypes()
            {
                return types;
            }

            @Override
            public Collection<ObjectInstance> getInstances()
            {
                return instances;
            }
        };
    }
    
    
}
