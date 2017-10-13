package com.securboration.immortals.deployment.Main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.securboration.immortals.deployment.parser3.Parser;
import com.securboration.immortals.deployment.pojos.FieldValue;
import com.securboration.immortals.deployment.pojos.ObjectInstance;
import com.securboration.immortals.deployment.pojos.TypeAbstraction;
import org.apache.commons.io.FileUtils;

import com.securboration.immortals.deployment.parser2.Node;
import com.securboration.immortals.deployment.parser2.NodeMapping;

/**
 * 
 * 
 * 
 * @author jstaples
 *
 */
public class Main3 {
    
    public static void main(String[] args) throws IOException, IllegalArgumentException, IllegalAccessException{
        final String pathToDeploymentJson = 
                "src/test/resources/immortals_dm_test.json";
//        "src/test/resources/immortals_fm_test.json";
        
        final String json = 
                FileUtils.readFileToString(new File(pathToDeploymentJson));

        Parser parser = new Parser();
        parser.parse(json);
        System.out.println();
        System.out.println(parser.getTypes().size() + " types.");
        System.out.println(parser.getInstances().size() + " instances.\n");

        List<FieldValue> fields = parser.getInstances().stream()
                .map(ObjectInstance::getFieldValues)
                .filter(fva -> fva != null)
                .flatMap(Arrays::stream)
                .peek(fv -> System.out.println(fv.getName()))
                .filter(fv -> fv.getName().startsWith("Doc:"))
                .collect(Collectors.toList());

        fields.addAll(parser.getTypes().stream()
                .map(TypeAbstraction::getFieldValues)
                .filter(fva -> fva != null)
                .flatMap(Arrays::stream)
                .peek(fv -> System.out.println(fv.getName()))
                .filter(fv -> fv.getName().startsWith("Doc:"))
                .collect(Collectors.toList()));

        System.out.println("\n\n***Doc Nodes in Fields***");
        System.out.println("Number of nodes: " + fields.size());
        for(FieldValue fv: fields)
            System.out.println(fv.getName());

        List<ObjectInstance> docNodes = parser.getInstances().stream()
                .filter(o -> o.getInstanceType() != null)
                .filter(o -> o.getInstanceType().getName().equals("Doc: Language"))
                .collect(Collectors.toList());

        System.out.println("\n\n***Documentation Nodes***");
        System.out.println("Number of nodes: " + docNodes.size());
        docNodes.stream().forEach(n -> System.out.println(n.getName() + "\n" + n.getUuid() + "\n" + n.getComments()));

        List<ObjectInstance> commentNodes = parser.getInstances().stream()
                .filter(o -> o.getInstanceType() == null || !o.getInstanceType().getName().equals("Doc: Language"))
                .filter(o -> o.getComments() != null)
                .collect(Collectors.toList());

        System.out.println("\n\n***Nodes with comments***");
        System.out.println("Number of nodes: " + commentNodes.size());
        commentNodes.stream().forEach(n -> System.out.println(n.getName() + "\n" + n.getUuid() + "\n" + n.getComments()));


        System.out.println("\n\nType Comments:");
        parser.getTypes().stream()
                .filter(t -> t.getComments() != null)
                .forEach(t -> System.out.println(t.getName() + ":\n" + t.getComments()));
    }

}
