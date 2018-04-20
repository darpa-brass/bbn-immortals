package Helper;



import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;

import java.io.File;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


/**
 * Created by root on 12/14/17.
 */
public class Instrumentation {

    public static MethodDeclaration intrumentationMethod(){
        MethodDeclaration m = new MethodDeclaration();
        ;
        m.setName("writeline");

        m.setModifier(Modifier.PUBLIC,false);
        ;
        NodeList<Parameter> parameters = new NodeList<>();
        Parameter p1 = new Parameter();
        Type t = new ClassOrInterfaceType("String");

        p1.setType(t);
        //VariableDeclarator v = new VariableDeclarator("fullFilePath");
        p1.setName("fullFilePath");
        Parameter p2 = new Parameter();
        Type t2 = new ClassOrInterfaceType("String");
        p2.setType(t2);
        //VariableDeclaratorId v2= new VariableDeclaratorId("text");
        p2.setName("text");

        parameters.add(p1);
        parameters.add(p2);

        m.setParameters(parameters);

        VoidType tReturnType = new VoidType();

        m.setType(tReturnType);

        BlockStmt b = instrumentationMethodBody();
        m.setBody(b);

        return m;
    }

    public static MethodDeclaration instrumentationMethodStatic(){
        MethodDeclaration m = intrumentationMethod();
        m.setName(m.getName().asString() + "Static");
        m.getModifiers().add(Modifier.STATIC);
        return m;
    }





    private static ExpressionStmt getFileInitialization(){

        ObjectCreationExpr objectCreationExpr = new ObjectCreationExpr();
        objectCreationExpr.setType(new ClassOrInterfaceType("File"));
        NameExpr nameExpr = new NameExpr("fullFilePath");
        NodeList<Expression> args = new NodeList<>();
        args.add(nameExpr);
        objectCreationExpr.setArguments(args);

        ExpressionStmt expressionStmt = new ExpressionStmt();

        ClassOrInterfaceType clst = new ClassOrInterfaceType();

        clst.setName("File");
        VariableDeclarator vrt = new VariableDeclarator(clst,"file");
        vrt.setInitializer(objectCreationExpr);
        Expression expr = new VariableDeclarationExpr(vrt);
        expressionStmt.setExpression(expr);


        return  expressionStmt;

    }


    private static ExpressionStmt getOutputInitialization(){

        ObjectCreationExpr objectCreationExpr = new ObjectCreationExpr();
        objectCreationExpr.setType(new ClassOrInterfaceType("BufferedWriter"));
        NameExpr nameExpr = new NameExpr("fileWriter");
        NodeList<Expression> args = new NodeList<>();
        args.add(nameExpr);
        objectCreationExpr.setArguments(args);
        ClassOrInterfaceType clst = new ClassOrInterfaceType();
        clst.setName("BufferedWriter");
        VariableDeclarator vrt = new VariableDeclarator(clst,"output");
        vrt.setInitializer(objectCreationExpr);
        ExpressionStmt expressionStmt = new ExpressionStmt();
        VariableDeclarationExpr expr = new VariableDeclarationExpr(vrt);

        expressionStmt.setExpression(expr);


        return  expressionStmt;

    }

    private static ExpressionStmt getFileWriteInitialization(){

        ObjectCreationExpr objectCreationExpr = new ObjectCreationExpr();
        objectCreationExpr.setType(new ClassOrInterfaceType("FileWriter"));
        NameExpr nameExpr = new NameExpr("file");
        BooleanLiteralExpr trueExpr = new BooleanLiteralExpr(true);
        NodeList<Expression> args = new NodeList<>();
        args.add(nameExpr);
        args.add(trueExpr);
        objectCreationExpr.setArguments(args);

        ExpressionStmt expressionStmt = new ExpressionStmt();
        ClassOrInterfaceType clst = new ClassOrInterfaceType();
        clst.setName("FileWriter");
        VariableDeclarator vrt = new VariableDeclarator(clst,"fileWriter");
        vrt.setInitializer(objectCreationExpr);
        VariableDeclarationExpr expr = new VariableDeclarationExpr(vrt);

        expressionStmt.setExpression(expr);


        return  expressionStmt;

    }

    public static ExpressionStmt outputAppendText(){
        NameExpr nameExpr = new NameExpr("output");
        NameExpr args1 = new NameExpr("text");
        NodeList<Expression> args = new NodeList<>();
        args.add((Expression)args1);
        MethodCallExpr methodCallExpr = new MethodCallExpr();
        methodCallExpr.setName("append");
        methodCallExpr.setScope(nameExpr);
        methodCallExpr.setArguments(args);

        ExpressionStmt expressionStmt = new ExpressionStmt();
        expressionStmt.setExpression(methodCallExpr);
        return expressionStmt;



    }

    public static ExpressionStmt outputNewLine(){
        NameExpr nameExpr = new NameExpr("output");


        MethodCallExpr methodCallExpr = new MethodCallExpr();
        methodCallExpr.setName("newLine");
        methodCallExpr.setScope(nameExpr);


        ExpressionStmt expressionStmt = new ExpressionStmt();
        expressionStmt.setExpression(methodCallExpr);
        return expressionStmt;
    }

