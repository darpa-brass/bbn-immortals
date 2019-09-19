package com.securboration.immortals.swri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securboration.immortals.bridge.CannedSchemaVersionDetector;
import com.securboration.immortals.service.eos.api.types.Document;
import com.securboration.immortals.service.eos.api.types.EvaluationConfiguration;
import com.securboration.immortals.service.eos.api.types.SchemaDefinition;


/**
 * Utility API for creating an evaluation package
 * 
 * @author jstaples
 *
 */
public class EvaluationPackageBuilder {

    public static enum MdlSchemaVersion{
//        MDL_V0_8_17("v1","v17","V0_8_17"),
//        MDL_V0_8_19("v2","v19","V0_8_19"),
        
        
        MDL_V0_8_7("v7","v7","V0_8_7","v0_8_7"),
        MDL_V0_8_8("v8","v8","V0_8_8","v0_8_8"),
        MDL_V0_8_9("v9","v9","V0_8_9","v0_8_9"),
        MDL_V0_8_10("v10","v10","V0_8_10","v0_8_10"),
        MDL_V0_8_11("v11","v11","V0_8_11","v0_8_11"),
        MDL_V0_8_12("v12","v12","V0_8_12","v0_8_12"),
        MDL_V0_8_13("v13","v13","V0_8_13","v0_8_13"),
        MDL_V0_8_14("v14","v14","V0_8_14","v0_8_14"),
        MDL_V0_8_16("v16","v16","V0_8_16","v0_8_16"),
        MDL_V0_8_17("v17","v17","V0_8_17","v0_8_17"),
        MDL_V0_8_19("v19","v19","V0_8_19","v0_8_19"),
        
        ;
        
        private final String tag;
        private final String shortName;
        private final String[] aliases;
        
        private MdlSchemaVersion(String tag,String shortName,String...aliases){
            this.tag = tag;
            this.shortName = shortName;
            this.aliases = aliases;
        }
        
        public String getTag(){
            return tag;
        }
        
        public static MdlSchemaVersion get(final String textForm){
            final MdlSchemaVersion[] versions = MdlSchemaVersion.values();
            
            for(MdlSchemaVersion v:versions){
                if(v.name().equals(textForm)){
                    return v;
                }
                
                if(v.toString().equals(textForm)){
                    return v;
                }
                
                if(v.tag.equals(textForm)){
                    return v;
                }
                
                if(v.shortName.equals(textForm)){
                    return v;
                }
                
                for(String alias:v.aliases){
                    if(alias.equals(textForm)){
                        return v;
                    }
                }
            }
            
            throw new RuntimeException(textForm + " does not map to an MDL version enum value");
        }
    };
    
    private static Collection<Document> getDocumentsFromTemplateDir(
            final File dir,
            final String basePath
            ) throws IOException{
        final List<Document> docs = new ArrayList<>();
        
        if(!dir.exists()){
            throw new RuntimeException("dir does not exist: " + dir.getAbsolutePath());
        }
        
        for(File f:FileUtils.listFiles(dir, null, true)){
            final String name = f.getAbsolutePath().replace(dir.getAbsolutePath(), "");
            //dirA
            //  a.dat
            //  b.dat
            //  c.dat (dirA/c.dat)
            //  dirB
            //    d.dat
            //    e.dat (dirA/dirB/e.dat)
            
            Document d = new Document();
            d.setDocumentContent(FileUtils.readFileToString(f));
            d.setDocumentName(basePath + name);
            
            d.setDocumentName(d.getDocumentName().replace("\\", "/"));
            
            docs.add(d);
        }
        
        return docs;
    }
    
