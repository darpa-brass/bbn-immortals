package com.securboration.immortals.generate.uris;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
        
        //args[0] is the output dir base
        //args[1] is the name of the generated class
        //args[2]...args[n] are directories containing ontologies
        
        StringBuilder javaFile = new StringBuilder();
        StringBuilder jsFile = new StringBuilder();
        
        generateClassHeader(args[1],javaFile);
        
        for(int i=2;i<args.length;i++){
            String path = args[i];
            File dir = new File(path);
            
            FileUtils.listFiles(dir, null, true).forEach(f->{
                try{
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
                    
                    generateJavaClass(
                        javaFile,
                        name,
                        new TreeSet<>(classUris),
                        new TreeSet<>(propertyUris)
                        );
                    
                    generateJavaScript(
                        jsFile,
                        name,
                        new TreeSet<>(classUris),
                        new TreeSet<>(propertyUris)
                        );
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        
        generateClassFooter(javaFile);
        
        FileUtils.writeStringToFile(
            getJavaOutputFile(args[0],args[1]), 
            javaFile.toString()
            );
        
        FileUtils.writeStringToFile(
            getJsOutputFile(args[0],args[1]), 
            jsFile.toString()
            );
    }
    
    private static File getJavaOutputFile(
            String outputDir,
            String className
            ){
        final String basePath = new File(outputDir).getAbsolutePath();
        
        return new File(basePath + "/" + className.replace(".", "/") + ".java");
    }
    
    private static File getJsOutputFile(
            String outputDir,
            String className
            ){
        final String basePath = new File(outputDir).getAbsolutePath();
        
        return new File(basePath + "/" + className.replace(".", "/") + ".js");
    }
    
    private static Iterator<String> iterator(Set<String> classUris){
        Map<String,String> map = new HashMap<>();
        
        for(String s:classUris){
            map.put(getFieldNameFromUri(s), s);
        }
        
        List<String> ordered = new ArrayList<>();
        new TreeSet<>(map.keySet()).iterator().forEachRemaining(i->{
            ordered.add(map.get(i));
        });
        
        return ordered.iterator();
    }
    
    private static void generateJavaScript(
            StringBuilder sb, 
            String name, 
            Set<String> classUris, 
            Set<String> propertyUris
            ){
        sb.append("var ");
        sb.append(name);
        sb.append(" = {");
        sb.append("\n");
        
        iterator(classUris).forEachRemaining(s->{
            sb.append("    " + getFieldNameFromUri(s) + " : \"" + s + "\",");
            sb.append("//a class URI");
            sb.append("\n");
        });
        sb.append("\n");
        iterator(propertyUris).forEachRemaining(s->{
            sb.append("    " + getFieldNameFromUri(s) + " : \"" + s + "\",");
            sb.append("//a property URI");
            sb.append("\n");
        });
        
        sb.append("}\n");
    }
    
    private static void generateJavaClass(
            StringBuilder sb, 
            String name, 
            Set<String> classUris, 
            Set<String> propertyUris
            ){
        sb.append("\n");
        
        sb.append("  public static class " + name + " {\n");
        
        iterator(classUris).forEachRemaining(s->{
            System.out.println(s);
            sb.append("    public static final String " + getFieldNameFromUri(s) + " = \"" + s + "\";");
            sb.append("//a class URI");
            sb.append("\n");
        });
        sb.append("\n");
        iterator(propertyUris).forEachRemaining(s->{
            System.out.println(s);
            sb.append("    public static final String " + getFieldNameFromUri(s) + " = \"" + s + "\";");
            sb.append("//a property URI");
            sb.append("\n");
        });
        
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
    
    private static void generateClassHeader(
            final String className,
            StringBuilder sb
            ){
        
        final String packageName = 
                className.substring(0,className.lastIndexOf("."));
        
        final String classPart = 
                className.substring(className.lastIndexOf(".")+1);
        
        sb.append("package ");
        sb.append(packageName);
        sb.append(";\n");
        sb.append("\n\n");
        
        sb.append("public class ");
        sb.append(classPart);
        sb.append("{");
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
                    "PREFIX owl:  <http://www.w3.org/2002/07/owl#>\r\n" +
                    "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" +
                    "SELECT ?c\r\n" + 
                    "WHERE {\r\n" + 
                    "  { ?c a rdfs:Class. } UNION { ?c a owl:Class. } \r\n" +
                    "}";
            
            executeSelect(m,q,s->{
                if(s.get("c").isAnon()){
                    return;
                }
                
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
        
        try(QueryExecution queryExecution = 
                QueryExecutionFactory.create(query, m);){
        
            ResultSet r = queryExecution.execSelect();
            while(r.hasNext()){
                QuerySolution q = r.next();
                
                p.process(q);
            }
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
