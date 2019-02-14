package Coverage;

import ASTManipulation.EmptyStatementChecker;
import ASTManipulation.StatementType;
import ASTManipulation.StatementTypeQuery;
import Helper.Debugger;
import Helper.Globals;
import com.github.javaparser.ast.NodeList;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by root on 12/6/17.
 *
 * IfStmt - done,
 WhileStmt  - done,
 ForStmt - done,
 ExprStmt - done,
 ReturnStmt - done,
 SwitchStmt  - done,
 SwitchEntryStmt - done,
 BreakStmt - done,
 ForeachStmt - done,
 BlockStmt - done,
 ThrowStmt - done,
 TryStmt - done,
 ContnueStmt,
 SynchronizedStmt - done,
 Unknown
 */
public class BlockMarker {



    BlockStmt blockStmtOriginal;
    BlockStmt blockStmtMarked;
    String outputFileName;
    public boolean isStaticMarker;

    public BlockMarker(BlockStmt blockStmt){
        this.blockStmtOriginal = blockStmt;
    }

    public BlockStmt mark(){

        /*List<Statement> insideStatements = blockStmtOriginal.getStmts();
        for (Statement s : insideStatements
             ) {
            if(StatementTypeQuery.isBlockStmt(s)){
                BlockMarker blkMakerInside = new BlockMarker((BlockStmt)s);
                blkMakerInside.mark();
            }
            if(StatementTypeQuery.isIfElseStmt(s)){
                mark((IfStmt) s);
            }

        }

        ExpressionStmt exMarkerStmt = getExpressionStmt();

        blockStmtOriginal.getStmts().add(exMarkerStmt);
        Debugger.log(blockStmtOriginal.toString());
        return blockStmtOriginal;*/
        return mark(blockStmtOriginal);
    }

    public void mark(Statement s){
        if(StatementTypeQuery.isBlockStmt(s)){
            mark((BlockStmt)s);
            return;
        }
        if(StatementTypeQuery.isIfElseStmt(s)){
            mark((IfStmt)s);
            return;
        }
        if(StatementTypeQuery.isSForStmt(s)){
            mark((ForStmt)s);
            return;
        }
        if(StatementTypeQuery.isForEachStmt(s)){
            mark((ForeachStmt)s);
            return;
        }
        if(StatementTypeQuery.isTryStmt(s)){
            mark((TryStmt)s);
            return;
        }
        if(StatementTypeQuery.isSwitchStmt(s)){
            mark((SwitchStmt)s);
            return;
        }
        if(StatementTypeQuery.isThrowStmt(s)){
            mark((ThrowStmt)s);
            return;
        }
        if(StatementTypeQuery.isSwitchEntryStmt(s)){
            mark((SwitchEntryStmt)s);
            return;
        }
        if(StatementTypeQuery.isExprStmt(s)){
            mark((ExpressionStmt)s);
            return;
        }
        if(StatementTypeQuery.isReturnStmt(s)){
            mark((ReturnStmt)s);
            return;
        }
        if(StatementTypeQuery.isBreakStmt(s)){

            mark((BreakStmt)s);
            return;
        }

    }

