package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.AbstractDataCollector;
import mil.darpa.immortals.flitcons.Configuration;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import nu.xom.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class XmlElementCollector extends AbstractDataCollector<Node> {

	public XmlElementCollector(
			@Nonnull File inputXmlConfigurationFile,
			@Nonnull File inputXmlDauInventory,
			@Nonnull Configuration.PropertyCollectionInstructions collectionInstructions,
			@Nonnull Configuration.TransformationInstructions transformationInstructions) {
		super(collectionInstructions, transformationInstructions);
		this.inputXmlConfigurationFile = inputXmlConfigurationFile;
		this.inputXmlDauInventory = inputXmlDauInventory;
	}

	public XmlElementCollector(
			@Nonnull File inputXmlFile,
			@Nonnull Configuration.PropertyCollectionInstructions collectionInstructions,
			@Nonnull Configuration.TransformationInstructions transformationInstructions) {
		super(collectionInstructions, transformationInstructions);
		this.inputXmlConfigurationFile = inputXmlFile;
		this.inputXmlDauInventory = inputXmlFile;
	}

	private final File inputXmlConfigurationFile;
	private final File inputXmlDauInventory;
	private Document inputConfigurationDoc;
	private Document dauInventoryDoc;

	private static final XPathContext mdlContext = new XPathContext("mdl", "http://www.wsmr.army.mil/RCC/schemas/MDL");


	protected synchronized void init() {
		try {
			if (inputConfigurationDoc == null && inputXmlConfigurationFile != null) {
				Builder parser = new Builder();
				inputConfigurationDoc = parser.build(inputXmlConfigurationFile);
			}

			if (dauInventoryDoc == null && inputXmlDauInventory != null) {
				Builder parser = new Builder();
				dauInventoryDoc = parser.build(inputXmlDauInventory);
			}
		} catch (IOException | ParsingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isDauInventory() {
		init();
		return dauInventoryDoc.query("//DAUInventory", mdlContext).size() > 0;
	}

	@Override
	public boolean isInputConfiguration() {
		init();
		return inputConfigurationDoc.query("//mdl:MDLRoot", mdlContext).size() > 0;
	}

	private LinkedHashMap<Node, List<Node>> collectNodeTrees(@Nonnull Document doc, @Nonnull String xpath) {
		LinkedHashMap<Node, List<Node>> rval = new LinkedHashMap<>();
		Nodes nodes = doc.query(xpath, mdlContext);

		for (Node n : nodes) {
			if (!(n instanceof Element)) {
				throw new RuntimeException("All root nodes must be Elements!");
			}

			Element parentNode = (Element) n;
			List<Node> childNodes = new LinkedList<>();
			recurseAndGather(parentNode, childNodes);
			rval.put(parentNode, childNodes);
		}
		return rval;
	}

	@Override
	protected LinkedHashMap<Node, List<Node>> collectRawDauInventoryData() {
		init();
		return collectNodeTrees(dauInventoryDoc, "//DAUInventory/NetworkNode");
	}

	@Override
	protected LinkedHashMap<Node, List<Node>> collectRawDauData() {
		init();
		return collectNodeTrees(inputConfigurationDoc, "//mdl:MDLRoot/mdl:NetworkDomain/mdl:Networks/mdl:Network/mdl:NetworkNodes/mdl:NetworkNode[mdl:TmNSApps/mdl:TmNSApp/mdl:TmNSDAU]");
	}

	@Override
	protected LinkedHashMap<Node, List<Node>> collectRawMeasurementData() {
		init();
		return collectNodeTrees(inputConfigurationDoc, "//mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement");
	}

	private static Element getElementById(@Nonnull Document doc, @Nonnull String id) {
		Nodes nodes = doc.query("//*[@ID='" + id + "']", mdlContext);
		if (nodes.size() != 1) {
			throw new RuntimeException("Expected one and only one node with id '" + id + "'!");
		}

		Node node = nodes.get(0);
		if (!(node instanceof Element)) {
			throw new RuntimeException("Node with id '" + id + "' must be an element!");
		}
		return (Element) node;
	}

	@Override
	protected Map<Node, Set<Node>> getRawDauMeasurementIndirectRelations() {
		init();
		Map<Node, Set<Node>> rval = new HashMap<>();

		Nodes portMappingNodes = inputConfigurationDoc.query("//mdl:MDLRoot/mdl:NetworkDomain/mdl:Networks/mdl:Network/mdl:PortMappings/mdl:PortMapping", mdlContext);

		for (Node portMappingNode : portMappingNodes) {
			Nodes measurementRefNodes = portMappingNode.query("mdl:MeasurementRefs/mdl:MeasurementRef", mdlContext);
			if (measurementRefNodes.size() != 1) {
				throw new RuntimeException("Expected one and only one MeasurementRef value but found " + measurementRefNodes.size() + "!");
			}

			Node measurementRefNode = measurementRefNodes.get(0);

			if (!(measurementRefNode instanceof Element)) {
				throw new RuntimeException("MeasurementRef node must be an element!");
			}
			String idref = ((Element) measurementRefNode).getAttributeValue("IDREF");

			Element measurement = getElementById(inputConfigurationDoc, idref);

			Nodes portRefs = portMappingNode.query("mdl:PortRef", mdlContext);
			for (Node portRef : portRefs) {
				if (!(portRef instanceof Element)) {
					throw new RuntimeException("PortRef node  must be an element!");
				}

				Set<Node> measurementSet = new HashSet<>();
				measurementSet.add(measurement);
				rval.put(portRef, measurementSet);
			}
		}
		return rval;
	}

	private static void recurseAndGather(@Nonnull Node currentNode, @Nonnull List<Node> targetNodeSet) {

		if (targetNodeSet.contains(currentNode)) {
			if (currentNode instanceof Element) {
				Element e = (Element) currentNode;
				throw new RuntimeException("Node of type '" + e.getQualifiedName() + "' with value '" + currentNode.getValue() + "' already exists in the node set!");
			} else {
				throw new RuntimeException("Node with value '" + currentNode.getValue() + "' already exists in the node set!");
			}
		}

		targetNodeSet.add(currentNode);

		for (int i = 0; i < currentNode.getChildCount(); i++) {
			recurseAndGather(currentNode.getChild(i), targetNodeSet);
		}
	}

	@Override
	public synchronized HierarchicalDataContainer createContainer(@Nonnull LinkedHashMap<Node, List<Node>> input, @Nonnull Set<String> valuesToDefaultToTrue, @Nonnull Map<String, Set<String>> interestedProperties, @Nonnull Map<String, Set<String>> debugProperties) {
		LinkedHashMap<HierarchicalIdentifier, HierarchicalData> identifierDataMap = new LinkedHashMap<>();
		LinkedHashMap<HierarchicalData, List<HierarchicalData>> rootChildMap = new LinkedHashMap<>();

		Set<Node> parentNodes = input.keySet();

		for (Node parentNode : parentNodes) {
			if (!(parentNode instanceof Element)) {
				throw new RuntimeException("Root nodes must be XML Elements!");
			}
			Element parentElement = (Element) parentNode;

			HierarchicalData parentData = createData(parentElement, true, valuesToDefaultToTrue, interestedProperties, debugProperties);
			if (parentData != null) {
				identifierDataMap.put(parentData.node, parentData);
				List<HierarchicalData> children = new LinkedList<>();
				rootChildMap.put(parentData, children);
				for (Node childNode : input.get(parentElement)) {
					if (childNode instanceof Element && !parentNodes.contains(childNode)) {
						Element childElement = (Element) childNode;

						HierarchicalData childData = createData(childElement, false, valuesToDefaultToTrue, interestedProperties, debugProperties);

						if (childData != null) {
							identifierDataMap.put(childData.node, childData);

							if (children.contains(childData)) {
								throw new RuntimeException("Duplicate with identification '" + childData.toString() + "");
							}
							children.add(childData);
						}
					}
				}
			}
		}

		Set<HierarchicalData> outboundReferenceNodes = identifierDataMap.values().stream().filter(x -> x.getOutboundReferencesIterator().hasNext()).collect(Collectors.toSet());
		for (HierarchicalData outboundReferenceNode : outboundReferenceNodes) {
			Iterator<HierarchicalIdentifier> outboundReferenceIter = outboundReferenceNode.getOutboundReferencesIterator();
			while (outboundReferenceIter.hasNext()) {
				HierarchicalIdentifier partialOutboundIdentifier = outboundReferenceIter.next();

				List<HierarchicalIdentifier> fullNodeSet = identifierDataMap.keySet().stream().filter(x -> partialOutboundIdentifier.referenceIdentifier != null && partialOutboundIdentifier.referenceIdentifier.equals(x.referenceIdentifier)).collect(Collectors.toList());

				if (fullNodeSet.size() == 0) {
					throw new RuntimeException("Could not find matching ID for IDREF '" + partialOutboundIdentifier.referenceIdentifier + "'!");

				} else if (fullNodeSet.size() > 1) {
					throw new RuntimeException("Found multiple Nodes with ID '" + partialOutboundIdentifier.referenceIdentifier + "'!");

				} else {
					HierarchicalIdentifier fullNode = fullNodeSet.iterator().next();
					outboundReferenceNode.updateOutboundReference(fullNode);
				}
			}
		}

		return new HierarchicalDataContainer(identifierDataMap, rootChildMap);
	}


	private static void splitChildrenByClass(Node parentNode, Set<Element> elementNodes, Set<Text> textNodes) {
		int childCount = parentNode.getChildCount();

		for (int i = 0; i < childCount; i++) {
			Node child = parentNode.getChild(i);

			if (child instanceof Text) {
				if (!child.getValue().trim().equals("")) {
					textNodes.add((Text) child);
				}

			} else if (child instanceof Element) {
				elementNodes.add((Element) child);

			} else {
				throw new RuntimeException("Unexpected node type '" + child.getClass().getName() + "'!");
			}
		}
	}

	private static void setOrAppendPropValue(@Nonnull String qName, @Nonnull String value, @Nonnull Map<String, Object> collectedProps) {
		Object currentProp = collectedProps.get(qName);

		if (currentProp == null) {
			// Adding it if it doesn't exist
			collectedProps.put(qName, value);

		} else if (currentProp instanceof List) {
			// Adding it to the existing list of values if a list exists
			List<Object> currentListProp = (List<Object>) currentProp;
			currentListProp.add(value);

		} else {
			// Or adding it and the current value to a new list and replacing the current value
			List<Object> newValList = new LinkedList<>();
			newValList.add(currentProp);
			newValList.add(value);
			collectedProps.put(qName, newValList);
		}
	}

	private static void gatherProperties(@Nonnull Element src,
	                                     @Nonnull Set<String> valuesToDefaultToTrue,
	                                     @Nonnull Map<String, Set<String>> interestedProperties,
	                                     @Nonnull Map<String, Set<String>> debugProperties,
	                                     @Nonnull HashMap<String, Object> targetPropertyMap,
	                                     @Nonnull HashMap<String, Object> targetDebugPropertyMap,
	                                     @Nonnull Set<HierarchicalIdentifier> targetOutboundReferenceSet) {
		Set<String> interestedProps = interestedProperties.get(src.getQualifiedName());
		Set<String> debugPropSet = debugProperties.get(src.getQualifiedName());

		if (interestedProps != null || debugPropSet != null) {

			for (int i = 0; i < src.getAttributeCount(); i++) {
				Attribute attr = src.getAttribute(i);
				String attrLabel = attr.getQualifiedName();
				String attrValue = attr.getValue();

				if (attr.getQualifiedName().equals("ID")) {
					// Skip since it is already incorporated in the identifier

				} else if (attr.getQualifiedName().equals("IDREF")) {
					// Add to outbound references
					targetOutboundReferenceSet.add(new HierarchicalIdentifier(attr.getValue()));
				} else if (interestedProps != null && interestedProps.contains(attrLabel)) {
					// And add them to their respective lists if they match

					if ((attrValue == null || attrValue.equals("")) && valuesToDefaultToTrue.contains(attrLabel)) {
						targetPropertyMap.put(attrLabel, true);
					} else {
						targetPropertyMap.put(attrLabel, attrValue);
					}

				} else if (debugPropSet != null && debugPropSet.contains(attrLabel)) {
					// And add them to their respective lists if they match
					if ((attrValue == null || attrValue.equals("")) && valuesToDefaultToTrue.contains(attrLabel)) {
						targetDebugPropertyMap.put(attrLabel, true);
					} else {
						targetDebugPropertyMap.put(attrLabel, attrValue);
					}
				}
			}
		}
	}

	private static void gatherInboundReferences(@Nonnull Element src, @Nonnull Set<HierarchicalIdentifier> targetSet) {
		Attribute idAttr = src.getAttribute("ID");

		if (idAttr != null) {
			Nodes nodes = src.getDocument().query("//*[@IDREF='" + idAttr.getValue() + "']");
			for (Node node : nodes) {
				if (!(node instanceof Element)) {
					throw new RuntimeException("Reference source should be an Element!");
				}
				targetSet.add(createIdentifier((Element) node));

			}
		}
	}

	private static void gatherAttributeElementsAndChildren(@Nonnull Set<Element> elementNodes,
	                                                       @Nullable Set<String> interestedProperties,
	                                                       @Nullable Set<String> debugProperties,
	                                                       @Nonnull Map<String, Object> targetPropertyMap,
	                                                       @Nonnull Map<String, Object> targetDebugPropertyMap,
	                                                       @Nonnull Map<String, Set<HierarchicalIdentifier>> targetCollectedChildrenMap) {
		// Then, for all children nodes
		for (Element childElement : elementNodes) {
			// If it is a Text-only node that was omitted in the above textNodes.size == 1 and
			// elementNodes.size == 1 step, pull it into the properties
			if (childElement.getChildCount() == 1 && childElement.getChild(0) instanceof Text &&
					childElement.getAttributeCount() == 0) {
				Node childNode = childElement.getChild(0);
				String qName = childElement.getQualifiedName();
				String value = childNode.getValue();

				if (interestedProperties != null && interestedProperties.contains(qName)) {
					setOrAppendPropValue(qName, value, targetPropertyMap);
				}

				if (debugProperties != null && debugProperties.contains(qName)) {
					setOrAppendPropValue(qName, value, targetDebugPropertyMap);
				}

			} else {
				if (childElement.getChildCount() != 0 || childElement.getAttributeCount() != 0) {
					// Otherwise, add the identifier to the children
					Set<HierarchicalIdentifier> childSet =
							targetCollectedChildrenMap.computeIfAbsent(childElement.getQualifiedName(), k -> new HashSet<>());

					childSet.add(createIdentifier(childElement));
				}
			}
		}
	}

	@Override
	public synchronized HierarchicalData createData(@Nonnull Node srcNode, boolean isRootObject, @Nonnull Set<String> valuesToDefaultToTrue, @Nonnull Map<String, Set<String>> interestedProperties, @Nonnull Map<String, Set<String>> debugProperties) {
		if (!(srcNode instanceof Element)) {
			throw new RuntimeException("Cannot create data from a Text node!");
		}

		Element src = (Element) srcNode;

		if (src.getAttributeCount() == 0 && src.getChildCount() == 0) {
			return null;
		}

		HierarchicalIdentifier identifier = createIdentifier(src);
		HashMap<String, Object> collectedProps = new HashMap<>();
		Set<HierarchicalIdentifier> outboundReferences = new HashSet<>();
		Set<HierarchicalIdentifier> inboundReferences = new HashSet<>();
		Map<String, Set<HierarchicalIdentifier>> childMap = new HashMap<>();
		HashMap<String, Object> collectedDebugProps = new HashMap<>();

		// Get the parent node
		Node parentNode = src.getParent();

		if (!(parentNode instanceof Element)) {
			throw new RuntimeException("Parent node must be an element!");
		}

		HierarchicalIdentifier parent = isRootObject ? null : createIdentifier((Element) parentNode);

		// Split out the Element and Text child nodes
		Set<Element> elementNodes = new HashSet<>();
		Set<Text> textNodes = new HashSet<>();
		splitChildrenByClass(src, elementNodes, textNodes);
		int attributeCount = src.getAttributeCount();

		if (textNodes.size() == 0) {
			if (elementNodes.size() == 0) {
				if (attributeCount == 0) {
					// Add empty node?
					throw new RuntimeException("Empty element node not valid!");

				} else {
					// Gather attributes/outbound references
					gatherProperties(src, valuesToDefaultToTrue, interestedProperties, debugProperties, collectedProps, collectedDebugProps, outboundReferences);
				}

			} else {
				if (attributeCount > 0) {
					// Gather attributes/outbound references
					gatherProperties(src, valuesToDefaultToTrue, interestedProperties, debugProperties, collectedProps, collectedDebugProps, outboundReferences);
				}
				// Add attribute child nodes and child nodes
				gatherAttributeElementsAndChildren(elementNodes, interestedProperties.get(src.getQualifiedName()), debugProperties.get(src.getQualifiedName()),
						collectedProps, collectedDebugProps, childMap);
			}

		} else if (textNodes.size() == 1) {
			if (elementNodes.size() == 0) {
				if (attributeCount == 0) {
					// Is collected by the parent
					return null;

				} else {
					// Gather attributes/outbound references
					gatherProperties(src, valuesToDefaultToTrue, interestedProperties, debugProperties, collectedProps, collectedDebugProps, outboundReferences);
					// Ignore Text data
				}

			} else {
				throw new RuntimeException("Text nodes should not have children!");
			}

		} else {
			throw new RuntimeException("There should only be a single text node per Element!");
		}

		gatherInboundReferences(src, inboundReferences);

		return new HierarchicalData(
				identifier,
				collectedProps,
				src,
				isRootObject,
				parent,
				inboundReferences,
				outboundReferences,
				childMap,
				collectedDebugProps
		);
	}

	private static HierarchicalIdentifier createIdentifier(@Nonnull Element src) {
		Attribute attr = src.getAttribute("ID");
		// Adding the I prevents it from being serialized into a number and failing validation
		if (attr != null) {
			return new HierarchicalIdentifier("I" + src.hashCode(), src.getQualifiedName(), attr.getValue());
		} else {
			return new HierarchicalIdentifier("I" + src.hashCode(), src.getQualifiedName());
		}
	}

	public Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> convertOneToManyMap(@Nonnull Map<Node, Set<Node>> indirectRelations) {
		Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> rval = new HashMap<>();

		for (Map.Entry<Node, Set<Node>> entry : indirectRelations.entrySet()) {
			if (!(entry.getKey() instanceof Element)) {
				throw new RuntimeException("Value must be an XML Element!");
			}
			HierarchicalIdentifier primaryNode = createIdentifier((Element) entry.getKey());
			Set<HierarchicalIdentifier> relationSet = rval.computeIfAbsent(primaryNode, k -> new HashSet<>());
			relationSet.addAll(entry.getValue().stream().map(x -> createIdentifier((Element) x)).collect(Collectors.toSet()));
		}
		return rval;
	}
}
