package com.securboration.immortals.service.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import com.securboration.immortals.repo.model.build.JarIngestor;

@Configuration
public class ResourceMapper extends WebMvcAutoConfigurationAdapter {
    
    private final Map<String,File> models = new HashMap<>();
    
    @Autowired(required = true)
    private ImmortalsServiceProperties properties;

    public ResourceMapper(){
        System.out.println("created a resource mapper...\n");
    }
    
    private Map<String,File> getRelevantModels(final String prefix){
        Map<String,File> m = new HashMap<>();
        
        for(String s:models.keySet()){
            if(s.startsWith(prefix)){
                m.put(s,models.get(s));
            }
        }
        
        return m;
    }
    
    private final String getUrl(final String resource){
        return String.format(
            "http://127.0.0.1:%d/%s", 
            properties.getLocalServerPort(), 
            resource
            );
    }
    
    private File generateVocabularyOwlFile(
            File tempDir
            ) throws IOException{
        
        Model m = ModelFactory.createDefaultModel();
        
        org.apache.jena.rdf.model.Resource ontology = 
                m.createResource(properties.getImmortalsNs());
        
        ontology.addProperty(RDF.type, OWL2.Ontology);
        ontology.addProperty(
            RDFS.comment, 
            "An aggregation of the various pieces of the IMMoRTALS vocabulary"
            );
        
//        ontology.addProperty(
//            OWL2.versionIRI,
//            m.createResource(properties.getImmortalsNs()+"/"+properties.getImmortalsVersion())
//            );
        
        final Property desc = 
                m.createProperty("http://purl.org/dc/elements/1.1/description");
        
        final Property title = 
                m.createProperty("http://purl.org/dc/elements/1.1/title");
        
        ontology.addProperty(
            desc, 
            "This is the IMMoRTALS vocabulary, used to describe software and " +
            "the ecosystem in which it executes"
            );
        
        ontology.addProperty(
            title, 
            "IMMoRTALS Vocabulary"
            );
        
        Map<String,File> models = getRelevantModels("ontology/immortals-vocab");
        
        for(String model:models.keySet()){
            
            final String uri = getUrl(model);
            
            ontology.addProperty(
                OWL2.imports, 
                m.createResource(uri)
                );
        }
        
        final String outputName = 
                "ontology/immortals-vocab/immortals_vocabulary.ttl";
        
        final File outputFile = new File(tempDir,outputName);
        
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        m.write(o, "Turtle");
        
        FileUtils.writeByteArrayToFile(outputFile, o.toByteArray());
        
        this.models.put(outputName, outputFile);
        
        return outputFile;
    }
    
    private void addOntology(final String url,final Model m){
        
        org.apache.jena.rdf.model.Resource ontology = 
                m.createResource(url);
        
        ontology.addProperty(RDF.type, OWL2.Ontology);
        ontology.addProperty(
            RDFS.comment, 
            "A component of the IMMoRTALS vocabulary"
            );
        
//        ontology.addProperty(
//            OWL2.versionIRI,
//            m.createResource(properties.getImmortalsNs()+"/"+properties.getImmortalsVersion())
//            );
    }
    
    @PostConstruct
    private void init() throws IOException{
        final String version = properties.getImmortalsVersion();
        
        try(InputStream jarStream = 
                this.getClass().getClassLoader().getResourceAsStream(
                    "ontology/immortals-ontologies-package-" + version + ".jar");
                ){
            Map<String,byte[]> models = new HashMap<>();
            JarIngestor.openJar(jarStream, Arrays.asList(".ttl"), models);
            
            final File tempDir = Files.createTempDir();
            tempDir.deleteOnExit();
            
            for(String s:models.keySet()){
                final File outputFile = new File(tempDir,s);
                
                final byte[] modelBytes = models.get(s);
                
                System.out.printf("> %s\n",s);
                
                Model model = ModelFactory.createDefaultModel();
                model.read(
                    new ByteArrayInputStream(modelBytes), 
                    null, 
                    "Turtle"
                    );
                
                final String url = getUrl(s);
                
                addOntology(url,model);
                
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                model.write(o, "Turtle");
                
                FileUtils.writeByteArrayToFile(outputFile, o.toByteArray());
                
                this.models.put(s, outputFile);
            }
            
            
            final File vocabulary = generateVocabularyOwlFile(tempDir);
            
            
            
        }
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        
        System.out.println("added a resource handler...\n");
        
        registry.addResourceHandler("/**").resourceChain(false).addResolver(
            new ResourceResolver(){
            
            @Override
            public Resource resolveResource(
                    HttpServletRequest request,
                    String requestPath, 
                    List<? extends Resource> locations,
                    ResourceResolverChain chain
                    ) {
                
//                System.out.println("\tHERE1\n");
//                System.out.printf("%s\n", requestPath);
//                System.out.printf("%s\n", request.getMethod());
                
                if(models.keySet().contains(requestPath)){
                    return new FileSystemResource(models.get(requestPath));
                }
                
                //TODO: generate aggregator files
                
                return chain.resolveResource(request, requestPath, locations);
            }

            @Override
            public String resolveUrlPath(
                    String resourcePath,
                    List<? extends Resource> locations,
                    ResourceResolverChain chain
                    ) {
                throw new RuntimeException("resolveUrlPath not implemented");
            }
            
        });
        
        super.addResourceHandlers(registry);
    }
    
}
