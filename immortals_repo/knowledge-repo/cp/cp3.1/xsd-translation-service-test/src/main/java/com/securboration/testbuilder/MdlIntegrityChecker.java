package com.securboration.testbuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

public class MdlIntegrityChecker {
    
    public static void retainGoodMdlVersions(
            final File mdlVersionsDir,
            final File outputDir,
            final File mdlTranslationProblemOutputDir
            ) throws Exception{
        {//purge output dir
            if(outputDir.exists()){
                FileUtils.deleteDirectory(outputDir);
            }
            FileUtils.forceMkdir(outputDir);
        }
        
        final Map<Xsd,DocumentBag> mdlVersions = new LinkedHashMap<>();
        final Map<File,Exception> errors = new LinkedHashMap<>();
        for(final File f:mdlVersionsDir.listFiles()){
            if(!f.isDirectory()){
                continue;
            }
            System.out.println(f.getName());
            
            try{
                final File xsdFile = new File(f,"schema/" + f.getName() + ".xsd");
                final File documentDir = new File(f,"datasource");
                
                {
                    final String xsdContent = FileUtils.readFileToString(xsdFile,StandardCharsets.UTF_8);
                    
                    if(!xsdContent.contains("<xsd:element name=\"MDLRoot\" type=\"MDLRootType\">")){
                        throw new RuntimeException("schema does not contain an MDLRoot");
                    }
                }
                
                final Xsd xsd = new Xsd(xsdFile);
                final DocumentBag docs = new DocumentBag(xsd);
                
                docs.add(documentDir, errors);
                
                if(docs.docs.size() == 0){
                    throw new RuntimeException("no conformant docs found in dir " + f.getAbsolutePath());
                }
                
                mdlVersions.put(xsd, docs);
            } catch(Exception e){
                e.printStackTrace();
                errors.put(f, e);
            }
        }
        
        {//print status
            System.out.println();
            
            System.out.printf("ERRORS:\n");
            for(File f:errors.keySet()){
                final Exception e = errors.get(f);
                System.out.printf("\t%s: %s\n", f.getAbsolutePath(), e.getMessage());
            }
            
            System.out.printf("GOOD (%d):\n",mdlVersions.size());
            for(Xsd src:mdlVersions.keySet()){
                System.out.printf("\t%s (%d valid docs)\n",src.id,mdlVersions.get(src).docs.size());
            }
        }
        
        {//copy the verified XSD and XML into the output directory
            for(final Xsd src:mdlVersions.keySet()){
                final DocumentBag docs = mdlVersions.get(src);
                
                final File outputDirBase = new File(outputDir,src.id);
                final File schemaOutputDir = new File(outputDirBase,"schema");
                final File datasourceOutputDir = new File(outputDirBase,"datasource");
                
                FileUtils.copyDirectory(
                    src.xsdFile.getParentFile(), 
                    schemaOutputDir
                    );
                
                for(File f:docs.docs){
                    FileUtils.copyFileToDirectory(f, datasourceOutputDir);
                }
                
                {//add a synthetic schema for use in a wsdl
                    final String schemaName = src.xsdFile.getName();
                    final String wsdlSchema = Magic.wsdlSchemaTemplate.replace("${SCHEMA_NAME}", schemaName);
                    final File wsdlSchemaFile = new File(schemaOutputDir,"MessageListenerSchema.xsd");
                    
                    FileUtils.writeStringToFile(
                        wsdlSchemaFile, 
                        wsdlSchema, 
                        StandardCharsets.UTF_8
                        );
                    
                    {//sanity check the integrity of the synthetic schema
                        try{
                            new Xsd(wsdlSchemaFile);
                        } catch(Exception e){
                            e.printStackTrace();
                            errors.put(wsdlSchemaFile, e);
                            
                            FileUtils.deleteDirectory(outputDirBase);
                        }
                    }
                }
            }
        }
        
        {//create translation problem output
            createTranslationProblemArtifacts(
                mdlVersions,
                mdlTranslationProblemOutputDir
                );
        }
    }
    
