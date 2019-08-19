package com.securboration.test.xsdmut;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.sf.saxon.lib.NamespaceConstant;

public class XsdMutator {
    
    private static List<Node> xpath(
            final Node node, 
            final String query,
            final String...namespaceMappings
            ) throws XPathFactoryConfigurationException, XPathExpressionException{
        final NamespaceContextImpl context = new NamespaceContextImpl(
            "xsd", "http://www.w3.org/2001/XMLSchema"
            );
        context.add(namespaceMappings);
        
        XPathFactory xpathfactory = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
        XPath xpath = xpathfactory.newXPath();
        xpath.setNamespaceContext(context);
        XPathExpression expr = xpath.compile(query);
        
        NodeList list = (NodeList) expr.evaluate(node, XPathConstants.NODESET);
        
        ArrayList<Node> nodes = new ArrayList<>();
        for(int i=0;i<list.getLength();i++){
            nodes.add(list.item(i));
        }
        
        return nodes;
    }
    
    
    
    private static class CategoryNodeMap{
        private final Map<String,Set<Node>> categories = new LinkedHashMap<>();
        
        private void add(final String category, final Node node){
            Set<Node> nodesForCategory = categories.get(category);
            
            if(nodesForCategory == null){
                nodesForCategory = new LinkedHashSet<>();
                categories.put(category, nodesForCategory);
            }
            
            nodesForCategory.add(node);
        }
        
        private Set<Node> getCategorized(){
            final Set<Node> c = new LinkedHashSet<>();
            
            categories.values().forEach(v->c.addAll(v));
            
            return c;
        }
        
        private Set<String> getCategories(){
            return categories.keySet();
        }
        
        private Set<Node> getNodes(final String category){
            return categories.get(category);
        }
        
        private void map(
                final Collection<Node> nodes, 
                String...kvs
                ) throws XPathFactoryConfigurationException, XPathExpressionException{
            
            for(Node n:nodes){
                for(int i=0;i<kvs.length;i+=2){
                    final String category = kvs[i];
                    final String xpath = kvs[i+1];
                    
                    if(xpath(n,xpath).size() == 0){
                        continue;
                    }
                    
                    add(category,n);
                    break;//TODO:each node goes into its first matching category, might not be a good thing
                }
            }
            
            {
                final List<Node> uncategorized = new ArrayList<>();
                for(Node n:nodes){
                    boolean found = false;
                    for(Set<Node> category:categories.values()){
                        if(category.contains(n)){
                            found = true;
                            break;
                        }
                    }
                    
                    if(!found){
                        uncategorized.add(n);
                    }
                }
                
                for(Node n:uncategorized){
                    add("uncategorized",n);
                }
            }
        }
    }
    
