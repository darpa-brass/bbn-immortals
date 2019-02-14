package com.securboration.immortals.aspectj;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.securboration.immortals.aframes.AnalysisFrameAssessment;
import com.securboration.immortals.constraint.ConstraintAssessment;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.ontology.analysis.InterMethodDataflowEdge;
import com.securboration.immortals.ontology.analysis.InterMethodDataflowNode;
import com.securboration.immortals.ontology.dfu.instance.DfuInstance;
import com.securboration.immortals.ontology.dfu.instance.FunctionalAspectInstance;
import com.securboration.immortals.ontology.functionality.aspects.AspectConfigureSolution;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.functionality.datatype.Text;
import com.securboration.immortals.utility.GradleTaskHelper;
import org.apache.jena.atlas.lib.Pair;

import java.util.*;

import static com.securboration.immortals.aframes.AnalysisFrameAssessment.generateMagicString;

public class AspectJConstructor {
    
    public static void constructAspectFile(GradleTaskHelper taskHelper, ObjectToTriplesConfiguration config, FunctionalAspectInstance aspectInstance,
                                           AspectConfigureSolution solution, Set<InterMethodDataflowEdge> effectedEdges) throws ClassNotFoundException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        
        //TODO need a better mechanism for constructing both the pointcut methods and their various annotation requirements. More than likely
        //TODO a need for helper POJOs is present, primarily due to the amount of data being passed around
        Map<DfuInstance, Pair<String, String>> dfuInstanceStringMap = AnalysisFrameAssessment.getDfuInstancePairMap(taskHelper, config, aspectInstance);
        
        String cipherImpl = null;
        String magicString = null;
        if (solution != null) {
            DfuInstance chosenInstance = solution.getChosenInstance();
            Pair<String, String> dfuUUIDToCipherImpl = null;
            for (DfuInstance dfuInstance : dfuInstanceStringMap.keySet()) {
                if (chosenInstance.getClassPointer().equals(dfuInstance.getClassPointer())) {
                    dfuUUIDToCipherImpl = dfuInstanceStringMap.get(dfuInstance);
                }
            }

            String getUsageParadigmMagicString = AnalysisFrameAssessment.getImplementationSpecificStrings(taskHelper,
                    dfuUUIDToCipherImpl.getLeft());
            GradleTaskHelper.AssertableSolutionSet usageParadigmSolutions = new GradleTaskHelper.AssertableSolutionSet();
            taskHelper.getClient().executeSelectQuery(getUsageParadigmMagicString, usageParadigmSolutions);

            magicString = usageParadigmSolutions.getSolutions().get(0).get("magicString");
            magicString = generateMagicString(taskHelper, magicString, solution, usageParadigmSolutions);
            cipherImpl = dfuUUIDToCipherImpl.getRight();
        }
        
        Map<String, Set<InterMethodDataflowEdge>> classNameToMethodSigs = getEffectedMethods(taskHelper, effectedEdges);

        CompilationUnit newAspect = JavaParser.parse("");
        List<Pair<MethodDeclaration, String>> pointcutMethods = new ArrayList<>();

        Optional<ClassOrInterfaceDeclaration> newAspectClassOption = newAspect.getClassByName(BASE_ASPECT_NAME);
        