    private static void createTranslationProblemArtifacts(
            final Map<Xsd,DocumentBag> xsds, 
            final File outputDir
            ) throws IOException{
        {
            if(outputDir.exists()){
                FileUtils.deleteDirectory(outputDir);
            }
            FileUtils.forceMkdir(outputDir);
        }
        
        for(final Xsd srcXsd:xsds.keySet()){
            final DocumentBag srcDocs = xsds.get(srcXsd);
            
            for(final Xsd dstXsd:xsds.keySet()){
                
                System.out.printf("writing %s --> %s\n", srcXsd.id, dstXsd.id);
                
                //dir
                // datasource
                // schema
                //  client
                //  datasource
                //  server
                
                final File tpOutDir = new File(
                    outputDir,
                    String.format("%s-%s", srcXsd.id, dstXsd.id)
                    );
                
                {//dir/datasource
                    final File datasourceDir = new File(tpOutDir,"datasource");
                    for(File doc:srcDocs.docs){
                        FileUtils.copyFileToDirectory(doc, datasourceDir);
                    }
                }
                
                {
                    //dir/schema/client
                    //dir/schema/datasource
                    FileUtils.copyDirectory(srcXsd.xsdFile.getParentFile(), new File(tpOutDir,"schema/client"));
                    FileUtils.copyDirectory(srcXsd.xsdFile.getParentFile(), new File(tpOutDir,"schema/datasource"));
                    FileUtils.copyDirectory(dstXsd.xsdFile.getParentFile(), new File(tpOutDir,"schema/server"));
                }
            }
        }
    }
    
    
    private static class DocumentBag{
        
        private final List<File> docs = new ArrayList<>();
        private final Xsd schema;
        
        public DocumentBag(final Xsd schema) {
            this.schema = schema;
        }
        
        public void add(
                final File docDir,
                final Map<File,Exception> exceptionMap
                ) throws Exception {
            for(File doc:FileUtils.listFiles(docDir, new String[]{"xml"}, true)) {
                
                try(FileInputStream fis = new FileInputStream(doc)){
                    schema.checkConformance(new StreamSource(fis));
                    
                    docs.add(doc);
                } catch(Exception e){
                    e.printStackTrace();
                    exceptionMap.put(doc, e);
                }
            }
        }
        
    }
    
    private static class Xsd{

        private final String id;
        
        private final Schema schema;
        
        private final Validator validator;
        
        final File xsdFile;
        
        private Xsd(final File xsdFile) throws IOException, SAXException {
            this.id = FilenameUtils.removeExtension(xsdFile.getName());
            this.xsdFile = xsdFile;
            
            final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            this.schema = factory.newSchema(xsdFile);
            this.validator = schema.newValidator();//TODO
        }
        
        private void checkConformance(final Source document) throws Exception {
            validator.validate(document);
        }
    }
    
    private static String getMdlSchemaName(final Collection<String> xsdNames){
        for(String s:xsdNames){
            if(s.startsWith("MDL_")){
                return s;
            }
        }
        
        throw new RuntimeException("could not determine schema name");
    }
    
    
    private static class Magic{
        private static final String wsdlSchemaTemplate = 
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + 
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + 
                "    xmlns:mdl=\"http://inetprogram.org/projects/MDL\"\r\n" + 
                "    targetNamespace=\"http://mls.securboration.com/wsdl\">\r\n" + 
                "\r\n" + 
                "    <xs:import namespace=\"http://inetprogram.org/projects/MDL\"\r\n" + 
                "        schemaLocation=\"${SCHEMA_NAME}\" />\r\n" + 
                "\r\n" + 
                "    <xs:element name=\"ingestMessageRequest\">\r\n" + 
                "        <xs:complexType>\r\n" + 
                "            <xs:sequence>\r\n" + 
                "                <xs:element name=\"message\" type=\"mdl:MDLRootType\" />\r\n" + 
                "            </xs:sequence>\r\n" + 
                "        </xs:complexType>\r\n" + 
                "    </xs:element>\r\n" + 
                "    \r\n" + 
                "    <xs:element name=\"ingestMessageResponse\">\r\n" + 
                "    <xs:complexType>\r\n" + 
                "      <xs:sequence>\r\n" + 
                "        <xs:element name=\"message\" type=\"mdl:MDLRootType\" />\r\n" + 
                "      </xs:sequence>\r\n" + 
                "    </xs:complexType>\r\n" + 
                "  </xs:element>\r\n" + 
                "  \r\n" + 
                "  \r\n" + 
                "  <xs:element name=\"pingResponse\">\r\n" + 
                "    <xs:complexType>\r\n" + 
                "      <xs:sequence>\r\n" + 
                "        <xs:element name=\"delta\" type=\"xs:long\" />\r\n" + 
                "      </xs:sequence>\r\n" + 
                "    </xs:complexType>\r\n" + 
                "  </xs:element>\r\n" + 
                "  \r\n" + 
                "  <xs:element name=\"pingRequest\">\r\n" + 
                "    <xs:complexType>\r\n" + 
                "      <xs:sequence>\r\n" + 
                "        <xs:element name=\"timestamp\" type=\"xs:long\" />\r\n" + 
                "      </xs:sequence>\r\n" + 
                "    </xs:complexType>\r\n" + 
                "  </xs:element>\r\n" + 
                "  \r\n" + 
                "  \r\n" + 
                "\r\n" + 
                "</xs:schema>";
    }

}
