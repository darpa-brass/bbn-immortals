package ASTManipulation;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.util.Optional;

/**
 * Created by root on 2/20/18.
 * This is very crude way to compare statements.
 * It is not correct theoritically, though for almost all practical purpose it will work for us.
 * We had to take this route, becuase we generated reductions long time ago and now its difficult to go back and regenerate everything
 */
public class StatementComparor {

    // returns true, if two statements are same, false otherwise.
    // We don't compare exact block statements, we trust that programs are written by smart people.
    // For example: If two for statements are written after one another consecutively, probability that they have same initialization, same condition and same increment is very low.
    public static boolean compare(Statement s1, Statement s2){
        if(StatementTypeQuery.FindType(s1) != StatementTypeQuery.FindType(s2)){
            return false;
        }
        if(StatementTypeQuery.isLeafLevelStatement(s1)){
            return s1.toString().equals(s2.toString());
        }
        if(StatementTypeQuery.isSForStmt(s1)){
            return compare(s1.asForStmt(),s2.asForStmt());
        }
        if(StatementTypeQuery.isIfElseStmt(s1)){
            return compare(s1.asIfStmt(),s2.asIfStmt());
        }

        return false;


    }

    private static boolean compare(ForStmt f1, ForStmt f2){
        boolean bInitialization = compare(f1.getInitialization(),f2.getInitialization());
        boolean bUpdate = compare(f1.getUpdate(),f2.getUpdate());
        boolean bCompare = compare(f1.getCompare(),f2.getCompare());
        return bInitialization && bUpdate && bCompare;

    }

    private static boolean compare(IfStmt if1, IfStmt if2){
        return compare(if1.getCondition(),if2.getCondition());
    }


    private static boolean  compare(NodeList<Expression> e1List, NodeList<Expression> e2List){
        if(e1List.size() != e2List.size())
            return  false;


        for(int i = 0;i< e1List.size();i++){
            if(!e1List.get(i).toString().equals(e2List.get(i).toString())){
                return false;

            }


        }
        return  true;


    }

    private static boolean compare(Optional<Expression> e1, Optional<Expression> e2){
        if(e1.isPresent() && !e2.isPresent())
            return false;
        if(!e1.isPresent() && e2.isPresent())
            return false;
        if(!e1.isPresent() && !e2.isPresent())
            return true;

        if(e1.isPresent() && e2.isPresent()){
            return e1.get().toString().equals(e2.get().toString());
        }
        return false;
    }

    private static boolean compare(Expression e1, Expression e2){
        return e1.toString().equals(e2.toString());
    }
}
