package com.securboration.immortals.utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

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

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class SchemaMinimizer {
    
    public static void main(String[] args) throws Exception{
        
        final File essMinDir = new File("C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\cp-eval-service\\eval-out\\ess\\ess");
        
        final File lightenerXslt = new File(essMinDir,"etc/lightener/SchemaLightener1.xslt");
        final File schemaToLighten = new File(essMinDir,"schema/client/");
        final File exemplarDocumentDir = new File(essMinDir,"datasource");
        final File schemaOutputDir = new File(essMinDir,"analysis/client/schema-minimized");
        
        minimizeSchema(
            lightenerXslt,
            findMdlSchema(schemaToLighten),
            exemplarDocumentDir,
            schemaOutputDir
            );
    }
    
    private static File findMdlSchema(final File schemaDir){
        for(File f:FileUtils.listFiles(schemaDir, new String[]{"xsd"}, true)){
            final String name = f.getName().toLowerCase();
            
            if(name.startsWith("mdl")){
                return f;
            }
        }
        
        throw new RuntimeException("could not find MDL schema definition in " + schemaDir.getAbsolutePath());
    }
    
    private static File createTemporaryUberDocument(final Iterable<File> exemplars) throws SAXException, IOException, ParserConfigurationException, TransformerException{
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        
        final Document uber = factory.newDocumentBuilder().newDocument();
        
        final Element wrapperElement = uber.createElementNS(
            "http://securboration.com/immortals/uberXml",
            "tns:WrappedElements"
            );
        
        for(File f:exemplars){
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document d = builder.parse(f);
            final Node adopted = uber.adoptNode(d.getDocumentElement());
            wrapperElement.appendChild(adopted);
        }
        
        uber.appendChild(wrapperElement);
        
        final String stringForm = prettyPrint(uber);
        
        final File temp = File.createTempFile("immortals-analysis", ".uberXml");
        
        FileUtils.writeStringToFile(temp, stringForm, StandardCharsets.UTF_8);
        
        System.out.printf(
            "uber file is %dB @ %s\n",
            temp.length(),
            temp.getAbsolutePath()
            );
        
        return temp;
    }
    
    
    private static String prettyPrint(Document doc) throws UnsupportedEncodingException, TransformerException{
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), 
             new StreamResult(new OutputStreamWriter(bos, StandardCharsets.UTF_8.name())));
        
        return bos.toString(StandardCharsets.UTF_8.name());
    }
    
    /**
     * 
     * @param lightenerXslt
     *            the schema lightener XSLT
     * @param schemaToLighten
     *            an XML schema definition (it may import other schema content)
     * @param exemplarDocumentDir
     *            a directory containing XML documents that embody a complete
     *            and correct use case of the schema
     * @param outputDir
     *            a directory to dump the resultant schema into
     * @throws IOException
     * @throws TransformerException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static void minimizeSchema(
            final File lightenerXslt,
            final File schemaToLighten,
            final File exemplarDocumentDir,
            final File outputDir
            ) throws IOException, TransformerException, SAXException, ParserConfigurationException{
        FileUtils.forceMkdir(outputDir);
        
        /*
         * Step 1: add all of the exemplar documents into an uber XML document
         * Step 2: run the lightener XSLT on the uber document
         */
        
        final File exemplarDocument = createTemporaryUberDocument(
            FileUtils.listFiles(exemplarDocumentDir, new String[]{"xml"}, true)
            );
        
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer xsltTransformer = transformerFactory.newTransformer(new StreamSource(lightenerXslt));
        
        final String docPath = "file:///" + exemplarDocument.getCanonicalPath().replace("\\", "/");
        final String outPath = "file:///" + outputDir.getCanonicalPath().replace("\\", "/") + "/";
        
        xsltTransformer.setParameter("instanceFilePathAndName",docPath);
        xsltTransformer.setParameter("resultBasePath",outPath);
//        xsltTransformer.setParameter("trace", "true");
        
        xsltTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
        xsltTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        xsltTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
        xsltTransformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        xsltTransformer.transform(
            new StreamSource(schemaToLighten),
            new StreamResult(out)
        );
        
        System.out.println(FileUtils.readFileToString(exemplarDocument));//TODO
        
        final byte[] result = out.toByteArray();
        
        if(result.length > 0){
            throw new RuntimeException("expected an empty result but got " + result.length);
        }
    }

}