    /*
Configuration is via -D flags.  The minimal configuration to run requires that you specify an input XSD file (contained in a flat directory with all of the XSD dependencies but nothing else) and an output directory (which will be deleted each time the tool runs).

java -jar schema-mutator.jar -DXsdMutatorConfig.inputXsd=./xmlInput/MDL_v0_8_19.xsd -DXsdMutatorConfig.outputDir="./xmlOutput/mutatedSchemas/test1/"

The tool emits a mutation report with xpath expressions to point you to the things that were changed.

Additional configuration parameters for an example run are listed below and example output is attached:

# Path to an XSD to mutate.  Should reside in a flat directory containing only XSD content.
XsdMutatorConfig.inputXsd = C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\src\\main\\java\\zxml\\MDL_v0_8_19.xsd

# Path to a directory to dump output into.  Defaults to ./out
XsdMutatorConfig.outputDir = ./target/mutatedSchemas/

# Iff specified, this seed value will be used.  Otherwise, a random seed will be used.
# XsdMutatorConfig.rngSeed = null

# The number of sequence element shuffles to perform (default is 10).
XsdMutatorConfig.numSequenceShuffles = 10

# The number of sequence element deletions to perform (default is 10).
XsdMutatorConfig.numSequenceElementDeletions = 10

# The number of sequence element renames to perform (default is 10).
XsdMutatorConfig.numSequenceElementRenames = 10

# The number of type renames to perform (default is 10).
XsdMutatorConfig.numTypeRenames = 10

# The number of sequence element type changes to perform (default is 10).
XsdMutatorConfig.numElementTypeChanges = 10

# The number of min/max occurs changes to perform (default is 10).
XsdMutatorConfig.numMinMaxOccursChanges = 10

# The number of enumeration element deletions to perform (default is 10).
XsdMutatorConfig.numEnumDeletions = 10

     */
    public static void main(String[] args) throws Exception {
        {//magic
            System.setProperty(
                "javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON, 
                "net.sf.saxon.xpath.XPathFactoryImpl"
                );
        }//the gathering
        
        final XsdMutatorConfig config = 
                ConfigurationHelper.acquire(XsdMutatorConfig.class);
        
        System.out.println(ConfigurationHelper.dumpConfig(config));
        
        
        //config properties
        final File inputXsd = new File(config.getInputXsd());//"C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\src\\main\\java\\zxml\\MDL_v0_8_19.xsd"
        final File outputDir = new File(config.getOutputDir());//"./target/mutatedSchemas/"
        
        final Random rng = config.getRngSeed() == null ? new Random() : new Random(config.getRngSeed());
        
        
        
        final int numSequenceShuffles = config.getNumSequenceShuffles();
        final int numSequenceElementDeletions = config.getNumSequenceElementDeletions();
        final int numSequenceElementRenames = config.getNumSequenceElementRenames();
        final int numTypeRenames = config.getNumTypeRenames();
        final int numElementTypeChanges = config.getNumElementTypeChanges();
        final int numMinMaxOccursChanges = config.getNumMinMaxOccursChanges();
        final int numEnumDeletions = config.getNumEnumDeletions();
        
        final int maxRenamePrefixSize = config.getMaxRenamePrefixSize();//max # of random chars to prepend to a mutated name
        final double renameCharacterMutationRate = config.getRenameCharacterMutationRate();//rate of mutation for original name chars
        final int maxRenameSuffixSize = config.getMaxRenameSuffixSize();//max # of random chars to append to a mutated name
        
        final int numIterations = config.getNumIterations();
        
        
        
        {
            FileUtils.deleteQuietly(outputDir);
            FileUtils.forceMkdir(outputDir);
            
            FileUtils.copyDirectory(inputXsd.getParentFile(), outputDir);
        }
        
        File xsdForIteration = inputXsd;
        for(int iterationCount=0;iterationCount<numIterations;iterationCount++){
            System.out.printf("mutation iteration %d of %d\n", iterationCount+1, numIterations);
            
            final Report r = new Report();
            final Document doc = XmlUtils.parse(xsdForIteration);
            
            {
                final Set<Node> interestingNodes = new LinkedHashSet<>(xpath(
                    doc,  
                    "/xsd:schema/xsd:complexType"
                    ));
                
                interestingNodes.addAll(xpath(doc,  
                    "/xsd:schema/xsd:simpleType"));
                
                final CategoryNodeMap categories = new CategoryNodeMap();
                
                categories.map(
                    interestingNodes, 
                    "sequenceType", "xsd:sequence",
                    "enumType", "xsd:restriction[@base='xsd:string']"
//                    "simpleContentType", "xsd:simpleContent",
//                    "complexContentType", "xsd:complexContent"
//                    
//                    ,"refType", "xsd:attribute"
                    );
                
//                final Set<Node> categorized = categories.getCategorized();
//                
//                for(Node n:complexTypes){
//                    if(!categorized.contains(n)){
//                        System.out.println(XmlUtils.prettyPrint(n));
//                    }
//                }
                
                {//print histogram
                    System.out.printf("\t%d complex types\n", interestingNodes.size());
                    int sum = 0;
                    for(final String c:categories.getCategories()){
                        final Set<Node> nodes = categories.getNodes(c);
                        
                        System.out.printf(
                            "\t\t%d complex types in category \"%s\"\n", 
                            nodes.size(),
                            c
                            );
                        sum += nodes.size();
                    }
                    System.out.printf("\tsum of category sizes is %d\n", sum);
                }
                
                
                //mutation loops
                
                for(int i=0;i<numEnumDeletions;i++){//delete enum values
                    final Set<Node> sequenceTypes = categories.getNodes("enumType");
                    final Node n = RandomUtils.randomElementFromSet(rng,sequenceTypes);
                    if(isProblematicNode(n)){
                        continue;
                    }
                    
                    final Node restriction = xpath(n,"xsd:restriction").get(0);
                    
                    final List<Node> enumValues = xpath(restriction,"xsd:enumeration");
                    
                    if(enumValues.size() == 0){
                        continue;
                    }
                    
                    {//perform delete
                        Node random = enumValues.get(rng.nextInt(enumValues.size()));
                        
                        restriction.removeChild(random);
                        
//                        System.out.println(XmlUtils.prettyPrint(n));
                        
                        r.$(
                            "\t%s: deleted enum value \"%s\"\n", 
                            XmlUtils.getXPathTo(restriction),
                            random.getAttributes().getNamedItem("value").getNodeValue()
                            );
                    }
                }
                
                for(int i=0;i<numSequenceShuffles;i++){//shuffle
                    final Set<Node> sequenceTypes = categories.getNodes("sequenceType");
                    final Node n = RandomUtils.randomElementFromSet(rng,sequenceTypes);
                    if(isProblematicNode(n)){
                        continue;
                    }
                    
                    {//perform shuffle
                        final Node sequence = xpath(n,"xsd:sequence").get(0);
                        
                        final List<Node> elements = xpath(
                            sequence, 
                            "xsd:element"
                            );
                        
                        if(elements.size() == 0){
                            continue;
                        }
                        
                        final int index1 = rng.nextInt(elements.size());
                        final Node node1 = elements.get(index1);
                        final int index2 = rng.nextInt(elements.size());
                        final Node node2 = elements.get(index2);
                        
                        elements.set(index1, node2);
                        elements.set(index2, node1);
                        
                        r.$(
                            "\t%s: swapped sequence elements %d (\"%s\", a %s) and %d (\"%s\", a %s)\n", 
                            XmlUtils.getXPathTo(sequence),
                            index1,XmlUtils.getNodeNameAttribute(node1),XmlUtils.getNodeTypeAttribute(node1),
                            index2,XmlUtils.getNodeNameAttribute(node2),XmlUtils.getNodeTypeAttribute(node2)
                            );
                        
                        //remove them
                        for(Node element:elements){
                            sequence.removeChild(element);
                        }
                        
                        //add them back in shuffled order
                        for(Node element:elements){
                            sequence.appendChild(element);
                        }
                    }
                }
                
                for(int i=0;i<numMinMaxOccursChanges;i++){//change min/max occurs
                    final Set<Node> sequenceTypes = categories.getNodes("sequenceType");
                    final Node n = RandomUtils.randomElementFromSet(rng,sequenceTypes);
                    if(isProblematicNode(n)){
                        continue;
                    }
                    
                    {//perform occurrence mutation
                        final Node sequence = xpath(n, "xsd:sequence").get(0);
                        final List<Node> elements = xpath(sequence, "xsd:element");
                        
                        if(elements.size() == 0){
                            continue;
                        }

                        final Node element = elements.get(rng.nextInt(elements.size()));

                        XsdTypeMutationMappings.mutateOccurrence(rng, (Element) element, r);
                    }
                }
                
                for(int i=0;i<numSequenceElementDeletions;i++){//delete sequence elements
                    final Set<Node> sequenceTypes = categories.getNodes("sequenceType");
                    final Node n = RandomUtils.randomElementFromSet(rng,sequenceTypes);
                    if(isProblematicNode(n)){
                        continue;
                    }
                    
                    {//perform deletion
                        final Node sequence = xpath(n,"xsd:sequence").get(0);
                        
                        final List<Node> elements = xpath(
                            sequence, 
                            "xsd:element"
                            );
                        
                        if(elements.size() == 0){
                            continue;
                        }
                        
                        final int removeIndex = rng.nextInt(elements.size());
                        final Node elementToRemove = elements.get(removeIndex);
                        
                        {
                            sequence.removeChild(elementToRemove);
                            elements.remove(elementToRemove);
                            
                            r.$(
                                "\t%s: deleted sequence element named \"%s\" with type %s\n", 
                                XmlUtils.getXPathTo(sequence),
                                XmlUtils.getNodeNameAttribute(elementToRemove), 
                                XmlUtils.getNodeTypeAttribute(elementToRemove)
                                );
                        }
                    }
                }
                
                
                for(int i=0;i<numSequenceElementRenames;i++){//rename sequence elements
                    final Set<Node> sequenceTypes = categories.getNodes("sequenceType");
                    final Node n = RandomUtils.randomElementFromSet(rng,sequenceTypes);
                    if(isProblematicNode(n)){
                        continue;
                    }
                    
                    {//perform the rename
                        final Node sequence = xpath(n,"xsd:sequence").get(0);
                        
                        final List<Node> elements = xpath(
                            sequence, 
                            "xsd:element"
                            );
                        
                        if(elements.size() == 0){
                            continue;
                        }
                        
                        final int renameIndex = rng.nextInt(elements.size());
                        final Node elementToRename = elements.get(renameIndex);
                        
                        final Node nameNode = elementToRename.getAttributes().getNamedItem("name");
                        final Node oldName = elementToRename.getAttributes().getNamedItem("name");
                        
                        final String newName = RandomUtils.randomElementName(
                            rng, 
                            oldName.getNodeValue(),
                            rng.nextInt(maxRenamePrefixSize),
                            renameCharacterMutationRate,
                            rng.nextInt(maxRenameSuffixSize)
                            );
                        r.$("\t%s: renamed element \"%s\" (a %s) to \"%s\"\n",
                            XmlUtils.getXPathTo(sequence),
                            oldName,
                            XmlUtils.getNodeTypeAttribute(elementToRename),
                            newName
                            );

                        nameNode.setNodeValue(newName);
                    }
                }
                
                for(int i=0;i<numTypeRenames;i++){//rename type
                    final Set<Node> sequenceTypes = categories.getNodes("sequenceType");
                    final Node n = RandomUtils.randomElementFromSet(rng,sequenceTypes);
                    if(isProblematicNode(n)){
                        continue;
                    }
                    
                    {//perform the rename
                        final Node nameNode = n.getAttributes().getNamedItem("name");
                        final String oldName = nameNode.getNodeValue();
                        
                        //select a random type with a name="X", rename X to X'
                        //replace all type="X" instances with type="X'"
                        
                        final String newName = RandomUtils.randomElementName(
                            rng, 
                            oldName,
                            rng.nextInt(maxRenamePrefixSize),
                            renameCharacterMutationRate,
                            rng.nextInt(maxRenameSuffixSize)
                            );
                        r.$(
                            "\t%s: renamed type \"%s\" to \"%s\"\n", 
                            XmlUtils.getXPathTo(n),
                            oldName,
                            newName
                            );
                        
                        nameNode.setNodeValue(newName);
                        
                        //that's only half the equation though-- 
                        //we have to ALSO replace every reference to the old name
                        
                        //xsd:complexType/xsd:sequence/
                        {
                            final String xpath = "//xsd:element[@type='" + oldName + "']";
                            final List<Node> references = xpath(doc,xpath);
                            for(Node ref:references){
                                r.$(
                                    "\t\t%s: changed use of old type name \"%s\" to \"%s\"\n", 
                                    XmlUtils.getXPathTo(ref),
                                    oldName,
                                    newName
                                    );
                                
                                ref.getAttributes().getNamedItem("type").setNodeValue(newName);
                            }
                        }
                        {//<xsd:extension base="DataOperationType">
                            final String xpath = "//xsd:extension[@base='" + oldName + "']";
                            final List<Node> references = xpath(doc,xpath);
                            for(Node ref:references){
                                r.$(
                                    "\t\t%s: changed use of old type name \"%s\" to \"%s\"\n", 
                                    XmlUtils.getXPathTo(ref),
                                    oldName,
                                    newName
                                    );
                                
                                ref.getAttributes().getNamedItem("base").setNodeValue(newName);
                            }
                        }
                    }
                }
                
                for(int i=0;i<numElementTypeChanges;i++){//change element type
                    final Set<Node> sequenceTypes = categories.getNodes("sequenceType");
                    final Node n = RandomUtils.randomElementFromSet(rng,sequenceTypes);
                    if(isProblematicNode(n)){
                        continue;
                    }
                    
                    {//perform the change
                        final Node sequence = xpath(n,"xsd:sequence").get(0);
                        final List<Node> elements = xpath(
                            sequence, 
                            "xsd:element"
                            );
                        
                        if(elements.size() == 0){
                            continue;
                        }
                        
                        final Node element = elements.get(rng.nextInt(elements.size()));
                        
                        final String type = XmlUtils.getNodeTypeAttribute(element);
                        
                        if(XsdTypeMutationMappings.xsdPrimitiveTypeMap.containsKey(type)){
                            final String[] alts = XsdTypeMutationMappings.xsdPrimitiveTypeMap.get(type);
                            
                            final String alt = alts[rng.nextInt(alts.length)];
                            
                            element.getAttributes().getNamedItem("type").setNodeValue(alt);
                            
                            r.$(
                                "\t%s: changed type of sequence element \"%s\" from %s to %s\n", 
                                XmlUtils.getXPathTo(sequence),
                                XmlUtils.getNodeNameAttribute(element), 
                                type,alt
                                );
                            
                            if(element.getAttributes().getNamedItem("default") != null){
                                Node defaultValue = element.getAttributes().removeNamedItem("default");
                                
                                r.$(
                                    "\t%s: removed default type of sequence element \"%s\" (was \"%s\")\n", 
                                    XmlUtils.getXPathTo(sequence),
                                    XmlUtils.getNodeNameAttribute(element), 
                                    defaultValue.getNodeValue()
                                    );
                            }
                        } else {
                            final List<Node> otherTypes = xpath(doc,"xsd:schema/xsd:complexType");
                            final String otherType = XmlUtils.getNodeNameAttribute(
                                otherTypes.get(rng.nextInt(otherTypes.size()))
                                );
                            
                            element.getAttributes().getNamedItem("type").setNodeValue(otherType);
                            
                            r.$(
                                "\t%s: changed type of sequence element \"%s\" from %s to %s\n", 
                                XmlUtils.getXPathTo(sequence),
                                XmlUtils.getNodeNameAttribute(element), 
                                type,otherType
                                );
                        }
                    }
                }
                
                
                
            }
            
            FileUtils.copyFile(inputXsd, new File(outputDir,inputXsd.getName() + "_ORIGINAL"));
            
            final int iterationNumber = iterationCount+1;
            FileUtils.writeStringToFile(
                new File(outputDir,"mutationReport.dat"), 
                "iteration " + iterationNumber + " of " + numIterations + " ", 
                StandardCharsets.UTF_8,
                true
                );
            
            FileUtils.writeStringToFile(
                new File(outputDir,"mutationReport.dat"), 
                r.toString(), 
                StandardCharsets.UTF_8,
                true
                );
            
            FileUtils.writeStringToFile(
                new File(outputDir,inputXsd.getName()), 
                XmlUtils.prettyPrint(doc),
                StandardCharsets.UTF_8
                );
            
            {//check that the schema is valid
                final File xsdToCheck = new File(outputDir,inputXsd.getName());
                System.out.printf("\tchecking integrity of XSD %s\n",xsdToCheck.getCanonicalPath());
                final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                
                Schema schema = factory.newSchema(xsdToCheck);
                Validator validator = schema.newValidator();
                
                System.out.printf("\t\tintegrity check passed!\n");
                System.out.printf("\temitted mutated schema to %s\n",xsdToCheck.getCanonicalPath());
            }
            
            xsdForIteration = new File(outputDir,inputXsd.getName());
        }
    }
    
