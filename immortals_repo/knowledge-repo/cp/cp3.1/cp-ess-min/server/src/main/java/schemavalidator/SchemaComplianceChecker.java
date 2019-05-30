package schemavalidator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * A "forgiving" schema validator. Instead of nuking the validation when a
 * single error is detected, this tool will continue to validate the remainder
 * of the document and return a score on [0,1] indicating how well the document
 * conformed to the schema (0 is completely nonconformant, 1 is completely
 * conformant).
 * 
 * @author jstaples
 *
 */
public class SchemaComplianceChecker {
    
    public static void main(String[] args) throws Exception{
        final File xmlToValidate = new File("C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\input\\MDL_v0_8_17-MDL_v0_8_19\\datasource\\AssetAssociations.xml");
        final File schemaDir = new File("C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\input\\MDL_v0_8_17-MDL_v0_8_19\\schema\\test");
        
        for(File f:FileUtils.listFiles(xmlToValidate.getParentFile(), new String[] {"xml"}, false)){
            System.out.printf("%s\n", f.getName());
            BadnessReport report = getDocumentBadnessScore(
                schemaDir,
                new StringSource(FileUtils.readFileToString(f,StandardCharsets.UTF_8))
                );
            
            System.out.println(report);
            System.out.println();
        }
        
        
        
        
//        System.out.println(getLineDepth("0"));
//        System.out.println(getLineDepth(" 1"));
//        System.out.println(getLineDepth("  2"));
//        System.out.println(getLineDepth("   3"));
//        System.out.println(getLineDepth("    4"));
    }
    
    private static String sourceToString(Source s) throws UnsupportedEncodingException, TransformerException {
    	Transformer transformer = TransformerFactory.newInstance().newTransformer();
    	
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
        Result output = new StreamResult(out);
        
        transformer.transform(s, output);
        
        return new String(out.toString(StandardCharsets.UTF_8.name()));
    }
    
    
    /**
     * 
     * 
     * 
     * @return 0 if no errors are discovered. Else, a real number on [0,1) will
     *         be returned indicating the fraction of the children of top-level
     *         elements in which errors were detected.
     * @throws IOException 
     * @throws SAXException 
     */
    public static BadnessReport getDocumentBadnessScore(
            final File schemaDir,
            final Source xmlToValidateSource
            ) throws Exception{
    	
    	final String xmlToValidate = sourceToString(xmlToValidateSource);

//    	System.out.println(xmlToValidate);
    	
//        <?xml version="1.0" encoding="UTF-8"?>
//        <topLevelElement>
//            <midLevelElementA>...</midLevelElementA>
//            <midLevelElementB>...</midLevelElementB>
//            <midLevelElementC>...</midLevelElementC>
//        </topLevelElement>

        
        //{error elements}, score
        //  {},                     1.0
        //  {A},{B},{C}:            0.66
        //  {A,B},{A,C},{B,C}:      0.33
        //  {A,B,C}:                0.0
        
        final String normalizedXmlInput = normalizeXml(xmlToValidate);
        
        final Map<String,List<Exception>> exceptions = verifyCompliance(
            normalizedXmlInput,
            schemaDir
            );
        
        final Map<Integer,DocumentNode> map = construct(normalizedXmlInput);
        
        final DocumentNode root = map.get(1);
        
        
        final Set<DocumentNode> badNodes = new HashSet<>();
        final List<BadnessReportElement> elements = new ArrayList<>();
        for(String key:exceptions.keySet()){
//            System.out.printf("%s\n", key);
            if(key.equals("warning")){
                continue;//don't include warnings in badness score
            }
            
            for(Exception e:exceptions.get(key)){
//                System.out.printf("\t%s\n", e);
                
                final String s = e.toString();
                
                if(!s.contains("org.xml.sax.SAXParseException; lineNumber: ")){
                    throw new RuntimeException("no line # info");
                }
                
                String[] parts = s.split(";");
                
                String linePart = parts[1].trim().replace("lineNumber:", "").trim();
                
                final int line = Integer.parseInt(linePart);
                final String lineContent = root.content.get(line-1);
                
                final DocumentNode d = map.get(line);
                
                badNodes.add(d);
                
                final List<String> content = d.content;
                final String first = content.get(0);
                final String last = content.get(content.size()-1);
                
                final String summary = String.format(
                    "%s @line %d \"%s\" in %d-line element spanning [%d,%d] \"%s\" ... \"%s\" with message \"%s\"", 
                    key.toUpperCase(),
                    line, 
                    lineContent.trim(),
                    d.endIndex - d.startIndex + 1,
                    d.startIndex, 
                    d.endIndex, 
                    first.trim(), 
                    last.trim(),
                    e.getMessage()
                    );
                
                final BadnessReportElement element = new BadnessReportElement(e,key,summary);
                elements.add(element);
            }
        }
        
        double badness = 0d;
        for(DocumentNode d:badNodes){
            badness += (d.endIndex - d.startIndex + 1);
        }
        
        final double rollupBadnessScore = badness / (1d*root.content.size());
        final double weightedBadnessScore = rollupBadnessScore * root.content.size();
        BadnessReport report = new BadnessReport(rollupBadnessScore,weightedBadnessScore);
        report.elements.addAll(elements);
        
        return report;
    }
    
    
    private static void pruneExtraneous(
            List<Exception> exceptions,
            String...filterSubstrings
            ){
        List<Exception> pruneThese = new ArrayList<>();
        for(Exception e:exceptions){
            final String eString = e.toString();
            for(String s:filterSubstrings){
                if(eString.contains(s)){
                    pruneThese.add(e);
                }
            }
        }
        
        exceptions.removeAll(pruneThese);
    }
    
    
    private static File getMdlXsd(final File dir){
        for(File f:dir.listFiles()){
            if(f.getName().startsWith("MessageListenerSchema")){
                return f;
            }
        }
        
        throw new RuntimeException("couldn't find an MDL xsd in " + dir.getAbsolutePath());
    }
    
