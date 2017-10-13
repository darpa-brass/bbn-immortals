//package com.securboration.immortals.deployment.model;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Base64;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.apache.commons.io.FileUtils;
//
//import com.securboration.immortals.o2t.analysis.ObjectNode;
//import com.securboration.immortals.o2t.analysis.ObjectPrinter;
//import com.securboration.immortals.o2t.etc.ExceptionWrapper;
//import com.securboration.immortals.ontology.deployment.uml.Diagram;
//import com.securboration.immortals.ontology.deployment.uml.DiagramNode;
//import com.securboration.immortals.ontology.deployment.uml.DiagramNodeSet;
//import com.securboration.immortals.ontology.deployment.uml.DiagramRelationship;
//import com.securboration.immortals.ontology.deployment.uml.Multiplicity;
//
//public class DiagramToSource {
//    
//    public static class DiagramToSourceConfig{
//        private final String baseNamespace;
//        private final File baseOutputDir;
//        
//        public DiagramToSourceConfig(String baseNamespace, File baseOutputDir) {
//            super();
//            this.baseNamespace = baseNamespace;
//            this.baseOutputDir = baseOutputDir;
//        }
//    }
//    
//    private final DiagramToSourceConfig config;
//    private final Diagram diagram;
//    private final Map<String,DiagramNode> idsToNodes = new HashMap<>();
//    
//    public static void generateSource(DiagramToSourceConfig c,Diagram d) throws IOException{
//        new DiagramToSource(c,d).convert();
//    }
//    
//    private DiagramToSource(DiagramToSourceConfig config, Diagram diagram) {
//        super();
//        this.config = config;
//        this.diagram = diagram;
//        
//        for(DiagramNodeSet s:diagram.getNodes()){
//            
//            idsToNodes.put(
//                    s.getNodeSetDescriptor().getId(), 
//                    s.getNodeSetDescriptor());
//            
//            if(s.getNodes() != null){
//                for(DiagramNode n:s.getNodes()){
//                    
//                    printObject(n);
//                    printObject(s);
//                    idsToNodes.put(n.getId(), n);
//                }
//            }
//        }
//    }
//    
//    private DiagramNode getSuper(DiagramNode n){
//        for(DiagramRelationship r:n.getRelationships()){
//            if(r.getRelationshipName().equals("base")){
//                return r.getTo();
//            }
//        }
//        
//        return null;
//    }
//    
//    private String getSuperName(DiagramNode n){
//        
//        DiagramNode superNode = getSuper(n);
//        
//        if(superNode == null){
//            return null;
//        }
//        
//        return makePackageName(superNode);
//    }
//    
//    private DiagramNodeSet getNodeSet(String name){
//        for(DiagramNodeSet s:diagram.getNodes()){
//            if(name.equals(s.getNodeSetDescriptor().getName())){
//                return s;
//            }
//        }
//        
//        throw new RuntimeException("couldn't find " + name);
//    }
//    
//    private void convert() throws IOException{
//        
//        FileUtils.deleteDirectory(config.baseOutputDir);
//        
//        DiagramNodeSet[] vocabulary = 
//                new DiagramNodeSet[]{
//                        getNodeSet("language"),
//                        getNodeSet("FCO")
//                };
//        
//        DiagramNodeSet[] instances = 
//                new DiagramNodeSet[]{
//                        getNodeSet("SituationAwareServer"),
//                        getNodeSet("SituationAwareMobile")
//                };
//        
//        for(DiagramNodeSet vocabularySubset:vocabulary){
//            
//            List<DiagramNode> nodes = new ArrayList<>();
//            nodes.add(vocabularySubset.getNodeSetDescriptor());
//            nodes.addAll(Arrays.asList(vocabularySubset.getNodes()));
//            
//            for(DiagramNode n:nodes){
//                printObject(n);
//                
//                final String packageName = makePackageName(n);
//                final String superName = getSuperName(n);
//                final String generatedClass = generateClass(n,packageName,superName);
//                System.out.println(packageName);
//                System.out.println(generatedClass);
//                
//                final String outputPath = config.baseOutputDir.getAbsolutePath()+"/"+packageName.replace(".", "/")+".java";
//                
//                final File outputDir = new File(outputPath);
//                
//                FileUtils.writeStringToFile(outputDir, generatedClass);
//                
//                
//                System.out.println();
//            }
//        }
//        
//        for(DiagramNodeSet instance:instances){
//            
//            
//            System.out.println("instance:");
//            printObject(instance);
//            
////            List<DiagramNode> nodes = new ArrayList<>();
////            nodes.add(instance.getNodeSetDescriptor());
////            nodes.addAll(Arrays.asList(instance.getNodes()));
////            
////            for(DiagramNode n:nodes){
////                printObject(n);
////                
////                final String packageName = makePackageName(n);
////                final String superName = getSuperName(n);
////                final String generatedClass = generateInstance(n,packageName,superName);
////                System.out.println(packageName);
////                System.out.println(generatedClass);
////                
////                final String outputPath = config.baseOutputDir.getAbsolutePath()+"/"+packageName.replace(".", "/")+"Instance.java";
////                
////                final File outputDir = new File(outputPath);
////                
////                FileUtils.writeStringToFile(outputDir, generatedClass);
////                
////                
////                System.out.println();
////            }
//        }
//        
//    }
//    
//    private String generateInstance(
//            DiagramNode instance,
//            final String packageName,
//            final String superPackageName
//            ){
//        
//        StringBuilder sb = new StringBuilder();
//        
//        addCommentString(sb,"DO NOT EDIT THIS FILE\nAutomatically generated from a deployment JSON model by Securboration's deployment-to-java tool.");
//        
//        sb.append("package ");
//        sb.append(getPackageNameFromPackageName(packageName));
//        sb.append(";");
//        sb.append(NL);sb.append(NL);
//        
//        addCommentString(sb,"Automatically generated from a deployment node with ID " + instance.getId() + " and name \"" + instance.getName() + "\"");
//        
//        sb.append("public class ");
//        sb.append(getClassNameFromPackageName(packageName)+"Instance");
//        
//        if(superPackageName != null){
//            sb.append(" extends ");
//            sb.append(superPackageName);
//        }
//        
//        sb.append("{");
//        sb.append(NL);
//        sb.append(NL);
//        
//        Set<DiagramRelationship> relationships = new HashSet<>();
//        if(instance.getRelationships() != null){
//            relationships.addAll(Arrays.asList(instance.getRelationships()));
//        }
//        
//        Set<DiagramRelationship> metaRelationships = new HashSet<>();
//        if(instance.getMetaRelationships() != null){
//            metaRelationships.addAll(Arrays.asList(instance.getMetaRelationships()));
//        }
//        
//        List<DiagramRelationship> allRelationships = new ArrayList<>();
//        allRelationships.addAll(relationships);
//        allRelationships.addAll(metaRelationships);
//        
//        Set<String> alreadyProcessedRelationships = new HashSet<>();
//        for(DiagramRelationship r:allRelationships){
//            
//            if(r.getRelationshipName().equals("base")){
//                continue;
//            }
//            
//            if(alreadyProcessedRelationships.contains(r.getRelationshipName())){
//                continue;
//            }
//            
//            alreadyProcessedRelationships.add(r.getRelationshipName());
//            
//            final String relationType = metaRelationships.contains(r) ? "meta ":"";
//            
//            final String type = getType(r.getToMultiplicity(),makePackageName(r.getTo()));
//            
//            addCommentString(sb,"Automatically generated from " + relationType + "relationship [" + r.getFrom().getId() + "] --> [" + r.getTo().getId() + "]");
//            sb.append("public ");
//            sb.append(type);
//            sb.append(" ");
//            sb.append(r.getRelationshipName());
//            sb.append(";");
//            
//            sb.append(NL);
//            sb.append(NL);
//        }
//        
//        sb.append("}");
//        sb.append(NL);
//        
//        return sb.toString();
//    }
//    
//    private String makePackageName(DiagramNode n){
//        String baseNamespace = config.baseNamespace;
//        
//        String name = n.getName();
//        if(name == null){
//            name = Base64.getEncoder().encodeToString(n.getId().getBytes());
//            name = name.replace("+", "_P_");
//            name = name.replace("/", "_S_");
//            name = "Unnamed" + name;
//        } else {
//            String[] parts = name.split("\\W+");
//            
//            StringBuilder sb = new StringBuilder();
//            
//            for(String part:parts){
//                sb.append(part.substring(0,1).toUpperCase());
//                sb.append(part.substring(1));
//            }
//            
//            name = sb.toString();
//        }
//        
//        StringBuilder sb = new StringBuilder();
//        sb.append(baseNamespace);
//        sb.append(".");
//        sb.append(name);
//        
//        return sb.toString();
//    }
//    
//    private static void addCommentString(StringBuilder sb, String s){
//        s = s.replace("\n", "\n * ");
//        
//        sb.append("/*");
//        sb.append(NL);
//        for(String line:s.split("(?<=\\G.{160})")){
//            sb.append(" * ");
//            sb.append(line);
//            sb.append(NL);
//        }
//        sb.append(" */");
//        sb.append(NL);
//    }
//    
//    private String getType(Multiplicity m,String baseType){
//        if(m == null){
//            return baseType;
//        }
//        
//        if(m.getUpperBound() == null || m.getUpperBound().getBoundValue() > 1){
//            return baseType + "[]";
//        }
//        
//        return baseType;
//    }
//    
//    private String generateClass(
//            DiagramNode source,
//            final String packageName,
//            final String superPackageName
//            ){
//        
//        StringBuilder sb = new StringBuilder();
//        
//        addCommentString(sb,"DO NOT EDIT THIS FILE\nAutomatically generated from a deployment JSON model by Securboration's deployment-to-java tool.");
//        
//        sb.append("package ");
//        sb.append(getPackageNameFromPackageName(packageName));
//        sb.append(";");
//        sb.append(NL);sb.append(NL);
//        
//        addCommentString(sb,"Automatically generated from a deployment node with ID " + source.getId() + " and name \"" + source.getName() + "\"");
//        
//        sb.append("public class ");
//        sb.append(getClassNameFromPackageName(packageName));
//        
//        if(superPackageName != null){
//            sb.append(" extends ");
//            sb.append(superPackageName);
//        }
//        
//        sb.append("{");
//        sb.append(NL);
//        sb.append(NL);
//        
//        Set<DiagramRelationship> relationships = new HashSet<>();
//        if(source.getRelationships() != null){
//            relationships.addAll(Arrays.asList(source.getRelationships()));
//        }
//        
//        Set<DiagramRelationship> metaRelationships = new HashSet<>();
//        if(source.getMetaRelationships() != null){
//            metaRelationships.addAll(Arrays.asList(source.getMetaRelationships()));
//        }
//        
//        List<DiagramRelationship> allRelationships = new ArrayList<>();
//        allRelationships.addAll(relationships);
//        allRelationships.addAll(metaRelationships);
//        
//        Set<String> alreadyProcessedRelationships = new HashSet<>();
//        for(DiagramRelationship r:allRelationships){
//            
//            if(r.getRelationshipName().equals("base")){
//                continue;
//            }
//            
//            if(alreadyProcessedRelationships.contains(r.getRelationshipName())){
//                continue;
//            }
//            
//            alreadyProcessedRelationships.add(r.getRelationshipName());
//            
//            final String relationType = metaRelationships.contains(r) ? "meta ":"";
//            
//            final String type = getType(r.getToMultiplicity(),makePackageName(r.getTo()));
//            
//            addCommentString(sb,"Automatically generated from " + relationType + "relationship [" + r.getFrom().getId() + "] --> [" + r.getTo().getId() + "]");
//            sb.append("public ");
//            sb.append(type);
//            sb.append(" ");
//            sb.append(r.getRelationshipName());
//            sb.append(";");
//            
//            sb.append(NL);
//            sb.append(NL);
//        }
//        
//        sb.append("}");
//        sb.append(NL);
//        
//        return sb.toString();
//    }
//    
//    private static final String NL = "\n";
//    
//    private static String getPackageNameFromPackageName(String className){
//        return className.substring(0,className.lastIndexOf("."));
//    }
//    
//    private static String getClassNameFromPackageName(String className){
//        return className.substring(className.lastIndexOf(".")+1);
//    }
//    
//    private static void printObject(Object o){
//        ExceptionWrapper.wrap(()->{
//            ObjectNode n = ObjectNode.build(o);
//            ObjectPrinter.getPrinterVisitor();
//            
//            n.accept(ObjectPrinter.getPrinterVisitor());
//        });
//    }
//
//    
//    
//}
