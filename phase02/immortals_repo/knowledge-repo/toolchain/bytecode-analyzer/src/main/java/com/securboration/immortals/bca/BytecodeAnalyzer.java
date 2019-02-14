package com.securboration.immortals.bca;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.objectweb.asm.Type;

import com.securboration.immortals.bcad.callgraph.Edge;
import com.securboration.immortals.bcad.callgraph.GraphBuildingListener;
import com.securboration.immortals.bcad.dataflow.DataflowHelper;
import com.securboration.immortals.bcad.dataflow.DataflowHelper.ParameterSpec;
import com.securboration.immortals.bcad.runtime.EventBroadcaster;
import com.securboration.immortals.bcad.runtime.listeners.CodeUnitUtilityTracker;
import com.securboration.immortals.bcad.runtime.listeners.EventPrinter;
import com.securboration.immortals.bcad.transformers.AboutToInvokeTransformer.AnalysisScopeDefinition;
import com.securboration.immortals.bcad.transformers.NameRegistry;
import com.securboration.immortals.bcad.transformers.TraceTransformer;
import com.securboration.immortals.instantiation.annotationparser.traversal.AnnotationParser;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.DynamicCallGraph;
import com.securboration.immortals.ontology.DynamicCallGraphEdge;
import com.securboration.immortals.ontology.FunctionalityTestRun;

public class BytecodeAnalyzer {
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        
        final String driverClassName = 
                "com.securboration.immortals.bca.example.example1.Driver";
        
        AnalysisClassloader classloader = new AnalysisClassloader();
        classloader.registerClasspathDir(
            new File("../../examples/atak-example/target/")
            );
        
        //TODO
        
        performDynamicAnalysis(
            driverClassName,
            classloader
            );
        
        staticAnalysis(classloader);
        
        
        
        //TODO: transform bytecode for dynamic analysis
        //TODO: invoke test method reflectively
        //TODO: acquire metrics
    }
    
    private static void analyzeClasses(AnalysisClassloader classloader) throws ClassNotFoundException, IOException{
        Set<Class<?>> analyzeThese = new LinkedHashSet<>();
        {
            Set<Class<?>> classes = new LinkedHashSet<>();
            
            classes.addAll(
                classloader.loadEverythingWithPrefix(
                    "com.securboration.immortals.bca.example.example1"
                    )
                );
            
            collect(classes,analyzeThese);
        }
        
        BytecodeAnalyzer analyzer = new BytecodeAnalyzer();
        
        analyzer.analyze(
            classloader,
            analyzeThese
            );
    }
    
    private static Method getPublicStaticMethod(Class<?> c){
        
        Set<Method> declared = new HashSet<>(
                Arrays.asList(c.getDeclaredMethods())
                );
        
        Set<Method> methods = new HashSet<>(
                Arrays.asList(c.getMethods())
                );
        
        Set<Method> intersection = new HashSet<>(declared);
        intersection.retainAll(methods);
        
        Set<Method> matches = new LinkedHashSet<>();
        for(Method m:intersection){
            boolean isStatic = (m.getModifiers() & Modifier.STATIC) > 0;
            boolean isPublic = (m.getModifiers() & Modifier.PUBLIC) > 0;
            boolean isNoArgs = (m.getParameters().length == 0);
            
            if(isStatic && isPublic && isNoArgs){
                matches.add(m);
            }
        }
        
        if(matches.size() == 1){
            return matches.iterator().next();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(
                "expected exactly 1 public static method with no args in " + c.getName() + 
                " but found " + matches.size() + ":\n");
            for(Method match:matches){
                sb.append(String.format("\t%s\n", match));
            }
            throw new RuntimeException(sb.toString());
        }
        
    }

    private static Method getMainMethod(Class<?> c){

        Set<Method> declared = new HashSet<>(
                Arrays.asList(c.getDeclaredMethods())
        );

        Set<Method> methods = new HashSet<>(
                Arrays.asList(c.getMethods())
        );

        Set<Method> intersection = new HashSet<>(declared);
        intersection.retainAll(methods);

        Set<Method> matches = new LinkedHashSet<>();
        for(Method m:intersection){
            boolean isStatic = (m.getModifiers() & Modifier.STATIC) > 0;
            boolean isPublic = (m.getModifiers() & Modifier.PUBLIC) > 0;
            boolean isMain = (m.getName().equals("main"));
            
            if(isStatic && isPublic && isMain) { 
                matches.add(m);
            }
        }

        if(matches.size() == 1){
            return matches.iterator().next();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "Class: " + c.getName() +
                            " does not have a declared main method, can not perform dynamic analysis.\n");
            return null;
        }

    }
    
    private static void performDynamicAnalysis(
            final String driverClassName,
            final AnalysisClassloader classloader
            ) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        
        //tracks which code regions are actually reached during execution
        CodeUnitUtilityTracker usedCodeTracker = new CodeUnitUtilityTracker();
        
        //builds a dynamic call graph
        GraphBuildingListener callGraphBuilder = new GraphBuildingListener();
        
        //wire up the event broadcaster system
        {
            EventBroadcaster.getListenersModifiable().clear();
            EventBroadcaster.getListenersModifiable().add(new EventPrinter());
            EventBroadcaster.getListenersModifiable().add(usedCodeTracker);
            EventBroadcaster.getListenersModifiable().add(callGraphBuilder);
        }
        
        
        //track mapping of hashes to human-readable names
        NameRegistry nameRegistry = new NameRegistry();
        
        //wire up the instrumentation system
        {
            classloader.getLazyTransformerChain().add(nameRegistry);
            
            //add a lazy trace transformer
            classloader.getLazyTransformerChain().add(new TraceTransformer());
        }
        
        //drive the application
        {
            Class<?> driverClass = classloader.findClass(driverClassName);
            Method driverMethod = getPublicStaticMethod(driverClass);
            driverMethod.invoke(null);
        }
        
        //use the results obtained
        {
            Set<String> usefulClasses = classloader.getLoadedClassHashes();
            Set<String> usefulMethods = usedCodeTracker.getUsefulMethods();
            
            System.out.printf(
                "SUMMARY: discovered %d useful classes containing %d useful methods\n", 
                usefulClasses.size(),
                usefulMethods.size()
                );
            
            System.out.printf("\tclasses:\n");
            for(String s:new TreeSet<>(usefulClasses)){
                System.out.printf(
                    "\t\t%s (%s)\n", s, classloader.getNameFromHash(s)
                    );
            }
            
            System.out.printf("\tmethods:\n");
            for(String s:new TreeSet<>(usefulMethods)){
                System.out.printf(
                    "\t\t%s\n", s
                    );
            }
            
            
            System.out.println(
                printEdges(
                    callGraphBuilder.getEdges(),
                    nameRegistry
                    )
                );
        }
        
