package mil.darpa.immortals.flitcons;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mil.darpa.immortals.flitcons.solvers.dsl.DslSolver;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;

public class DummyData {

	static final Namespace mdlNamespace = new Namespace("mdl", "http://www.wsmr.army.mil/RCC/schemas/MDL");

	public static Document getRawTestInventoryDocument() {
		try {
			InputStream is = DslSolver.class.getClassLoader().getResourceAsStream(
					"dummy_data/BRASS_Scenario5_AfterAdaptation.xml");
			SAXReader reader = new SAXReader();
			Document rval = reader.read(is);
			rval.getRootElement().add(mdlNamespace);
			return rval;
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}
	}

	public static Document getTaggedTestInventoryDocument() {
		try {
			InputStream is = DslSolver.class.getClassLoader().getResourceAsStream(
					"dummy_data/BRASS_Scenario5_AfterAdaptation-tagged.xml");
			SAXReader reader = new SAXReader();
			Document rval = reader.read(is);
			rval.getRootElement().add(mdlNamespace);
			return rval;
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}
	}


	public static class PortGroupingSet {

		private final Map<PortGrouping, Set<String>> groupingMap = new HashMap<>();

		public static class PortGrouping implements Comparable<PortGrouping>, Comparator<PortGrouping> {
			public final String functionality;
			public final String portDirection;
			public final String portType;
			/*public final String portExtensionKey;
			public final String portExtensionValue;*/
			public final String dataLength;
			public final String sampleRates;

			private PortGrouping(String functionality, String portDirection, String portType, /*String portExtensionKey, String portExtensionValue,*/ String dataLength, String sampleRates) {
				this.functionality = functionality;
				this.portDirection = portDirection;
				this.portType = portType;
				/*this.portExtensionKey = portExtensionKey;
				this.portExtensionValue = portExtensionValue;*/
				this.dataLength = dataLength;
				this.sampleRates = sampleRates;
			}

			public String toString() {
				return functionality + "-" + portDirection + "-" + portType + "-" + dataLength + "-" + sampleRates;
			}

			@Override
			public int compareTo(PortGrouping portGrouping) {
				return compare(this, portGrouping);
			}

			@Override
			public int compare(PortGrouping o, PortGrouping t1) {
				if (o == null) {
					if (t1 == null) {
						return 0;
					} else {
						return -1;
					}
				} else {
					if (t1 == null) {
						return 1;
					} else {
						if (o.functionality.equals(t1.functionality) &&
								o.portDirection.equals(t1.portDirection) &&
								o.portType.equals(t1.portType) &&
								/*o.portExtensionKey.equals(t1.portExtensionKey) &&
								o.portExtensionValue.equals(t1.portExtensionValue) &&*/
								o.dataLength.equals(t1.dataLength) &&
								o.sampleRates.equals(t1.sampleRates)) {
							return 0;
						} else {
							return 1;
						}
					}
				}
			}

			@Override
			public boolean equals(Object o) {
				if (!(o instanceof PortGrouping)) {
					return false;
				} else {
					return compare(this, (PortGrouping) o) == 0;
				}

			}
		}

		public synchronized PortGrouping addPortGrouping(@Nonnull String functionality, @Nonnull String portDirection,
														 @Nonnull String portType, /*@Nonnull String portExtensionKey,
														 @Nonnull String portExtensionValue,*/ @Nonnull String dataLength,
														 @Nonnull String sampleRates, @Nonnull String identifier) {

			Optional<PortGrouping> optionalPortGrouping = this.groupingMap.keySet().stream().filter(t ->
					t.functionality.equals(functionality) &&
							t.portDirection.equals(portDirection) &&
							t.portType.equals(portType) &&
							/*t.portExtensionKey.equals(portExtensionKey) &&
							t.portExtensionValue.equals(portExtensionValue) &&*/
							t.dataLength.equals(t.dataLength) &&
							t.sampleRates.equals(sampleRates)
			).findAny();

			if (optionalPortGrouping.isPresent()) {
				PortGrouping portGrouping = optionalPortGrouping.get();
				this.groupingMap.get(portGrouping).add(identifier);
				return portGrouping;
			} else {
				PortGrouping newGrouping = new PortGrouping(functionality, portDirection, portType, /*portExtensionKey, portExtensionValue,*/ dataLength, sampleRates);
				this.groupingMap.put(newGrouping, new HashSet<>());
				this.groupingMap.get(newGrouping).add(identifier);
				return newGrouping;
			}
		}

		public void writePortGroupingMap(@Nonnull File target) throws IOException {
			JsonObject jo = new JsonObject();
			for (PortGrouping obj : this.groupingMap.keySet()) {
				JsonArray identifiers = new JsonArray();
				for (String val : this.groupingMap.get(obj)) {
					identifiers.add(val);
				}
				jo.add(obj.toString(), identifiers);
			}
			FileWriter fw = new FileWriter(target);
			Utils.nonHtmlEscapingGson.toJson(jo, JsonObject.class, fw);
			fw.flush();
			fw.close();
		}
	}


	public static void generatePortGroupingMap(@Nonnull File sourceFile, @Nonnull File targetFile) {
		try {
			InputStream is = new FileInputStream(sourceFile);
			SAXReader reader = new SAXReader();
			Document ti = reader.read(is);
			ti.getRootElement().add(mdlNamespace);

			PortGroupingSet pgs = new PortGroupingSet();

			for (Node n : ti.selectNodes("//mdl:Port")) {
				Element parentElement = (Element) n;

				Element functionalityElement = (Element) parentElement.selectSingleNode("mdl:GenericParameter/mdl:BBNPortFunctionality");

				Element portInstanceIdentifierElement = (Element) parentElement.selectSingleNode("mdl:GenericParameter/mdl:PortInstanceGlobalIdentifier");

				if (functionalityElement != null) {
					String functionality = functionalityElement.getText();
					if (!functionality.equals("IgnoredPort")) {
						String portDirection = parentElement.selectSingleNode("mdl:PortDirection").getText();
						String portType = ((Element) parentElement.selectSingleNode("mdl:PortTypes/mdl:PortType")).getText();
						String dataLength = ((Element) parentElement.selectSingleNode("mdl:GenericParameter/mdl:BBNDataLength")).getText();
						String sampleRates = ((Element) parentElement.selectSingleNode("mdl:GenericParameter/mdl:BBNSampleRates")).getText();

						String tag = portInstanceIdentifierElement.getText();

						pgs.addPortGrouping(functionality, portDirection, portType, dataLength, sampleRates, tag);
					}
				}
			}

			pgs.writePortGroupingMap(targetFile);
		} catch (DocumentException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