    public BlockStmt mark(BlockStmt blockStmt){
        NodeList<Statement> insideStatements = blockStmt.getStatements();
        NodeList<Statement> newStatements = new NodeList<>();

        if(EmptyStatementChecker.isEmptyStatement(blockStmt))
            return blockStmt;
        for (Statement s : insideStatements
                ) {
            if(StatementTypeQuery.isBlockStmt(s)){
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                BlockStmt b = mark((BlockStmt)s);
                newStatements.add(b);
                //newStatements.add(exprStmt);

            }
            if(StatementTypeQuery.isIfElseStmt(s)){
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                mark((IfStmt)s);
                newStatements.add(s);
                //newStatements.add(exprStmt);
            }

            if(StatementTypeQuery.isExprStmt(s)){
                mark((ExpressionStmt)s);
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                newStatements.add(s);
            }
            if(StatementTypeQuery.isSForStmt(s)){
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                mark((ForStmt)s);
                newStatements.add(s);
                //newStatements.add(exprStmt);


            }
            if(StatementTypeQuery.isWhileStmt(s)){
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                mark((WhileStmt) s);
                newStatements.add(s);
                //newStatements.add(exprStmt);


            }
            if(StatementTypeQuery.isTryStmt(s)){
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                mark((TryStmt)s);
                newStatements.add(s);
                //newStatements.add(exprStmt);

            }
            if(StatementTypeQuery.isSynchronizedStmt(s)){
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                mark((SynchronizedStmt) s);
                newStatements.add(s);
                //newStatements.add(exprStmt);

            }
            if(StatementTypeQuery.isSwitchStmt(s)){
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                mark((SwitchStmt)s);
                newStatements.add(s);
                //newStatements.add(exprStmt);
            }
            if(StatementTypeQuery.isSwitchEntryStmt(s)){
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                mark((SwitchEntryStmt)s);
                newStatements.add(s);
                //newStatements.add(exprStmt);
            }
            if(StatementTypeQuery.isReturnStmt(s)){
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                mark((ReturnStmt)s);
                newStatements.add(s);
            }
            if(StatementTypeQuery.isBreakStmt(s)){
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                mark((BreakStmt) s);
                newStatements.add(s);
            }
            if(StatementTypeQuery.isContinueStmt(s)){
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                mark((ContinueStmt) s);
                newStatements.add(s);
            }
            if(StatementTypeQuery.isThrowStmt(s)){
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                mark((ThrowStmt)s);
                newStatements.add(s);
            }
            if(StatementTypeQuery.isForEachStmt(s)){
                ExpressionStmt exprStmt = getExpressionStmt();
                newStatements.add(exprStmt);
                mark((ForeachStmt) s);
                newStatements.add(s);
            }




        }

        ExpressionStmt exMarkerStmt = getExpressionStmt();

        blockStmt.setStatements(newStatements);
        //blockStmt.getStmts().add(0,exMarkerStmt);
        //blockStmt.getStmts().add(0,getFileWritingStmt2());

        return blockStmt;


    }

    public void mark(IfStmt s){

        IfStmt ifStmt = (IfStmt)s;
        Statement ifPart = ifStmt.getThenStmt();
        Statement elsePart = ifStmt.getElseStmt().isPresent() ? ifStmt.getElseStmt().get() : null;
        if(!EmptyStatementChecker.isEmptyStatement(ifPart)){
            if(!StatementTypeQuery.isExprStmt(ifPart) && !StatementTypeQuery.isThrowStmt(ifPart) && !StatementTypeQuery.isReturnStmt(ifPart)){
                BlockStmt blk = (BlockStmt)ifPart;
                mark(blk);

            }
            else{
                if(StatementTypeQuery.isExprStmt(ifPart)){
                    BlockStmt blk = mark((ExpressionStmt)ifPart);
                    ifStmt.setThenStmt(blk);
                }
                else if(StatementTypeQuery.isThrowStmt(ifPart)){
                    mark((ThrowStmt)ifPart);
                }
                else if(StatementTypeQuery.isReturnStmt(ifPart)){
                    mark((ReturnStmt)ifPart);
                }


            }
        }
        if(!EmptyStatementChecker.isEmptyStatement(elsePart)){
            if(!StatementTypeQuery.isExprStmt(elsePart)  ){
                if(StatementTypeQuery.isBlockStmt(elsePart)){
                    mark((BlockStmt)elsePart);

                }
                else if(StatementTypeQuery.isIfElseStmt(elsePart)){
                    mark((IfStmt)elsePart);
                }
            }
            else{
                BlockStmt blk = mark((ExpressionStmt)elsePart);
                ifStmt.setElseStmt(blk);

            }
        }




    }

    public void mark(ReturnStmt returnStmt){
        // for return statement, we add the statemetn at the beginning and not at the end.
        return;
    }

    public BlockStmt mark (ExpressionStmt s){
        BlockStmt newBlock = new BlockStmt();

        NodeList<Statement> list = new NodeList<>();
        list.add(s);
        newBlock.setStatements(list);
        newBlock.getStatements().add(0,getExpressionStmt());
        return newBlock;
    }



    void mark(ForStmt forStmt){
        Statement s = forStmt.getBody();
        if(StatementTypeQuery.isBlockStmt(s)){
            BlockStmt newBlock = mark((BlockStmt)s);
            forStmt.setBody(newBlock);

        }
        if(StatementTypeQuery.isExprStmt(s)){
            BlockStmt newBlock = mark((ExpressionStmt)s);
            forStmt.setBody(newBlock);
        }
        if(StatementTypeQuery.isIfElseStmt(s)){
            mark((IfStmt)s);

        }
        if(StatementTypeQuery.isWhileStmt(s)){
            mark((WhileStmt)s);
        }
        if(StatementTypeQuery.isForEachStmt(s)){
            mark((ForeachStmt)s);
        }
    }

