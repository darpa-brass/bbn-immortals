package Coverage;

import ASTManipulation.ExpressionTypeQuery;
import ASTManipulation.StatementTypeQuery;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.*;

import java.util.UUID;

/**
 * Created by root on 2/13/18.
 */
public class BlockUnMarker {
    public BlockStmt blockStmtMarked;
    public BlockStmt blockStmtUnmarked;

    public void unmark(){
        unmark(blockStmtMarked);
    }

    public Statement unmark(Statement s){
        if(isAnnotationStatement(s))
        {

        }
        if(StatementTypeQuery.isForEachStmt(s))
            unmark(s.asForeachStmt());
        if(StatementTypeQuery.isSForStmt(s))
            unmark(s.asForStmt());
        if(StatementTypeQuery.isLeafLevelStatement(s)){

        }
        if(StatementTypeQuery.isWhileStmt(s)){
            unmark(s.asWhileStmt());
        }
        if(StatementTypeQuery.isIfElseStmt(s)){
            unmark(s.asIfStmt());
        }
        if(StatementTypeQuery.isSwitchStmt(s)){
            unmark(s.asSwitchStmt());
        }
        if(StatementTypeQuery.isSwitchEntryStmt(s)){
            unmark(s.asSwitchEntryStmt());
        }
        if(StatementTypeQuery.isTryStmt(s)){
            unmark(s.asTryStmt());
        }
        if(StatementTypeQuery.isDoStmt(s)){
            unmark(s.asDoStmt());
        }

        return s;
    }

    public BlockStmt unmark(BlockStmt blockStmt){
        NodeList<Statement> statements = blockStmt.getStatements();
        BlockStmt unmark = new BlockStmt();
        NodeList<Statement> unmarkStatements = new NodeList<>();
        for(Statement s : statements){
            if(isAnnotationStatement(s))
                continue;
            if(StatementTypeQuery.isLeafLevelStatement(s)){
                unmarkStatements.add(s);
            }
            if(StatementTypeQuery.isIfElseStmt(s)){
                unmark(s.asIfStmt());
                unmarkStatements.add(s);
            }
            if(StatementTypeQuery.isForEachStmt(s)){
                unmark(s.asForeachStmt());
                unmarkStatements.add(s);
            }
            if(StatementTypeQuery.isSForStmt(s)){
                unmark(s.asForStmt());
                unmarkStatements.add(s);
            }
            if(StatementTypeQuery.isWhileStmt(s)){
                unmark(s.asWhileStmt());
                unmarkStatements.add(s);

            }
            if(StatementTypeQuery.isDoStmt(s)){
                unmark(s.asDoStmt());
                unmarkStatements.add(s);
            }
            if(StatementTypeQuery.isTryStmt(s)){
                unmark(s.asTryStmt());
                unmarkStatements.add(s);
            }
            if(StatementTypeQuery.isSwitchStmt(s)){
                unmark(s.asSwitchStmt());
                unmarkStatements.add(s);
            }
        }



        unmark.setStatements(unmarkStatements);
        return unmark;
    }

    private void unmark(IfStmt ifStmt){
        if(ifStmt.getThenStmt().isBlockStmt()){
            ifStmt.setThenStmt(unmark(ifStmt.getThenStmt().asBlockStmt()));
        }
        if(ifStmt.getElseStmt().isPresent()){
            if(ifStmt.getElseStmt().get().isBlockStmt()){
                ifStmt.setElseStmt(unmark(ifStmt.getElseStmt().get().asBlockStmt()));
            }
            else{
                ifStmt.setElseStmt(unmark(ifStmt.getElseStmt().get()));
            }
        }
    }

    private void unmark(ForStmt forStmt){
        if(forStmt.getBody().isBlockStmt()){
            forStmt.setBody(unmark(forStmt.getBody().asBlockStmt()));
        }
    }

    private void unmark(ForeachStmt foreachStmt){
        if(foreachStmt.getBody().isBlockStmt()){
            foreachStmt.setBody(unmark(foreachStmt.getBody().asBlockStmt()));
        }
    }

    private void unmark(WhileStmt whileStmt){
        if(whileStmt.getBody().isBlockStmt()){
            whileStmt.setBody(unmark(whileStmt.getBody().asBlockStmt()));
        }
    }

    private void unmark(DoStmt doStmt){
        if(doStmt.getBody().isBlockStmt()){
            doStmt.setBody(unmark(doStmt.getBody().asBlockStmt()));
        }
    }

    private void unmark(TryStmt tryStmt){
        if(tryStmt.getTryBlock().isBlockStmt()){
            unmark(tryStmt.getTryBlock());

        }
        NodeList<CatchClause> catchClauses = tryStmt.getCatchClauses();
        for (CatchClause s: catchClauses
                ) {
            if(s.getBody().isBlockStmt())
                unmark(s.getBody());

        }
        if(tryStmt.getFinallyBlock().isPresent()){
            unmark(tryStmt.getFinallyBlock().get());
        }

    }

    private void unmark(SwitchStmt switchStmt){
        NodeList<SwitchEntryStmt> switchStmts = switchStmt.getEntries();
        for (SwitchEntryStmt s: switchStmts
                ) {
            unmark(s.asSwitchEntryStmt());
        }

    }

    private void unmark(SwitchEntryStmt switchEntryStmt){
        NodeList<Statement> newStatements = new NodeList<>();
        for (Statement s :switchEntryStmt.getStatements()
                ) {
            if(isAnnotationStatement(s))
                continue;
            if(StatementTypeQuery.isLeafLevelStatement(s)){
                newStatements.add(s);
            }
            if(StatementTypeQuery.isIfElseStmt(s)){
                unmark(s.asIfStmt());
                newStatements.add(s);
            }
            if(StatementTypeQuery.isForEachStmt(s)){
                unmark(s.asForeachStmt());
                newStatements.add(s);
            }
            if(StatementTypeQuery.isSForStmt(s)){
                unmark(s.asForStmt());
                newStatements.add(s);
            }
            if(StatementTypeQuery.isWhileStmt(s)){
                unmark(s.asWhileStmt());
                newStatements.add(s);

            }
            if(StatementTypeQuery.isDoStmt(s)){
                unmark(s.asDoStmt());
                newStatements.add(s);
            }
            if(StatementTypeQuery.isTryStmt(s)){
                unmark(s.asTryStmt());
                newStatements.add(s);
            }

        }

        switchEntryStmt.setStatements(newStatements);
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
}
