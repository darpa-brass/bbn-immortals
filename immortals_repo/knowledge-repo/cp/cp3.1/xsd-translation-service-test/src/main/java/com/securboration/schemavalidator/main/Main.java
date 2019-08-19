package com.securboration.schemavalidator.main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securboration.schemavalidator.SchemaComplianceChecker;
import com.securboration.schemavalidator.SchemaComplianceChecker.BadnessReport;
import com.securboration.test.Document;
import com.securboration.test.DocumentSet;
import com.securboration.test.TranslationProblemDefinition;
import com.securboration.test.XsdtsClient;
import com.securboration.test.XsltTransformer;

public class Main {
    
    public static void main(String[] args) throws Exception {
//        if(false)
//        {//TODO
//            System.setProperty("srcSchema", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\zTest\\MDL_v0_8_17\\schema\\MDL_v0_8_17.xsd");
//            System.setProperty("dstSchema", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\zTest\\MDL_v0_8_19\\schema\\MDL_v0_8_19.xsd");
//            System.setProperty("srcDocs", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\zTest\\MDL_v0_8_17\\datasource");
//            System.setProperty("resultsDir", "./results/afterUpgrade");
//            System.setProperty("clientUrl", "http://localhost:8090/xsdsts");
//        }//TODO
        
        
//      {//TODO
//          System.setProperty("srcSchema", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\zTest\\austin\\initial.xsd");
//          System.setProperty("dstSchema", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\zTest\\austin\\updated.xsd");
//          System.setProperty("srcDocs", "C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-test\\zTest\\austin\\xml");
//          System.setProperty("resultsDir", "./results/testAustin");
//          System.setProperty("clientUrl", "http://localhost:8090/xsdsts");
//      }//TODO
        
        final File srcDocsDir = getFileFromProperty("srcDocs","path to a dir containing dst-compliant XML docs",null,true);
        final File resultsDir = getFileFromProperty("resultsDir","path to an output directory",new File("./results/" + System.currentTimeMillis()).getCanonicalPath(),true);
        
        final File srcSchemaFile = getFileFromProperty("srcSchema","path to a src schema xsd",null,false);
        final File dstSchemaFile = getFileFromProperty("dstSchema","path to a src schema xsd",null,false);
        final String clientUrl = getProperty("clientUrl","base URL of a translation endpoint to test",null,false);
        
        test(srcSchemaFile,dstSchemaFile,srcDocsDir,resultsDir,clientUrl);
    }
    
    
    private static void test(
            final File srcSchemaFile, 
            final File dstSchemaFile, 
            final File srcDocsDir, 
            final File resultsDir,
            final String clientUrl
            ) throws Exception {
        final Report report = new Report();
        
        try{
            {
                report.$("%s\n\n",new Date().toString());
            }
            
            {//write config to report
                report.$("testing client %s\n", clientUrl);
                report.$("srcSchema =  %s\n", srcSchemaFile.getCanonicalPath());
                report.$("dstSchema =  %s\n", dstSchemaFile.getCanonicalPath());
                report.$("srcDocsDir = %s\n", srcDocsDir == null ? null : srcDocsDir.getCanonicalPath());
                report.$("resultsDir = %s\n", resultsDir.getCanonicalPath());
                report.$("\n\n");
            }
            
            final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            final Schema src = factory.newSchema(srcSchemaFile);
            final Schema dst = factory.newSchema(dstSchemaFile);
            
            report.$("checking src schema...\n");
            final Validator srcValidator = src.newValidator();
            report.$("src schema is valid\n\n");
            
            report.$("checking dst schema...\n");
            final Validator dstValidator = dst.newValidator();
            report.$("dst schema is valid\n\n");
            
            
            
            final List<File> docsToTranslate = new ArrayList<>();
            
            {
                final List<File> xml = new ArrayList<>();
                if(srcDocsDir != null){
                    xml.addAll(FileUtils.listFiles(srcDocsDir, new String[]{"xml"}, true));
                }
                report.$("validating %d documents discovered in input dir against src schema...\n",xml.size());
                for(File f:xml){
                    report.$("\tchecking %s...\n",f.getCanonicalPath());
                    
                    try(FileInputStream fis = new FileInputStream(f)){
                        srcValidator.validate(new StreamSource(fis));
                        report.$("\t\tthe document is compliant with the src schema\n");
                        docsToTranslate.add(f);
                    } catch(Exception e){
                        System.out.println(f.getAbsolutePath());
                        e.printStackTrace(System.out);
                        
                        report.$("\t\tthe document is not compliant with the src schema and will be excluded during testing");
                        report.$(" because %s\n",e.getMessage());
                    }
                }
            }
            
            report.$("\n");
            report.$("found %d good input documents to translate\n",docsToTranslate.size());
            
            report.$("\n");
            report.$("testing interaction with client @ %s\n",clientUrl);
            
            final XsdtsClient client = new XsdtsClient(clientUrl);
            
            {
                report.$("\ntesting ping\n");
                for(int i=0;i<10;i++){
                    final long start = client.ping();
                    final long end = client.ping();
                    report.$("\tclient ping is %dms\n",end - start);
                }
            }
            
            final String xslt;
            {
                report.$("\ntesting acquisition of schema translation\n");
                
                final TranslationProblemDefinition t = new TranslationProblemDefinition();
                t.setSrcSchema(ClientHelper.getDocumentSetFromSchema(srcSchemaFile));
                t.setDstSchema(ClientHelper.getDocumentSetFromSchema(dstSchemaFile));
                
                FileUtils.writeStringToFile(
                    new File(resultsDir,"service/translation-request.json"),
                    new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(t),
                    StandardCharsets.UTF_8
                    );
                
                final long start = System.currentTimeMillis();
                xslt = client.getXsdTranslation(t);
                final long elapsed = System.currentTimeMillis() - start;
                
                FileUtils.writeStringToFile(
                    new File(resultsDir,"service/translation-response.xslt"),
                    xslt,
                    StandardCharsets.UTF_8
                    );
                
                report.$("obtained a %dB XSLT in %dms\n",xslt.getBytes().length,elapsed);
            }
            
            {
                report.$("\ntesting fidelity of schema translation\n");
                
                double normalizedBadness = 0d;
                double weightedBadness = 0d;
                double numPerfect = 0d;
                double numProblematic = 0d;
                int numDocuments = 0;
                
                for(File f:docsToTranslate){
                    report.$("\tusing %s...\n",f.getCanonicalPath());
                    
                    try{
                        final String inputXml = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
                        FileUtils.writeStringToFile(
                            new File(resultsDir,"input/" + f.getName()), 
                            inputXml, 
                            StandardCharsets.UTF_8
                            );
                        
                        final String convertedXml = XsltTransformer.translate(xslt, inputXml);
                        FileUtils.writeStringToFile(
                            new File(resultsDir,"output/" + f.getName()), 
                            convertedXml, 
                            StandardCharsets.UTF_8
                            );
                        
                        final BadnessReport r = SchemaComplianceChecker.getDocumentBadnessScore(
                            dstSchemaFile, 
                            convertedXml
                            );
                        
                        numDocuments++;
                        if(r.getBadnessScore() > 0){
                            normalizedBadness += r.getBadnessScore();
                            weightedBadness += r.getWeightedBadnessScore();
                            numProblematic += 1d;
                            
                            report.$("\tthe translated document is NOT schema compliant because:\n%s\n", r.toString());
                        } else {
                            numPerfect += 1d;
                            
                            report.$("\tthe translated document is schema compliant\n");
                        }
                    } catch(Exception e){
                        System.out.println(f.getAbsolutePath());
                        e.printStackTrace(System.out);
                        
                        report.$("\t\tthe translated document is not compliant with the dst schema");
                        report.$(" because %s\n",e.getMessage());
                    }
                }
                
                report.$(
                    "performed %d test translations of which " +
                    "%d were perfect and %d were imperfect with " +
                    "normalized badness score %1.4f and " +
                    "weighted badness score %1.4f\n",
                    numDocuments,
                    (int)numPerfect,
                    (int)numProblematic,
                    normalizedBadness,
                    weightedBadness
                    );
            }
            
        } finally {
            {//dump the report
                final File reportFile = new File(resultsDir,"report.dat");
                
                FileUtils.writeStringToFile(reportFile, report.sb.toString(), StandardCharsets.UTF_8);
            }
        }
    }
    
