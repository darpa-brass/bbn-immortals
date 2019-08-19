//package com.securboration.test;
//
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipFile;
//
//import javax.xml.XMLConstants;
//import javax.xml.transform.Source;
//import javax.xml.transform.stream.StreamSource;
//import javax.xml.validation.Schema;
//import javax.xml.validation.SchemaFactory;
//import javax.xml.validation.Validator;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
//import org.xml.sax.SAXException;
//
///**
// * Tests the capabilities of an XSD translation endpoint.
// *
// * @author jstaples
// *
// */
//public class CannedEssSchemaTranslator {
//    
////E.g., args:
////    http://192.168.9.107:8090/xsdsts
////    C:\Users\Securboration\Desktop\code\immortals\trunk\knowledge-repo\cp\cp3.1\xsd-translation-service-test\output\mdl-translation-problems.zip
//    
//    
////    http://localhost:9999/xsdsts
////        C:\Users\Securboration\Desktop\code\immortals\trunk\knowledge-repo\cp\cp3.1\xsd-translation-service-test\output\mdl-translation-problems-1719.zip
//    
////    public static void main(String[] args) throws Exception {
////        final String endpoint;
////        final File essDir;
////        
////        final boolean verbose = false;
////        final boolean cleanupEssDir;
////        
////        if(args.length > 0){
////            if(args.length != 2){
////                throw new RuntimeException(
////                    "Expected exactly 3 args.\n" +
////                    "  arg0 is the endpoint to test (e.g., http://192.168.9.107:8090/xsdsts)\n" +
////                    "  arg1 is a fully qualified path to the src schema\n" +
////                    "  arg2 is a fully qualified path to the dst schema\n" +
////                    "  arg3 is a fully qualified path to a dir containing src-compliant XML documents\n" +
////                    "  arg2 is a fully qualified name for the root document element to translate (e.g., http://inetprogram.org/projects/MDLMDLRoot)"
////                    );
////            }
////            
////            final String override = args[0];
////            final String testDirPath = args[1];
////            
////            if(!override.contains("/") || !override.contains(":")){
////                throw new RuntimeException(
////                    "arg0 must be an endpoint where an XSD " +
////                    "translation service is reachable from this machine.  " +
////                    "E.g., http://192.168.9.107:8090/xsdsts"
////                    );
////            }
////            
////            endpoint = override;
////            
////            if(testDirPath.endsWith(".zip")){
////                essDir = extract(testDirPath);
////                cleanupEssDir = true;
////            } else {
////                essDir = new File(testDirPath);
////                cleanupEssDir = false;
////            }
////            
////            if(!essDir.exists() || !essDir.isDirectory()){
////                throw new RuntimeException("the specified ESS dir doesn't exist or isn't a dir: " + essDir.getAbsolutePath());
////            }
////        } else{
////            endpoint = "http://192.168.9.107:8090/xsdsts";
////            essDir =  new File("../xsd-translation-service-tests");
////            cleanupEssDir = false;
////        }
////        
////        System.out.println("testing endpoint " + endpoint + " over " + essDir.getAbsolutePath());
////        
////        final Map<String,String> resultMap = new LinkedHashMap<>();
////        
////        final File outDir = new File("translations/" + System.currentTimeMillis());
////        final File xsdstsClientOutput = new File("xsdts-client");
////        
////        try {
////            System.out.println(essDir.getAbsolutePath());
////            
////            for(final File ess:essDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)){
////                System.out.println("testing \"" + ess.getName() + "\"");
////                
////                final String xslt;
////                {
////                    final String key = ess.getName() + ":acquireXslt";
////                    try{
////                        xslt = translateClientToServer(ess,endpoint).trim().replace("mdl:MDLRootType", "mdl:MDLRoot");
////                        resultMap.put(key,"PASS");
////                    } catch(Exception e){
////                        e.printStackTrace();
////                        if(e.getMessage().contains("timeout")){
////                            throw e;
////                        }
////                        resultMap.put(key,"FAIL: " + e.getMessage());
////                        continue;
////                    }
////                }
////                
////                FileUtils.moveDirectory(xsdstsClientOutput, new File(outDir,ess.getName()));
////    //            System.out.println(xslt);
////                
////                for(final File xmlFile:FileUtils.listFiles(new File(ess,"datasource"), null, true)) {
////                	System.out.printf("\tinput doc: %s\n", xmlFile.getName());
////                	
////                	final String key = String.format("%s:%s", ess.getName(),xmlFile.getName());
////                	
////                	try{
////                    	final String xml = FileUtils.readFileToString(xmlFile,StandardCharsets.UTF_8).trim();
////                    	
////                    	{
////                        	FileUtils.writeStringToFile(
////                                new File("target/output/docs/" + ess.getName() + "/before/" + xmlFile.getName()), 
////                                xml, 
////                                StandardCharsets.UTF_8
////                                );
////                    	}
////                    	
////                    	if(verbose){
////                        	System.out.printf("\t\toriginal XML:\n");
////                        	System.out.printf("%s\n\n", indent(xml,3));
////                    	}
////                        verifyCompliance(xml,new File(ess,"schema/client"));
////                        
////                        if(verbose){
////                            System.out.printf("\t\tXSLT:\n");
////                        	System.out.printf("%s\n\n", indent(xslt,3));
////                        }
////                        
////                        final String convertedXml = XsltTransformer.translate(xslt, xml);
////                        
////                        {
////                            FileUtils.writeStringToFile(
////                                new File("target/output/docs/" + ess.getName() + "/after/" + xmlFile.getName()), 
////                                convertedXml, 
////                                StandardCharsets.UTF_8
////                                );
////                        }
////                        
////                        if(verbose){
////                            System.out.printf("\t\ttranslated XML:\n");
////                        	System.out.printf("%s\n\n\n", indent(convertedXml,3));
////                        }
////                    	
////                    	verifyCompliance(convertedXml,new File(ess,"schema/server"));
////                    	resultMap.put(key, "PASS");
////                	}catch(Exception e){
////                	    resultMap.put(key, "FAIL: " + flatten(e));
////                	}
////                }
////            }
////        } finally {
////            if(cleanupEssDir){
////                FileUtils.deleteDirectory(essDir);
////            }
////        }
////        
////        final StringBuilder sb = new StringBuilder();
////        
////        sb.append("\n\n\n*** test results ***\n");
////        for(String key:resultMap.keySet()){
////            sb.append(String.format("\t%-75s: %s\n", key, resultMap.get(key)));
////        }
////        sb.append("\n");
////        
////        System.out.println(sb.toString());
////        
////        FileUtils.writeStringToFile(
////            new File("./target/output/results.dat"), 
////            sb.toString(), 
////            StandardCharsets.UTF_8
////            );
////    }
//    
//    private static File extract(final String zipPath) throws IOException{
//        final File zipInput = new File(zipPath);
//        final File unzippedOutput = new File("./tmp");
//        
//        {
//            if(unzippedOutput.exists()){
//                FileUtils.deleteDirectory(unzippedOutput);
//            }
//            FileUtils.forceMkdir(unzippedOutput);
//            unzippedOutput.deleteOnExit();
//        }
//
//        System.out.printf("unzipping %s into %s\n", zipInput, unzippedOutput.getCanonicalPath());
//        {
//            try (
//                    java.util.zip.ZipFile zipFile = new ZipFile(zipInput);
//                    ) {
//                final Enumeration<? extends ZipEntry> entries = zipFile.entries();
//                while (entries.hasMoreElements()) {
//                    final ZipEntry entry = entries.nextElement();
//                    final File entryDestination = new File(unzippedOutput, entry.getName());
//                    if (entry.isDirectory()) {
//                        entryDestination.mkdirs();
//                        System.out.printf("\t%s\n",entry.getName());
//                    } else {
//                        entryDestination.getParentFile().mkdirs();
//                        try (
//                                final InputStream in = zipFile.getInputStream(entry);
//                                final OutputStream out = new FileOutputStream(entryDestination);
//                                ) {
//                            IOUtils.copy(in, out);
//                        }
//                    }
//                }
//            }
//        }
//        System.out.printf("done unzipping into %s\n", unzippedOutput.getCanonicalPath());
//        
//        return new File(unzippedOutput,"mdl-translation-problems");
//    }
//    
//    private static String flatten(Exception e){
//        StackTraceElement[] elements = e.getStackTrace();
//        
//        StringBuilder sb = new StringBuilder();
//        
//        for(int i=elements.length-2;i>=0;i--){
//            StackTraceElement element = elements[i];
//            sb.append(String.format("[%s.%s:%d]", element.getClassName(), element.getMethodName(), element.getLineNumber()));
//            
//            if(!element.getClassName().startsWith("com.securboration")){
//                break;
//            }
//        }
//        sb.append(" " + e.getMessage());
//        
//        return sb.toString();
//    }
//    
//    private static String indent(String s, int n) {
//    	StringBuilder indent = new StringBuilder();
//    	for(int i=0;i<n;i++) {
//    		indent.append("\t");
//    	}
//    	
//    	return indent.toString() + s.replace("\n", "\n" + indent.toString());
//    }
//    
//    private static void verifyCompliance(
//    		final String xmlToValidate,
//    		final File xsdDir
//    		) throws SAXException, IOException {
//    	final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//    	
//		Schema schema = factory.newSchema(getMdlXsd(xsdDir));
//		Validator validator = schema.newValidator();
//		
//		validator.validate(new StreamSource(new ByteArrayInputStream(xmlToValidate.getBytes())));
//    }
//    
//    private static File getMdlXsd(final File dir){
//        for(File f:dir.listFiles()){
//            if(f.getName().startsWith("MDL_v")){//TODO
////            if(f.getName().startsWith("VICTORYMessages")){//TODO
//                return f;
//            }
//        }
//        
//        throw new RuntimeException("couldn't find an MDL xsd in " + dir.getAbsolutePath());
//    }
//    
//    private static Source[] dirToSources(final File dir) throws IOException {
//    	List<Source> sources = new ArrayList<>();
//    	
//    	for(File f:FileUtils.listFiles(dir, new String[] {"xsd"}, true)) {
//    		sources.add(new StreamSource(new ByteArrayInputStream(FileUtils.readFileToByteArray(f))));
//    	}
//    	
//    	return sources.toArray(new Source[0]);
//    }
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//
//    /**
//     * Returns an XSLT for translating client documents into a format that works
//     * for the server, or null if no translation is needed.
//     *
//     * @param essMinBaseDir
//     *            points to the base of an ess-min directory containing
//     *            build.gradle
//     * @param translationServiceEndpointUrl
//     *            URL of an XSD translation endpoint. E.g.,
//     *            http://localhost:9999/xsdsts
//     * @return an XSLT for translating, or null if one isn't needed
//     * @throws IOException
//     *             if anything goes awry
//     */
//    private static String translateClientToServer(
//            final File essMinBaseDir,
//            final String translationServiceEndpointUrl
//    ) throws IOException{
//        final DocumentSet src = getDocumentSetFromDirectory(
//                new File(essMinBaseDir,"schema/client")
//        );
//
//        final DocumentSet dst = getDocumentSetFromDirectory(
//                new File(essMinBaseDir,"schema/server")
//        );
//        
//        final String xslt = translate(translationServiceEndpointUrl,src,dst);
//        
//        FileUtils.writeStringToFile(
//            new File("target/output/xslts/" + essMinBaseDir.getName() + ".xslt"), 
//            xslt, 
//            StandardCharsets.UTF_8
//            );
//        
//        return xslt;
//    }
//
//    private static String translate(
//            final String translationEndpoint,
//            final DocumentSet fromSchema,
//            final DocumentSet toSchema
//    ) throws IOException{
//        final XsdtsClient client = new XsdtsClient(translationEndpoint);
//
//        TranslationProblemDefinition tpd = new TranslationProblemDefinition();
//        tpd.setSrcSchema(fromSchema);
//        tpd.setDstSchema(toSchema);
//
//        return client.getXsdTranslation(tpd);
//    }
//
//    private static DocumentSet getDocumentSetFromDirectory(final File dir) throws IOException{
//        final DocumentSet ds = new DocumentSet();
//
//        ds.setName(dir.getName());
//
//        final String base = normalName(dir);
//
////        System.out.printf(
////                "constructing \"%s\" from %s\n",
////                ds.getName(),
////                dir.getAbsolutePath()
////        );
//        for(File f:FileUtils.listFiles(dir, null, true)){
//            if(f.getName().endsWith(".dat")){
//                continue;
//            }
//
//            final String path = normalName(f);
//
//            final String name = path.substring(path.indexOf(base)+base.length()+1);
//
//            Document d = new Document();
//            d.setDocumentContent(FileUtils.readFileToString(f));
//            d.setDocumentName(name);
//
//            ds.getDocuments().add(d);
//
////            System.out.printf(
////                    "\tadded \"%s\" from %s\n",
////                    d.getDocumentName(),
////                    f.getAbsolutePath()
////            );
//        }
//
//        return ds;
//    }
//
//    private static String normalName(
//            final File f
//    ) throws IOException {
//        return f.getCanonicalPath().replace("\\", "/");
//    }
//
//}
