package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.AbstractDataSource;
import mil.darpa.immortals.flitcons.Configuration;
import mil.darpa.immortals.flitcons.datatypes.DataType;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.mdl.validation.PortMapping;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;
import mil.darpa.immortals.flitcons.validation.DebugData;
import nu.xom.*;
import org.apache.commons.lang.NotImplementedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class XmlElementDataSource extends AbstractDataSource<Node> {

	public XmlElementDataSource(@Nonnull File inputXmlFile) {
		super();
		this.xmlFile = inputXmlFile;
	}

	private final File xmlFile;
	private Document xmlDoc;
	private String primaryNode;

	public String getSourceFile() {
		return xmlFile.toString();
	}

	private static final XPathContext mdlContext = new XPathContext("mdl", "http://www.wsmr.army.mil/RCC/schemas/MDL");

	@Override
	protected synchronized void init() {
		try {
			if (xmlDoc == null) {
				Builder parser = new Builder();
				xmlDoc = parser.build(xmlFile);
				Configuration.PropertyCollectionInstructions config = Configuration.getInstance().dataCollectionInstructions;
				primaryNode = config.primaryNode;
			}
		} catch (IOException | ParsingException e) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, e);
		}
	}

	@Override
	public boolean isDauInventory() {
		init();
		return xmlDoc.query("//DAUInventory", mdlContext).size() > 0;
	}

	@Override
	public boolean isInputConfiguration() {
		init();
		return xmlDoc.query("//mdl:MDLRoot", mdlContext).size() > 0;
	}

	private LinkedHashMap<Node, List<Node>> collectNodeTrees(@Nonnull Document doc, @Nonnull String xpath) {
		LinkedHashMap<Node, List<Node>> rval = new LinkedHashMap<>();
		Nodes nodes = doc.query(xpath, mdlContext);

		for (Node n : nodes) {
			if (!(n instanceof Element)) {
				throw AdaptationnException.internal("All root nodes must be Elements!");
			}

			Element parentNode = (Element) n;
			List<Node> childNodes = new LinkedList<>();
			recurseAndGather(parentNode, childNodes);
			rval.put(parentNode, childNodes);
		}
		return rval;
	}

	@Override
	protected LinkedHashMap<Node, List<Node>> collectRawInventoryData() {
		init();
		if (!isDauInventory()) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Attempted to get the DAU Inventory from a document that does not have a DAUInventory root!");
		}
		return collectNodeTrees(xmlDoc, "//DAUInventory/NetworkNode");
	}

	@Override
	protected LinkedHashMap<Node, List<Node>> collectRawInputData() {
		init();
		if (!isInputConfiguration()) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Attempted to get the MDLRoot from a document that does not have an MDLRoot root!");
		}
		return collectNodeTrees(xmlDoc, "//mdl:MDLRoot/mdl:NetworkDomain/mdl:Networks/mdl:Network/mdl:NetworkNodes/mdl:NetworkNode[mdl:TmNSApps/mdl:TmNSApp/mdl:TmNSDAU]");
	}

	@Override
	protected LinkedHashMap<Node, List<Node>> collectRawExternalData() {
		init();
		if (!isInputConfiguration()) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Attempted to get the Measurements or DataStreams from a document that does not have an MDLRoot root!");
		}

		LinkedHashMap<Node, List<Node>> measurementData = collectNodeTrees(xmlDoc, "//mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement");
		measurementData.putAll(collectNodeTrees(xmlDoc, "//mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream"));
		measurementData.putAll(collectNodeTrees(xmlDoc, "//mdl:MDLRoot/mdl:NetworkDomain/mdl:Networks/mdl:Network/mdl:Devices/mdl:Device"));
		return measurementData;
	}

	/**
	 * Returns a mapping of the nodes referenced by the IDREF value of the input nodes to the input nodes
	 *
	 * @param doc        the source document
	 * @param inputNodes the input nodes containing "IDREF" fields
	 * @return The nodes
	 */
	private static Map<Element, Node> getIdrefElementsById(@Nonnull Document doc, @Nonnull Nodes inputNodes) {
		Map<Element, Node> rval = new HashMap<>();
		for (Node inputNode : inputNodes) {
			if (!(inputNode instanceof Element)) {
				throw AdaptationnException.input("Node type with an IDREF tag should be an element!");
			}
			Element element = (Element) inputNode;
			String id = element.getAttributeValue("IDREF");
			if (id == null) {
				throw AdaptationnException.internal("No IDREF found on node!");
			}

			Nodes nodes = doc.query("//*[@ID='" + id + "']", mdlContext);

			if (nodes.size() == 0) {
				throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "An IDREF value of '" + id + "' references a non-existing ID!");
			} else if (nodes.size() > 1) {
				throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "The ID '" + id + "' is assigned to multiple elements!");
			}

			Node node = nodes.get(0);
			if (!(node instanceof Element)) {
				throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "The node with the ID '" + id + "' must be an element!");
			}
			rval.put((Element) node, inputNode);
		}
		return rval;
	}

	public List<String> validateRelationalIntegrity() {
		Map<String, Integer> errorCount = new HashMap<>();

		List<String> seenIds = new LinkedList<>();
		Nodes nodes = xmlDoc.query("//*/@ID");

		for (Node node : nodes) {
			String value = node.getValue();
			if (seenIds.contains(value)) {
				String err = "The ID value '" + value + "' is assigned to multiple elements!";
				if (errorCount.containsKey(err)) {
					errorCount.put(err, errorCount.get(err) + 1);
				} else {
					errorCount.put(err, 1);
				}
			} else {
				seenIds.add(value);
			}
		}

		nodes = xmlDoc.query("//*/@IDREF");

		List<String> seenPortRefValues = new LinkedList<>();

		for (Node node : nodes) {
			Element parentNode = (Element) node.getParent();
			String value = node.getValue();
			if (!seenIds.contains(value) && !value.equals("dscp-0")) {
				String err = "The IDREF value of '" + value + "' references a non-existing ID!";
				if (errorCount.containsKey(err)) {
					errorCount.put(err, errorCount.get(err) + 1);
				} else {
					errorCount.put(err, 1);
				}
			}
			if (parentNode.getQualifiedName().equals("PortRef")) {
				Element parentParentNode;
				parentParentNode = (Element) parentNode.getParent();
				if (parentParentNode.getQualifiedName().equals("PortMapping")) {
					if (seenPortRefValues.contains(value)) {
						String err = "The IDREF value of '" + value + "' is used by multiple PortMapping PortRefs!";
						if (errorCount.containsKey(err)) {
							errorCount.put(err, errorCount.get(err) + 1);
						} else {
							errorCount.put(err, 1);
						}
					} else {
						seenPortRefValues.add(value);
					}
				}
			}
		}

		if (!errorCount.isEmpty()) {
			List<String> rval = new LinkedList<>();
			for (Map.Entry<String, Integer> entry : errorCount.entrySet()) {
				rval.add(entry.getKey() + " [" + entry.getValue() + " Occurrences]");
			}
			return rval;
		}
		return null;
	}

	/**
	 * Returns a map of the primary node's PortRef object to the related secondary node objects
	 */
	@Override
	protected Map<Node, Set<Node>> collectRawInputExternalDataIndirectRelations() {
		init();
		Map<Node, Set<Node>> rval = new HashMap<>();

		validateRelationalIntegrity();

		if (!isInputConfiguration()) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Attempted to get the relations from a document that does not have an MDLRoot root!");
		}

		Nodes portMappingNodes = xmlDoc.query("//mdl:MDLRoot/mdl:NetworkDomain/mdl:Networks/mdl:Network/mdl:PortMappings/mdl:PortMapping", mdlContext);

		for (Node portMappingNode : portMappingNodes) {
			Set<Node> secondaryPorts = new HashSet<>();
			Node primaryPortref = null;

			Map<Element, Node> portToPortRefMap = getIdrefElementsById(xmlDoc, portMappingNode.query("mdl:PortRef", mdlContext));
			secondaryPorts.addAll(getIdrefElementsById(xmlDoc, portMappingNode.query("mdl:MeasurementRefs/mdl:MeasurementRef", mdlContext)).keySet());
			secondaryPorts.addAll(getIdrefElementsById(xmlDoc, portMappingNode.query("mdl:DataStreamRefs/mdl:DataStreamRef", mdlContext)).keySet());

			for (Element port : portToPortRefMap.keySet()) {
				boolean isPrimaryNode = false;
				Element parent = (Element) port.getParent();

				while (!isPrimaryNode && parent != null) {
					if (parent.getLocalName().equals(primaryNode)) {
						isPrimaryNode = true;
					} else {
						ParentNode parentNode = parent.getParent();
						if (parentNode instanceof Element) {
							parent = (Element) parentNode;
						} else {
							parent = null;
						}
					}
				}

				if (isPrimaryNode) {
					primaryPortref = portToPortRefMap.get(port);
				} else {
					secondaryPorts.add(port);
				}
			}

			if (primaryPortref == null) {
				throw AdaptationnException.internal("No port that is a child of a NetworkNode could be found in the PortMapping!!");
			}

			if (rval.containsKey(primaryPortref)) {
				throw AdaptationnException.internal("This is invalid!");

			}
			rval.put(primaryPortref, secondaryPorts);
		}
		return rval;
	}

	private static void recurseAndGather(@Nonnull Node currentNode, @Nonnull List<Node> targetNodeSet) {

		if (targetNodeSet.contains(currentNode)) {
			if (currentNode instanceof Element) {
				Element e = (Element) currentNode;
				throw AdaptationnException.internal("Node of type '" + e.getQualifiedName() + "' with value '" + currentNode.getValue() + "' already exists in the node set!");
			} else {
				throw AdaptationnException.internal("Node with value '" + currentNode.getValue() + "' already exists in the node set!");
			}
		}

		targetNodeSet.add(currentNode);

		for (int i = 0; i < currentNode.getChildCount(); i++) {
			recurseAndGather(currentNode.getChild(i), targetNodeSet);
		}
	}

	@Override
	protected synchronized HierarchicalDataContainer createContainer(@Nonnull DataType dataType, @Nonnull LinkedHashMap<Node, List<Node>> input, @Nonnull Configuration.PropertyCollectionInstructions collectionInstructions) {
		LinkedHashMap<HierarchicalIdentifier, HierarchicalData> identifierDataMap = new LinkedHashMap<>();
		LinkedHashMap<HierarchicalData, List<HierarchicalData>> rootChildMap = new LinkedHashMap<>();

		Set<Node> parentNodes = input.keySet();

		for (Node parentNode : parentNodes) {
			if (!(parentNode instanceof Element)) {
				throw AdaptationnException.internal("Root nodes must be XML Elements!");
			}
			Element parentElement = (Element) parentNode;

			HierarchicalData parentData = createData(parentElement, true, collectionInstructions);
			if (parentData != null) {
				identifierDataMap.put(parentData.node, parentData);
				List<HierarchicalData> children = new LinkedList<>();
				rootChildMap.put(parentData, children);

				for (Node childNode : input.get(parentElement)) {
					if (childNode instanceof Element && !parentNodes.contains(childNode)) {
						Element childElement = (Element) childNode;

						HierarchicalData childData = createData(childElement, false, collectionInstructions);

						if (childData != null) {
							identifierDataMap.put(childData.node, childData);

							if (children.contains(childData)) {
								throw AdaptationnException.internal("Duplicate with identification '" + childData.toString() + "");
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
					throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Could not find matching ID for IDREF '" + partialOutboundIdentifier.referenceIdentifier + "'!");

				} else if (fullNodeSet.size() > 1) {
					throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Found multiple Nodes with ID '" + partialOutboundIdentifier.referenceIdentifier + "'!");

				} else {
					HierarchicalIdentifier fullNode = fullNodeSet.iterator().next();
					outboundReferenceNode.updateOutboundReference(fullNode);
				}
			}
		}

		return new HierarchicalDataContainer(dataType, identifierDataMap, rootChildMap);
	}

	private void splitChildrenByClass(Node parentNode, Set<Element> elementNodes, Set<Text> textNodes) {
		int childCount = parentNode.getChildCount();

		for (int i = 0; i < childCount; i++) {
			Node child = parentNode.getChild(i);

			if (child instanceof Text) {
				if (!child.getValue().trim().equals("")) {
					textNodes.add((Text) child);
				}

			} else if (child instanceof Element) {
				Element element = (Element) child;
				if (element.getChildCount() == 0) {
					element.appendChild(new Text(nullValuePlaceholder));
				}
				elementNodes.add((Element) child);

			} else if (!(child instanceof Comment)) {
				throw AdaptationnException.internal("Unexpected node type '" + child.getClass().getName() + "'!");
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

	private void gatherProperties(@Nonnull Element src,
	                              @Nonnull Configuration.PropertyCollectionInstructions collectionInstructions,
	                              @Nonnull Map<String, Object> targetPropertyMap,
	                              @Nonnull DebugData targetDebugData,
	                              @Nonnull Set<HierarchicalIdentifier> targetOutboundReferenceSet) {
		Set<String> interestedProps = collectionInstructions.collectedChildProperties.get(src.getQualifiedName());
		Set<String> debugPropSet = collectionInstructions.collectedDebugProperties.get(src.getQualifiedName());

		if (interestedProps != null || debugPropSet != null) {

			for (int i = 0; i < src.getAttributeCount(); i++) {
				Attribute attr = src.getAttribute(i);
				String attrLabel = attr.getQualifiedName();
				String attrValue = attr.getValue();

				if (attr.getQualifiedName().equals("IDREF")) {
					// Add to outbound references
					targetOutboundReferenceSet.add(HierarchicalIdentifier.createReferenceNode(attr.getValue(), null));
				} else if (interestedProps != null && interestedProps.contains(attrLabel)) {
					// And add them to their respective lists if they match

					if (attrValue == null) {
						targetPropertyMap.put(attrLabel, nullValuePlaceholder);
					} else {
						targetPropertyMap.put(attrLabel, attrValue);
					}

				} else if (debugPropSet != null && debugPropSet.contains(attrLabel)) {
					// And add them to their respective lists if they match
					if (attrValue == null) {
						targetDebugData.addAttribute(attrLabel, nullValuePlaceholder);
					} else {
						targetDebugData.addAttribute(attrLabel, attrValue);
					}
				}
			}
		}
	}

	private void gatherInboundReferences(@Nonnull Element src, @Nonnull Set<HierarchicalIdentifier> targetSet) {
		Attribute idAttr = src.getAttribute("ID");

		if (idAttr != null) {
			Nodes nodes = src.getDocument().query("//*[@IDREF='" + idAttr.getValue() + "']");
			for (Node node : nodes) {
				if (!(node instanceof Element)) {
					throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Reference source should be an Element!");
				}
				targetSet.add(createIdentifier(node));

			}
		}
	}

	private void gatherAttributeElementsAndChildren(@Nonnull Set<Element> elementNodes,
	                                                @Nullable Set<String> interestedProperties,
	                                                @Nullable Set<String> debugProperties,
	                                                @Nonnull Map<String, Object> targetPropertyMap,
	                                                @Nonnull DebugData debugData,
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
					debugData.addAttribute(qName, value);
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

	private synchronized HierarchicalData createData(
			@Nonnull Node srcNode, boolean isRootObject,
			@Nonnull Configuration.PropertyCollectionInstructions collectionInstructions) {

		if (!(srcNode instanceof Element)) {
			throw AdaptationnException.internal("Cannot create data from a Text node!");
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
		TreeMap<String, Object> collectedDebugProps = new TreeMap<>();

		DebugData debugData = new DebugData(identifier.getNodeType(), null);

		// Get the parent node
		Node parentNode = src.getParent();

		if (!(parentNode instanceof Element)) {
			throw AdaptationnException.internal("Parent node must be an element!");
		}

		HierarchicalIdentifier parent = isRootObject ? null : createIdentifier(parentNode);

		// Split out the Element and Text child nodes
		Set<Element> elementNodes = new HashSet<>();
		Set<Text> textNodes = new HashSet<>();
		splitChildrenByClass(src, elementNodes, textNodes);
		int attributeCount = src.getAttributeCount();

		if (textNodes.size() == 0) {
			if (elementNodes.size() == 0) {
				if (attributeCount == 0) {
					// Add empty node?
					throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Empty element node not valid!");

				} else {
					// Gather attributes/outbound references
					gatherProperties(src, collectionInstructions, collectedProps, debugData, outboundReferences);
				}

			} else {
				if (attributeCount > 0) {
					// Gather attributes/outbound references
					gatherProperties(src, collectionInstructions, collectedProps, debugData, outboundReferences);
				}
				// Add attribute child nodes and child nodes
				gatherAttributeElementsAndChildren(elementNodes, collectionInstructions.collectedChildProperties.get(src.getQualifiedName()), collectionInstructions.collectedDebugProperties.get(src.getQualifiedName()),
						collectedProps, debugData, childMap);
			}

		} else if (textNodes.size() == 1) {
			if (elementNodes.size() == 0) {
				if (attributeCount == 0) {
					// Is collected by the parent
					return null;

				} else {
					// Gather attributes/outbound references
					gatherProperties(src, collectionInstructions, collectedProps, debugData, outboundReferences);

					// Add the text data if it matches
					String nodeType = src.getLocalName();
					if (collectionInstructions.collectedChildProperties.containsKey(nodeType) &&
							collectionInstructions.collectedChildProperties.get(nodeType).contains(nodeType)) {
						// Add to values
						String value = textNodes.iterator().next().getValue();

						if (value == null) {
							collectedProps.put(nodeType, nullValuePlaceholder);
						} else {
							collectedProps.put(nodeType, value);
						}

					} else if (collectionInstructions.collectedDebugProperties.containsKey(nodeType) &&
							collectionInstructions.collectedDebugProperties.get(nodeType).contains(nodeType)) {

						String value = textNodes.iterator().next().getValue();

						if (value == null) {
							debugData.addAttribute(nodeType, nullValuePlaceholder);
						} else {
							debugData.addAttribute(nodeType, value);
						}
					}
				}

			} else {
				throw AdaptationnException.internal("Text nodes should not have children!");
			}

		} else {
			throw AdaptationnException.internal("There should only be a single text node per Element!");
		}

		gatherInboundReferences(src, inboundReferences);

		if (debugData.getAttributeSize() == 0 && !Configuration.getInstance().getGlobalTransformationInstructions().taggedNodes.contains(identifier.getNodeType())) {
			debugData = null;
		}
		return new HierarchicalData(
					identifier,
					collectedProps,
					src,
					isRootObject,
					parent,
					inboundReferences,
					outboundReferences,
					childMap,
					debugData
			);
	}

	@Override
	protected HierarchicalIdentifier createIdentifier(@Nonnull Node src) {

		if (!(src instanceof Element)) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Value must be an XML Element!");
		}

		Element ele = (Element) src;

		Attribute attr = ele.getAttribute("ID");
		// Adding the I prevents it from being serialized into a number and failing validation
		if (attr == null) {
			return HierarchicalIdentifier.produceTraceableNode("I" + Integer.toString(src.hashCode()), ele.getQualifiedName());
		} else {
			return HierarchicalIdentifier.createReferenceNode(attr.getValue(), ele.getQualifiedName());
		}
	}

	@Override
	public Map<String, PortMapping> getPortMappingDetails() {
		throw new NotImplementedException("This is only implemented for the OrientVertexDataSource!");
	}

	@Override
	public synchronized void shutdown() {
		xmlDoc = null;
	}

	@Override
	public synchronized void restart() {
		xmlDoc = null;
		init();

	}
}
