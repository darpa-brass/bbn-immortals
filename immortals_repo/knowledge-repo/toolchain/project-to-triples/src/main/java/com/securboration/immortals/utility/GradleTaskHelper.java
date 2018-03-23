package com.securboration.immortals.utility;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.securboration.immortals.j2s.mapper.PojoMappingContext;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.ontology.analysis.DataflowEdge;
import com.securboration.immortals.ontology.analysis.DataflowNode;
import com.securboration.immortals.ontology.analysis.InterMethodDataflowNode;
import com.securboration.immortals.ontology.constraint.*;
import com.securboration.immortals.ontology.dfu.instance.FunctionalAspectInstance;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.property.impact.ImpactStatement;
import com.securboration.immortals.ontology.property.impact.PropertyImpact;
import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.repo.query.TriplesToPojo;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradleTaskHelper {
    
    private FusekiClient client;
    private String graphName;
    private boolean previousTaskDetected = false;
    private String resultsDir;

    private PrintWriter pw;
    
    public GradleTaskHelper(FusekiClient _client, String _graphName, String _resultsDir, String projectName) {
        client = _client;
        graphName = _graphName;
        if (projectName != null && !foundPreviousExecution(_resultsDir)) {
            new File(_resultsDir + File.separator + projectName).mkdirs();
        } else {
            previousTaskDetected = true;
        }
        resultsDir = _resultsDir +"/";
    }
    public GradleTaskHelper(FusekiClient _client, String _graphName) {
        client = _client;
        graphName = _graphName;
    }

    public FusekiClient getClient() {
        return client;
    }

    public void setClient(FusekiClient client) {
        this.client = client;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public PrintWriter getPw() {
        return pw;
    }

    public void setPw(PrintWriter pw) {
        this.pw = pw;
    }

    public boolean isPreviousTaskDetected() {
        return previousTaskDetected;
    }

    public void setPreviousTaskDetected(boolean previousTaskDetected) {
        this.previousTaskDetected = previousTaskDetected;
    }

    public String getResultsDir() {
        return resultsDir;
    }

    public void setResultsDir(String resultsDir) {
        this.resultsDir = resultsDir;
    }

    public enum TaskType {
        CONSTRAINT,
        ANALYSIS_FRAME,
        BYTECODE
    }
    
    public static class AssertableSolutionSet implements FusekiClient.ResultSetProcessor {

        private List<Solution> solutions = new ArrayList<>();

        public List<Solution> getSolutions() {
            return solutions;
        }

        @Override
        public void processQuerySolution(QuerySolution s) {
            List<TupleValue> solution = new ArrayList<>();
            s.varNames().forEachRemaining(k->{
                final String v;
                RDFNode node = s.get(k);

                if(node.isLiteral()){
                    v = node.asLiteral().getLexicalForm();
                } else {
                    v = node.asResource().getURI();
                }

                solution.add(new TupleValue(k,v));
            });

            solutions.add(new Solution(solution));
        }
    }
    
    private enum ImpactStatementType {
        
        PROPERTY_IMPACT("com.securboration.immortals.ontology.property.impact.PropertyImpact");

        private final String className;
        ImpactStatementType(String _className) {
           className = _className;
        }
        
        public String getClassName() {
            return this.className;
        }
        
        public static ImpactStatementType getStatementType(String className) {
            for (ImpactStatementType type : ImpactStatementType.values()) {
                if (type.getClassName().equals(className)) {
                    return type;
                }
            }
            return null;
        }
        
    }

    private static class MethodCommentVisitor extends VoidVisitorAdapter<Void> {

        private String newClassName;
        
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (n.getDeclarationAsString(true, true, true).contains(newClassName)) {
                n.setComment(new BlockComment("This constructor was auto-generated by Immortals analysis."));
            } else {
                n.setComment(new BlockComment("This method was auto-generated by Immortals analysis"));
            }

        }
        
        public void setNewClassName(String newClassName) {
            this.newClassName = newClassName;
        }
    }

    private static class InitializerRemoverVisitor extends VoidVisitorAdapter<Void> {

        private List<Node> nodesToRemove;

        public InitializerRemoverVisitor() {
            super();
            nodesToRemove = new ArrayList<>();
        }

        private void removeInitializerBlock(Node nodeToRemove) {
            Node parentNode = nodeToRemove.getParentNode().get();
            parentNode.remove(nodeToRemove);
        }
        
        @Override
        public void visit(InitializerDeclaration n, Void arg) {
            if (n.isStatic()) {
                nodesToRemove.add(n);
            }
        }

        public List<Node> getNodesToRemove() {
            return nodesToRemove;
        }
    }
    
    private static class AddFieldToExistingClassVisitor extends VoidVisitorAdapter<Void> {
        
        private String fieldType;
        
        @Override
        public void visit(ClassOrInterfaceDeclaration cd, Void arg) {
            cd.addPrivateField(fieldType, fieldType.equals("java.io.OutputStream") ? "outputstream" : "inputstream");
        }
    }
    
    private static class AddAutoGeneratedMethodsVisitor extends VoidVisitorAdapter<Void> {
        
        private String streamType;
        private GradleTaskHelper taskHelper;
        private String aspectUUID;
        private String wrappedClassName;
        private List<String> augmentedMethods;
        private String cipherImpl;
        
        @Override
        public void visit(ClassOrInterfaceDeclaration cd, Void arg) {

            String getSpecs ="prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                    "prefix IMMoRTALS_pattern_spec: <http://darpa.mil/immortals/ontology/r2.0.0/pattern/spec#>\n" +
                    "prefix IMMoRTALS_functionality_alg_encryption: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/alg/encryption#>\n" +
                    "prefix IMMoRTALS_resources_streams: <http://darpa.mil/immortals/ontology/r2.0.0/resources/streams#>\n" +
                    "\n" +
                    "select ?specCode where {\n" +
                    "\tgraph <http://localhost:3030/ds/data/???GRAPH_NAME???> {" +
                    "\t\t?funcSpec a IMMoRTALS_pattern_spec:LibraryFunctionalAspectSpec\n" +
                    "\t\t; IMMoRTALS:hasAspect <???ASPECT???>\n" +
                    "\t\t; IMMoRTALS:hasComponent ?specComponent .\n" +
                    "\t\t\n" +
                    "\t\t?specComponent IMMoRTALS:hasAspectBeingPerformed ???SPEC_ASPECT???" +
                    "\t\t; IMMoRTALS:hasCodeSpec ?spec .\n\n" +
                    "\t\t" +
                    "\t\t?spec IMMoRTALS:hasClassName \"???CLASS_NAME???\"\n" +
                    "\t\t; IMMoRTALS:hasCode ?specCode ." +
                    "\t\t\n" +
                    "\t}" +
                    "}";
            getSpecs = getSpecs.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???ASPECT???", aspectUUID)
                    .replace("???CLASS_NAME???", wrappedClassName);
            AssertableSolutionSet specSolutions = new AssertableSolutionSet();
            
            if (streamType.equals("java.io.OutputStream") && augmentedMethods.contains("getStreamImpl()")) {
                NodeList<ReferenceType> thrownExceptions = new NodeList<>();
                thrownExceptions.add(JavaParser.parseClassOrInterfaceType("java.lang.Exception"));
                MethodDeclaration getOutputStreamImpl = new MethodDeclaration();
                getOutputStreamImpl.setName("getOutputStreamImpl");
                getOutputStreamImpl.setType(JavaParser.parseClassOrInterfaceType(streamType));
                getOutputStreamImpl.setThrownExceptions(thrownExceptions);
                Modifier[] modifiers = new Modifier[] {Modifier.PUBLIC};
                getOutputStreamImpl.setModifiers(Arrays.stream(modifiers).collect(Collectors.toCollection(() -> EnumSet.noneOf(Modifier.class))));

                taskHelper.getClient().executeSelectQuery(getSpecs.replace("???SPEC_ASPECT???",
                        "IMMoRTALS_resources_streams:AspectRetrieveStreamImpl"), specSolutions);
                String codeReplacement = specSolutions.getSolutions().get(0).get("specCode");
                getOutputStreamImpl.setBody(JavaParser.parseBlock(codeReplacement));
                
                cd.getMembers().add(getOutputStreamImpl);

                
                NodeList<Parameter> parameters = new NodeList<>();
                parameters.add(new Parameter(JavaParser.parseType(streamType), "var0"));
                parameters.add(new Parameter(JavaParser.parseType(cipherImpl), "var1"));
                specSolutions = new AssertableSolutionSet();
                MethodDeclaration wrapOutputStreamDeclaration = new MethodDeclaration();
                wrapOutputStreamDeclaration.setName("wrapOutputStream");
                wrapOutputStreamDeclaration.setType(JavaParser.parseClassOrInterfaceType(streamType));
                wrapOutputStreamDeclaration.setParameters(parameters);
                wrapOutputStreamDeclaration.setThrownExceptions(thrownExceptions);
                modifiers = new Modifier[] {Modifier.PUBLIC, Modifier.STATIC};
                wrapOutputStreamDeclaration.setModifiers(Arrays.stream(modifiers).collect(Collectors.toCollection(() -> EnumSet.noneOf(Modifier.class))));

                taskHelper.getClient().executeSelectQuery(getSpecs.replace("???SPEC_ASPECT???",
                        "IMMoRTALS_resources_streams:WrapStreamWithCipher"), specSolutions);
                codeReplacement = specSolutions.getSolutions().get(0).get("specCode");
                wrapOutputStreamDeclaration.setBody(JavaParser.parseBlock(codeReplacement));
                
                cd.getMembers().add(wrapOutputStreamDeclaration);
            } else if (streamType.equals("java.io.InputStream") && augmentedMethods.contains("getStreamImpl()")) {

                NodeList<ReferenceType> thrownExceptions = new NodeList<>();
                thrownExceptions.add(JavaParser.parseClassOrInterfaceType("java.lang.Exception"));
                MethodDeclaration getInputStreamImpl = new MethodDeclaration();
                getInputStreamImpl.setName("getInputStreamImpl");
                getInputStreamImpl.setType(JavaParser.parseClassOrInterfaceType(streamType));
                getInputStreamImpl.setThrownExceptions(thrownExceptions);
                Modifier[] modifiers = new Modifier[] {Modifier.PUBLIC};
                getInputStreamImpl.setModifiers(Arrays.stream(modifiers).collect(Collectors.toCollection(() -> EnumSet.noneOf(Modifier.class))));

                taskHelper.getClient().executeSelectQuery(getSpecs.replace("???SPEC_ASPECT???",
                        "IMMoRTALS_resources_streams:AspectRetrieveStreamImpl"), specSolutions);
                String codeReplacement = specSolutions.getSolutions().get(0).get("specCode");
                getInputStreamImpl.setBody(JavaParser.parseBlock(codeReplacement));

                cd.getMembers().add(getInputStreamImpl);

                specSolutions = new AssertableSolutionSet();
                NodeList<Parameter> parameters = new NodeList<>();
                parameters.add(new Parameter(JavaParser.parseType(streamType), "var0"));
                parameters.add(new Parameter(JavaParser.parseType(cipherImpl), "var1"));
                MethodDeclaration wrapInputStreamDeclaration = new MethodDeclaration();
                wrapInputStreamDeclaration.setName("wrapInputStream");
                wrapInputStreamDeclaration.setType(JavaParser.parseClassOrInterfaceType(streamType));
                wrapInputStreamDeclaration.setParameters(parameters);
                wrapInputStreamDeclaration.setThrownExceptions(thrownExceptions);
                modifiers = new Modifier[] {Modifier.PUBLIC, Modifier.STATIC};
                wrapInputStreamDeclaration.setModifiers(Arrays.stream(modifiers).collect(Collectors.toCollection(() -> EnumSet.noneOf(Modifier.class))));

                taskHelper.getClient().executeSelectQuery(getSpecs.replace("???SPEC_ASPECT???",
                        "IMMoRTALS_resources_streams:WrapStreamWithCipher"), specSolutions);
                codeReplacement = specSolutions.getSolutions().get(0).get("specCode");
                wrapInputStreamDeclaration.setBody(JavaParser.parseBlock(codeReplacement));
                
                cd.getMembers().add(wrapInputStreamDeclaration);
            }
        }
    }
    
    private static class AffectedMethodsVisitor extends VoidVisitorAdapter<Void> {
        
        private List<String> affectedMethods = new ArrayList<>();
        
        @Override
        public void visit(MethodDeclaration md, Void arg) {
            StreamImplementationArchivist.affectedMethodsToRepair.keySet().stream().filter(method -> method.equals(md.getSignature().asString()))
                    .findFirst().ifPresent(x -> {
                        affectedMethods.add(md.getSignature().asString());
                        BlockStmt blockStmt = JavaParser.parseBlock(StreamImplementationArchivist.affectedMethodsToRepair.get(x));
                        md.removeBody();
                        md.setBody(blockStmt);
            });
        }
    }
    
    private static class AugmentConstructorVisitor extends VoidVisitorAdapter<Void> {

        private String streamType;
        private String wrappedClassSimpleName;
        
        @Override
        public void visit(ClassOrInterfaceDeclaration cd, Void arg) {
            
            Optional<ConstructorDeclaration> constructorOption = cd.getConstructorByParameterTypes(wrappedClassSimpleName);
            NodeList<CatchClause> tryCatches = null;
            String cipherImplName = null;
            NodeList<Statement> rebuildConstructor = new NodeList<>();
            String tryBody = null;
            if (constructorOption.isPresent()) {
                
                ConstructorDeclaration constructor = constructorOption.get();
                List<Statement> constructorBody = constructor.getBody().getStatements();
                
                for (Statement bodyStatement : constructorBody) {
                    
                    if (bodyStatement instanceof TryStmt) {
                        
                        TryStmt tryStmt = (TryStmt) bodyStatement;
                        tryCatches = tryStmt.getCatchClauses();
                        tryBody = tryStmt.getTryBlock().toString();
                        List<Node> tryNodes = tryStmt.getChildNodes().get(0).getChildNodes();
                        Node wrapNode = tryNodes.get(tryNodes.size() - 2);
                        
                        if (wrapNode instanceof ExpressionStmt) {
                            
                            ExpressionStmt wrapExpression = (ExpressionStmt) wrapNode;
                            Expression expression = wrapExpression.getExpression();
                            
                            if (expression instanceof AssignExpr) {
                                
                                Expression valueExpression = ((AssignExpr) expression).getValue();
                                
                                if (valueExpression instanceof MethodCallExpr) {
                                    
                                    MethodCallExpr wrapCall = (MethodCallExpr) valueExpression;
                                    Node getCipherImplNameNode = wrapCall.getChildNodes().get(wrapCall.getChildNodes().size() - 1);
                                    
                                    if (getCipherImplNameNode instanceof NameExpr) {
                                        cipherImplName = ((NameExpr) getCipherImplNameNode).getName().getIdentifier();
                                    }
                                }
                            }
                        }
                    } else {
                        rebuildConstructor.add(bodyStatement);
                    }
                }

                
                
                if (streamType.equals("java.io.OutputStream")) {
                    int indexOfInputStream = tryBody.indexOf("this.inputstream");
                    String temp = tryBody.substring(indexOfInputStream);
                    int tempInt = temp.indexOf("\n");

                    String newInsertions = "\t\tOutputStream os = getOutputStreamImpl();\n" +
                            "\t\tos = wrapOutputStream(os, ???CIPHER_IMPL_NAME???);\n" +
                            "\t\tthis.outputstream = os;\n";
                    newInsertions = newInsertions.replace("???CIPHER_IMPL_NAME???", cipherImplName);
                    
                    StringBuilder buildString = new StringBuilder(tryBody);
                    buildString.insert((indexOfInputStream + tempInt) + 1, newInsertions);
                    
                    BlockStmt newTryBody = JavaParser.parseBlock(buildString.toString());
                    TryStmt newTryStmt = new TryStmt(newTryBody, tryCatches, new BlockStmt());
                    rebuildConstructor.add(newTryStmt);
                    constructor.setBody(new BlockStmt(rebuildConstructor));
                } else if (streamType.equals("java.io.InputStream")) {
                    int indexOfOutputStream = tryBody.indexOf("this.outputstream");
                    String temp = tryBody.substring(indexOfOutputStream);
                    int tempInt = temp.indexOf("\n");

                    String newInsertions = "InputStream is = getInputStreamImpl();\n" +
                            "            is = wrapInputStream(is, ???CIPHER_IMPL_NAME???);\n" +
                            "            this.inputstream = is;\n";
                    newInsertions = newInsertions.replace("???CIPHER_IMPL_NAME???", cipherImplName);
                    
                    StringBuilder buildString = new StringBuilder(tryBody);
                    buildString.insert((indexOfOutputStream + tempInt) + 1, newInsertions);

                    BlockStmt newTryBody = JavaParser.parseBlock(buildString.toString());
                    TryStmt newTryStmt = new TryStmt(newTryBody, tryCatches, new BlockStmt());
                    rebuildConstructor.add(newTryStmt);
                    constructor.setBody(new BlockStmt(rebuildConstructor));
                }
            }
        }
    }
    
    private static class MethodLineNumberVisitor extends VoidVisitorAdapter<Void> {
        
       private GradleTaskHelper taskHelper;
       private String aspectUUID;
       private String wrappedClassName;
       private List<String> autoGeneratedMethods = new ArrayList<>();
       
        @Override
        public void visit(MethodDeclaration md, Void arg) {
            
            if (md.getNameAsString().equals("initCipherImpl") ||
                md.getNameAsString().equals("getStreamImpl")  ||
                md.getNameAsString().equals("wrap")) {
                
                autoGeneratedMethods.add(md.getSignature().asString());

                String getSpecs ="prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "prefix IMMoRTALS_pattern_spec: <http://darpa.mil/immortals/ontology/r2.0.0/pattern/spec#>\n" +
                        "prefix IMMoRTALS_functionality_alg_encryption: <http://darpa.mil/immortals/ontology/r2.0.0/functionality/alg/encryption#>\n" +
                        "prefix IMMoRTALS_resources_streams: <http://darpa.mil/immortals/ontology/r2.0.0/resources/streams#>\n" +
                        "\n" +
                        "select ?specCode where {\n" +
                        "\tgraph <http://localhost:3030/ds/data/???GRAPH_NAME???> {" +
                        "\t\t?funcSpec a IMMoRTALS_pattern_spec:LibraryFunctionalAspectSpec\n" +
                        "\t\t; IMMoRTALS:hasAspect <???ASPECT???>\n" +
                        "\t\t; IMMoRTALS:hasComponent ?specComponent .\n" +
                        "\t\t\n" +
                        "\t\t?specComponent IMMoRTALS:hasAspectBeingPerformed ???SPEC_ASPECT???" +
                        "\t\t; IMMoRTALS:hasCodeSpec ?spec .\n\n" +
                        "\t\t" +
                        "\t\t?spec IMMoRTALS:hasClassName \"???CLASS_NAME???\"\n" +
                        "\t\t; IMMoRTALS:hasCode ?specCode ." +
                        "\t\t\n" +
                        "\t}" +
                        "}";
                getSpecs = getSpecs.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace("???ASPECT???", aspectUUID)
                        .replace("???CLASS_NAME???", wrappedClassName);
                AssertableSolutionSet specSolutions = new AssertableSolutionSet();

                String codeReplacement;
                BlockStmt blockStmt;
                
                if (md.getNameAsString().equals("initCipherImpl")) {
                    taskHelper.getClient().executeSelectQuery(getSpecs.replace("???SPEC_ASPECT???",
                            "IMMoRTALS_functionality_alg_encryption:AspectCipherInitialize"), specSolutions);
                    codeReplacement = specSolutions.getSolutions().get(0).get("specCode");
                    blockStmt = JavaParser.parseBlock(codeReplacement);
                    md.removeBody();
                    md.setBody(blockStmt);

                } else if (md.getNameAsString().equals("getStreamImpl")) {
                    taskHelper.getClient().executeSelectQuery(getSpecs.replace("???SPEC_ASPECT???",
                            "IMMoRTALS_resources_streams:AspectRetrieveStreamImpl"), specSolutions);
                    codeReplacement = specSolutions.getSolutions().get(0).get("specCode");
                    blockStmt = JavaParser.parseBlock(codeReplacement);
                    md.removeBody();
                    md.setBody(blockStmt);
                    
                } else if (md.getNameAsString().equals("wrap")) {
                    taskHelper.getClient().executeSelectQuery(getSpecs.replace("???SPEC_ASPECT???",
                            "IMMoRTALS_resources_streams:WrapStreamWithCipher"), specSolutions);
                    codeReplacement = specSolutions.getSolutions().get(0).get("specCode");
                    blockStmt = JavaParser.parseBlock(codeReplacement);
                    md.removeBody();
                    md.setBody(blockStmt);
                }
            }
        }
    }
    
    public String parseRawCode(String source, String wrappedClassName, List<String> augmentedMethods, String streamType,
                               String aspectUUID, String cipherImpl) {
        
        StreamImplementationArchivist.initializeArchive(wrappedClassName);
        
        AffectedMethodsVisitor affectedMethodsVisitor = new AffectedMethodsVisitor();
        
        AddFieldToExistingClassVisitor addFieldToExistingClassVisitor = new AddFieldToExistingClassVisitor();
        addFieldToExistingClassVisitor.fieldType = streamType;
        
        AddAutoGeneratedMethodsVisitor addAutoGeneratedMethodsVisitor = new AddAutoGeneratedMethodsVisitor();
        addAutoGeneratedMethodsVisitor.streamType = streamType;
        addAutoGeneratedMethodsVisitor.aspectUUID = aspectUUID;
        addAutoGeneratedMethodsVisitor.taskHelper = this;
        addAutoGeneratedMethodsVisitor.wrappedClassName = wrappedClassName;
        addAutoGeneratedMethodsVisitor.augmentedMethods = augmentedMethods;
        addAutoGeneratedMethodsVisitor.cipherImpl = cipherImpl;
        
        AugmentConstructorVisitor augmentConstructorVisitor = new AugmentConstructorVisitor();
        augmentConstructorVisitor.streamType = streamType;
        augmentConstructorVisitor.wrappedClassSimpleName = wrappedClassName.substring(wrappedClassName.lastIndexOf("/") + 1);
        
        CompilationUnit compilationUnit = JavaParser.parse(source);
        compilationUnit.accept(affectedMethodsVisitor, null);
        compilationUnit.accept(addFieldToExistingClassVisitor, null);
        compilationUnit.accept(addAutoGeneratedMethodsVisitor, null);
        compilationUnit.accept(augmentConstructorVisitor, null);
        
        augmentedMethods.addAll(affectedMethodsVisitor.affectedMethods);
        
        String sourceCode = null;
        if (streamType.equals("java.io.OutputStream") && augmentedMethods.contains("getStreamImpl()")) {
             sourceCode = compilationUnit.toString().replaceAll("getStreamImpl", "getInputStreamImpl")
             .replaceAll(" wrap\\(", " wrapInputStream(");
        } else if (streamType.equals("java.io.InputStream") && augmentedMethods.contains("getStreamImpl()")) {
            sourceCode = compilationUnit.toString().replaceAll("getStreamImpl", "getOutputStreamImpl")
                    .replaceAll(" wrap\\(", " wrapOutputStream(");
        }

        return sourceCode;
    }
    
    public String parseWrapperClasses(File wrapperSource, String aspectUUID, String wrappedClassName, List<String> augmentedMethods) {
        
        StreamImplementationArchivist.initializeArchive(wrappedClassName);
        
        InitializerRemoverVisitor removerVisitor = new InitializerRemoverVisitor();
        MethodLineNumberVisitor lineNumberVisitor = new MethodLineNumberVisitor();
        AffectedMethodsVisitor affectedMethodsVisitor = new AffectedMethodsVisitor();
        
        try {
            CompilationUnit compilationUnit = JavaParser.parse(wrapperSource);
            compilationUnit.accept(affectedMethodsVisitor, null);
            augmentedMethods.addAll(affectedMethodsVisitor.affectedMethods);
            
            lineNumberVisitor.taskHelper = this;
            lineNumberVisitor.aspectUUID = aspectUUID;
            lineNumberVisitor.wrappedClassName = wrappedClassName;
            compilationUnit.accept(lineNumberVisitor, null);
            augmentedMethods.addAll(lineNumberVisitor.autoGeneratedMethods);

            compilationUnit.accept(removerVisitor, null);
            
            for (Node nodeToRemove : removerVisitor.nodesToRemove) {
                removerVisitor.removeInitializerBlock(nodeToRemove);
            }

            Files.write(wrapperSource.toPath(), Collections.singleton(compilationUnit.toString()), StandardCharsets.UTF_8);
            
            return compilationUnit.toString();
        } catch (Exception exc) {
            exc.printStackTrace();
            System.out.println("Error trying to parse java file, decompilation produced irreconcilable artifacts. ");
        }
        
        return null;
    }
    
    private boolean foundPreviousExecution(String filePath) {
        return !(new File(filePath).mkdirs());
    }
    
    
    public AspectAugmentationImpact constructAspectImpact(FunctionalAspectInstance aspectInstance, InterMethodDataflowNode node,
                                                          AspectAugmentationSpecification specification,
                                                          int lineNumber, ObjectToTriplesConfiguration config) throws Exception {
        
        String nodeUUID = config.getNamingContext().getNameForObject(node);
        AspectAugmentationImpact aspectAugmentationImpact = new AspectAugmentationImpact();
        
        aspectAugmentationImpact.setAspectInstance(aspectInstance);
        aspectAugmentationImpact.setSpecification(specification);
        aspectAugmentationImpact.setLineNumberToInject(lineNumber);
        aspectAugmentationImpact.setEdgesAffected(getEffectedEdges(nodeUUID, config));
        
        switch (specification) {
            
            case AUGMENT_ONE:
                getPw().println("Insert the following aspect: \n");
                getPw().println(aspectInstance.getMethodPointer());
                getPw().println("\nIn method " + node.getJavaMethodName() + " in class " + node.getJavaClassName() + " at line number " +
                        (node.getLineNumber()) + ".");
                break;
                
            case AUGMENT_ALL:
                // TODO
                break;
                
            default:
                break;
            
            
        }
        
        TriplesToPojo.SparqlPojoContext consumerResults = getObjectRepresentation(nodeUUID, DATAFLOW_NODE_TYPE, config);
        consumerResults.forEach(consumerResult -> aspectAugmentationImpact.setAugmentationNode((DataflowNode) consumerResult.get("obj")));

        return aspectAugmentationImpact;
    }
    
    public List<Solution> getDataflows(String graphName) {

        String getDataflows = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_analysis:  <http://darpa.mil/immortals/ontology/r2.0.0/analysis#>\n" +
                "prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "\n" +
                "select distinct ?dataFlowEdge where {\n" +
                "\t\n" +
                "    graph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                " \n" +
                "\t\t  {?dataFlowEdge a IMMoRTALS_analysis:InterMethodDataflowEdge .}\n" +
                "\t}\n" +
                "\t\n" +
                "}";
        getDataflows = getDataflows.replace("???GRAPH_NAME???", graphName);

        GradleTaskHelper.AssertableSolutionSet flowSolutions = new GradleTaskHelper.AssertableSolutionSet();
        client.executeSelectQuery(getDataflows, flowSolutions);
        
        return flowSolutions.getSolutions();
    }
    
    public ArrayList<FunctionalAspectInstance> getInstanceWithMultipleImpacts(ImpactStatement[] impactStatements, ObjectToTriplesConfiguration config) throws Exception {
        
        int size = impactStatements.length;
        ArrayList<FunctionalAspectInstance> instances = new ArrayList<>();
        
        ArrayList<ImpactStatement> impactStatementArrayList = new ArrayList<>(Arrays.asList(impactStatements));
        
        Set<Set<ImpactStatement>> sets = Sets.powerSet(ImmutableSet.copyOf(impactStatements));
        ArrayList<Set<ImpactStatement>> list = new ArrayList(sets);
        Collections.reverse(list);
        
        for (Set<ImpactStatement> statementSet : list) {
            if (statementSet.size() != 0) {
                String getAllAspects = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                        "\n" +
                        "select ?aspect where {\n" +
                        "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                        "\t\t?aspect IMMoRTALS:hasImpactStatements ???IMPACTS???\n" +
                        "\t}\n" +
                        "}";
                getAllAspects = getAllAspects.replace("???GRAPH_NAME???", this.getGraphName())
                        .replace("???IMPACTS???", generateImpacts(statementSet, config));
                
                AssertableSolutionSet aspectSolutions = new AssertableSolutionSet();
                client.executeSelectQuery(getAllAspects, aspectSolutions);
                
                if (aspectSolutions.getSolutions().size() != 0) {
                    
                    String aspectUUID = aspectSolutions.getSolutions().get(0).get("aspect");
                    String getAspectInstances = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                            "prefix IMMoRTALS_dfu_instance: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> \n" +
                            "\n" +
                            "select ?method ?aspectInstance where {\n" +
                            "\tgraph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                            "\t\t?aspectInstance a IMMoRTALS_dfu_instance:FunctionalAspectInstance\n" +
                            "\t\t; IMMoRTALS:hasAbstractAspect <???ASPECT???>\n" +
                            "\t\t; IMMoRTALS:hasMethodPointer ?method .\n" +
                            "\t}\n" +
                            "}";
                    getAspectInstances = getAspectInstances.replace("???GRAPH_NAME???", this.getGraphName())
                            .replace("???ASPECT???", aspectUUID);

                    AssertableSolutionSet instanceSolutions = new AssertableSolutionSet();
                    client.executeSelectQuery(getAspectInstances, instanceSolutions);
                    
                    for (Solution instanceSolution : instanceSolutions.getSolutions()) {
                        String instanceUUID = instanceSolution.get("aspectInstance");

                        TriplesToPojo.SparqlPojoContext instanceResults = getObjectRepresentation(instanceUUID, ASPECT_INSTANCE_TYPE,
                                config);
                        
                        instanceResults.forEach(instanceResult -> instances.add((FunctionalAspectInstance) instanceResult.get("obj")));
                    }
                    
                    if ((size - statementSet.size()) <= 0) {
                        return instances;
                    } else {
                        
                        impactStatementArrayList.removeAll(statementSet);
                        instances.addAll(getInstanceWithMultipleImpacts(impactStatementArrayList.toArray
                                (new ImpactStatement[impactStatementArrayList.size()]), config));
                        
                        return instances;
                    }
                }
            }
        }
        return new ArrayList<>();
    }
    
    public String generateImpacts(Set<ImpactStatement> impactStatements, ObjectToTriplesConfiguration config) {
        
        StringBuilder baseQuery = new StringBuilder(" ");
        
        for (ImpactStatement impactStatement : impactStatements) {
            
            if (impactStatement instanceof PropertyImpact) {
                PropertyImpact propertyImpact = (PropertyImpact) impactStatement;
                
                Model m = ObjectToTriples.convert(config, propertyImpact);
                String propertyURI = config.getNamingContext().getNameForObject(propertyImpact.getImpactedProperty());

                baseQuery.append(("[ IMMoRTALS:hasImpactOnProperty \"???IMPACT_TYPE???\"\n" +
                        "\t\t; IMMoRTALS:hasImpactedProperty <???PROPERTY???> ] ,\n").replace("???IMPACT_TYPE???", propertyImpact.getImpactOnProperty().name())
                        .replace("???PROPERTY???", propertyURI));
            }
        }
        
        baseQuery.replace(baseQuery.length() - 2, baseQuery.length() - 1, ".");
        
        return baseQuery.toString();
    }
    
    
    public boolean isAspectInstance(String nodeURI) {
        
        String isAspectInstanceQuery = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_dfu_instance: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#>\n" +
                "\n" +
                "ask {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t<???NODE???> IMMoRTALS:hasJavaMethodPointer ?pointer .\n" +
                "    ?aspectInstance a IMMoRTALS_dfu_instance:FunctionalAspectInstance\n" +
                "    ; IMMoRTALS:hasMethodPointer ?pointer .\n" +
                "\t}\n" +
                "}";
        isAspectInstanceQuery = isAspectInstanceQuery.replace("???GRAPH_NAME???", getGraphName()).replace("???NODE???", nodeURI);
        
        return getClient().executeAskQuery(isAspectInstanceQuery);
    }
    
    
    public FunctionalAspectInstance[] getInstancesFromImpactStatement(ImpactStatement impactStatement, ObjectToTriplesConfiguration config) throws Exception {
        
        ArrayList<FunctionalAspectInstance> functionalAspectInstances = new ArrayList<>();
        ImpactStatementType type = ImpactStatementType.getStatementType(impactStatement.getClass().getName());
        
        String getAllAspects = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "\n" +
                "select distinct ?aspect where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t?aspect IMMoRTALS:hasImpactStatements ?impact .\n" +
                "\t\t?impact IMMoRTALS:hasImpactOnProperty \"???IMPACT_TYPE???\"\n" +
                "\t\t; IMMoRTALS:hasImpactedProperty <???PROPERTY???> . \n" +
                "\t}\n" +
                "}";
        getAllAspects = getAllAspects.replace("???GRAPH_NAME???", this.getGraphName());
        AssertableSolutionSet aspectSolutions = new AssertableSolutionSet();

        String getAspectInstances = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_dfu_instance: <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#> \n" +
                "\n" +
                "select distinct ?method ?aspectInstance where {\n" +
                "\tgraph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t?aspectInstance a IMMoRTALS_dfu_instance:FunctionalAspectInstance\n" +
                "\t\t; IMMoRTALS:hasAbstractAspect <???ASPECT???>\n" +
                "\t\t; IMMoRTALS:hasMethodPointer ?method .\n" +
                "\t}\n" +
                "}";
        getAspectInstances = getAspectInstances.replace("???GRAPH_NAME???", this.getGraphName());
        AssertableSolutionSet instanceSolutions = new AssertableSolutionSet();

        assert type != null;
        switch (type) {
            case PROPERTY_IMPACT:
                PropertyImpact propertyImpact = (PropertyImpact) impactStatement;
                ObjectToTriples.convert(config, propertyImpact);
                String propertyURI = config.getNamingContext().getNameForObject(propertyImpact.getImpactedProperty());
                getAllAspects = getAllAspects.replace("???PROPERTY???", propertyURI);
                
                switch (propertyImpact.getImpactOnProperty()) {
                    case ADDS:
                        getAllAspects = getAllAspects.replace("???IMPACT_TYPE???", PropertyImpactType.ADDS.name());
                        getPw().println("Getting abstract aspects using query:\n\n" + getAllAspects + "\n\n");
                        getClient().executeSelectQuery(getAllAspects, aspectSolutions);
                        break;
                        
                    case REMOVES:
                        getAllAspects = getAllAspects.replace("???IMPACT_TYPE???", PropertyImpactType.REMOVES.name());
                        getPw().println("Getting abstract aspects using query:\n\n" + getAllAspects + "\n\n");
                        getClient().executeSelectQuery(getAllAspects, aspectSolutions);
                        break;
                    default:
                        break;
                }
                
                Set<String> instances = new HashSet<>();
                
                for (Solution aspectSolution : aspectSolutions.getSolutions()) {
                    String aspectUUID = aspectSolution.get("aspect");
                    getClient().executeSelectQuery(getAspectInstances.replace("???ASPECT???", aspectUUID), instanceSolutions);
                    getPw().println("Getting concrete aspect instances using query:\n\n" + getAspectInstances.replace(
                            "???ASPECT???", aspectUUID) + "\n\n");
                    for (Solution instanceSolution : instanceSolutions.getSolutions()) {
                        String instanceUUID = instanceSolution.get("aspectInstance");
                        if (instances.add(instanceUUID)) {
                            TriplesToPojo.SparqlPojoContext instanceResults = getObjectRepresentation(instanceUUID, ASPECT_INSTANCE_TYPE, config);
                            instanceResults.forEach(instanceResult -> functionalAspectInstances.add((FunctionalAspectInstance) instanceResult.get("obj")));
                        }
                    }
                }
                break;
            default:
                break;
        }
        
        return  functionalAspectInstances.toArray(new FunctionalAspectInstance[functionalAspectInstances.size()]);
    }
    
    public DataflowEdge[] getEffectedEdges(String nodeUUID, ObjectToTriplesConfiguration config) {
        
        String getAffectedEdges = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#>\n" +
                "prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n " +
                "\n" +
                "select distinct ?edge where {\n" +
                "\t\n" +
                "    graph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t{?edge a  IMMoRTALS_analysis:DataflowEdge}\n" +
                "\t\tUNION\n" +
                "\t\t{?edgeSubClass rdfs:subClassOf* IMMoRTALS_analysis:DataflowEdge .\n" +
                "         ?edge a ?edgeSubClass .}\n" +
                "\t\t \n" +
                "\t\t ?edge IMMoRTALS:hasConsumer <???NODE_UUID???> .\n" +
                "\t\t \n" +
                "\t}\n" +
                "}\n";
        getAffectedEdges = getAffectedEdges.replace("???GRAPH_NAME???", graphName).replace("???NODE_UUID???", nodeUUID);
        AssertableSolutionSet edgeSolutions = new AssertableSolutionSet();

        client.executeSelectQuery(getAffectedEdges, edgeSolutions);
        ArrayList<DataflowEdge> edges = new ArrayList<>();
        
        edgeSolutions.getSolutions().forEach(solution -> {
            
            String edgeUUID = solution.get("edge");

            try {
                TriplesToPojo.SparqlPojoContext edgePojos = getObjectRepresentation(edgeUUID, DATAFLOW_TYPE, config);
                edgePojos.forEach(edgePojo -> edges.add((DataflowEdge) edgePojo.get("obj")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        return edges.toArray(new DataflowEdge[edges.size()]);
    }
    
    public static void emitZipArchive(File destination, File source) throws IOException {

        ZipArchiveOutputStream archive = new ZipArchiveOutputStream(destination);
        
        Collection<File> zipFiles = FileUtils.listFiles(source, new String[] {"ttl"}, true);
        
        for (File zipFile : zipFiles) {
            String entryName = getEntryName(source, zipFile);
            ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
            archive.putArchiveEntry(entry);

            BufferedInputStream input = new BufferedInputStream(new FileInputStream(zipFile));
            IOUtils.copy(input, archive);
            input.close();
            archive.closeArchiveEntry();
        }
        
        archive.finish();
        archive.close();
    }

    private static String getEntryName(File source, File file) throws IOException {
        int index = source.getAbsolutePath().length() + 1;
        String path = file.getCanonicalPath();

        return path.substring(index);
    }

    public TriplesToPojo.SparqlPojoContext getObjectRepresentation(String objectUUID, String objectType, ObjectToTriplesConfiguration config) throws Exception {

        String getObject = "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT ?obj WHERE {\n" +
                "    GRAPH <http://localhost:3030/ds/data/???GRAPH_NAME???> { \n" +
                "        {?obj a <???OBJECT_TYPE???>}\n" +
                "  UNION {?tester rdfs:subClassOf* <???OBJECT_TYPE???> . ?obj a* ?tester .}\n" +
                "        FILTER(?obj = <???OBJECT_UUID???>)\n" +
                "        \n" +
                "    } .\n" +
                "} LIMIT 1";
        getObject = getObject.replace("???GRAPH_NAME???", graphName).replace("???OBJECT_TYPE???", objectType)
                .replace("???OBJECT_UUID???", objectUUID);

        return TriplesToPojo.sparqlSelect(getObject, graphName, client, config);//.getCleanContext(true)); //config);
    }

    public static boolean compareProperties(Object actualProperty, Object expectedProperty, PropertyMisMatch misMatch,
                                            PrintWriter pw) throws Exception {
        
        if (!actualProperty.getClass().getName().equals(expectedProperty.getClass().getName())) {
            return false;
        }
        
        List<Field> actualPropertyFields = Arrays.asList(actualProperty.getClass().getDeclaredFields());
        List<Field> expectedPropertyFields = Arrays.asList(expectedProperty.getClass().getDeclaredFields());

        if (actualPropertyFields.size() == expectedPropertyFields.size()) {

            for (Field expectedField : expectedPropertyFields) {
                expectedField.setAccessible(true);
                String expectedFieldName = expectedField.getName();
                boolean hasField = false;
                for (Field actualField : actualPropertyFields) {
                    actualField.setAccessible(true);
                    String actualFieldName = actualField.getName();

                    if (expectedFieldName.equals(actualFieldName)) {

                        if (!expectedField.get(expectedProperty).equals(actualField.get(actualProperty))) {
                            pw.println("Found property differs from property anticipated.");
                            pw.println("Expected property field, " + expectedFieldName + ", with value: " + expectedField.get(expectedProperty));
                            pw.println("Actual property field, " + actualFieldName + ", with value: " + actualField.get(actualProperty) + "\n\n");

                            misMatch.setProperty1FieldName(expectedFieldName);
                            misMatch.setProperty1FieldValue(expectedField.get(expectedProperty));
                            misMatch.setProperty2FieldName(actualFieldName);
                            misMatch.setProperty2FieldValue(actualField.get(actualProperty));

                            return false;
                        }
                        
                       /* // embedded properties
                        Class<?> expectedEmbeddedProperties = expectedField.getType();
                        Class<?> actualEmbeddedProperties = actualField.getType();
                        Object expectedObject = expectedEmbeddedProperties.getConstructor().newInstance();
                        Object actualObject = actualEmbeddedProperties.getConstructor().newInstance();
                        
                         if (!compareProperties(expectedObject, actualObject)) {
                             return false;
                         }*/

                        hasField = true;
                    }
                }
                if (!hasField) {
                    pw.println("Found property lacks at least one field anticipated by specified aspect.");
                    pw.println("Expected property field: " + expectedFieldName +"\n\n");
                    return false;
                }
            }


        } else if (actualPropertyFields.size() > expectedPropertyFields.size()) {
            pw.println("The property found has additional fields not anticipated by the specified aspect.");
            return false;
        } else {
            pw.println("The property found lacks fields anticipated by the specified aspect.");
            return false;
        }

        return true;
    }

    public static Model getKnowledge(Class<?> c){
        PojoMappingContext mappingContext =
                PojoMappingContext.acquireContext("r2.0.0");

        for(Object o:instantiate(c)){
            mappingContext.addToModel(o);
        }

        return mappingContext.getCurrentModel();
    }

    private static List<Object> instantiate(Class<?> wrapper){
        List<Object> objects = new ArrayList<>();

        if(wrapper.getAnnotation(ConceptInstance.class) != null){
            objects.add(instantiateInternal(wrapper));
        }

        for(Class<?> c:wrapper.getClasses()){
            if(c.getAnnotation(ConceptInstance.class) == null){
                continue;
            }
            if(c.getAnnotation(Ignore.class) != null){
                continue;
            }
            objects.add(instantiateInternal(c));
        }

        return objects;
    }

    private static Object instantiateInternal(Class<?> c){
        System.out.println("instantiating " + c.getName());
        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void mergeDataflowDataWithBytecodeStructures(FusekiClient client, String graphName) {
        String getMethodNodes = "prefix IMMoRTALS_analysis: <http://darpa.mil/immortals/ontology/r2.0.0/analysis#>\n" +
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "select ?interMethodNode where {\n" +
                "\t\n" +
                "    graph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                " \n" +
                "\t\t  {?interMethodNode a IMMoRTALS_analysis:DataflowNode .}\n" +
                "\t\t  UNION\n" +
                "\t\t  {?tester rdfs:subClassOf* IMMoRTALS_analysis:DataflowNode.\n" +
                "\t\t   ?interMethodNode a ?tester. }\n" +
                "\t\t  \n" +
                "\t}\n" +
                "\t\n" +
                "}\n";
        getMethodNodes = getMethodNodes.replace("???GRAPH_NAME???", graphName);

        GradleTaskHelper.AssertableSolutionSet methodNodeSolutions = new GradleTaskHelper.AssertableSolutionSet();
        client.executeSelectQuery(getMethodNodes, methodNodeSolutions);

        for (GradleTaskHelper.Solution methodNodeSolution : methodNodeSolutions.getSolutions()) {
            String methodNodeUUID = methodNodeSolution.get("interMethodNode");

            String getNodeNamesAndClasses = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                    "\n" +
                    "select ?methodName ?className where {\n" +
                    "\t\n" +
                    "    graph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                    " \n" +
                    "\t\t<???METHOD_NODE???> IMMoRTALS:hasJavaMethodName ?methodName\n" +
                    "\t\t; IMMoRTALS:hasJavaClassName ?className .\n" +
                    "\t\t  \n" +
                    "\t}\n" +
                    "\t\n" +
                    "}";
            getNodeNamesAndClasses = getNodeNamesAndClasses.replace("???GRAPH_NAME???", graphName).replace("???METHOD_NODE???", methodNodeUUID);

            GradleTaskHelper.AssertableSolutionSet nodeNameClassSolutions = new GradleTaskHelper.AssertableSolutionSet();
            client.executeSelectQuery(getNodeNamesAndClasses, nodeNameClassSolutions);

            Solution nodeNameClassSolution = nodeNameClassSolutions.getSolutions().get(nodeNameClassSolutions.getSolutions().size() - 1);
            String methodName = nodeNameClassSolution.get("methodName");
            String className = nodeNameClassSolution.get("className");

            String getMethodPointer = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                    "\n" +
                    "select ?methodPointer where {\n" +
                    "\t\n" +
                    "    graph <http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                    "\t\t  ?method IMMoRTALS:hasMethodName \"???METHOD_NAME???\"\n" +
                    "\t\t; IMMoRTALS:hasBytecodePointer ?methodPointer \n" +
                    "\t\t; IMMoRTALS:hasOwner ?class .\n" +
                    "\t\t ?class IMMoRTALS:hasClassName \"???CLASS_NAME???\" .\n" +
                    "\t\t  \n" +
                    "\t}\n" +
                    "\t\n" +
                    "}";
            getMethodPointer = getMethodPointer.replace("???GRAPH_NAME???", graphName)
                    .replace("???METHOD_NAME???", methodName).replace("???CLASS_NAME???", className);

            GradleTaskHelper.AssertableSolutionSet methodPointerSolutions = new GradleTaskHelper.AssertableSolutionSet();
            client.executeSelectQuery(getMethodPointer, methodPointerSolutions);

            Solution methodPointerSolution = methodPointerSolutions.getSolutions().get(0);

            String methodPointer = methodPointerSolution.get("methodPointer");

            String insertPointer = "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \n" +
                    "\t\n" +
                    "insert data {\n" +
                    "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                    "\t\t<???METHOD_NODE???> IMMoRTALS:hasJavaMethodPointer \"???METHOD_POINTER???\" .\n" +
                    "\t}\n" +
                    "}\n" +
                    "\t";
            insertPointer = insertPointer.replace("???GRAPH_NAME???", graphName)
                    .replace("???METHOD_NODE???", methodNodeUUID)
                    .replace("???METHOD_POINTER???", methodPointer);

            client.executeUpdate(insertPointer);

        }

    }

    public static ArrayList<String> splitMethodDesc(String desc) {
        int beginIndex = desc.indexOf('(');
        int endIndex = desc.lastIndexOf(')');
        if((beginIndex == -1 && endIndex != -1) || (beginIndex != -1 && endIndex == -1)) {
            System.err.println(beginIndex);
            System.err.println(endIndex);
            throw new RuntimeException();
        }
        String x0;
        if(beginIndex == -1 && endIndex == -1) {
            x0 = desc;
        }
        else {
            x0 = desc.substring(beginIndex + 1, endIndex);
        }
        Pattern pattern = Pattern.compile("\\[*L[^;]+;|\\[[ZBCSIFDJ]|[ZBCSIFDJ]");
        Matcher matcher = pattern.matcher(x0);

        ArrayList<String> listMatches = new ArrayList<String>();

        while(matcher.find())
        {
            listMatches.add(matcher.group());
        }

        return listMatches;
    }

    public static void recordCPElement(String serialMod, String path) throws IOException {
        File mod2Rdf = new File(path);
        mod2Rdf.getParentFile().mkdirs();
        mod2Rdf.createNewFile();

        FileWriter fileWriter = new FileWriter(mod2Rdf);
        fileWriter.write(serialMod);
        fileWriter.flush();
        fileWriter.close();
    }

    public static class Solution{
        private final List<TupleValue> tuples = new ArrayList<>();
        private final Map<String,String> map = new HashMap<>();

        public Solution(String...kvs){

            if(kvs.length %2 != 0){
                throw new RuntimeException(
                        "expected an even number of kvs but got " + kvs.length);
            }

            for(int i=0;i<kvs.length;i+=2){
                tuples.add(new TupleValue(kvs[i],kvs[i+1]));
            }

            index();
        }

        protected Solution(TupleValue...tuples){
            this.tuples.addAll(Arrays.asList(tuples));

            index();
        }

        private Solution(Collection<TupleValue> tuples){
            this.tuples.addAll(tuples);

            index();
        }

        public String get(final String key){
//            Assert.assertTrue(
//                "found null but expected to find value for key " + key + 
//                ", valid keys = " + map.keySet(),
//                map.containsKey(key)
//                );

            return map.get(key);
        }

        private void index(){
            for(TupleValue t:tuples){
                map.put(t.key, t.value);
            }
        }

        private boolean matches(Solution solution){

            for(TupleValue mustContainThisTuple:tuples){

                boolean foundMatch = false;
                for(TupleValue candidateTuple:solution.tuples){
                    if(mustContainThisTuple.key.equals(candidateTuple.key)){
                        final String mustContainValue = mustContainThisTuple.value;
                        final String actualValue = candidateTuple.value;

                        if(mustContainValue == null){
                            foundMatch = true;
                        } else if(mustContainValue.endsWith("*")){
                            foundMatch =
                                    actualValue.startsWith(
                                            mustContainValue.substring(
                                                    0,
                                                    mustContainValue.length()-1
                                            )
                                    );
                        } else {
                            foundMatch = mustContainValue.equals(actualValue);
                        }

                        if(foundMatch){
                            break;
                        }
                    }
                }

                if(!foundMatch){
                    return false;
                }
            }

            return true;
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();

            sb.append("solution {\n");
            for(TupleValue t:tuples){
                sb.append("\t").append(String.format("%-16s",t.key)).append(" : ").append(t.value).append("\n");
            }
            sb.append("}\n");

            return sb.toString();
        }
    }

    private static class TupleValue{
        private final String key;
        private final String value;

        protected TupleValue(String k, String v){
            this.key = k;
            this.value = v;
        }

        @Override
        public int hashCode(){
            return 17 + 13*(1337*key.hashCode() + value.hashCode());
        }

        @Override
        public boolean equals(Object o){
            if(!(o instanceof TupleValue)){
                return false;
            }

            TupleValue t = (TupleValue)o;

            if(!t.key.equals(this.key)){
                return false;
            }

            if(!t.value.equals(this.value)){
                return false;
            }

            return true;
        }
    }
    
    public static final String CONSTRAINT_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/property/impact#ProscriptiveCauseEffectAssertion";
    public static final String DATAFLOW_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/analysis#DataflowEdge";
    public static final String DATAFLOW_METHOD_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/analysis#MethodInvocationDataflowEdge";
    public static final String DATAFLOW_INTER_METHOD_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/analysis#InterMethodDataflowEdge";
    public static final String DATAFLOW_NODE_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/analysis#DataflowNode";
    public static final String MITIGATION_STRATEGY_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/property/impact#PrescriptiveCauseEffectAssertion";
    public static final String ASPECT_INSTANCE_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#FunctionalAspectInstance";
    public static final String ABSTRACT_PROPERTY_CRIT_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/property/impact#CriterionStatement";
    public static final String PROPERTY_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/property#Property";
    public static final String WRAPPER_SOURCE_FILE_TYPE = "http://darpa.mil/immortals/ontology/r2.0.0/lang#WrapperSourceFile";

}