//        tCDY1SFM+HtvIT75KyY5rbtg5i/qbCGzFiN7QdxQInw=/methods/formatNameBytes(Ljava/lang/String;[BIILorg/apache/commons/compress/archivers/zip/ZipEncoding;)I
        
//        System.out.println(classloader.getClassWithHash("tCDY1SFM+HtvIT75KyY5rbtg5i/qbCGzFiN7QdxQInw=").getName());
//        classloader.getMethodFromHash("tCDY1SFM+HtvIT75KyY5rbtg5i/qbCGzFiN7QdxQInw=/methods/formatNameBytes(Ljava/lang/String;[BIILorg/apache/commons/compress/archivers/zip/ZipEncoding;)I");
    }
    public static FunctionalityTestRun invokeTestMethod(final String driverClassName, final HashSet<String> dependencies,
                                                        final String methodName, DynamicCallGraph callGraph)  {
        
        AnalysisClassloader classloader = new AnalysisClassloader();
        try {
            for (String path : dependencies) {
                File file = new File(path);
                if (file.getName().endsWith(".class")) {
                    classloader.registerClass(FileUtils.readFileToByteArray(file));
                } else if (file.getName().endsWith(".jar")) {
                    classloader.registerJar(FileUtils.readFileToByteArray(file));
                }
            }
        } catch (IOException exc) {
            System.out.println("Unable to load files on classpath");
            return null;
        }
        
        //tracks which code regions are actually reached during execution
        CodeUnitUtilityTracker usedCodeTracker = new CodeUnitUtilityTracker();

        //builds a dynamic call graph
        GraphBuildingListener callGraphBuilder = new GraphBuildingListener();

        //wire up the event broadcaster system
        {
            EventBroadcaster.getListenersModifiable().clear();
            EventBroadcaster.getListenersModifiable().add(new EventPrinter());
            EventBroadcaster.getListenersModifiable().add(usedCodeTracker);
            EventBroadcaster.getListenersModifiable().add(callGraphBuilder);
        }

        //track mapping of hashes to human-readable names
        NameRegistry nameRegistry = new NameRegistry();
        
        //wire up the instrumentation system
        {
            classloader.getLazyTransformerChain().add(nameRegistry);

            //add a lazy trace transformer
            classloader.getLazyTransformerChain().add(new TraceTransformer());
        }
        
        //drive the application
        {
            Method testMethod;
            try {
                Class<?> driverClass = classloader.findClass(driverClassName);
                testMethod = driverClass.getMethod(methodName, null);
            } catch (ClassNotFoundException exc) {
                System.out.println("Unable to load class: " + driverClassName);
                return null;
            } catch (NoSuchMethodException exc) {
                System.out.println("Unable to find method: " + methodName);
                return null;
            }
           
            FunctionalityTestRun functionalityTestRun = new FunctionalityTestRun();
            functionalityTestRun.setTestBeginTime(new Timestamp(System.currentTimeMillis()).toString());
            try {
                testMethod.invoke(null);
                functionalityTestRun.setTestEndTime(new Timestamp(System.currentTimeMillis()).toString());
                Map<Thread,Map<Edge, AtomicLong>> results = callGraphBuilder.getEdges();
                DynamicCallGraphEdge[] edges = callGraph.getDynamicCallGraphEdges();
                List<DynamicCallGraphEdge> edgeList;
                if (edges != null) {
                    edgeList = Arrays.asList(edges);
                    edgeList = new ArrayList<>(edgeList);
                } else {
                    edgeList = new ArrayList<>();
                }

                for(Thread t:results.keySet()){
                    Map<Edge,AtomicLong> edgesForThread = results.get(t);
                    for(Edge e:edgesForThread.keySet()){
                        final String src = e.getFrom();
                        final String dst = e.getTo();

                        DynamicCallGraphEdge dynamicEdge = new DynamicCallGraphEdge();
                        dynamicEdge.setCallerHash(src);
                        dynamicEdge.setCalledHash(dst);
                        edgeList.add(dynamicEdge);
                    }
                }

                callGraph.setDynamicCallGraphEdges(edgeList.toArray(new DynamicCallGraphEdge[edgeList.size()]));
                functionalityTestRun.setSuccess(true);
            } catch (Exception exc) {
                exc.printStackTrace();
                return null;
            }
            return functionalityTestRun;
        }
    }

    private static void staticAnalysis(AnalysisClassloader classloader) throws ClassNotFoundException{
        for(Class<?> c:classloader.loadEverythingWithPrefix("com.securboration.immortals.bca.example")){
            
            System.out.println("\nanalyzing " + c.getName());
            for(Method m:c.getDeclaredMethods()){
                System.out.printf("\t%s %s\n", m.getName(), Type.getMethodDescriptor(m));
                ParameterSpec[] specs = DataflowHelper.getArgumentTypes(m);//TODO
                
                for(ParameterSpec spec:specs){
                    System.out.printf(
                        "\t\t%s%d %s %s -> %s\n", 
                        spec.getSemanticType() != null ? "*":"",
                        spec.getParameterIndex(), 
                        spec.getParameterName(), 
                        spec.getJavaTypeDesc(), 
                        spec.getSemanticType()
                        );
                }
            }
        }
    }
    
    private static AnalysisScopeDefinition getScope(
            AnalysisClassloader classloader
            ){
        AnalysisScopeDefinition scope = new AnalysisScopeDefinition(){

            @Override
            public boolean isInScope(String className) {
                return classloader.isKnownClass(className);
            }
            
        };
        
        return scope;
    }
    
    
    
    private static void collect(
            Collection<Class<?>> examineThese, 
            Collection<Class<?>> collected
            ){
        for(Class<?> c:examineThese){
            
            if(collected.contains(c)){
                continue;
            }
            collected.add(c);
            
            Class<?>[] inners = c.getDeclaredClasses();
            
            if(inners == null || inners.length == 0){
                //do nothing
            } else {
                collect(Arrays.asList(inners),collected);
            }
        }
    }
    
    private void analyze(
            AnalysisClassloader classloader,
            Collection<Class<?>> classesToAnalyze
            ) throws IOException{
        {
            System.out.println("the following classes will be analyzed:");
            classesToAnalyze.forEach(c->{
                System.out.println("  " + c.getName());
            });
        }
        
        ObjectToTriplesConfiguration config = 
                new ObjectToTriplesConfiguration("r2.0.0");
        
        AnnotationParser a = new AnnotationParser(config);
        
        for(Class<?> c:classesToAnalyze){
            final byte[] bytecode = classloader.getBytecode(c);
            a.visitClass(hash(bytecode), bytecode);
        }
        
        Set<Object> parsedObjects = config.getMapper().getObjectsToSerialize();
        
        Model m = ModelFactory.createDefaultModel();
        for(Object o:parsedObjects){
            System.out.println(o.getClass().getName());
            
            m.add(ObjectToTriples.convert(config, o));
        }
        
        System.out.println();
        System.out.println(OntologyHelper.serializeModel(m, "TURTLE", false));
    }
    
    private static String hash(byte[] data){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    private static String printEdges(
            Map<Thread,Map<Edge,AtomicLong>> map,
            NameRegistry registry
            ){
        
        Map<String,Set<String>> connectivity = new TreeMap<>();
        
        for(Thread t:map.keySet()){
            Map<Edge,AtomicLong> edgesForThread = map.get(t);
            
            for(Edge e:edgesForThread.keySet()){
                final String src = e.getFrom();
                final String dst = e.getTo();
                
                Set<String> connectionsForSrc = connectivity.get(src);
                
                if(connectionsForSrc == null){
                    connectionsForSrc = new TreeSet<>();
                    connectivity.put(src, connectionsForSrc);
                }
                
                connectionsForSrc.add(dst);
            }
            
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("*** call graph ***\n");
        for(String src:connectivity.keySet()){
            Set<String> called = connectivity.get(src);
            sb.append(String.format("\t%s was observed to call the following %d methods:\n", registry.getHumanReadableForm(src), called.size()));
            for(String dst:called){
                sb.append(String.format("\t\t%s\n", registry.getHumanReadableForm(dst)));
            }
        }
        sb.append("*** END call graph ***\n");
        
        return sb.toString();
    }
    

}
