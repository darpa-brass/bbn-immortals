package mil.darpa.immortals.flitcons.xml;

import mil.darpa.immortals.flitcons.DummyData;
import mil.darpa.immortals.flitcons.datatypes.HierarchicalData;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class XmlHelper {


//	public boolean isElementProperty(Element e) {
//		return e.attributes().stream()
//				.anyMatch(t -> !(t.getName().equals("ID") || t.getName().equals("IDREF"))) ||
//				e.elements().stream().anyMatch(t -> t.elements().size() > 0 ||
//						t.attributes().stream().anyMatch(k -> !(k.getName().equals("ID") || k.getName().equals("IDREF"))));
//
//	}

	@Nullable
	public static Object getElementAsPropertyValue(Element e) {
			if (e.attributes().stream().anyMatch(t -> !(t.getName().equals("ID") || t.getName().equals("IDREF"))) ||
					e.elements().size() > 0) {
				return null;
			}
			return e.getStringValue();
	}

	private static HierarchicalData getOrCreateNodeData(Element element, Map<Element, HierarchicalData> targetDataSet, boolean isRootNode) {
		HierarchicalData primaryNodeData;
		if (!targetDataSet.containsKey(element)) {

			Map<String, Object> attributes = new HashMap<>();

			for (Element childElement : element.elements()) {
				Object childElementPropertyValue = getElementAsPropertyValue(childElement);
				if (childElementPropertyValue != null) {
					attributes.put(childElement.getName(), childElementPropertyValue);
				}
			}

			for (Attribute attribute : element.attributes()) {
				if (!attribute.getName().equals("ID") && !attribute.getName().equals("IDREF")) {
					attributes.put(attribute.getName(), attribute.getValue());
				}
			}


			primaryNodeData = new HierarchicalData(
					element.getName(),
					attributes,
					element.getUniquePath(),
					element,
					isRootNode
			);
			targetDataSet.put(element, primaryNodeData);
			return primaryNodeData;
		}
		return targetDataSet.get(element);
	}

	public XmlHelper() {

	}

	private HierarchicalData serializeObjectStructureIntoSet(@Nonnull Element parentElement, @Nonnull Map<Element, HierarchicalData> nodeDataMap, boolean isRootNode) {
		HierarchicalData parentNodeData = getOrCreateNodeData(parentElement, nodeDataMap, true);

		for (Element childElement : parentElement.elements()) {
			if (getElementAsPropertyValue(childElement) == null) {
				HierarchicalData childNodeData = serializeObjectStructureIntoSet(childElement, nodeDataMap, false);
				childNodeData.setParentNode(parentNodeData);
			}
		}

		return parentNodeData;
	}


	public Set<HierarchicalData> getDau(@Nonnull String identifier) {
		Map<Element, HierarchicalData> graphDataElementMap = new HashMap<>();

		Document source = DummyData.getTaggedTestInventoryDocument();

		Element dau = (Element) source.selectSingleNode("//*[mdl:GenericParameter/mdl:BBNDauUUID/text()=\"" + identifier + "\"]");

		HierarchicalData dauNodeData = serializeObjectStructureIntoSet(dau, graphDataElementMap, true);


		return null;
	}



	public static void main(String args[]) {
		XmlHelper xh = new XmlHelper();

		xh.getDau("DAU-f6b2bb54-7d78-4f5a-ba7d-1e0234c8661d");
	}
}
