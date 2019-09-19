package com.securboration.immortals.swri.eval;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import com.securboration.immortals.service.eos.api.types.Document;
import com.securboration.immortals.service.eos.api.types.SchemaDefinition;

public class PreflightSanityChecker {
    
    private static void validate(
            final File xsd,
            final List<File> documents
            ) throws FileNotFoundException, IOException, SAXException{
        final SchemaFactory factory = 
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        final Schema schema = factory.newSchema(xsd);
        final Validator validator = schema.newValidator();
        
        for(File f:documents){
            try(InputStream is = new FileInputStream(f)){
                validator.validate(new StreamSource(is));
            } catch(SAXException e){
                throw new RuntimeException(
                    "provided document " + f.getName() + 
                    " is not compliant with " + xsd.getName(),
                    e
                    );
            }
        }
    }
    
    public static void verify(
            final SchemaDefinition schema,
            final List<Document> documentsToValidate
            ) throws IOException, SAXException{
        
        final File tmpDir = new File("./evalHarness-tmp/schema");
        
        FileUtils.deleteQuietly(tmpDir);
        FileUtils.forceMkdir(tmpDir);
 
        try{
            final List<Document> roots = new ArrayList<>();
            final List<Document> xsds = new ArrayList<>();
            for(Document xsd:schema.getXsds()){
                if(xsd.isPrimarySchemaDoc() || new File(xsd.getDocumentName()).getName().startsWith("MDL_")){
                    roots.add(xsd);
                }
                xsds.add(xsd);
            }
            
            if(roots.size() != 1){
                throw new RuntimeException(
                    "exactly one document must be declared as the " +
                    "primary schema document but found " + roots.size()
                    );
            }
            
            final Document root = roots.get(0);
            
            for(Document d:xsds){
                final String name = d.getDocumentName();
                
                FileUtils.writeStringToFile(
                    new File(tmpDir,name), 
                    d.getDocumentContent()
                    );
            }
            
            final List<File> docs = new ArrayList<>();
            for(Document d:documentsToValidate){
                File f = new File(tmpDir,d.getDocumentName());
                
                FileUtils.writeStringToFile(
                    f, 
                    d.getDocumentContent()
                    );
                
                docs.add(f);
            }
            
            final File rootSchemaFile = new File(tmpDir,root.getDocumentName());
            
            //first validate the schema
            validate(rootSchemaFile, new ArrayList<>());
            
            //next validate the document set
            validate(rootSchemaFile,docs);
        } finally {
            FileUtils.deleteQuietly(tmpDir);
        }
    }

}
