package com.securboration.immortals.generate.uris;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class GenerateUriConstants {
    
    public static void main(String[] args) throws IOException{
        
        //args[0] is the output dir
        //args[1]...args[n] are directories containing ontologies
        
//        args = new String[]{
//                "../ontology-static/ontology/third-party"
//        };
        
        StringBuilder sb = new StringBuilder();
        generateClassHeader(sb);
        
        for(int i=1;i<args.length;i++){
            String path = args[i];
            File dir = new File(path);
            
            FileUtils.listFiles(dir, null, true).forEach(f->{
                try{
                    System.out.printf("processing %s\n", f.getAbsolutePath());
                    
                    final String name = FilenameUtils.removeExtension(f.getName());
                    
                    Model m = getModel(f);
                    
                    Set<String> classUris = new LinkedHashSet<>();
                    Set<String> propertyUris = new LinkedHashSet<>();
                    
                    scanModel(
                            name,
                            m,
                            classUris,
                            propertyUris
                            );
                    
                    generateInternalClass(sb,name,classUris,propertyUris);
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        
        generateClassFooter(sb);
        
        FileUtils.writeStringToFile(getOutputFile(args[0]), sb.toString());
    }
    
    private static File getOutputFile(String outputDir){
        final String basePath = new File(outputDir).getAbsolutePath();
        
        return new File(basePath + "/com/securboration/immortals/uris/Uris.java");
    }
    
    private static void generateInternalClass(
            StringBuilder sb, 
            String name, 
            Set<String> classUris, 
            Set<String> propertyUris
            ){
        sb.append("\n");
        
        sb.append("  public static class " + name + " {\n");
        
        Set<String> all = new LinkedHashSet<>();
        all.addAll(classUris);
        all.addAll(propertyUris);
        for(String s:all){
            sb.append("    public static final String " + getFieldNameFromUri(s) + " = \"" + s + "\";\n");
        }
        
        sb.append("  }\n");
    }
    
    private static String getFieldNameFromUri(String uri){
        return getFieldNameFromUriBase(uri).replace("-", "_")+"$";
    }
    
    private static String getFieldNameFromUriBase(String uri){
        if(uri.contains("#")){
            return uri.substring(uri.lastIndexOf("#")+1);
        } else if(uri.contains("/")) {
            return uri.substring(uri.lastIndexOf("/")+1);
        } else {
            return uri;
        }
    }
    
    private static void generateClassHeader(StringBuilder sb){
        sb.append("package com.securboration.immortals.uris;");
        sb.append("\n\n");
        
        sb.append("public class Uris{");
    }
    
    private static void generateClassFooter(StringBuilder sb){
        sb.append("\n}\n\n");
    }
    
    private static void scanModel(
            final String name,
            Model m,
            Set<String> classUris,
            Set<String> propertyUris
            ){
        
        //class URIs
        {
            final String q = 
                    "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" +
                    "SELECT ?c\r\n" + 
                    "WHERE {\r\n" + 
                    "  ?c rdf:type rdfs:Class.\r\n" +
                    "}";
            
            executeSelect(m,q,s->{
                final String uri = s.get("c").asResource().getURI();
                
                System.out.printf("\tfound class URI: %s\n", uri);
                
                classUris.add(uri);
            });
        }
        
        //property URIs
        {
            final String q = 
                    "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" +
                    "PREFIX owl:  <http://www.w3.org/2002/07/owl#>\r\n" +
                    "SELECT ?c\r\n" + 
                    "WHERE { {\r\n" + 
                    "  ?c a rdf:Property.\r\n" +
                    "} UNION {\r\n" +
                    "  ?c a owl:ObjectProperty.\r\n" +
                    "} UNION {\r\n" +
                    "  ?c a owl:DatatypeProperty.\r\n" +
                    "} UNION {\r\n" +
                    "  ?c a rdfs:Datatype.\r\n" +
                    "} }";
            
            executeSelect(m,q,s->{
                final String uri = s.get("c").asResource().getURI();
                
                System.out.printf("\tfound property URI: %s\n", uri);
                
                propertyUris.add(uri);
            });
        }
        
    }
    
    private static void executeSelect(
            Model m,
            String selectQuery,
            ResultProcessor p
            ){
        Query query = QueryFactory.create(selectQuery);
        
        QueryExecution queryExecution = 
                QueryExecutionFactory.create(query, m);
        
        ResultSet r = queryExecution.execSelect();
        while(r.hasNext()){
            QuerySolution q = r.next();
            
            p.process(q);
        }
    }
    
    private static interface ResultProcessor{
        public void process(QuerySolution s);
    }
    
    private static Model getModel(File f) throws IOException{
        ByteArrayInputStream input = 
                new ByteArrayInputStream(FileUtils.readFileToByteArray(f));
        
        final String inputLanguage;
        if(f.getName().endsWith(".ttl")){
            inputLanguage = "TURTLE";
        } else if(f.getName().endsWith(".rdf")){
            inputLanguage = "RDF/XML";
        } else {
            throw new RuntimeException(
                    "don't know what language to use for " + f.getName());
        }
        
        System.out.printf("language = %s\n", inputLanguage);
        
        Model model=ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        model.read(input,null,inputLanguage);
        
        return model;
    }

}