    private static boolean isProblematicNode(Node n) throws XPathFactoryConfigurationException, XPathExpressionException{
        {//don't transform sequences containing xsd:any--too easy to introduce ambiguity
            final List<Node> elements = xpath(
                n, 
                "xsd:sequence/xsd:any"
                );
            
            if(elements.size() > 0){
                return true;
            }
        }//TODO: kludge
        
        {//don't transform choice elements
            final List<Node> elements = xpath(
                n, 
                "xsd:sequence/xsd:choice/xsd:element"
                );
            
            if(elements.size() > 0){
                return true;
            }
        }//TODO: kludge
        
        return false;
    }
    
    private static class XsdTypeMutationMappings{
        private static final Map<String,String[]> xsdPrimitiveTypeMap = new HashMap<>();
        
        private static final Map<String,String[]> occurrenceMap = new HashMap<>();
        
        static void mutateOccurrence(
                final Random rng, 
                final Element element,
                final Report r
                ){
            
            {//min
                final Node minOccurs = element.getAttributes().getNamedItem("minOccurs");
                if(minOccurs == null){
                    String newOccurrence = getRandomOccurrence(rng,"1");
                    if(newOccurrence.trim().equals("unbounded")){
                        newOccurrence = "0";
                    }
                    element.setAttribute("minOccurs", newOccurrence);
                    
                    r.$(
                        "\t%s: changed minOccurs attribute of sequence element \"%s\" from 1 (the default value) to %s\n", 
                        XmlUtils.getXPathTo(element),
                        XmlUtils.getNodeNameAttribute(element), 
                        newOccurrence
                        );
                } else {
                    String newOccurrence = XsdTypeMutationMappings.getRandomOccurrence(rng, minOccurs.getNodeValue());
                    if(newOccurrence.trim().equals("unbounded")){
                        newOccurrence = "0";
                    }
                    
                    r.$(
                        "\t%s: changed minOccurs attribute of sequence element \"%s\" from %s to %s\n", 
                        XmlUtils.getXPathTo(element),
                        XmlUtils.getNodeNameAttribute(element), 
                        minOccurs.getNodeValue(),newOccurrence
                        );
                    
                    element.setAttribute("minOccurs", newOccurrence);
                }
            }
            
            {//max
                final Node maxOccurs = element.getAttributes().getNamedItem("maxOccurs");
                if(maxOccurs == null){
                    String newOccurrence = getRandomOccurrence(rng,"1");
                    if(newOccurrence.trim().equals("0")){
                        newOccurrence = "unbounded";
                    }//handle the case where we accidentally specify something invalid (min > max)
                    
                    element.setAttribute("maxOccurs", newOccurrence);
                    
                    r.$(
                        "\t%s: changed maxOccurs attribute of sequence element \"%s\" from 1 (the default value) to %s\n", 
                        XmlUtils.getXPathTo(element),
                        XmlUtils.getNodeNameAttribute(element), 
                        newOccurrence
                        );
                } else {
                    String newOccurrence = XsdTypeMutationMappings.getRandomOccurrence(rng, maxOccurs.getNodeValue());
                    
                    if(newOccurrence.trim().equals("0")){
                        newOccurrence = "unbounded";
                    }//handle the case where we accidentally specify something invalid (min > max)
                    
                    r.$(
                        "\t%s: changed maxOccurs attribute of sequence element \"%s\" from %s to %s\n", 
                        XmlUtils.getXPathTo(element),
                        XmlUtils.getNodeNameAttribute(element), 
                        maxOccurs.getNodeValue(),newOccurrence
                        );
                    
                    element.setAttribute("maxOccurs", newOccurrence);
                }
            }
        }
        
