package com.securboration.immortals.deployment.parser3;

import com.securboration.immortals.deployment.pojos.*;
import com.securboration.immortals.deployment.pojos.values.ValueComplex;
import com.securboration.immortals.deployment.pojos.values.ValuePrimitive;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Parser implements DeploymentParser
{
    private HashMap<String, TypeAbstraction> types;
    private HashMap<String, ObjectInstance> instances;

    private String documentationNodeName;
    private String documentationNodeGuid;

    private boolean biDirectionalConnections;

    public Parser()
    {
        types = new HashMap<>();
        instances = new HashMap<>();
        documentationNodeName = "Doc: Language";
    }

    public Parser(String documentationNodeName)
    {
        types = new HashMap<>();
        instances = new HashMap<>();
        this.documentationNodeName = documentationNodeName;
    }

    public Parser(String documentationNodeName, boolean biDirectionalConnections)
    {
        this(documentationNodeName);
        this.biDirectionalConnections = biDirectionalConnections;
    }

    @Override
    public void parse(String deploymentJson)
    {
        JSONObject nodes = new JSONObject(deploymentJson);

        parseTypes(findNode("ROOT", nodes), nodes);
        buildTypeDocumentation(nodes);
        parseNodes(nodes);
    }

    private JSONObject findNode(String name, JSONObject nodes)
    {
        return nodes.keySet().stream()
                .map(nodes::getJSONObject)
                .filter(n -> {
                    JSONObject attr = n.getJSONObject("attributes");
                    return attr.has("name") && attr.getString("name").equals(name);
                })
                .findFirst()
                .get();
    }

    private void parseTypes(JSONObject root, JSONObject nodes)
    {
        JSONObject metaNodes = root.getJSONObject("sets").getJSONObject("MetaAspectSet");

        Map<String, JSONObject> typeNodes = metaNodes.keySet().stream()
                .collect(Collectors.toMap(Function.identity(), nodes::getJSONObject));

        processTypes(typeNodes);
    }

    private void parseNodes(JSONObject nodes)
    {
        Map<String, JSONObject> instanceNodes = nodes.keySet().stream()
                .filter(k -> !types.containsKey(k))
                .collect(Collectors.toMap(Function.identity(), nodes::getJSONObject));

        processInstances(instanceNodes);
    }

    private void processTypes(Map<String, JSONObject> typeNodes)
    {
        ArrayDeque<String> processStack = new ArrayDeque<>();

        for(String id : typeNodes.keySet())
        {
            if(!types.containsKey(id))
            {
                processStack.push(id);
                while(!processStack.isEmpty())
                {
                    if(types.containsKey(processStack.peek()))
                    {
                        processStack.pop();
                        return;
                    }

                    JSONObject typeObj = typeNodes.get(processStack.peek());

                    if(typeObj.isNull("base") || types.containsKey(typeObj.getString("base")))
                    {
                        TypeAbstraction parent = null;
                        if(!typeObj.isNull("base"))
                            parent = types.get(typeObj.getString("base"));

                        String curId = processStack.pop();

                        // Build our attributes
                        JSONObject attributes = typeObj.getJSONObject("attributes");

                        String name = attributes.getString("name");

                        List<FieldValue> fieldValues = new ArrayList<>();
                        for(String attribute : attributes.keySet())
                        {
                            if(attribute.equals("name"))
                                continue;

                            ValuePrimitive value = ValuePrimitive.instantiatePrimitive(attributes.get(attribute));
                            fieldValues.add(new FieldValue(attribute, value));
                        }

                        TypeAbstraction type = new TypeAbstraction();
                        type.setUuid(curId);
                        type.setName(name);
                        type.setParent(parent);

                        if(!fieldValues.isEmpty())
                            type.setFieldValues(fieldValues.toArray(new FieldValue[0]));

                        types.put(curId, type);
                    }
                    else
                    {
                        processStack.push(typeObj.getString("base"));
                    }
                }
            }
        }
    }


    private void buildTypeDocumentation(JSONObject nodes)
    {
        documentationNodeGuid = types.keySet().stream()
                .filter(n -> types.get(n).getName().equals(documentationNodeName))
                .findFirst()
                .orElse(null);

        if(documentationNodeGuid == null)
        {
            System.out.println("Warning: Documentation node " + documentationNodeName + " not found!");
            return;
        }

        TypeAbstraction docParent = types.get(documentationNodeGuid);

        // Set the parent's comments field
        docParent.setComments(Arrays.stream(docParent.getFieldValues())
                .filter(n -> n.getName().equals("documentation"))
                .map(FieldValue::getValue)
                .map(v -> (ValuePrimitive<String>) v)
                .map(ValuePrimitive::getValue)
                .findFirst().get());

        // Get all instance nodes that are contained within a type node
        Set<String> docNodeSet = nodes.keySet().stream()
                .filter(n -> {
                    JSONObject node = nodes.getJSONObject(n);
                    return !types.containsKey(n) && !node.isNull("base") && !node.isNull("parent") &&
                            types.containsKey(node.getString("parent"));
                })
                .collect(Collectors.toSet());

        // Get all instance nodes that are (or potentially are) children of the documentation type node
        Map<String, JSONObject> docNodes = docNodeSet.stream()
                .filter(n -> {
                    String b = nodes.getJSONObject(n).getString("base");
                    return b.equals(documentationNodeGuid) || docNodeSet.contains(b);
                })
                .collect(Collectors.toMap(Function.identity(), nodes::getJSONObject));

        ArrayDeque<String> processStack = new ArrayDeque<>();
        for(String id : docNodes.keySet())
        {
            if(!instances.containsKey(id))
            {
                processStack.push(id);
                while(!processStack.isEmpty())
                {
                    JSONObject node = docNodes.get(processStack.peek());

                    String baseId = node.getString("base");
                    if(baseId.equals(documentationNodeGuid) || instances.containsKey(baseId))
                    {
                        String curId = processStack.pop();
                        ObjectInstance docInstance = buildInstance(curId, baseId, node.getString("parent"),
                                node.getJSONObject("attributes"));

                        instances.put(curId, docInstance);
                    }
                    else
                    {
                        if(!types.containsKey(baseId))
                            processStack.push(baseId);
                    }
                }
            }
        }
    }

    private void processInstances(Map<String, JSONObject> instanceNodes)
    {
        Set<String> processedIds = new HashSet<>(instances.keySet());
        ArrayDeque<String> processStack = new ArrayDeque<>();

        for(String id : instanceNodes.keySet())
        {
            if(!processedIds.contains(id))
            {
                processStack.push(id);
                while(!processStack.isEmpty())
                {
                    if(processedIds.contains(processStack.peek()))
                    {
                        processStack.pop();
                        continue;
                    }

                    JSONObject nodeObj = instanceNodes.get(processStack.peek());

                    String baseId = null;
                    if(!nodeObj.isNull("base"))
                        baseId = nodeObj.getString("base");

                    String containerId = null;
                    if(!nodeObj.isNull("parent"))
                        containerId = nodeObj.getString("parent");

                    boolean foundParent = false;
                    if(baseId == null || types.containsKey(baseId) || instances.containsKey(baseId))
                        foundParent = true;

                    boolean foundContainer = false;
                    if(containerId == null || types.containsKey(containerId) || instances.containsKey(containerId))
                        foundContainer = true;

                    JSONObject pointers = nodeObj.getJSONObject("pointers");

                    String srcId = null;
                    String dstId = null;
                    boolean isConnection = false;
                    boolean foundSrc = false;
                    boolean foundDst = false;
                    if(pointers.has("src") && pointers.has("dst") && !pointers.isNull("src") && !pointers.isNull("dst"))
                    {
                        srcId = pointers.getString("src");
                        dstId = pointers.getString("dst");

                        isConnection = true;
                        foundSrc = types.containsKey(srcId) || instances.containsKey(srcId);
                        foundDst = types.containsKey(dstId) || instances.containsKey(dstId);
                    }

                    if(foundParent && foundContainer && (!isConnection || (foundSrc && foundDst)))
                    {
                        String curId = processStack.pop();
                        ObjectInstance instance = buildInstance(curId, baseId, containerId, nodeObj.getJSONObject("attributes"));

                        if(isConnection)
                        {
                            // Build the destination side first on a bidirectional connections
                            if(biDirectionalConnections)
                                addConnection(instance.copy(), dstId, srcId);

                            addConnection(instance, srcId, dstId);
                        }
                        else instances.put(curId, instance);

                        processedIds.add(curId);
                    }
                    else // collect the prerequisite nodes
                    {
                        if(isConnection)
                        {
                            if(!foundDst)
                                processStack.push(dstId);
                            if(!foundSrc)
                                processStack.push(srcId);
                        }

                        if(!foundParent)
                            processStack.push(baseId);
                        if(!foundContainer)
                            processStack.push(containerId);
                    }
                }
            }
        }
    }

    private void addConnection(ObjectInstance connection, String srcId, String dstId)
    {
        ObjectInstance source = instances.get(srcId);
        ObjectInstance destination = instances.get(dstId);

        // Create destination value
        ValueComplex destinationValue = new ValueComplex();
        destinationValue.setValue(destination);
        destinationValue.setType(destination.getInstanceType());

        // Create connection value pointer
        ValueComplex connectionValue = new ValueComplex();
        connectionValue.setValue(connection);
        connectionValue.setType(connection.getInstanceType());
        connectionValue.setPointer(true);

        // Update the connection with a field to the destination
        connection.addFieldValue(new FieldValue(destination.getName(), destinationValue));

        // Add the connection to the source
        source.addFieldValue(new FieldValue(connection.getName(), connectionValue));
    }


    private ObjectInstance buildInstance(String nodeId, String baseId, String containerId, JSONObject attributes)
    {
        ObjectInstance parent = null;

        boolean isDocumentation = false;
        if(baseId != null)
        {
            if(types.containsKey(baseId))
                parent = types.get(baseId).makeInstance();
            else
                parent = instances.get(baseId);

            isDocumentation = parent.getInstanceType().getUuid().equals(documentationNodeGuid);
        }

        // parse attributes
        String name = "";
        if(attributes.has("name"))
            name = attributes.getString("name");
        else if(parent != null)
            name = parent.getName();

        String comments = null;
        if(isDocumentation)
        {
            if(attributes.has("documentation"))
                comments = attributes.getString("documentation");
            else
                comments = parent.getComments();
        }

        // Collect field values
        List<FieldValue> fieldValues = new ArrayList<>();
        for(String attribute : attributes.keySet())
        {
            if(attribute.equals("name") || attribute.equals("documentation"))
                continue;

            ValuePrimitive value = ValuePrimitive.instantiatePrimitive(attributes.get(attribute));
            fieldValues.add(new FieldValue(attribute, value));
        }

        // Build the instance
        ObjectInstance instance = new ObjectInstance();
        instance.setUuid(nodeId);
        instance.setName(name);
        instance.setInstanceParent(parent);

        if(parent != null)
            instance.setInstanceType(parent.getInstanceType());

        if(isDocumentation)
            instance.setComments(comments);

        if(!fieldValues.isEmpty())
            instance.setFieldValues(fieldValues.toArray(new FieldValue[0]));

        // Add this instance node to it's container's field values
        if(containerId != null)
        {
            FieldValueContainer container;
            if(types.containsKey(containerId))
                container = types.get(containerId);
            else
                container = instances.get(containerId);

            // Set the comment field of the container if this is a documentation node
            if(isDocumentation)
                container.setComments(comments);

            // Build the field value
            ValueComplex value = new ValueComplex();
            value.setValue(instance);

            if(parent != null)
                value.setType(parent.getInstanceType());

            FieldValue fieldValue = new FieldValue(name, value);

            // Update the container's field values with this instance
            container.addFieldValue(fieldValue);
        }

        return instance;
    }

    @Override
    public Collection<TypeAbstraction> getTypes()
    {
        return types.values();
    }

    @Override
    public Collection<ObjectInstance> getInstances()
    {
        return instances.values();
    }
}
