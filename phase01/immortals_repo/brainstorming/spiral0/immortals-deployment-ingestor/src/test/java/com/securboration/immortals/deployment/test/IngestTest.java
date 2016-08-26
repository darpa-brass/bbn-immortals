package com.securboration.immortals.deployment.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.securboration.immortals.deployment.parser3.Parser;
import com.securboration.immortals.deployment.pojos.DeploymentParser;
import com.securboration.immortals.deployment.pojos.FieldValue;
import com.securboration.immortals.deployment.pojos.ObjectInstance;
import com.securboration.immortals.deployment.pojos.TypeAbstraction;
import com.securboration.immortals.deployment.pojos.values.ValueComplex;
import com.securboration.immortals.deployment.pojos.values.ValuePrimitive;

public class IngestTest
{
    private static DeploymentParser parser;

    @BeforeClass
    public static void setup()
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

        parser = new DeploymentParser()
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

    @Test
    public void test1()
    {
        System.out.println("Running Test1");

        System.out.println("Types:");
        for(TypeAbstraction types: parser.getTypes())
            System.out.println(types.getName());
        System.out.println("Instances:");
        for(ObjectInstance instances: parser.getInstances())
            System.out.println(instances.getName());
    }

    @Test
    public void testDeploymentModel()
    {
        final String pathToDeploymentJson =
                "src/test/resources/immortals_dm_test.json";

        final String docNodeGuid = "7f4a49a5-9374-953b-c267-03e8837a8ba8";

        String json = "";
        try
        {
            json = FileUtils.readFileToString(new File(pathToDeploymentJson));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        Parser parser = new Parser();
        parser.parse(json);
        System.out.println(parser.getTypes().size() + " types.");
        assertEquals(81, parser.getTypes().size());
        System.out.println(parser.getInstances().size() + " instances.");
        assertEquals(111, parser.getInstances().size());

        Optional<ObjectInstance> result = parser.getInstances().stream()
                .filter(n -> n.getUuid().equals("bf6f2813-c593-1c9f-f71c-880c1a9e21e0")) // mobile B2
                .findFirst();

        assertTrue(result.isPresent());
        ObjectInstance nexus7 = result.get();

        // mobile B2 should have a doc node, a GPS Onboard Receiver, and 2 pointers: a network connection, and an outbound msg load
        assertEquals(4, nexus7.getFieldValues().length);

        // Expecting 2 pointers
        Set<String> pointers = Arrays.stream(nexus7.getFieldValues())
                .map(FieldValue::getValue)
                .map(ValueComplex.class::cast)
                .filter(ValueComplex::isPointer)
                .map(ValueComplex::getValue)
                .map(ObjectInstance::getUuid)
                .collect(Collectors.toSet());
        assertEquals(2, pointers.size());
        assertEquals(pointers, Stream.of("e8673100-5f7c-bbfa-b935-6987ee533d0c", // outbound msg load
                                         "d3904b10-7baf-9e9a-8e25-1ca529374a0b") // NetworkConnection
                                     .collect(Collectors.toSet()));

        // Expecting 1 comment
        assertEquals(1, Arrays.stream(nexus7.getFieldValues()).filter(fv -> ((ValueComplex)fv.getValue()).getType().getUuid().equals(docNodeGuid)).count());

        Arrays.stream(nexus7.getFieldValues())
                .map(fv -> ((ValueComplex)fv.getValue()).getValue().getName())
                .forEach(System.out::println);

        int count = parser.getInstances().stream()
                .mapToInt(t -> Arrays.stream(t.getFieldValues())
                        .map(FieldValue::getValue)
                        .filter(v -> v instanceof ValueComplex && ((ValueComplex)v).isPointer())
                        .mapToInt(e -> 1).sum())
                .sum();

        // 50 instance connections
        assertEquals(50, count);
    }

    @Test
    public void testFunctionalModel()
    {
        final String pathToDeploymentJson =
                "src/test/resources/immortals_fm_test.json";

        String json = "";
        try
        {
            json = FileUtils.readFileToString(new File(pathToDeploymentJson));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        Parser parser = new Parser();
        parser.parse(json);

        System.out.println(parser.getTypes().size() + " types.");
        assertEquals(8, parser.getTypes().size());
        System.out.println(parser.getInstances().size() + " instances.");
        assertEquals(37, parser.getInstances().size());
    }
}