        static String getRandomOccurrence(final Random rng, final String current){
            if(occurrenceMap.containsKey(current)){
                final String[] options = occurrenceMap.get(current);
                return options[rng.nextInt(options.length)];
            } else {
                final int currentVal = Integer.parseInt(current);
                
                return ""+rng.nextInt(2*currentVal);
            }
        }
        
        static{
            {//occurrences
                occurrenceMap.put("0", new String[]{"1","unbounded"});
                occurrenceMap.put("1", new String[]{"0","unbounded"});
                occurrenceMap.put("unbounded", new String[]{"0","1"});
            }//occurrences
                
//            map.put(
//                "xsd:string",
//                new String[]{
//                        "xsd:string",
//                        "xsd:boolean",
//                        "xsd:decimal","xsd:float","xsd:double","xsd:integer",
//                        "xsd:duration","xsd:dateTime","xsd:date","xsd:time",
//                        "xsd:hexBinary","xsd:base64Binary",
//                        }
//                );//TEMPLATE
            
            xsdPrimitiveTypeMap.put(
                "xsd:string",
                new String[]{
                        "xsd:boolean",
                        "xsd:decimal","xsd:float","xsd:double","xsd:integer",
                        "xsd:duration","xsd:dateTime","xsd:date","xsd:time",
                        "xsd:hexBinary","xsd:base64Binary",
                        }
                );
            
            xsdPrimitiveTypeMap.put(
                "xsd:boolean",
                new String[]{
                        "xsd:string"
                        }
                );
            
            {//numerics
                xsdPrimitiveTypeMap.put(
                    "xsd:decimal",
                    new String[]{
                            "xsd:string",
                            "xsd:float","xsd:double","xsd:integer",
                            }
                    );
                xsdPrimitiveTypeMap.put(
                    "xsd:float",
                    new String[]{
                            "xsd:string",
                            "xsd:decimal","xsd:double","xsd:integer",
                            }
                    );
                xsdPrimitiveTypeMap.put(
                    "xsd:double",
                    new String[]{
                            "xsd:string",
                            "xsd:decimal","xsd:float","xsd:integer",
                            }
                    );
                xsdPrimitiveTypeMap.put(
                    "xsd:integer",
                    new String[]{
                            "xsd:string",
                            "xsd:decimal","xsd:float","xsd:double",
                            }
                    );
            }//numerics
            
            {//time
                xsdPrimitiveTypeMap.put(
                    "xsd:duration",
                    new String[]{
                            "xsd:string",
                            "xsd:dateTime","xsd:date","xsd:time",
                            }
                    );
                xsdPrimitiveTypeMap.put(
                    "xsd:dateTime",
                    new String[]{
                            "xsd:string",
                            "xsd:duration","xsd:date","xsd:time",
                            }
                    );
                xsdPrimitiveTypeMap.put(
                    "xsd:date",
                    new String[]{
                            "xsd:string",
                            "xsd:duration","xsd:dateTime","xsd:time",
                            }
                    );
                xsdPrimitiveTypeMap.put(
                    "xsd:time",
                    new String[]{
                            "xsd:string",
                            "xsd:duration","xsd:dateTime","xsd:date",
                            }
                    );
            }//time
            
            {//binary values
                xsdPrimitiveTypeMap.put(
                    "xsd:hexBinary",
                    new String[]{
                            "xsd:string",
                            "xsd:base64Binary",
                            }
                    );
                xsdPrimitiveTypeMap.put(
                    "xsd:base64Binary",
                    new String[]{
                            "xsd:string",
                            "xsd:hexBinary",
                            }
                    );
            }//binary values
        }
    }
    
    
    private static class RandomUtils{
        private static <T> T randomElementFromSet(final Random rng, final Set<T> set){
            final int index = rng.nextInt(set.size());
            
            Iterator<T> iterator = set.iterator();
            for(int i=0;i<index;i++){
                iterator.next();
            }
            
            return iterator.next();
        }//TODO: naive O(N) impl
        