        if (newAspectClassOption.isPresent()) {
            
            ClassOrInterfaceDeclaration newAspectClass = newAspectClassOption.get();
            
            String aspectUUID = generateAspectUUID();
            newAspectClass.setName(BASE_ASPECT_NAME + aspectUUID);
            
            Type cipherImplType = JavaParser.parseType(cipherImpl);
            newAspectClass.addField(cipherImplType, "cipherImplField", Modifier.PUBLIC);
            
            for (String effectedClass : classNameToMethodSigs.keySet()) {
                
                Set<InterMethodDataflowEdge> effectedEdgesSet = classNameToMethodSigs.get(effectedClass);
                for (InterMethodDataflowEdge effectedEdge : effectedEdgesSet) {
                    
                    InterMethodDataflowNode effectedNode = (InterMethodDataflowNode) effectedEdge.getProducer();
                    String methodSig = null;//ConstraintAssessment.getMethodSignatureFromPointer(effectedNode.getJavaMethodPointer());
                    
                    MethodDeclaration newPointcut = newAspectClass.addMethod(extractNameFromSig(methodSig) + "Pointcut", Modifier.PROTECTED);
                    NodeList<Parameter> pointcutParameters = getCutPointParameters(methodSig);
                    newPointcut.setParameters(pointcutParameters);
                    String pointcutExpression = getPointcutExpression(extractNameFromSig(methodSig), pointcutParameters,effectedEdge.getDataTypeCommunicated().newInstance());
                    newPointcut.addSingleMemberAnnotation("Pointcut", pointcutExpression);
                    Pair<MethodDeclaration, String> methodExpressionPair = new Pair<>(newPointcut, pointcutExpression);
                    pointcutMethods.add(methodExpressionPair);
                }
            }
            
            for (Pair<MethodDeclaration, String> pointcutMethodToExpression : pointcutMethods) {
                
                MethodDeclaration pointcutMethod = pointcutMethodToExpression.getLeft();
                
                MethodDeclaration newAdvice = newAspectClass.addMethod(pointcutMethod.getNameAsString() + "Advice", Modifier.PUBLIC);
                newAdvice.setType(JavaParser.parseType("java.lang.Object"));
                NodeList<Parameter> adviceParameters = getAdviceParameters(pointcutMethodToExpression.getRight());
                newAdvice.setParameters(adviceParameters);
                newAdvice.addSingleMemberAnnotation("Around", getAdviceExpression());
                newAdvice.setBody(getAdviceBody());
            }
        }
    }

    private static BlockStmt getAdviceBody() {
        //TODO
        return null;
    }

    private static String getAdviceExpression() {
        //TODO
        return null;
    }

    private static NodeList<Parameter> getAdviceParameters(String pointcutExpression) {
        
        NodeList<Parameter> adviceParameters = new NodeList<>();
        Parameter joinPointParameter = new Parameter(JavaParser.parseType("org.aspectj.lang.ProceedingJoinPoint"), "jp");
        adviceParameters.add(joinPointParameter);
        
        pointcutExpression = pointcutExpression.substring(0, pointcutExpression.indexOf("&&"));
        pointcutExpression = pointcutExpression.substring(pointcutExpression.indexOf("(") + 1, pointcutExpression.lastIndexOf(")"));
        
        String[] pointcutParams = pointcutExpression.split(",");
        for (String pointcutParam : pointcutParams) {
            if (!pointcutParam.equals("*")) {
                
            }
        }
        
        
        //TODO
        return null;
    }

    private static String generateAspectUUID() {
        return UUID.randomUUID().toString();
    }

    private static String getPointcutExpression(String methodName, NodeList<Parameter> parameters, DataType dataType) {
        
        StringBuilder cutpointExpression = new StringBuilder();
        cutpointExpression.append("call( * ");
        cutpointExpression.append(methodName);
        cutpointExpression.append("(..)) && args(");
        
        String typeToMatch = "";
        
        if (dataType instanceof Text) {
            typeToMatch = "string";
        } else if (dataType instanceof BinaryData) {
            typeToMatch = "byte";
        }
        
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            if (parameter.getType().asString().contains(typeToMatch)) {
                cutpointExpression.append(typeToMatch);
            } else {
                cutpointExpression.append("*");
            }
            if (i != parameters.size() - 1) {
                cutpointExpression.append(", ");
            }
        }
        cutpointExpression.append(")");
        
        return cutpointExpression.toString();
    }

    //TODO need to formulate a robust system capable of parsing the parameters of the effected methods and generating the parameters of the new cutpoint
    private static NodeList<Parameter> getCutPointParameters(String effectedMethodParams) {
        
        NodeList<Parameter> pointCutParams = new NodeList<>();
        String paramString = effectedMethodParams.substring(effectedMethodParams.indexOf("(") + 1, effectedMethodParams.lastIndexOf(")"));
        String[] params = paramString.split(",");
        
        for (String param : params) {
            if (param.contains("String")) {
                pointCutParams.add(new Parameter(JavaParser.parseType("java.lang.String"), "string"));
            }
        }
        
        return pointCutParams;
    }

    private static String extractNameFromSig(String methodSig) {
        return methodSig.substring(methodSig.lastIndexOf("methods/") + 9, methodSig.indexOf("("));
    }

    private static String BASE_ASPECT_NAME = "com.securboration.aspectj.DynamicAspect";

    private static Map<String, Set<InterMethodDataflowEdge>> getEffectedMethods(GradleTaskHelper taskHelper, Set<InterMethodDataflowEdge> effectedEdges) {
        
        Map<String, Set<InterMethodDataflowEdge>> classNameToMethods = new HashMap<>();
        
        for (InterMethodDataflowEdge effectedEdge : effectedEdges) {
            InterMethodDataflowNode effectedNode = (InterMethodDataflowNode) effectedEdge.getProducer();
            String className = getClassNameOfMethodInvoke(taskHelper, AnalysisFrameAssessment.getInterMethodNodeUUID(taskHelper, effectedNode));
            if (classNameToMethods.get(className) != null) {
                classNameToMethods.get(className).add(effectedEdge);
            } else {
                Set<InterMethodDataflowEdge> newClassMethods = new HashSet<>();
                newClassMethods.add(effectedEdge);
                classNameToMethods.put(className, newClassMethods);
            }
        }
        return classNameToMethods;
    }

    private static String getClassNameOfMethodInvoke(GradleTaskHelper taskHelper, String interMethodNodeUUID) {
        
        String getClassNameOfMethodInvoke = "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\n" +
                "select ?className where {\n" +
                "\tgraph<http://localhost:3030/ds/data/???GRAPH_NAME???> {\n" +
                "\t\t?class IMMoRTALS:hasClassName ?className\n" +
                "\t\t; IMMoRTALS:hasMethods ?methods .\n" +
                "\t\t\n" +
                "\t\t?methods IMMoRTALS:hasInterestingInstructions ?ii .\n" +
                "\t\t\n" +
                "\t\t?ii IMMoRTALS:hasSemanticLink <???NODE_UUID???> .\n" +
                "\t}\n" +
                "}";
        getClassNameOfMethodInvoke = getClassNameOfMethodInvoke.replace("???GRAPH_NAME???", taskHelper.getGraphName()).replace(
                "???NODE_UUID???", interMethodNodeUUID);
        GradleTaskHelper.AssertableSolutionSet classNameSolutions = new GradleTaskHelper.AssertableSolutionSet();
        taskHelper.getClient().executeSelectQuery(getClassNameOfMethodInvoke, classNameSolutions);
        
        String className = null;
        if (!classNameSolutions.getSolutions().isEmpty()) {
            className = classNameSolutions.getSolutions().get(0).get("className");
        }
        
        return className;
    }
}
