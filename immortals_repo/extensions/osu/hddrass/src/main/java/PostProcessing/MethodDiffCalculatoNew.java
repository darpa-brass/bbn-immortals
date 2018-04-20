package PostProcessing;

import ASTManipulation.*;
import Coverage.BlockUnMarker;
import Helper.Debugger;
import Helper.Globals;
import com.github.javaparser.JavaParser;
import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import PostProcessing.StatInfo;


/**
 * Created by root on 1/4/17.
 */
public class MethodDiffCalculatoNew {

    ClassMethodLineManipulator clmOriginal,clmReduced,clmAnnotated;
    TreeManipulator treeOriginal,treeReduced,treeAnnotated;
    String methodName;

    public MethodDiffCalculatoNew(String fileOriginal, String fileReduced, String fileAnnotated,String methodName){
        clmOriginal = new ClassMethodLineManipulator(CreateCompilationUnit(fileOriginal),methodName);
        clmReduced = new ClassMethodLineManipulator(CreateCompilationUnit(fileReduced),methodName);
        clmAnnotated = new ClassMethodLineManipulator(CreateCompilationUnit(fileAnnotated),methodName);
        treeOriginal = new TreeManipulator(clmOriginal._cu,methodName);
        treeReduced = new TreeManipulator(clmReduced._cu,methodName);
        this.methodName = methodName;
    }

    private CompilationUnit CreateCompilationUnit(String javaFile){
        FileInputStream in = null;
        CompilationUnit cu = null;
        String fullPath = javaFile;
        try {
            String current = new java.io.File(".").getCanonicalPath();
            Debugger.log("Current dir:"+current);
        }catch(Exception ex2){

        }

        try {
            in = new FileInputStream(fullPath);
        }
        catch(FileNotFoundException fex){
            Debugger.log("file not found");

        }

        try {
            // parse the file
            cu = JavaParser.parse(in);



        }

        catch(Exception ex){
            Debugger.log(ex);
        }
        finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // prints the resulting compilation unit to default system output

        return cu;

    }

    public StatInfo Calculate(){
        int highestDepthLevelOriginal = treeOriginal.GetHeigestDepthLevel();
        int highestDepthLevelReduced = treeReduced.GetHeigestDepthLevel();
        PostProcessing.StatInfo statInfo = new PostProcessing.StatInfo();
        List<Statement> lastPoolOfOriginalStatement = new ArrayList<>();
        List<Statement> lastPoolOfReducedStatement = new ArrayList<>();
        List<ExtendedStatement> originalStatementExtended = new ArrayList<ExtendedStatement>();
        List<ExtendedStatement> lastpoolOfOriginalStatementExtended = new ArrayList<ExtendedStatement>();
        List<ExtendedStatement> reducedStatementExtended = new ArrayList<>();
        int diffBetweenHighestLevels = highestDepthLevelOriginal - highestDepthLevelReduced;
        if(highestDepthLevelOriginal > highestDepthLevelReduced){
            statInfo.highestlevelFromLeafNode = highestDepthLevelOriginal - highestDepthLevelReduced;
            for(int i = highestDepthLevelOriginal;i>highestDepthLevelReduced;i--){
                List<Statement> statements = clmOriginal.GetNthLevelStatementsFromMethodName(this.methodName,i);
                statInfo.numberOfNodesReduced += statements.size();
                if(i == highestDepthLevelReduced + 1){
                    lastPoolOfOriginalStatement.addAll(statements);
                    lastpoolOfOriginalStatementExtended.addAll(lastPoolOfOriginalStatement.stream().map(x -> new ExtendedStatement(x,0)).collect(Collectors.toList()));

                }

            }

        }



        for(int i = highestDepthLevelReduced; i> 0;i--){
            if(methodName.equals("getSeriesIndex")){
                Debugger.log("here");
            }
            List<Statement> currentPoolOfOriginalStatement = clmOriginal.GetNthLevelStatementsFromMethodName(this.methodName,i);
            List<Statement> currentPoolOfReducedStatement = clmReduced.GetNthLevelStatementsFromMethodName(this.methodName,i);
            //List<Statement> diffStatement = clmOriginal.GetNthLevelStatementsFromMethodName(this.methodName,i);
            List<Statement> diffStatement2 = new ArrayList<>();
            List<Statement> annotatedStatement = clmAnnotated.GetNthLevelStatementsFromMethodName(this.methodName,i);

            if(currentPoolOfOriginalStatement.size() != currentPoolOfReducedStatement.size()){
                Diff diff = new Diff();
                diffStatement2 = diff.calculate(currentPoolOfOriginalStatement,currentPoolOfReducedStatement);
            }

            Debugger.log(diffStatement2);
            originalStatementExtended.clear();
            originalStatementExtended.addAll(currentPoolOfOriginalStatement.stream().map(x -> new ExtendedStatement(x,0)).collect(Collectors.toList()));
            if(currentPoolOfOriginalStatement.size() != currentPoolOfReducedStatement.size()){
                statInfo.numberOfNodesReduced += Math.max(currentPoolOfOriginalStatement.size() - currentPoolOfReducedStatement.size(),0);
                statInfo.statementList.addAll(diffStatement2);
                statInfo.statementListIDS.addAll(annotatedDiffIDs(diffStatement2,annotatedStatement));
//                for (int j = 0; j < currentPoolOfOriginalStatement.size(); j++) {
//                    Statement statement = currentPoolOfOriginalStatement.get(j);
//                    boolean exist = false;
//                    for (int k = 0; k < currentPoolOfReducedStatement.size(); k++) {
//                        Statement statement1 = currentPoolOfReducedStatement.get(k);
//                        if (statement.toString().equals(statement1.toString())) {
//                            exist = true;
//                            break;
//
//                        }
//                    }
//                    if (!exist) {
//                        statInfo.statementList.add(statement);
//                    }
//                }
                originalStatementExtended.forEach(x -> {
                    for (ExtendedStatement e: lastpoolOfOriginalStatementExtended) {
                        if(treeOriginal.ContainsStatement(x.statement,e.statement))
                            x.level = e.level + 1;
                    }



                });

            }
            else{
                if(i != highestDepthLevelReduced){
                    originalStatementExtended.forEach(x -> {
                        for (ExtendedStatement e: lastpoolOfOriginalStatementExtended) {
                            if(treeOriginal.ContainsStatement(x.statement,e.statement))
                                x.level = e.level + 1;
                        }

                    });

                }

            }

            lastpoolOfOriginalStatementExtended.clear();
            lastpoolOfOriginalStatementExtended.addAll(diffStatement2.stream().map(x -> new ExtendedStatement(x,0)).collect(Collectors.toList()));

        }


        if(highestDepthLevelReduced == 0){
            List<Statement> diffStatement = new ArrayList<Statement>();
            for(int i = highestDepthLevelOriginal;i>0;i--){
                diffStatement.addAll(clmOriginal.GetNthLevelStatementsFromMethodName(this.methodName,i));
            }
            statInfo.statementList = diffStatement;

        }
        Debugger.log(lastpoolOfOriginalStatementExtended);
        Debugger.log(originalStatementExtended);
        if(!(originalStatementExtended.size() == 0)){
            statInfo.highestlevelFromLeafNode += originalStatementExtended.stream().mapToInt(x -> x.level).max().getAsInt() ;
        }
        if(statInfo.numberOfNodesReduced > 0){
            if(statInfo.highestlevelFromLeafNode  == 0){
                statInfo.highestlevelFromLeafNode = 1;
            }
        }


        return statInfo;
    }