        private static String randomElementName(
                final Random rng,
                final String originalName, 
                final int prefixLength,
                double retention,
                final int suffixLength
                ){
            if(retention > 1){
                retention = 1;
            }
            if(retention < 0){
                retention = 0;
            }
            
            final StringBuilder sb = new StringBuilder();
            
            sb.append(randomString(rng,prefixLength));
            
            for(int i=0;i<originalName.length();i++){
                if(rng.nextDouble() <= retention){
                    //change it
                    sb.append(randomString(rng,1));
                } else {
                    //keep it
                    sb.append(originalName.charAt(i));
                }
            }
            
            sb.append(randomString(rng,suffixLength));
            
            return sb.toString();
        }
        
        private static final String alphas = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz";
        private static String randomString(final Random rng, final int length){
            final StringBuilder sb = new StringBuilder();
            
            for(int i=0;i<length;i++){
                sb.append(alphas.charAt(rng.nextInt(alphas.length())));
            }
            
            return sb.toString();
        }
    }
    
    private static class XsltTemplates{
        
        private static String getRenameRefTypeXslt(
                final String oldName, 
                final String newName
                ){
            return null;
        }
        
        private static String getRenameComplexTypeXslt(
                final String oldName, 
                final String newName
                ){
            final String template = 
                    "<!-- \r\n" + 
                    "Renames complexType with name oldName to newName\r\n" + 
                    "-->\r\n" + 
                    "<xsl:stylesheet version=\"1.0\" \r\n" + 
                    "    xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\r\n" + 
                    "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">   \r\n" + 
                    "    <xsl:output method=\"xml\" version=\"1.0\" encoding=\"UTF-8\"/>\r\n" + 
                    "    \r\n" + 
                    "    <xsl:variable name=\"oldName\" select=\"'${OLD_NAME}'\"/>\r\n" + 
                    "    <xsl:variable name=\"newName\" select=\"'${NEW_NAME}'\"/>\r\n" + 
                    "       \r\n" + 
                    "    <xsl:template match=\"@*|node()\">\r\n" + 
                    "      <xsl:copy>\r\n" + 
                    "        <xsl:apply-templates select=\"@*|node()\"/>\r\n" + 
                    "      </xsl:copy>\r\n" + 
                    "    </xsl:template>\r\n" + 
                    "    \r\n" + 
                    "    <xsl:template match=\"xsd:complexType/@name[.=$oldName]\">\r\n" + 
                    "        <xsl:attribute name=\"name\">\r\n" + 
                    "            <xsl:value-of select=\"replace(.,$oldName,$newName)\"/>\r\n" + 
                    "        </xsl:attribute>\r\n" + 
                    "    </xsl:template>\r\n" + 
                    "    \r\n" + 
                    "    <xsl:template match=\"xsd:element/@type[.=$oldName]\">\r\n" + 
                    "        <xsl:attribute name=\"type\">\r\n" + 
                    "            <xsl:value-of select=\"replace(.,$oldName,$newName)\"/>\r\n" + 
                    "        </xsl:attribute>\r\n" + 
                    "    </xsl:template>\r\n" + 
                    "\r\n" + 
                    "</xsl:stylesheet>";
            
            return template
                    .replace("${OLD_NAME}", oldName)
                    .replace("${NEW_NAME}", newName)
                    ;
        }
    }
    