    private static class Report{
        final StringBuilder sb = new StringBuilder();
        
        void $(String format, Object...args){
            System.out.printf(format, args);
            sb.append(String.format(format, args));
        }
    }
    
    private static File getFileFromProperty(
            final String key, 
            final String desc, 
            final String defaultValue,
            final boolean allowNull
            ){
        final String property = getProperty(key,desc,defaultValue,allowNull);
        
        if(property == null){
            return null;
        }
        
        return new File(property);
    }
    
    private static String getProperty(
            final String key, 
            final String desc,
            final String defaultValue,
            final boolean allowNull
            ){
        final String value = System.getProperties().getProperty(key);
        
        if(!allowNull && value == null){
            throw new RuntimeException("you must override system property \"" + key + "\" which holds \"" + desc + "\"");
        }
        
        final String propertyValue = value == null ? defaultValue : value;
        
        System.out.printf(
            "Using value %s for property \"%s\" (System property value was %s)\n",
            propertyValue, 
            key, 
            value
            );
        
        return propertyValue;
    }
    
    private static class ClientHelper{
        
        private static void verifyCompliance(
                final String xmlToValidate,
                final File xsd
                ) throws SAXException, IOException {
            final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            Schema schema = factory.newSchema(xsd);
            Validator validator = schema.newValidator();
            
            validator.validate(new StreamSource(new ByteArrayInputStream(xmlToValidate.getBytes())));
        }
        
        private static String normalName(
                final File f
        ) throws IOException {
            return f.getCanonicalPath().replace("\\", "/");
        }
        
        private static DocumentSet getDocumentSetFromSchema(final File schema) throws IOException{
            final DocumentSet ds = new DocumentSet();
    
            ds.setName(schema.getName());
            
            final File dir = schema.getParentFile();
    
            final String base = normalName(dir);
            
            for(File f:FileUtils.listFiles(dir, new String[]{"xml","xsd"}, true)){
                if(f.getName().endsWith(".dat")){
                    continue;
                }
    
                final String path = normalName(f);
    
                final String name = path.substring(path.indexOf(base)+base.length()+1);
    
                Document d = new Document();
                d.setDocumentContent(FileUtils.readFileToString(f,StandardCharsets.UTF_8));
                d.setDocumentName(name);
                
                if(f.getName().equals(schema.getName())){
                    d.setPrimarySchemaDoc(true);
                }
    
                ds.getDocuments().add(d);
            }
    
            return ds;
        }
    }

}