    void mark(ForeachStmt foreachStmt){
        Statement s = foreachStmt.getBody();
        if(StatementTypeQuery.isBlockStmt(s)){
            BlockStmt newBlock = mark((BlockStmt)s);
            foreachStmt.setBody(newBlock);

        }
        if(StatementTypeQuery.isExprStmt(s)){
            BlockStmt newBlock = mark((ExpressionStmt)s);
            foreachStmt.setBody(newBlock);
        }
        if(StatementTypeQuery.isIfElseStmt(s)){
            mark((IfStmt)s);

        }
        if(StatementTypeQuery.isWhileStmt(s)){
            mark((WhileStmt)s);
        }
        if(StatementTypeQuery.isTryStmt(s)){
            mark((TryStmt)s);
        }
    }

    void mark(SynchronizedStmt synchronizedStmt){
        Statement s = synchronizedStmt.getBody();
        Expression expr = synchronizedStmt.getExpression();
        if(StatementTypeQuery.isBlockStmt(s)){
            BlockStmt newBlock = mark((BlockStmt)s);
            synchronizedStmt.setBody(newBlock);
        }
        if(StatementTypeQuery.isExprStmt(s)) {
            BlockStmt newBlock = mark((ExpressionStmt) s);

            synchronizedStmt.setBody(newBlock);
        }

    }
    void mark(TryStmt tryStmt){
        Statement s = tryStmt.getTryBlock();
        if(StatementTypeQuery.isBlockStmt(s)){
            BlockStmt newBlock = mark((BlockStmt)s);
            tryStmt.setTryBlock(newBlock);

        }
        if(StatementTypeQuery.isExprStmt(s)){
            BlockStmt newBlock = mark((ExpressionStmt)s);
            tryStmt.setTryBlock(newBlock);
        }
        if(StatementTypeQuery.isIfElseStmt(s)){
            mark((IfStmt)s);

        }

        if(tryStmt.getCatchClauses() != null){
            List<CatchClause> sCatchBlock = tryStmt.getCatchClauses();

            for(CatchClause catchClause : sCatchBlock){
                mark(catchClause);
            }
        }

        if(tryStmt.getFinallyBlock() != null){
            BlockStmt finallyBlock = tryStmt.getFinallyBlock().isPresent() ? tryStmt.getFinallyBlock().get() : null;
            if(finallyBlock != null){
                BlockStmt newFinallyBlock = mark(finallyBlock);
                tryStmt.setFinallyBlock(newFinallyBlock);
            }

        }





    }

    void mark(WhileStmt whileStmt){
        Statement s = whileStmt.getBody();
        if(StatementTypeQuery.isBlockStmt(s)){
            BlockStmt newBlock = mark((BlockStmt)s);
            whileStmt.setBody(newBlock);
        }
        if(StatementTypeQuery.isExprStmt(s)){
            BlockStmt newBlock = mark((ExpressionStmt)s);
            whileStmt.setBody(newBlock);
        }
        if(StatementTypeQuery.isIfElseStmt(s)){
            mark((IfStmt)s);

        }
        if(StatementTypeQuery.isSForStmt(s)){
            mark((ForStmt)s);
        }
    }

    void mark(CatchClause catchStmt){
        BlockStmt b = catchStmt.getBody();

        if(EmptyStatementChecker.isEmptyStatement(b)){
            return;
        }
        BlockStmt newBlock = mark(b);
        catchStmt.setBody(newBlock);

    }

    void mark(SwitchStmt switchStmt){
        List<SwitchEntryStmt> switchEntryStmts = switchStmt.getEntries();
        for (SwitchEntryStmt s : switchEntryStmts) {

            mark(s);

        }

    }

    void mark(SwitchEntryStmt switchEntryStmt){
        NodeList<Statement> statements = switchEntryStmt.getStatements();
        NodeList<Statement> newStatements = new NodeList<>();
        for (Statement s : statements
                ) {
            if(StatementTypeQuery.isBreakStmt(s)){
                newStatements.add(getExpressionStmt());
                newStatements.add(s);
                continue;
            }
            newStatements.add(getExpressionStmt());
            mark(s);
            newStatements.add(s);



        }

        switchEntryStmt.setStatements(newStatements);
    }

    void mark(BreakStmt breakStmt){
        return;
    }

    public void mark(ContinueStmt continueStmt){
        return;
    }

    public void mark(ThrowStmt throwStmt){
        return;
    }