    private static class XmlUtils{
        private static String getNodeNameAttribute(Node n){
            final Node nameNode = n.getAttributes().getNamedItem("name");
            return nameNode.getNodeValue();
        }
        
        private static String getNodeTypeAttribute(Node n){
            final Node nameNode = n.getAttributes().getNamedItem("type");
            return nameNode.getNodeValue();
        }
        
        private static Document parse(final File input) throws ParserConfigurationException, SAXException, IOException{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(input);
        }
        
        private static String prettyPrint(Node doc)
                throws UnsupportedEncodingException, TransformerException {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount",
                "4"
                );

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            transformer.transform(
                new DOMSource(doc),
                new StreamResult(new OutputStreamWriter(out, "UTF-8")));

            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
        
        public static String getXPathTo(Node n) {
            // abort early
            if (null == n)
                return null;

            // declarations
            Node parent = null;
            Stack<Node> hierarchy = new Stack<Node>();
            StringBuffer buffer = new StringBuffer();

            // push element on stack
            hierarchy.push(n);

            switch (n.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                parent = ((Attr) n).getOwnerElement();
                break;
            case Node.ELEMENT_NODE:
                parent = n.getParentNode();
                break;
            case Node.DOCUMENT_NODE:
                parent = n.getParentNode();
                break;
            default:
                throw new IllegalStateException(
                        "Unexpected Node type" + n.getNodeType());
            }

            while (null != parent &&
                    parent.getNodeType() != Node.DOCUMENT_NODE) {
                // push on stack
                hierarchy.push(parent);

                // get parent of parent
                parent = parent.getParentNode();
            }

            // construct xpath
            Object obj = null;
            while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
                Node node = (Node) obj;
                boolean handled = false;

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;

                    // is this the root element?
                    if (buffer.length() == 0) {
                        // root element - simply append element name
                        buffer.append(node.getNodeName());
                    } else {
                        // child element - append slash and element name
                        buffer.append("/");
                        buffer.append(node.getNodeName());

                        if (node.hasAttributes()) {
                            // see if the element has a name or id attribute
                            if (e.hasAttribute("id")) {
                                // id attribute found - use that
                                buffer.append(
                                    "[@id='" + e.getAttribute("id") + "']");
                                handled = true;
                            } else if (e.hasAttribute("name")) {
                                // name attribute found - use that
                                buffer.append(
                                    "[@name='" + e.getAttribute("name") + "']");
                                handled = true;
                            }
                        }

                        if (!handled) {
                            // no known attribute we could use - get sibling
                            // index
                            int prev_siblings = 1;
                            Node prev_sibling = node.getPreviousSibling();
                            while (null != prev_sibling) {
                                if (prev_sibling.getNodeType() == node
                                        .getNodeType()) {
                                    if (prev_sibling.getNodeName()
                                            .equalsIgnoreCase(
                                                node.getNodeName())) {
                                        prev_siblings++;
                                    }
                                }
                                prev_sibling =
                                    prev_sibling.getPreviousSibling();
                            }
                            buffer.append("[" + prev_siblings + "]");
                        }
                    }
                } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                    buffer.append("/@");
                    buffer.append(node.getNodeName());
                }
            }
            // return buffer
            return buffer.toString();
        }
    }
    
    private static class Report{
        final StringBuilder sb = new StringBuilder();
        
        Report(){
            $("schema mutation report\n");
        }
        
        void $(final String format, Object...args){
            sb.append(String.format(format, args));
        }
        
        @Override
        public String toString(){
            return sb.toString();
        }
    }
    
    private static class XsltUtils{
        
        private static String translate(
                final String xslt,
                final Document xmlToTranslate
                ) throws JAXBException, TransformerException, UnsupportedEncodingException {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer(new StreamSource(new ByteArrayInputStream(xslt.getBytes())));

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.VERSION, "1.0");

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            transformer.transform(
                    new StreamSource(new ByteArrayInputStream(XmlUtils.prettyPrint(xmlToTranslate).getBytes())),
                    new StreamResult(out)
            );

            return out.toString();
        }
    }
    
    
    
    
    
    private static void traverse(
            final Node current,
            final List<Node> path
            ){
        
        for(int i=0;i<path.size();i++){
            System.out.printf("  ");
        }
        System.out.printf(
            "%s, node type = %d\n",
            current.getNodeName(),
            current.getNodeType()
            );
        
        NodeList children = current.getChildNodes();
        for(int i=0;i<children.getLength();i++){
            final Node child = children.item(i);
            final List<Node> newPath = new ArrayList<>(path);
            
            newPath.add(child);
            traverse(child,newPath);
        }
        
        
//        if(current.getNodeType() == Node.COMMENT_NODE){
//            return;//comment
//        } else if(current.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE){
//            return;
//        }
        
    }
    
    
    
    
    
    private static class NamespaceContextImpl implements NamespaceContext{
        private final Map<String,String> prefixesToUris = new HashMap<>();
        private final Map<String,Set<String>> urisToPrefixes = new HashMap<>();
        
        public NamespaceContextImpl(String...kvs){
            
            add(kvs);
        }
        
        public void add(String...kvs){
            for(int i=0;i<kvs.length;i+=2){
                final String k = kvs[i];
                final String v = kvs[i+1];
            
            
                prefixesToUris.put(k, v);
                
                Set<String> prefixes = urisToPrefixes.get(v);
                if(prefixes == null){
                    prefixes = new HashSet<>();
                    urisToPrefixes.put(v, prefixes);
                }
                
                prefixes.add(k);
            }
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return prefixesToUris.get(prefix);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            System.out.println("2: " + namespaceURI);
            return urisToPrefixes.get(namespaceURI).iterator().next();
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            System.out.println("3: " + namespaceURI);
            return urisToPrefixes.get(namespaceURI).iterator();
        }
    }
    
    
    
