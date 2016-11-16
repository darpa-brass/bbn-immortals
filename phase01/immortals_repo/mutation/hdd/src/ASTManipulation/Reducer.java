
package ASTManipulation;
import Helper.Debugger;
import japa.parser.ast.stmt.*;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

/**
 * Created by arpit on 6/4/16.
 */
public class Reducer {
    BlockStmt block; // block on which reduction needs to be applied
    List<Statement> statementToBeReduced;
    BlockStmt reducedBlock;
    public Reducer(){

    }

    public Reducer(BlockStmt b, List<Statement> l){
        block = b;
        statementToBeReduced = l;
    }

    public BlockStmt reduce(BlockStmt b, List<Statement> l){

        BlockStmt newBlock = new BlockStmt();
        List<Statement> newList = new ArrayList<Statement>();
        for (Statement s: b.getStmts()) {

            if(s.getClass().getSimpleName().equals("ExpressionStmt")) {
                if(!l.contains(s)){
                   newList.add(s);
                }

            }
            else if(s.getClass().getSimpleName().toString().equals("ReturnStmt")){
                if(!l.contains(s)){
                    newList.add(s);
                }
            }
            else if(s.getClass().getSimpleName().toString().equals("ForStmt")){
                ReduceForStmt(l, newList, s);

            }
            else if(s.getClass().getSimpleName().toString().equals("WhileStmt")){
                if(!l.contains(s)){
                    WhileStmt w = (WhileStmt) s;
                    if(!StatementTypeQuery.isExprStmt(w.getBody()))
                        w.setBody(reduce((BlockStmt) w.getBody(),l));
                    else{
                        if(l.contains(w.getBody())){
                            newList.add(w.getBody());
                        }
                    }
                    newList.add(w);
                }

            }
            else if(s.getClass().getSimpleName().toString().equals("IfStmt")){
                ReduceIfStmt(l, newList, s);
            }
            else if(StatementTypeQuery.isSwitchStmt(s)){

                Debugger.log("********** switch statement found");
                if(!l.contains(s)){
                    ReduceSwitchCase(l, (SwitchStmt) s);
                    newList.add(s);
                }
                else{
                    newList.add(s);
                }


            }
            else if(StatementTypeQuery.isForEachStmt(s)){
                ReduceForEachStmt(l, newList, s);
            }
            else{

                Debugger.log(s);
                try {
                    throw new Exception("Not Imeplented \n" + s.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    exit(-99);
                }
            }
        }
        newBlock.setStmts(newList);
        return newBlock;

    }

    private void ReduceIfStmt(List<Statement> l, List<Statement> newList, Statement s) {
        if(!l.contains(s)){
            // reduce if statement, reduce else statement and then combine it.
            IfStmt ifStmt = (IfStmt)s;
            Statement ifPart = ifStmt.getThenStmt();
            Statement elsePart = ifStmt.getElseStmt();
            if(!EmptyStatementChecker.isEmptyStatement(ifPart)){
               if(!StatementTypeQuery.isExprStmt(ifPart)){
                   ifStmt.setThenStmt(reduce((BlockStmt)ifPart,l));
               }
                else{
                   if(l.contains(ifPart)){
                      newList.add(ifPart);
                   }
               }
            }

            if(!EmptyStatementChecker.isEmptyStatement(elsePart)){
                if(!StatementTypeQuery.isExprStmt(ifPart)){
                    ifStmt.setElseStmt(reduce((BlockStmt)elsePart,l));
                }
                else{
                    if(l.contains(elsePart)){
                        newList.add(elsePart);
                    }
                }
            }


            newList.add(ifStmt);
        }
    }

    private void ReduceSwitchCase(List<Statement> l, SwitchStmt s) {
        SwitchStmt switchStmt = s;
        for ( SwitchEntryStmt switchEntryStmt : switchStmt.getEntries()  ) {
            List<Statement> statementsToBeRetained = new ArrayList<Statement>();
            for (Statement s2: switchEntryStmt.getStmts()) {
                if(StatementTypeQuery.isExprStmt(s2)){
                    if(!l.contains(s2)){
                        statementsToBeRetained.add(s2);
                    }
                    else{

                    }

                }
                else if(StatementTypeQuery.isSForStmt(s2)){
                    ReduceForStmt(l, statementsToBeRetained, s2);


                }
                else if(StatementTypeQuery.isBreakStmt(s2)){
                    statementsToBeRetained.add(s2);

                }
                else if(StatementTypeQuery.isIfElseStmt(s2)){
                    ReduceIfStmt(l,statementsToBeRetained,s2);
                }
            }
            switchEntryStmt.setStmts(statementsToBeRetained);



        }
    }

    private void ReduceForStmt(List<Statement> l, List<Statement> newList, Statement s2) {
        if(!l.contains(s2)){
            ForStmt f = (ForStmt)s2;
            if(!((ForStmt)s2).getBody().toString().equals("{\n}"))
                f.setBody(reduce((BlockStmt) ((ForStmt)s2).getBody(),l));
            newList.add(f);
        }
    }

    private void ReduceForEachStmt(List<Statement> l, List<Statement> newList, Statement s2) {
        if(!l.contains(s2)){
            ForeachStmt f = (ForeachStmt) s2;
            if(!((ForeachStmt)s2).getBody().toString().equals("{\n}"))
                f.setBody(reduce((BlockStmt) ((ForeachStmt)s2).getBody(),l));
            newList.add(f);
        }
    }

    public BlockStmt reduceSingleStatement(BlockStmt b, Statement s){

        return reducedBlock;
    }



}
