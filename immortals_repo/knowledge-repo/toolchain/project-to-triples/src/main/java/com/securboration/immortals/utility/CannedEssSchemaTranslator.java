package com.securboration.immortals.utility;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.xml.sax.SAXException;

/**
 * This is a placeholder API for translating XML documents passed along dataflow
 * edges known before adaptation. This API wraps calls to Vanderbilt's XSD
 * translation service endpoint.
 *
 * @author jstaples
 *
 */
public class CannedEssSchemaTranslator {
    
    public static void main(String[] args) throws IOException{
        final String endpoint = "http://192.168.9.107:8090/xsdsts";
        
        final File outDir = new File("translations/" + System.currentTimeMillis());
        final File xsdstsClientOutput = new File("xsdts-client");
        final File essDir = new File("C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\xsd-translation-service-tests");
        
        for(File ess:essDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)){
            System.out.println(ess.getAbsolutePath());
            
            final String xslt = translateClientToServer(true, ess,endpoint);
            FileUtils.moveDirectory(xsdstsClientOutput, new File(outDir,ess.getName()));
            
            for(File xml:FileUtils.listFiles(new File(ess,"dataset"), null, true)){
//                final String xmlContent = 
            }
        }
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
    
    private static File createMinimizedClientSchema(
            final File essMinDir
            ) throws IOException, TransformerException, SAXException, ParserConfigurationException{
        final File lightenerXslt = new File(essMinDir,"etc/lightener/SchemaLightener1.xslt");
        final File schemaToLighten = new File(essMinDir,"schema/client/");
        final File exemplarDocumentDir = new File(essMinDir,"datasource");
        final File schemaOutputDir = new File(essMinDir,"analysis/client/schema-minimized");
        
        SchemaMinimizer.minimizeSchema(
            lightenerXslt,
            findMdlSchema(schemaToLighten),
            exemplarDocumentDir,
            schemaOutputDir
            );
        
        return schemaOutputDir;
    }
    
    private static File createMinimizedServerSchema(
            final File essMinDir
            ) throws IOException, TransformerException, SAXException, ParserConfigurationException{
        
        //TODO: it is unclear what, exactly, we should use as exemplar 
        //       documents for the server schema's utilization
        
        //client schema: use datasource as exemplar documents
        //server schema: use full schema for now, eventually use something similar to etc/messages/v2?
        
        return new File(essMinDir,"schema/server/");//TODO
    }
    

    /**
     * Returns an XSLT for translating client documents into a format that works
     * for the server, or null if no translation is needed.
     *
     * @param minimizeSchema
     *            iff true, the client schema will be minimized using exemplar
     *            documents to determine the portion of the schema that is
     *            actually used
     * @param essMinBaseDir
     *            points to the base of an ess-min directory containing
     *            build.gradle
     * @param translationServiceEndpointUrl
     *            URL of an XSD translation endpoint. E.g.,
     *            http://localhost:9999/xsdsts
     * @return an XSLT for translating, or null if one isn't needed
     * @throws IOException
     *             if anything goes awry
     */
    public static String translateClientToServer(
            final boolean minimizeSchema,
            final File essMinBaseDir,
            final String translationServiceEndpointUrl
    ) throws IOException{
        final File srcSchema;
        
        if(minimizeSchema){
            try {
                srcSchema = createMinimizedClientSchema(essMinBaseDir);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            srcSchema = new File(essMinBaseDir,"schema/client");
        }
        
        final DocumentSet srcDocumentSet = getDocumentSetFromDirectory(srcSchema);

        final DocumentSet dstDocumentSet = getDocumentSetFromDirectory(
                new File(essMinBaseDir,"schema/server")
        );

        return translate(
            translationServiceEndpointUrl,
            srcDocumentSet,
            dstDocumentSet
            );
    }


    /**
     * Returns an XSLT for translating server documents into a format that works
     * for the client, or null if no translation is needed.
     *
     * @param minimizeSchema
     *            iff true, the client schema will be minimized using exemplar
     *            documents to determine the portion of the schema that is
     *            actually used
     * @param essMinBaseDir
     *            points to the base of an ess-min directory containing
     *            build.gradle
     * @param translationServiceEndpointUrl
     *            URL of an XSD translation endpoint. E.g.,
     *            http://localhost:9999/xsdsts
     * @return an XSLT for translating, or null if one isn't needed
     * @throws IOException
     *             if anything goes awry
     */
    public static String translateServerToClient(
            final boolean minimizeSchema,
            final String translationServiceEndpointUrl,
            final File essMinBaseDir
    ) throws IOException{
        final File srcSchema;
        
        if(minimizeSchema){
            try {
                srcSchema = createMinimizedServerSchema(essMinBaseDir);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            srcSchema = new File(essMinBaseDir,"schema/server");
        }
        
        final DocumentSet srcDocumentSet = getDocumentSetFromDirectory(srcSchema);

        final DocumentSet dstDocumentSet = getDocumentSetFromDirectory(
                new File(essMinBaseDir,"schema/client")
        );

        return translate(translationServiceEndpointUrl,srcDocumentSet,dstDocumentSet);
    }
    

    private static String translate(
            final String translationEndpoint,
            final DocumentSet fromSchema,
            final DocumentSet toSchema
    ) throws IOException{
        final XsdtsClient client = new XsdtsClient(translationEndpoint);

        TranslationProblemDefinition tpd = new TranslationProblemDefinition();
        tpd.setSrcSchema(fromSchema);
        tpd.setDstSchema(toSchema);

        return client.getXsdTranslation(tpd);
    }

    private static DocumentSet getDocumentSetFromDirectory(
            final File dir
            ) throws IOException{
        final DocumentSet ds = new DocumentSet();

        ds.setName(dir.getName());

        final File mdl = findMdlSchema(dir);
        final String base = normalName(dir);

        System.out.printf(
                "constructing \"%s\" from %s\n",
                ds.getName(),
                dir.getAbsolutePath()
        );
        for(File f:FileUtils.listFiles(dir, null, true)){
            if(f.getName().endsWith(".dat")){
                continue;
            }

            final String path = normalName(f);

            final String name = path.substring(path.indexOf(base)+base.length()+1);

            Document d = new Document();
            d.setDocumentContent(FileUtils.readFileToString(f));
            d.setDocumentName(name);
            
            if(f.getName().equals(mdl.getName())){
                d.setPrimarySchemaDoc(true);
            }

            ds.getDocuments().add(d);

            System.out.printf(
                    "\tadded \"%s\" from %s\n",
                    d.getDocumentName(),
                    f.getAbsolutePath()
            );
        }

        return ds;
    }

    private static String normalName(
            final File f
    ) throws IOException {
        return f.getCanonicalPath().replace("\\", "/");
    }

}