    /**
     * Creates an evaluation package using some combination of the two MDL
     * schemas provided by SwRI
     * 
     * @param clientSchemaVersion
     *            the MDL compliance level to be used by the client
     * @param serverSchemaVersion
     *            the MDL compliance level to be used by the server
     * @param datasourceSchemaVersion
     *            the MDL compliance level to be used by the datasource
     * @return an array of bytes comprising a zipped evaluation package
     * @throws IOException 
     */
    public static EvaluationConfiguration createEvaluationPackageSimple(
            final File templateDir,
            final MdlSchemaVersion clientSchemaVersion,
            final MdlSchemaVersion serverSchemaVersion,
            final MdlSchemaVersion datasourceSchemaVersion,
            final byte[] cheatZip
            ) throws IOException{
        
        EvaluationConfiguration config = new EvaluationConfiguration();
        
        if(clientSchemaVersion != null){
            SchemaDefinition xsd = new SchemaDefinition();
            
            xsd.setSchemaId(clientSchemaVersion.name());
            xsd.getXsds().addAll(getDocumentsFromTemplateDir(
                new File(templateDir,"etc/schemas/"+clientSchemaVersion.getTag()),
                "ess/schema/client"
                ));
            
            config.setClientSchemaDefinition(xsd);
        }
        
        if(serverSchemaVersion != null){
            SchemaDefinition xsd = new SchemaDefinition();
            
            xsd.setSchemaId(serverSchemaVersion.name());
            xsd.getXsds().addAll(getDocumentsFromTemplateDir(
                new File(templateDir,"etc/schemas/"+serverSchemaVersion.getTag()),
                "ess/schema/server"
                ));
            
            config.setServerSchemaDefinition(xsd);
        }
        
        if(datasourceSchemaVersion != null){
            SchemaDefinition xsd = new SchemaDefinition();
            
            xsd.setSchemaId(datasourceSchemaVersion.name());
            xsd.getXsds().addAll(getDocumentsFromTemplateDir(
                new File(templateDir,"etc/schemas/"+datasourceSchemaVersion.getTag()),
                "ess/schema/datasource"
                ));
            
            config.setDatasourceSchemaDefinition(xsd);
            
            config.getDatasourceXmls().addAll(getDocumentsFromTemplateDir(
                new File(templateDir,"etc/messages/"+datasourceSchemaVersion.tag),
                "ess/datasource"
                ));
        }
        
        {//cheat
            config.setCheatZip(cheatZip);
        }
        
        return config;
    }
    
    /**
     * Creates an evaluation package using some combination of the two MDL
     * schemas provided by SwRI
     * 
     * @param clientSchemaVersion
     *            the MDL compliance level to be used by the client
     * @param serverSchemaDefinition
     *            an uber XSD describing the server's schema
     * @param datasourceSchemaVersion
     *            the MDL compliance level to be used by the datasource
     * @return an array of bytes comprising a zipped evaluation package
     * @throws IOException 
     */
    public static EvaluationConfiguration createEvaluationPackageSimple(
            final File templateDir,
            final MdlSchemaVersion clientSchemaVersion,
            final String serverSchemaName,
            final String serverSchemaDefinition,
            final MdlSchemaVersion datasourceSchemaVersion,
            final byte[] cheatZip
            ) throws IOException{
        
        EvaluationConfiguration config = new EvaluationConfiguration();
        
        if(clientSchemaVersion != null){
            SchemaDefinition xsd = new SchemaDefinition();
            
            xsd.setSchemaId(clientSchemaVersion.name());
            xsd.getXsds().addAll(getDocumentsFromTemplateDir(
                new File(templateDir,"etc/schemas/"+clientSchemaVersion.getTag()),
                "ess/schema/client"
                ));
            
            config.setClientSchemaDefinition(xsd);
        }
        
        if(serverSchemaDefinition != null){
            SchemaDefinition xsd = new SchemaDefinition();
            
            if(serverSchemaName == null){
                xsd.setSchemaId("MDL-custom-version-unspecified");
            } else {
                xsd.setSchemaId(serverSchemaName);
            }
            
            {//add the schema definition
                Document d = new Document();
                d.setDocumentContent(serverSchemaDefinition);
                d.setDocumentName("ess/schema/server/MDL_custom.xsd");
            
                xsd.getXsds().add(d);
            }
            
            {//add a message listener schema template
                final File template = new File(templateDir,"etc/schemas/v1/MessageListenerSchema.xsd");
                final String content = FileUtils.readFileToString(template,StandardCharsets.UTF_8);
                
                final String mls = content.replace(
                    "MDL_v0_8_17.xsd", 
                    "MDL_custom.xsd"
                    );
                
                Document d = new Document();
                d.setDocumentContent(mls);
                d.setDocumentName("ess/schema/server/MessageListenerSchema.xsd");
                
                xsd.getXsds().add(d);
            }
            
            {//add a reference to the schema artifact
                Document d = new Document();
                d.setDocumentContent("MDL_custom.xsd");
                d.setDocumentName("ess/schema/server/schemaVersion.dat");
                
                xsd.getXsds().add(d);
            }
            
            {//add everything else from the appropriate template
                final String templateVersion = CannedSchemaVersionDetector.detectMdlTemplateVersion(serverSchemaDefinition);
                
                final File templateSchemaDir = new File(templateDir,"etc/schemas/" + templateVersion);
                
                for(Document d:getDocumentsFromTemplateDir(templateSchemaDir,"ess/schema/server")){
                    if(d.getDocumentName().endsWith("MessageListenerSchema.xsd")){
                        continue;
                    }
                    if(d.getDocumentName().contains("MDL_v") && d.getDocumentName().endsWith("xsd")){
                        continue;
                    }
                    if(d.getDocumentName().endsWith("schemaVersion.dat")){
                        continue;
                    }
                    
                    xsd.getXsds().add(d);
                }
            }//TODO
            
            config.setServerSchemaDefinition(xsd);
        }
        
        if(datasourceSchemaVersion != null){
            SchemaDefinition xsd = new SchemaDefinition();
            
            xsd.setSchemaId(datasourceSchemaVersion.name());
            xsd.getXsds().addAll(getDocumentsFromTemplateDir(
                new File(templateDir,"etc/schemas/"+datasourceSchemaVersion.getTag()),
                "ess/schema/datasource"
                ));
            
            config.setDatasourceSchemaDefinition(xsd);
            
            config.getDatasourceXmls().addAll(getDocumentsFromTemplateDir(
                new File(templateDir,"etc/messages/"+datasourceSchemaVersion.tag),
                "ess/datasource"
                ));
        }
        
        {//cheat
            config.setCheatZip(cheatZip);
        }
        
        return config;
    }
    