    private static Map<String,List<Exception>> verifyCompliance(
            final String xmlToValidate,
            final File xsdDir
            ) throws SAXException, IOException {
        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        Schema schema = factory.newSchema(getMdlXsd(xsdDir));
        Validator validator = schema.newValidator();
        
        final List<Exception> warnings = new ArrayList<>();
        final List<Exception> errors = new ArrayList<>();
        final List<Exception> fatals = new ArrayList<>();
        
        validator.setErrorHandler(new ErrorHandler() {

            @Override
            public void warning(SAXParseException exception) throws SAXException {
                warnings.add(exception);
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                errors.add(exception);
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                fatals.add(exception);
            }
        });
        
        validator.validate(new StreamSource(new ByteArrayInputStream(xmlToValidate.getBytes())));
        
        {//prune
            final String[] filterSubstrings = {
                    "not found for identity constraint of element ",
                    "There is no ID/IDREF binding for IDREF"
            };
            
            pruneExtraneous(warnings,filterSubstrings);
            pruneExtraneous(errors,filterSubstrings);
            pruneExtraneous(fatals,filterSubstrings);
        }//prune
        
        final Map<String,List<Exception>> exceptions = new LinkedHashMap<>();
        exceptions.put("warning", warnings);
        exceptions.put("error", errors);
        exceptions.put("fatal", fatals);

        return exceptions;
    }
    
    /**
     * "normalizes" an XML input string (removes comments and regularizes whitespace)
     * 
     * @param xml
     * @return
     * @throws TransformerFactoryConfigurationError
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    private static String normalizeXml(final String xml) throws TransformerFactoryConfigurationError, SAXException, IOException, TransformerException, ParserConfigurationException{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
         
        Document doc = db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        
        
        Transformer tf = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(removeCommentsXsl)));
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        
        Writer out = new StringWriter();
        tf.transform(new DOMSource(doc), new StreamResult(out));
        
        return out.toString();
    }
    
    private static int getLineDepth(final String xmlLine){
        if(xmlLine.isEmpty()){
            return -1;
        }
        
        int count = 0;
        for(int i=0;i<xmlLine.length();i++){
            if(xmlLine.charAt(i) == ' '){
                count++;
            }
            
            if(xmlLine.charAt(i) != ' '){
                return count;
            }
        }
        
        return count;
    }
    
    
    private static Map<Integer,DocumentNode> construct(final String xml){
        DocumentNode current = null;
        
        int index = 0;
        int lastDepth = 0;
        
        System.out.println(xml);//TODO
        final List<String> lines = new ArrayList<>();
        for(String line:xml.split("\\r?\\n")){
            lines.add(line);
            index++;
            
            final int depth = getLineDepth(line);
            final int depthDelta = depth - lastDepth;
            
            if(!line.trim().startsWith("<")){
                continue;//it's a continuation of a previous element
            } else if(current == null){
                current = new DocumentNode();
                current.startIndex = index;
                current.endIndex = index;
            } else if(depthDelta > 3){
                //ignore this
            } else if(depthDelta == +3){
                DocumentNode child = new DocumentNode();
                current.children.add(child);
                child.startIndex = index;
                child.endIndex = index;
                child.parent = current;
                
                current = child;
            } else if(depthDelta == -3){
                DocumentNode parent = current.parent;
                current.parent.endIndex = index;
                
                current = parent;
            } else if(depthDelta == 0){
                DocumentNode child = new DocumentNode();
                child.parent = current.parent;
                child.startIndex = index;
                child.endIndex = index;
                
//                System.out.println(line);
                current.parent.children.add(child);
                
                current = child;
            } else {
                continue;//TODO
            }
            
            {//update counters
                if(depthDelta == 3 || depthDelta == -3){
                    lastDepth = depth;
                }
            }
        }
        
        current.endIndex = index;
        
        {
            final Map<Integer,DocumentNode> lineToNode = new TreeMap<>();
            final List<DocumentNode> nodes = DocumentNode.collect(current);
            
            for(int i=1;i<=index;i++){
                DocumentNode best = null;
                
                for(DocumentNode n:nodes){
                    if((n.startIndex<=i) && (i<=n.endIndex)){
                        if(best == null){
                            best = n;
                        } else {
                            final int bestDelta = best.endIndex - best.startIndex;
                            final int currentDelta = n.endIndex - n.startIndex;
                            
                            if(currentDelta < bestDelta){
                                best = n;
                            }
                        }
                    }
                }
                
                lineToNode.put(i, best);
            }
            
            for(DocumentNode n:nodes){
                for(int i=n.startIndex-1;i<=n.endIndex-1;i++){
                    n.content.add(lines.get(i));
                }
            }
            
            return lineToNode;
        }
    }
    
    
    private static final String removeCommentsXsl = 
            "<xsl:stylesheet version=\"1.0\"\r\n" + 
            " xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\r\n" + 
            "  <xsl:output omit-xml-declaration=\"yes\" indent=\"yes\"/>\r\n" + 
            "  <xsl:strip-space elements=\"*\"/>\r\n" + 
            "\r\n" + 
            "  <xsl:template match=\"node()|@*\" name=\"identity\" priority=\"5\">\r\n" + 
            "        <xsl:copy>\r\n" + 
            "            <xsl:apply-templates select=\"node()|@*\"/>\r\n" + 
            "        </xsl:copy>\r\n" + 
            "   </xsl:template>\r\n" + 
            "   <xsl:template match=\"comment()\"/>\r\n" + 
            "</xsl:stylesheet>";
}