    private Set<String> annotatedDiffIDs(List<Statement> diffStatements, List<Statement> annotatedStatements){
        Set<String> diffIDs = new HashSet<>();
        String lastAnnotation = Globals.EmptyString;
        for(Statement annotated : annotatedStatements){
            if(isAnnotationStatement(annotated)){
                lastAnnotation = getUUID(annotated);
                continue;
            }
            for (Statement normal: diffStatements
                    ) {


                if(normal.toString().equals(annotated.toString())){
                    if(!lastAnnotation.isEmpty())
                        diffIDs.add(lastAnnotation);
                    lastAnnotation = Globals.EmptyString;
                }
                BlockUnMarker marker1 = new BlockUnMarker();
                BlockUnMarker marker2 = new BlockUnMarker();
                marker1.unmark(annotated);

                if(normal.toString().equals(annotated.toString())){
                    if(!lastAnnotation.isEmpty())
                        diffIDs.add(lastAnnotation);
                    lastAnnotation = Globals.EmptyString;
                }



            }

        }
        return  diffIDs;
    }

    private boolean isAnnotationStatement(Statement s){
        if(!StatementTypeQuery.isExprStmt(s)){
            return false;
        }
        ExpressionStmt expressionStmt = (ExpressionStmt)s;
        if(!ExpressionTypeQuery.isMethodCall(expressionStmt.getExpression()))
            return false;

        MethodCallExpr methodCallExpr = (MethodCallExpr) expressionStmt.getExpression();
        if(!methodCallExpr.getName().asString().equals("writeline"))
            return false;
        if(methodCallExpr.getArguments().size() != 2)
            return false;
        Expression e = (Expression)(methodCallExpr.getArguments().get(1));
        if(!ExpressionTypeQuery.isStringLiteralExpression(e))
            return false;


        try {
            UUID.fromString(((StringLiteralExpr)e).getValue());
            return true;
        } catch (Exception ex) {
            return false;
        }


    }

    private String getUUID(Statement s){
        if(!StatementTypeQuery.isExprStmt(s)){
            return Globals.EmptyString;
        }
        ExpressionStmt expressionStmt = (ExpressionStmt)s;
        if(!ExpressionTypeQuery.isMethodCall(expressionStmt.getExpression()))
            return Globals.EmptyString;

        MethodCallExpr methodCallExpr = (MethodCallExpr) expressionStmt.getExpression();
        if(!methodCallExpr.getName().asString().equals("writeline"))
            return Globals.EmptyString;
        if(methodCallExpr.getArguments().size() != 2)
            return Globals.EmptyString;
        Expression e = (Expression)(methodCallExpr.getArguments().get(1));
        if(!ExpressionTypeQuery.isStringLiteralExpression(e))
            return Globals.EmptyString;


        try {
            UUID.fromString(((StringLiteralExpr)e).getValue());
            return ((StringLiteralExpr)e).getValue();
        } catch (Exception ex) {
            return Globals.EmptyString;
        }


    }

}