    /**
     * Utility method for creating a custom evaluation package.
     * 
     * @param dirContainingClientXsds
     *            a directory containing the definition of the client schema
     * @param dirContainingServerXsds
     *            a directory containing the definition of the server schema
     * @param dirContainingDatasourceXsds
     *            a directory containing the definition of the datasource schema
     * @param dirContainingDatasourceXmlDocs
     *            a directory containing XML messages (conformant with the
     *            datasource schema definition)
     * @return an evaluation configuration with the provided characteristics
     * @throws IOException
     */
    public static EvaluationConfiguration createCustomEvaluationPackage(
            final File dirContainingClientXsds,
            final File dirContainingServerXsds,
            final File dirContainingDatasourceXsds,
            final File dirContainingDatasourceXmlDocs,
            final byte[] cheatZip
            ) throws IOException{
        final EvaluationConfiguration config = new EvaluationConfiguration();
        
        {//client
            SchemaDefinition xsd = new SchemaDefinition();
            
            xsd.setSchemaId(dirContainingClientXsds.getName());
            xsd.getXsds().addAll(getDocumentsFromTemplateDir(
                dirContainingClientXsds,
                "ess/schema/client"
                ));
            
            config.setClientSchemaDefinition(xsd);
        }
        
        {//server
            SchemaDefinition xsd = new SchemaDefinition();
            
            xsd.setSchemaId(dirContainingServerXsds.getName());
            xsd.getXsds().addAll(getDocumentsFromTemplateDir(
                dirContainingServerXsds,
                "ess/schema/server"
                ));
            
            config.setServerSchemaDefinition(xsd);
        }
        
        {//datasource
            SchemaDefinition xsd = new SchemaDefinition();
            
            xsd.setSchemaId(dirContainingDatasourceXsds.getName());
            xsd.getXsds().addAll(getDocumentsFromTemplateDir(
                dirContainingDatasourceXsds,
                "ess/schema/datasource"
                ));
            
            config.setDatasourceSchemaDefinition(xsd);
            
            config.getDatasourceXmls().addAll(getDocumentsFromTemplateDir(
                dirContainingDatasourceXmlDocs,
                "ess/datasource"
                ));
        }
        
        {//cheat
            config.setCheatZip(cheatZip);
        }
        
        return config;
    }
    
    
    public static void main(String[] args) throws Exception {
        final File mdlArchive = new File("C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\etc\\mdl\\mdl-clean.zip");
        
        final File extracted = extract(mdlArchive.getCanonicalPath());
        final File out = new File("./configs.zip");
        
        try(
                FileOutputStream fos = new FileOutputStream(out);
                ZipOutputStream zos = new ZipOutputStream(fos);
                ){
            
            System.out.printf("%s\n", extracted.getCanonicalPath());
            for(final File src:extracted.listFiles()){
                if(!src.isDirectory()){
                    continue;
                }
                
                for(final File dst:extracted.listFiles()){
                    if(!dst.isDirectory()){
                        continue;
                    }
                    
                    for(final File ds:extracted.listFiles()){
                        if(!ds.isDirectory()){
                            continue;
                        }
                        
                        if(!ds.getName().equals(src.getName())){
                            continue;
                        }//TODO: this skips over all cases where datasource != dst
                        
                        final String tag = String.format(
                            "client=%sdatasource=%sserver=%s", 
                            src.getName(),
                            ds.getName(),
                            dst.getName()
                            );
                        
                        
                        System.out.printf("\t%s\n",tag);
                        
                        //dir
                        // datasource
                        // schema
                        //  client
                        //  datasource
                        //  server

                        final File datasourceDir = new File(ds,"datasource");
                        final File datasourceSchemaDir = new File(ds,"schema");
                        final File clientSchemaDir = new File(src,"schema");
                        final File serverSchemaDir = new File(dst,"schema");
                        
                        final EvaluationConfiguration config = createCustomEvaluationPackage(
                                clientSchemaDir,//final File dirContainingClientXsds,
                                serverSchemaDir,//final File dirContainingServerXsds,
                                datasourceSchemaDir,//final File dirContainingDatasourceXsds,
                                datasourceDir,//final File dirContainingDatasourceXmlDocs,
                                null//final byte[] cheatZip
                                );

                        final String json = new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(config);
                        
                        ZipEntry e = new ZipEntry("configs/" + tag);
                        {
                            zos.putNextEntry(e);
                            IOUtils.write(json.getBytes(StandardCharsets.UTF_8), zos);
                            zos.closeEntry();
                        }
                    }
                }
            }
            
        }
        
        
    }
    
    
    private static File extract(final String zipPath) throws IOException{
        final File zipInput = new File(zipPath);
        final File unzippedOutput = new File("./tmp");
        
        {
            if(unzippedOutput.exists()){
                FileUtils.deleteDirectory(unzippedOutput);
            }
            FileUtils.forceMkdir(unzippedOutput);
            unzippedOutput.deleteOnExit();
        }

        System.out.printf("unzipping %s into %s\n", zipInput, unzippedOutput.getCanonicalPath());
        {
            try (
                    java.util.zip.ZipFile zipFile = new ZipFile(zipInput);
                    ) {
                final Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    final ZipEntry entry = entries.nextElement();
                    final File entryDestination = new File(unzippedOutput, entry.getName());
                    if (entry.isDirectory()) {
                        entryDestination.mkdirs();
                        System.out.printf("\t%s\n",entry.getName());
                    } else {
                        entryDestination.getParentFile().mkdirs();
                        try (
                                final InputStream in = zipFile.getInputStream(entry);
                                final OutputStream out = new FileOutputStream(entryDestination);
                                ) {
                            IOUtils.copy(in, out);
                        }
                    }
                }
            }
        }
        System.out.printf("done unzipping into %s\n", unzippedOutput.getCanonicalPath());
        
        return new File(unzippedOutput,"mdl-clean");
    }
//    
//    
//    public static void main(String[] args) throws IOException{
//        
//        final File templateDir = new File("../cp-ess");
//        final File outputDir = new File("./examples");
//        
//        final Map<String,EvaluationConfiguration> evaluationConfigs = new LinkedHashMap<>();
//        for(MdlSchemaVersion cv:MdlSchemaVersion.values()){
//            for(MdlSchemaVersion sv:MdlSchemaVersion.values()){
//                for(MdlSchemaVersion dv:MdlSchemaVersion.values()){
//                    final String tag = 
//                            String.format(
//                                "client_%s-server_%s-datasource_%s.json", 
//                                cv.shortName, 
//                                sv.shortName, 
//                                dv.shortName
//                                );
//                    
//                    evaluationConfigs.put(
//                        tag,
//                        createEvaluationPackageSimple(
//                            templateDir,
//                            cv,sv,dv
//                            )
//                        );
//                }
//            }
//        }
//        
//        for(String key:evaluationConfigs.keySet()){
//            FileUtils.writeStringToFile(
//                new File(outputDir,key), 
//                evaluationConfigs.get(key).toJson(), 
//                Charsets.UTF_8
//                );
//        }
//    }

}
