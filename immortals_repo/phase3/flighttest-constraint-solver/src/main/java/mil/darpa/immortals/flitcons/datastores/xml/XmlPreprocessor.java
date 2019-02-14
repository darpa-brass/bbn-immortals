package mil.darpa.immortals.flitcons.datastores.xml;

import mil.darpa.immortals.flitcons.DummyData;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class XmlPreprocessor {

	private static final Namespace mdlNamespace = new Namespace("mdl", "http://www.wsmr.army.mil/RCC/schemas/MDL");
	private static Logger logger = LoggerFactory.getLogger(XmlPreprocessor.class);

	private String tagElement(@Nonnull String tagName, @Nonnull Element element, @Nonnull String prefix) {
		Element genericParameterElement = (Element) element.selectSingleNode("mdl:GenericParameter");

		if (genericParameterElement == null) {
			genericParameterElement = element.addElement("GenericParameter");
			Element nvs = genericParameterElement.addElement("NameValues");
			Element nv = nvs.addElement("NameValue");
			nv.addAttribute("Name", "User");
			nv.addAttribute("Index", "1");
			nv.setText("BBN");
		}

		String identifier = prefix + "-" + UUID.randomUUID().toString();

		genericParameterElement.addElement(tagName).setText(identifier);
		return identifier;
	}

	public void preprocessDauInventory(@Nonnull File targetFile) {
		logger.info("Tagging Dau, Module, and Port instances in DAU Inventory xml...");
		Document ti = DummyData.getRawTestInventoryDocument();

		for (Node n : ti.selectNodes("//*[mdl:TmNSApps/mdl:TmNSApp/mdl:TmNSDAU]")) {
			tagElement("DauInstanceGlobalIdentifier", (Element) n, "DAU");
		}

		for (Node n : ti.selectNodes("//mdl:Module")) {
			tagElement("ModuleInstanceGlobalIdentifier", (Element) n, "MODULE");
		}

		for (Node n : ti.selectNodes("//mdl:Port")) {
			Element parentElement = (Element) n;

			Element functionalityElement = (Element) parentElement.selectSingleNode("mdl:GenericParameter/mdl:BBNPortFunctionality");

			if (functionalityElement != null) {
				String functionality = functionalityElement.getText();
				if (!functionality.equals("IgnoredPort")) {
					String portDirection = parentElement.selectSingleNode("mdl:PortDirection").getText();
					String portType = ((Element) parentElement.selectSingleNode("mdl:PortTypes/mdl:PortType")).getText();
					String dataLength = ((Element) parentElement.selectSingleNode("mdl:GenericParameter/mdl:BBNDataLength")).getText();
					String sampleRates = ((Element) parentElement.selectSingleNode("mdl:GenericParameter/mdl:BBNSampleRates")).getText();

					String identifier = functionality + "-" + portDirection + "-" + portType + "-" + dataLength + "-" + sampleRates;

					tagElement("PortInstanceGlobalIdentifier", parentElement, identifier);
				}
			}
		}

		try {
			OutputStream os = new FileOutputStream(targetFile);
			XMLWriter writer = new XMLWriter(os);
			writer.write(ti);
			writer.flush();
			writer.close();
			logger.info("Tagged file saved to '" + targetFile.getPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