    public static ExpressionStmt ePrintStackTrace(){
        NameExpr nameExpr = new NameExpr("e");

        MethodCallExpr methodCallExpr = new MethodCallExpr();
        methodCallExpr.setName("printStackTrace");
        methodCallExpr.setScope(nameExpr);


        ExpressionStmt expressionStmt = new ExpressionStmt();
        expressionStmt.setExpression(methodCallExpr);
        return expressionStmt;
    }

    public static ExpressionStmt outputClose(){
        NameExpr nameExpr = new NameExpr("output");

        MethodCallExpr methodCallExpr = new MethodCallExpr();
        methodCallExpr.setName("close");
        methodCallExpr.setScope(nameExpr);


        ExpressionStmt expressionStmt = new ExpressionStmt();
        expressionStmt.setExpression(methodCallExpr);
        return expressionStmt;
    }


    public static BlockStmt catchBlock(){
        BlockStmt blockStmt = new BlockStmt();
        NodeList<Statement> statementList = new NodeList<>();
        statementList.add(ePrintStackTrace());
        blockStmt.setStatements(statementList);
        return blockStmt;
    }

    public static BlockStmt finallyBlock(){
        BlockStmt blockStmt = new BlockStmt();

        // if condition
        BinaryExpr binOpExpr = new BinaryExpr();
        binOpExpr.setOperator(BinaryExpr.Operator.NOT_EQUALS);
        binOpExpr.setLeft((Expression)(new NameExpr("output")));
        binOpExpr.setRight((Expression) new NullLiteralExpr());

        // then block
        BlockStmt thenBlock = new BlockStmt();
        List<Statement> statementList = new LinkedList<>();

        // internal try catch
        TryStmt tryStmt = new TryStmt();
        BlockStmt tryBlock = new BlockStmt();
        NodeList<Statement> tryBlockStatements = new NodeList<>();
        tryBlockStatements.add(outputClose());
        tryBlock.setStatements(tryBlockStatements);
        tryStmt.setTryBlock(tryBlock);
        CatchClause catchClause = new CatchClause();
        ClassOrInterfaceType c = new ClassOrInterfaceType("IOException");
        Parameter p =  new Parameter();
        p.setType(c);
        p.setName("e");
        catchClause.setParameter(p);
        BlockStmt catchInternalBlock = new BlockStmt();
        NodeList<Statement> list = new NodeList<>();
        list.add(ePrintStackTrace());
        catchInternalBlock.setStatements(list);
        catchClause.setBody(catchInternalBlock);
        tryStmt.setCatchClauses((new NodeList<>()));
        tryStmt.getCatchClauses().add(catchClause);

        thenBlock.setStatements(new NodeList<>());
        thenBlock.getStatements().add(tryStmt);

        IfStmt ifStmt = new IfStmt();
        ifStmt.setCondition(binOpExpr);
        ifStmt.setThenStmt(thenBlock);


        blockStmt.setStatements(new NodeList<>());
        blockStmt.getStatements().add(ifStmt);






        // else block is null



        return blockStmt;

    }


    public static BlockStmt instrumentationMethodBody(){
        BlockStmt blockStmt = new BlockStmt();
        NodeList<Statement> statementList = new NodeList<>();

        blockStmt.setStatements(statementList);
        //blockStmt.getStmts().add(getBufferWriterDeclarationStmt());
        blockStmt.getStatements().add(getFileInitialization());
        blockStmt.getStatements().add(getFileWriteInitialization());
        blockStmt.getStatements().add(getOutputInitialization());
        blockStmt.getStatements().add(outputAppendText());
        blockStmt.getStatements().add(outputNewLine());
        blockStmt.getStatements().add(outputClose());

        TryStmt tryStmt = new TryStmt();
        tryStmt.setTryBlock(blockStmt);;

        CatchClause catchClause = new CatchClause();
        Parameter parameter = new Parameter();
        ClassOrInterfaceType c = new ClassOrInterfaceType("IOException");
        parameter.setType(c);
        parameter.setName("e");

        catchClause.setParameter(parameter);
        catchClause.setBody(catchBlock());
        NodeList<CatchClause> catchClauseList = new NodeList<>();
        catchClauseList.add(catchClause);
        tryStmt.setCatchClauses(catchClauseList);
        //BlockStmt blockStmt1 = finallyBlock();
        //tryStmt.setFinallyBlock(blockStmt1);

        //statementList.add(tryStmt);
        //blockStmt.setStmts(statementList);
        //blockStmt.getStmts().add(0,tryStmt);
        //blockStmt.getStmts().add(tryStmt);



        NodeList<Statement> methodStmts = new NodeList<>();
        methodStmts.add(tryStmt);
        BlockStmt methodBlock = new BlockStmt();
        methodBlock.setStatements(methodStmts);


        return  methodBlock;

    }

    public static void main(String[] args){
        MethodDeclaration m = intrumentationMethod();
        m.setName("writelineStatic");
        m.getModifiers().add(Modifier.STATIC);

    }
}