    private ExpressionStmt getExpressionStmt() {
       /* MethodCallExpr methodCallExpr = new MethodCallExpr();
        FieldAccessExpr expr = new FieldAccessExpr();
        expr.setField("out");
        ;
        NameExpr nameExpr = new NameExpr();
        nameExpr.setName("System");
        expr.setScope(nameExpr);
        methodCallExpr.setName("println");
        methodCallExpr.setScope(expr);

        StringLiteralExpr stringLiteralExpr = new StringLiteralExpr();
        stringLiteralExpr.setValue(UUID.randomUUID().toString());
        LinkedList<Expression> literalExprs = new LinkedList<>();
        literalExprs.add(stringLiteralExpr);

        methodCallExpr.setArgs(literalExprs);


        System.out.println(methodCallExpr.toString());
        ExpressionStmt exMarkerStmt = new ExpressionStmt();
        exMarkerStmt.setExpression(methodCallExpr);
        return exMarkerStmt;*/
        return getFileWritingStmt2(isStaticMarker);
    }

    private ExpressionStmt getFileWritingStmt(){
        MethodCallExpr methodCallExpr = new MethodCallExpr();


        NameExpr nameExpr = new NameExpr();
        nameExpr.setName("FileWriter");
        methodCallExpr.setScope(nameExpr);
        methodCallExpr.setName("writeline");


        StringLiteralExpr stringLiteralExpr = new StringLiteralExpr();
        stringLiteralExpr.setValue(UUID.randomUUID().toString());
        StringLiteralExpr fileNameLiteralExpr = new StringLiteralExpr();
        fileNameLiteralExpr.setValue(outputFileName.toString());
        NodeList<Expression> literalExprs = new NodeList<>();
        literalExprs.add(fileNameLiteralExpr);
        literalExprs.add(stringLiteralExpr);


        methodCallExpr.setArguments(literalExprs);


        System.out.println(methodCallExpr.toString());
        ExpressionStmt exMarkerStmt = new ExpressionStmt();
        exMarkerStmt.setExpression(methodCallExpr);
        return exMarkerStmt;
    }

    private ExpressionStmt getFileWritingStmt2(boolean isStatic){
        NameExpr nameExpr = new NameExpr("output");
        StringLiteralExpr args1 = new StringLiteralExpr(UUID.randomUUID().toString());
        StringLiteralExpr args2 = new StringLiteralExpr(outputFileName);
        NodeList<Expression> args = new NodeList<>();
        args.add((Expression)args2);
        args.add((Expression)args1);
        MethodCallExpr methodCallExpr = new MethodCallExpr();
        methodCallExpr.setName("writeline");
        if(isStatic)
            methodCallExpr.setName(methodCallExpr.getName().asString() + "Static");
        methodCallExpr.setArguments(args);

        ExpressionStmt expressionStmt = new ExpressionStmt();
        expressionStmt.setExpression(methodCallExpr);
        return expressionStmt;
    }


    private ExpressionStmt getReturnStatementMarker(){
        MethodCallExpr methodCallExpr = new MethodCallExpr();
        FieldAccessExpr expr = new FieldAccessExpr();
        expr.setField("out");
        ;
        NameExpr nameExpr = new NameExpr();
        nameExpr.setName("System");
        expr.setScope(nameExpr);
        methodCallExpr.setName("println");
        methodCallExpr.setScope(expr);

        StringLiteralExpr stringLiteralExpr = new StringLiteralExpr();
        stringLiteralExpr.setValue("RETURN" +  UUID.randomUUID().toString());
        NodeList<Expression> literalExprs = new NodeList<>();
        literalExprs.add(stringLiteralExpr);

        methodCallExpr.setArguments(literalExprs);


        System.out.println(methodCallExpr.toString());
        ExpressionStmt exMarkerStmt = new ExpressionStmt();
        exMarkerStmt.setExpression(methodCallExpr);
        return exMarkerStmt;
    }

    public static void main(String[] args){
        MethodCallExpr methodCallExpr = new MethodCallExpr();
        FieldAccessExpr expr = new FieldAccessExpr();
        expr.setField("out");
        ;
        NameExpr nameExpr = new NameExpr();
        nameExpr.setName("System");
        expr.setScope(nameExpr);
        methodCallExpr.setName("println");
        methodCallExpr.setScope(expr);

        StringLiteralExpr stringLiteralExpr = new StringLiteralExpr();
        stringLiteralExpr.setValue(UUID.randomUUID().toString());
        NodeList<Expression> literalExprs = new NodeList<>();
        literalExprs.add(stringLiteralExpr);

        methodCallExpr.setArguments(literalExprs);


        System.out.println(methodCallExpr.toString());
        ;

    }
}
