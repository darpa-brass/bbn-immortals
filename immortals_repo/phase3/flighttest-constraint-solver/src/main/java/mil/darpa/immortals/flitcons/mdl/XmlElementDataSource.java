package mil.darpa.immortals.flitcons.mdl;

import mil.darpa.immortals.flitcons.Configuration;
import mil.darpa.immortals.flitcons.DataSourceInterface;
import mil.darpa.immortals.flitcons.Utils;
import mil.darpa.immortals.flitcons.datatypes.DataType;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalDataContainer;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;
import nu.xom.*;
import org.apache.commons.lang.NotImplementedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static mil.darpa.immortals.flitcons.Utils.ATTRIBUTE_DEBUG_LABEL_IDENTIFIER;

public class XmlElementDataSource implements DataSourceInterface<Node> {

	public XmlElementDataSource(@Nonnull File inputXmlFile) {
		this.xmlFile = inputXmlFile;
	}

	private final File xmlFile;
	private Document xmlDoc;
	private Set<String> emptyValuesToDefaultToTrue;

	private static final XPathContext mdlContext = new XPathContext("mdl", "http://www.wsmr.army.mil/RCC/schemas/MDL");


	@Override
	public synchronized void init() {
		try {
			if (xmlDoc == null) {
				Builder parser = new Builder();
				xmlDoc = parser.build(xmlFile);

				emptyValuesToDefaultToTrue = Configuration.getInstance().dataCollectionInstructions.valuesToDefaultToTrue;
			}
		} catch (IOException | ParsingException e) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, e);
		}
	}

	@Override
	public void commit() {
		throw new NotImplementedException("Updating of XML data is currently not supported!");
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
	public LinkedHashMap<Node, List<Node>> collectRawDauInventoryData() {
		init();
		if (!isDauInventory()) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Attempted to get the DAU Inventory from a document that does not have a DAUInventory root!");
		}
		return collectNodeTrees(xmlDoc, "//DAUInventory/NetworkNode");
	}

	@Override
	public void update_rewireNode(@Nonnull Node originalNode, @Nonnull Node replacementNode) {
		throw new NotImplementedException("Updating of XML data is currently not supported!");
	}

	@Override
	public void update_removeNodeTree(Node node) {
		throw new NotImplementedException("Updating of XML data is currently not supported!");
	}

	@Override
	public void update_insertNodeAsChild(@Nonnull Node newNode, @Nonnull Node parentNode) {
		throw new NotImplementedException("Updating of XML data is currently not supported!");
	}

	@Override
	public void update_NodeAttribute(@Nonnull Node node, @Nonnull String attributeName, @Nonnull Object attributeValue) {
		throw new NotImplementedException("Updating of XML data is currently not supported!");
	}

	@Override
	public LinkedHashMap<Node, List<Node>> collectRawDauData() {
		init();
		if (!isInputConfiguration()) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Attempted to get the MDLRoot from a document that does not have an MDLRoot root!");
		}
		return collectNodeTrees(xmlDoc, "//mdl:MDLRoot/mdl:NetworkDomain/mdl:Networks/mdl:Network/mdl:NetworkNodes/mdl:NetworkNode[mdl:TmNSApps/mdl:TmNSApp/mdl:TmNSDAU]");
	}

	@Override
	public LinkedHashMap<Node, List<Node>> collectRawMeasurementData() {
		init();
		if (!isInputConfiguration()) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Attempted to get the Measurements from a document that does not have an MDLRoot root!");
		}

		return collectNodeTrees(xmlDoc, "//mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement");
	}

	private static Element getElementById(@Nonnull Document doc, @Nonnull String id) {
		Nodes nodes = doc.query("//*[@ID='" + id + "']", mdlContext);
		if (nodes.size() != 1) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Expected one and only one node with id '" + id + "'!");
		}

		Node node = nodes.get(0);
		if (!(node instanceof Element)) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Node with id '" + id + "' must be an element!");
		}
		return (Element) node;
	}

	@Override
	public Map<Node, Set<Node>> collectRawDauMeasurementIndirectRelations() {
		init();
		Map<Node, Set<Node>> rval = new HashMap<>();

		if (!isInputConfiguration()) {
			throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Attempted to get the relations from a document that does not have an MDLRoot root!");
		}

		Nodes portMappingNodes = xmlDoc.query("//mdl:MDLRoot/mdl:NetworkDomain/mdl:Networks/mdl:Network/mdl:PortMappings/mdl:PortMapping", mdlContext);

		for (Node portMappingNode : portMappingNodes) {
			Nodes measurementRefNodes = portMappingNode.query("mdl:MeasurementRefs/mdl:MeasurementRef", mdlContext);
			if (measurementRefNodes.size() != 1) {
				throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Expected one and only one MeasurementRef value but found " + measurementRefNodes.size() + "!");
			}

			Node measurementRefNode = measurementRefNodes.get(0);

			if (!(measurementRefNode instanceof Element)) {
				throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "MeasurementRef node must be an element!");
			}
			String idref = ((Element) measurementRefNode).getAttributeValue("IDREF");

			Element measurement = getElementById(xmlDoc, idref);

			Nodes portRefs = portMappingNode.query("mdl:PortRef", mdlContext);
			for (Node portRef : portRefs) {
				if (!(portRef instanceof Element)) {
					throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "PortRef node  must be an element!");
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
	public synchronized HierarchicalDataContainer createContainer(@Nonnull DataType dataType, @Nonnull LinkedHashMap<Node, List<Node>> input, @Nonnull Configuration.PropertyCollectionInstructions collectionInstructions) {
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


	private static void splitChildrenByClass(Node parentNode, Set<Element> elementNodes, Set<Text> textNodes, Set<String> emptyValuesToDefaultToTrue) {
		int childCount = parentNode.getChildCount();

		for (int i = 0; i < childCount; i++) {
			Node child = parentNode.getChild(i);

			if (child instanceof Text) {
				if (!child.getValue().trim().equals("")) {
					textNodes.add((Text) child);
				}

			} else if (child instanceof Element) {
				Element element = (Element) child;
				if (emptyValuesToDefaultToTrue.contains(element.getLocalName()) && element.getChildCount() == 0) {
					element.appendChild("true");
				}
				elementNodes.add((Element) child);

			} else {
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

	private static void gatherProperties(@Nonnull Element src,
	                                     @Nonnull Configuration.PropertyCollectionInstructions collectionInstructions,
	                                     @Nonnull Map<String, Object> targetPropertyMap,
	                                     @Nonnull Map<String, Object> targetDebugPropertyMap,
	                                     @Nonnull Set<HierarchicalIdentifier> targetOutboundReferenceSet) {
		Set<String> interestedProps = collectionInstructions.collectedChildProperties.get(src.getQualifiedName());
		Set<String> debugPropSet = collectionInstructions.collectedDebugProperties.get(src.getQualifiedName());
		Set<String> valuesToDefaultToTrue = collectionInstructions.valuesToDefaultToTrue;

		String debugLabel = null;

		if (interestedProps != null || debugPropSet != null) {

			for (int i = 0; i < src.getAttributeCount(); i++) {
				Attribute attr = src.getAttribute(i);
				String attrLabel = attr.getQualifiedName();
				String attrValue = attr.getValue();

				if (attr.getQualifiedName().equals("ID")) {
					// Skip since it is already incorporated in the identifier

				} else if (attr.getQualifiedName().equals("IDREF")) {
					// Add to outbound references
					targetOutboundReferenceSet.add(HierarchicalIdentifier.createReferenceNode(attr.getValue(), null));
				} else if (interestedProps != null && interestedProps.contains(attrLabel)) {
					// And add them to their respective lists if they match

					if ((attrValue == null || attrValue.equals("")) && valuesToDefaultToTrue.contains(attrLabel)) {
						targetPropertyMap.put(attrLabel, true);
					} else {
						targetPropertyMap.put(attrLabel, attrValue);
					}

				} else if (debugPropSet != null && debugPropSet.contains(attrLabel)) {
					if (debugLabel == null) {
						debugLabel = "v(" + src.getQualifiedName() + "){" + attrLabel + "=" + attrValue;
					} else {
						debugLabel += "," + attrLabel + "=" + attrValue;
					}
					// And add them to their respective lists if they match
					if ((attrValue == null || attrValue.equals("")) && valuesToDefaultToTrue.contains(attrLabel)) {
						targetDebugPropertyMap.put(attrLabel, true);
					} else {
						targetDebugPropertyMap.put(attrLabel, attrValue);
					}
				}
			}

			if (debugLabel != null) {
				targetDebugPropertyMap.put(ATTRIBUTE_DEBUG_LABEL_IDENTIFIER, debugLabel + "}");
			}
		}
	}

	private static void gatherInboundReferences(@Nonnull Element src, @Nonnull Set<HierarchicalIdentifier> targetSet) {
		Attribute idAttr = src.getAttribute("ID");

		if (idAttr != null) {
			Nodes nodes = src.getDocument().query("//*[@IDREF='" + idAttr.getValue() + "']");
			for (Node node : nodes) {
				if (!(node instanceof Element)) {
					throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Reference source should be an Element!");
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
	public synchronized HierarchicalData createData(@Nonnull Node srcNode, boolean isRootObject, @Nonnull Configuration.PropertyCollectionInstructions collectionInstructions) {

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

		// Get the parent node
		Node parentNode = src.getParent();

		if (!(parentNode instanceof Element)) {
			throw AdaptationnException.internal("Parent node must be an element!");
		}

		HierarchicalIdentifier parent = isRootObject ? null : createIdentifier((Element) parentNode);

		// Split out the Element and Text child nodes
		Set<Element> elementNodes = new HashSet<>();
		Set<Text> textNodes = new HashSet<>();
		splitChildrenByClass(src, elementNodes, textNodes, emptyValuesToDefaultToTrue);
		int attributeCount = src.getAttributeCount();

		if (textNodes.size() == 0) {
			if (elementNodes.size() == 0) {
				if (attributeCount == 0) {
					// Add empty node?
					throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Empty element node not valid!");

				} else {
					// Gather attributes/outbound references
					gatherProperties(src, collectionInstructions, collectedProps, collectedDebugProps, outboundReferences);
				}

			} else {
				if (attributeCount > 0) {
					// Gather attributes/outbound references
					gatherProperties(src, collectionInstructions, collectedProps, collectedDebugProps, outboundReferences);
				}
				// Add attribute child nodes and child nodes
				gatherAttributeElementsAndChildren(elementNodes, collectionInstructions.collectedChildProperties.get(src.getQualifiedName()), collectionInstructions.collectedDebugProperties.get(src.getQualifiedName()),
						collectedProps, collectedDebugProps, childMap);
			}

		} else if (textNodes.size() == 1) {
			if (elementNodes.size() == 0) {
				if (attributeCount == 0) {
					// Is collected by the parent
					return null;

				} else {
					// Gather attributes/outbound references
					gatherProperties(src, collectionInstructions, collectedProps, collectedDebugProps, outboundReferences);
					// Ignore Text data
				}

			} else {
				throw AdaptationnException.internal("Text nodes should not have children!");
			}

		} else {
			throw AdaptationnException.internal("There should only be a single text node per Element!");
		}

		gatherInboundReferences(src, inboundReferences);

		String debugLabel = null;
		if (collectedDebugProps.size() > 0) {
			List<String> values = collectedDebugProps.values().stream().map(Object::toString).collect(Collectors.toList());
			debugLabel = "(" + identifier.getNodeType() + ")[?]{" + String.join("/", values) + "}";
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
				debugLabel
		);
	}

	private static HierarchicalIdentifier createIdentifier(@Nonnull Element src) {
		Attribute attr = src.getAttribute("ID");
		// Adding the I prevents it from being serialized into a number and failing validation
		if (attr == null) {
			return HierarchicalIdentifier.produceTraceableNode(Integer.toString(src.hashCode()), src.getQualifiedName());
		} else {
			return HierarchicalIdentifier.createReferenceNode(attr.getValue(), src.getQualifiedName());
		}
	}

	public Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> convertOneToManyMap(@Nonnull Map<Node, Set<Node>> indirectRelations) {
		Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> rval = new HashMap<>();

		for (Map.Entry<Node, Set<Node>> entry : indirectRelations.entrySet()) {
			if (!(entry.getKey() instanceof Element)) {
				throw new AdaptationnException(ResultEnum.PerturbationInputInvalid, "Value must be an XML Element!");
			}
			HierarchicalIdentifier primaryNode = createIdentifier((Element) entry.getKey());
			Set<HierarchicalIdentifier> relationSet = rval.computeIfAbsent(primaryNode, k -> new HashSet<>());
			relationSet.addAll(entry.getValue().stream().map(x -> createIdentifier((Element) x)).collect(Collectors.toSet()));
		}
		return rval;
	}

	@Override
	public TreeMap<String, TreeMap<String, Object>> getPortMappingChartData() {
		return null;
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