//    mutateSchema(int numMutations){
//        Node ct = selectRandomComplexType();
//
//        //rename the type
//        
//        if(isSequence(ct)){//<xsd:sequence>
//            //change the ordering of the sequence
//            //change the multiplicity of sequence elements (e.g., min1 max1 -> min0 max1)
//            //delete an element from the sequence
//            //add an element to the sequence
//        } else if(isSimpleContentExtension(ct)){//<xsd:simpleContent>
//            //
//        } else if(isAttributeRef(ct)){//<xsd:attribute name="IDREF" type="xsd:IDREF" use="required"/>
//            
//        }
    
    
//    private static class MutationConfig{
//        double probabilityOfRelation;
//    }
    
    
    private static class Graveyard{
        
        

//      final double probabilityOfShuffle = 1d;//swap two elements in a sequence
//      final double probabilityOfDeletionMutation = 1d;//delete an element in a sequence
//      final double probabilityOfRenameSequenceElementMutation = 1d;//rename an element in a sequence
//      final double probabilityOfTypeRenameMutation = 1d;//rename a type
//      
//      final double probabilityOfElementTypeMutation = 1d;//change element type
//      final double probabilityOfOccursMutation = 1d;//change min/max occurrs attribute
      

//        for(int i=0;i<numMinMaxOccursChanges;i++){//change min/max occurs
//            final Set<Node> sequenceTypes = categories.getNodes("sequenceType");
//            final Node n = RandomUtils.randomElementFromSet(rng,sequenceTypes);
//            if(isProblematicNode(n)){
//                continue;
//            }
//            
//            {//perform the min/max occurs mutation
//                final Node sequence = xpath(n,"xsd:sequence").get(0);
//                final List<Node> elements = xpath(
//                    sequence, 
//                    "xsd:element"
//                    );
//                
//                final Node element = elements.get(rng.nextInt(elements.size()));
//                
//                XsdTypeMutationMappings.mutateOccurrence(rng,(Element)element,r);
//            }
//        }
//        
//        
//        
//        for(int i=0;i<numMutationIterations;i++){
//            final Set<Node> sequenceTypes = categories.getNodes("sequenceType");
//            final Node n = RandomUtils.randomElementFromSet(rng,sequenceTypes);
//            
//            if(false){
//                System.out.printf(
//                    "selected node %d with @name=%s\n", 
//                    n.hashCode(),
//                    XmlUtils.getNodeNameAttribute(n)
//                    );
//                
//                System.out.println(XmlUtils.prettyPrint(n));
//            }
//            
//            {//don't transform sequences containing xsd:any--too easy to introduce ambiguity
//                final List<Node> elements = xpath(
//                    n, 
//                    "xsd:sequence/xsd:any"
//                    );
//                
//                if(elements.size() > 0){
//                    continue;
//                }
//            }//TODO: kludge
//            
//            {//don't transform choice elements
//                final List<Node> elements = xpath(
//                    n, 
//                    "xsd:sequence/xsd:choice/xsd:element"
//                    );
//                
//                if(elements.size() > 0){
//                    continue;
//                }
//            }//TODO: kludge
//            
//            if(rng.nextDouble() < probabilityOfShuffle){//shuffles a random number of elements in a sequence
//                final Node sequence = xpath(n,"xsd:sequence").get(0);
//                
//                final List<Node> elements = xpath(
//                    sequence, 
//                    "xsd:element"
//                    );
//                
//                if(elements.size() == 0){
//                    continue;
//                }
//                
//                final int index1 = rng.nextInt(elements.size());
//                final Node node1 = elements.get(index1);
//                final int index2 = rng.nextInt(elements.size());
//                final Node node2 = elements.get(index2);
//                
//                elements.set(index1, node2);
//                elements.set(index2, node1);
//                
//                r.$(
//                    "\t%s: swapped sequence elements %d (\"%s\", a %s) and %d (\"%s\", a %s)\n", 
//                    XmlUtils.getXPathTo(sequence),
//                    index1,XmlUtils.getNodeNameAttribute(node1),XmlUtils.getNodeTypeAttribute(node1),
//                    index2,XmlUtils.getNodeNameAttribute(node2),XmlUtils.getNodeTypeAttribute(node2)
//                    );
//                
//                //remove them
//                for(Node element:elements){
//                    sequence.removeChild(element);
//                }
//                
//                //add them back in shuffled order
//                for(Node element:elements){
//                    sequence.appendChild(element);
//                }
//            }
//            
//            if(rng.nextDouble() < probabilityOfOccursMutation){//changes minOccurs/maxOccurs for a random number of elements from a complex type
//                final Node sequence = xpath(n,"xsd:sequence").get(0);
//                final List<Node> elements = xpath(
//                    sequence, 
//                    "xsd:element"
//                    );
//                
//                final Node element = elements.get(rng.nextInt(elements.size()));
//                
//                XsdTypeMutationMappings.mutateOccurrence(rng,(Element)element,r);
//            }
//            
//            if(rng.nextDouble() < probabilityOfElementTypeMutation){//changes xsd type of simple elements within a complex type sequence
//                final Node sequence = xpath(n,"xsd:sequence").get(0);
//                final List<Node> elements = xpath(
//                    sequence, 
//                    "xsd:element"
//                    );
//                
//                final Node element = elements.get(rng.nextInt(elements.size()));
//                
//                final String type = XmlUtils.getNodeTypeAttribute(element);
//                
//                if(XsdTypeMutationMappings.xsdPrimitiveTypeMap.containsKey(type)){
//                    final String[] alts = XsdTypeMutationMappings.xsdPrimitiveTypeMap.get(type);
//                    
//                    final String alt = alts[rng.nextInt(alts.length)];
//                    
//                    element.getAttributes().getNamedItem("type").setNodeValue(alt);
//                    
//                    r.$(
//                        "\t%s: changed type of sequence element \"%s\" from %s to %s\n", 
//                        XmlUtils.getXPathTo(sequence),
//                        XmlUtils.getNodeNameAttribute(element), 
//                        type,alt
//                        );
//                    
//                    if(element.getAttributes().getNamedItem("default") != null){
//                        Node defaultValue = element.getAttributes().removeNamedItem("default");
//                        
//                        r.$(
//                            "\t%s: removed default type of sequence element \"%s\" (was \"%s\")\n", 
//                            XmlUtils.getXPathTo(sequence),
//                            XmlUtils.getNodeNameAttribute(element), 
//                            defaultValue.getNodeValue()
//                            );
//                    }
//                } else {
//                    final List<Node> otherTypes = xpath(doc,"xsd:schema/xsd:complexType");
//                    final String otherType = XmlUtils.getNodeNameAttribute(
//                        otherTypes.get(rng.nextInt(otherTypes.size()))
//                        );
//                    
//                    element.getAttributes().getNamedItem("type").setNodeValue(otherType);
//                    
//                    r.$(
//                        "\t%s: changed type of sequence element \"%s\" from %s to %s\n", 
//                        XmlUtils.getXPathTo(sequence),
//                        XmlUtils.getNodeNameAttribute(element), 
//                        type,otherType
//                        );
//                }
//            }
//            
//            if(rng.nextDouble() < probabilityOfDeletionMutation){//deletes a random number of elements from a complex type
//                final Node sequence = xpath(n,"xsd:sequence").get(0);
//                
//                final List<Node> elements = xpath(
//                    sequence, 
//                    "xsd:element"
//                    );
//                
//                final int removeIndex = rng.nextInt(elements.size());
//                final Node elementToRemove = elements.get(removeIndex);
//                
//                {
//                    sequence.removeChild(elementToRemove);
//                    elements.remove(elementToRemove);
//                    
//                    r.$(
//                        "\t%s: deleted sequence element named \"%s\" with type %s\n", 
//                        XmlUtils.getXPathTo(sequence),
//                        XmlUtils.getNodeNameAttribute(elementToRemove), 
//                        XmlUtils.getNodeTypeAttribute(elementToRemove)
//                        );
//                }
//            }
//            
//            if(rng.nextDouble() < probabilityOfRenameSequenceElementMutation){//renames a random number of elements from a complex type
//                final Node sequence = xpath(n,"xsd:sequence").get(0);
//                
//                final List<Node> elements = xpath(
//                    sequence, 
//                    "xsd:element"
//                    );
//                
//                if(elements.size() == 0){
//                    continue;
//                }
//                
//                final int renameIndex = rng.nextInt(elements.size());
//                final Node elementToRename = elements.get(renameIndex);
//                
//                final Node nameNode = elementToRename.getAttributes().getNamedItem("name");
//                final Node oldName = elementToRename.getAttributes().getNamedItem("name");
//                
//                final String newName = RandomUtils.randomElementName(
//                    rng, 
//                    oldName.getNodeValue(),
//                    rng.nextInt(maxRenamePrefixSize),
//                    renameCharacterMutationRate,
//                    rng.nextInt(maxRenameSuffixSize)
//                    );
//                r.$("\t%s: renamed element \"%s\" (a %s) to \"%s\"\n",
//                    XmlUtils.getXPathTo(sequence),
//                    oldName,
//                    XmlUtils.getNodeTypeAttribute(elementToRename),
//                    newName
//                    );
//
//                nameNode.setNodeValue(newName);
//            }
//            
//            if(rng.nextDouble() < probabilityOfTypeRenameMutation){//renames a randomly selected complex type
//                final Node nameNode = n.getAttributes().getNamedItem("name");
//                final String oldName = nameNode.getNodeValue();
//                
//                //select a random type with a name="X", rename X to X'
//                //replace all type="X" instances with type="X'"
//                
//                final String newName = RandomUtils.randomElementName(
//                    rng, 
//                    oldName,
//                    rng.nextInt(maxRenamePrefixSize),
//                    renameCharacterMutationRate,
//                    rng.nextInt(maxRenameSuffixSize)
//                    );
//                r.$(
//                    "\t%s: renamed type \"%s\" to \"%s\"\n", 
//                    XmlUtils.getXPathTo(n),
//                    oldName,
//                    newName
//                    );
//                
//                nameNode.setNodeValue(newName);
//                
//                //that's only half the equation though-- 
//                //we have to ALSO replace every reference to the old name
//                
//                //xsd:complexType/xsd:sequence/
//                {
//                    final String xpath = "//xsd:element[@type='" + oldName + "']";
//                    final List<Node> references = xpath(doc,xpath);
//                    for(Node ref:references){
//                        r.$(
//                            "\t\t%s: changed use of old type name \"%s\" to \"%s\"\n", 
//                            XmlUtils.getXPathTo(ref),
//                            oldName,
//                            newName
//                            );
//                        
//                        ref.getAttributes().getNamedItem("type").setNodeValue(newName);
//                    }
//                }
//                {//<xsd:extension base="DataOperationType">
//                    final String xpath = "//xsd:extension[@base='" + oldName + "']";
//                    final List<Node> references = xpath(doc,xpath);
//                    for(Node ref:references){
//                        r.$(
//                            "\t\t%s: changed use of old type name \"%s\" to \"%s\"\n", 
//                            XmlUtils.getXPathTo(ref),
//                            oldName,
//                            newName
//                            );
//                        
//                        ref.getAttributes().getNamedItem("base").setNodeValue(newName);
//                    }
//                }
//                
//                
//            }
//            
//        }
    }

}
