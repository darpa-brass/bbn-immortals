package com.securboration.test;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import com.securboration.dfus.xml.transform.XsltTransformer;

public class TestTranslationDfu {
	
	private static class Xsd{

		private final String id;
		
		private final Schema schema;
		
		private final Validator validator;
		
		private Xsd(final File xsdFile) throws IOException, SAXException {
			this.id = xsdFile.getName();
			
			final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			
			this.schema = factory.newSchema(xsdFile);
			this.validator = schema.newValidator();
		}
		
		private void checkConformance(final Source document) throws Exception {
	        validator.validate(document);
		}
	}
	
	private static class DocumentBag{
		
		private final List<File> docs = new ArrayList<>();
		private final Xsd schema;
		
		public DocumentBag(final Xsd schema, final File docDir) throws IOException {
			this.schema = schema;
			for(File doc:FileUtils.listFiles(docDir, "xml")) {
				docs.add(doc);
			}
		}
		
	}
	
	private static class DocumentBags{
		private final Map<Xsd,DocumentBag> bagMap = new HashMap<>();
		
		private DocumentBags(DocumentBag...bags) {
			for(DocumentBag bag:bags) {
				bagMap.put(bag.schema, bag);
			}
		}
		
		private DocumentBag get(final Xsd id) {
			return bagMap.get(id);
		}
	}
	
	private static class Translator{
		private final Xsd src;
		private final Xsd dst;
		private final String id;
		private final String xslt;
		
		private Translator(final Xsd src, final Xsd dst, final File xslt) throws FileNotFoundException, IOException {
			this.src = src;
			this.dst = dst;
			this.id = xslt.getName();
			this.xslt = FileUtils.readFileToString(xslt,StandardCharsets.UTF_8);
		}
		
		private String translate(final String xmlToTranslate) throws TransformerException, JAXBException {
			System.out.println("using dfu");
			
			return translate(xslt, xmlToTranslate);
		}
		
		private static String translate(
				final String xslt,
				final String xmlToTranslate
		) throws JAXBException, TransformerException {

			if (xslt.equals("no translation needed")) {
				return xmlToTranslate;
			}

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer(new StreamSource(new StringReader(xslt)));

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.VERSION, "1.0");

			ByteArrayOutputStream out = new ByteArrayOutputStream();

			transformer.transform(
					new StreamSource(new StringReader(xmlToTranslate)),
					new StreamResult(out)
			);

			return out.toString();
		}
	}
	
	private static class Translators {
		private final List<Translator> translators = new ArrayList<>();
		
		private Translators(Translator...translators) {
			for(Translator translator:translators) {
				this.translators.add(translator);
			}
		}
		
		private Translator getTranslator(final Xsd srcVersion, final Xsd dstVersion) {
			for(Translator t:translators) {
				if(t.src.equals(srcVersion) && t.dst.equals(dstVersion)) {
					return t;
				}
			}
			
			throw new RuntimeException("no translator from " + srcVersion + " to " + dstVersion);
		}
		
		private String translate(
				final Xsd srcVersion, 
				final Xsd dstVersion, 
				final String document
				) throws TransformerException, JAXBException {
			return getTranslator(srcVersion,dstVersion).translate(document);
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		
		final Xsd v17 = new Xsd(new File("../etc/schemas/v1/MDL_v0_8_17.xsd"));
		final Xsd v19 = new Xsd(new File("../etc/schemas/v2/MDL_v0_8_19.xsd"));
		
		final Xsd[] xsds = {v17,v19};
		
		final DocumentBags docs = new DocumentBags(
				new DocumentBag(v17,new File("../etc/messages/v1")),
				new DocumentBag(v19,new File("../etc/messages/v2"))
				);
		
		final Translators translators = new Translators(
				new Translator(v17,v19,new File("../etc/xslt/v1-to-v2/v17_to_19.xsl")),
				new Translator(v19,v17,new File("../etc/xslt/v2-to-v1/v19_to_17.xsl"))
				);
		
		final long start = System.currentTimeMillis();
		double count = 0d;
		
		for(int i=0;i<1;i++) {System.out.printf("iteration %d\n",i);
		for(Xsd src:xsds) {
			final DocumentBag srcDocs = docs.get(src);
			for(Xsd dst:xsds) {
				if(src == dst) {
					continue;
				}
				
				System.out.printf("%s  (%d docs)--> %s\n", src.id, srcDocs.docs.size(), dst.id);
				
				for(File doc:srcDocs.docs) {
					count++;
					System.out.printf("\t"+doc.getName());//TODO
					
					final String docContent = FileUtils.readFileToString(doc, StandardCharsets.UTF_8);
					
					src.checkConformance(new StreamSource(new StringReader(docContent)));//verify that the document is a valid src document
					
					System.out.printf(" is a valid %s document", src.id);
					
					final Source converted = new StreamSource(new StringReader(translators.translate(src, dst, docContent)));
					
					dst.checkConformance(converted);//verify that the document is a valid dst document
					
					System.out.printf(" that was translated into a valid %s document",dst.id);
					
					final String translatedViaDfu = XsltTransformer.translate(
							translators.getTranslator(src, dst).xslt, 
							FileUtils.readFileToString(doc, StandardCharsets.UTF_8)
							);
					
					dst.checkConformance(new StreamSource(new StringReader(translatedViaDfu)));
					
					System.out.printf(" and for which DFU functionality was verified\n",dst.id);
				}
			}
		}
		}
		
		final double elapsed = (double)(System.currentTimeMillis() - start);
		
		final double countPerMinute = 
				(count / elapsed)	//count/milli
				*1000*60 			//# millis/minute
				;
		
		System.out.printf(
				"processed %d documents in %dms, rate = %1.2f/min", 
				(int)count, 
				(long)elapsed, 
				countPerMinute
				);
	}
	
	private static Source getSource(File f) throws FileNotFoundException, IOException {
		final String text = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
		return new StreamSource(new StringReader(text));
	}
	//TODO: fails to find schema for a source not instantiated with a file arg

}
