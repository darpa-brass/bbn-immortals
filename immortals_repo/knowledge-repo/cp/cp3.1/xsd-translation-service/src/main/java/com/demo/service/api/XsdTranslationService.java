package com.demo.service.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.demo.service.api.types.Document;
import com.demo.service.api.types.DocumentSet;
import com.demo.service.api.types.TranslationProblemDefinition;

/**
 * A REST API to be used during CP3.1 evaluation
 *
 * @author jstaples
 *
 */
@RestController
@RequestMapping("/xsdsts")
public class XsdTranslationService {
    
    private static final Charset encoding = Charsets.UTF_8;
    
    private static final String mdl17To19 = "v17_to_19.xsl";
    private static final String mdl19To17 = "v19_to_17.xsl";
    
    /**
     * A simple method useful for approximating the client/server latency.
     * 
     * @return the server's current epoch time in milliseconds
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value="/ping",
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String ping() {
        System.out.println("received a ping request\n");
        return "" + System.currentTimeMillis();
    }
    
    /**
     * A test method.  Returns an xsl on the classpath as a binary stream.
     * 
     * E.g., http://localhost:9999/xsdsts/xsl/v17_to_19 or 
     * http://localhost:9999/xsdsts/xsl/v19_to_17
     * 
     * @param translation the name of a translation xsl in src/main/resources
     * @return the xsl with the indicated name
     * @throws IOException if something goes wrong
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value="/xsl/{xslName}",
            consumes=MediaType.ALL_VALUE,
            produces=MediaType.APPLICATION_XML_VALUE
            )
    public String getCannedTranslation(
            @PathVariable("xslName") String translation
            ) throws IOException {
        return getClasspathResource(translation + ".xsl");
    }
    
    /**
     * Returns a monolithic XSL document (as a binary stream) that translates
     * from the src to dst schemas in the provided translation problem
     * definition.
     * 
     * <br>
     * <br>
     * 
     * <b>NOTE:</b> for now, this is a placeholder implementation that only
     * supports MDL 17 and 19 and relies upon the existence of the XSLs provided
     * by SwRI. To clarify, this is a 5-minute implementation that should be
     * entirely discarded when implementing an actual solution.
     * 
     * @param translationProblemDefinition
     *            describes a src and dst schema. The src is a subset of MDL
     *            derived from dynamic analysis of the exemplar software system.
     * @return a monolithic XSL document (as a binary stream) that converts
     *         documents conformant with src into documents compliant with dst
     * @throws IOException
     *             if something goes wrong
     */
    @RequestMapping(
        method = RequestMethod.POST,
        value="/translate",
        produces=MediaType.APPLICATION_OCTET_STREAM_VALUE,
        consumes=MediaType.APPLICATION_JSON_VALUE
        )
    public String translate(
            @RequestBody
            TranslationProblemDefinition translationProblemDefinition
            ) throws IOException{
        final DocumentSet srcSchema = translationProblemDefinition.getSrcSchema();
        final DocumentSet dstSchema = translationProblemDefinition.getDstSchema();
        
        {//verify sanity of inputs
            if(srcSchema == null){
                throw new RuntimeException("src schema cannot be null");
            }
            
            if(dstSchema == null){
                throw new RuntimeException("dst schema cannot be null");
            }
        }
        
        {//check for hard-coded translation
            if(isMdlVersion17(srcSchema) && isMdlVersion17(dstSchema)){
                return "no translation needed";//no translation needed
            }
            
            if(isMdlVersion19(srcSchema) && isMdlVersion19(dstSchema)){
                return "no translation needed";//no translation needed
            }
            
            if(isMdlVersion17(srcSchema) && isMdlVersion19(dstSchema)){
                return getClasspathResource(mdl17To19);
            }
            
            if(isMdlVersion19(srcSchema) && isMdlVersion17(dstSchema)){
                return getClasspathResource(mdl19To17);
            }
        }
        
        System.err.printf(
            "unable to identify an appropriate hard-coded translation from \"%s\" to \"%s\"\n",
            srcSchema.getName(),
            dstSchema.getName()
            );
        
        return "no translation needed";
    }
    
    private static boolean isMdlVersion17(DocumentSet s){
        return doesContainMagicString(
            s,
            "xsi:schemaLocation=\"http://inetprogram.org/projects/MDL MDL_v0_8_17.xsd\"",
            "MDL_v0_8_17.xsd"
            );
    }
    
    private static boolean isMdlVersion19(DocumentSet s){
        return doesContainMagicString(
            s,
            "xsi:schemaLocation=\"http://inetprogram.org/projects/MDL MDL_v0_8_19.xsd\"",
            "MDL_v0_8_19.xsd"
            );
    }
    
    private static boolean doesContainMagicString(
            DocumentSet s, 
            String...magics
            ){
        for(Document d:s.getDocuments()){
            for(String magic:magics){
                if(d.getDocumentContent().contains(magic)){
                    return true;
                }
                
                if(d.getDocumentName().equals(magic)){
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private static String getClasspathResource(final String name) throws IOException{
        Resource r = new ClassPathResource(name);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(r.getInputStream(),out);
        
        return new String(out.toByteArray(),encoding);
    }
    

}


