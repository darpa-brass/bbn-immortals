package ASTManipulation;

import Helper.Debugger;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.stmt.*;
import sun.reflect.generics.tree.Tree;

import java.util.List;

import static java.lang.System.exit;

/**
 * Created by arpit on 6/11/16.
 */
public class TreeManipulator {
    BlockStmt root;
    int highestDepthLevel;
    CompilationUnit cu;
    String testMethod;

    public TreeManipulator(CompilationUnit cu, String testMethod){
        this.root = root;
        highestDepthLevel = 0;
        this.cu = cu;
        this.testMethod = testMethod;

    }

    public int GetHeigestDepthLevel() {
        for (TypeDeclaration t : cu.getTypes()) {
            for (BodyDeclaration b : t.getMembers()) {
                if (b.getClass().getSimpleName().toString().equals("MethodDeclaration")) {
                    MethodDeclaration m = (MethodDeclaration) b;
                    if (m.getName().toString().equals(testMethod)) {
                        return GetHeighestDepthLevel(m.getBody());

                    }
                }
            }
        }
        return 0;
    }
    public int GetHeighestDepthLevel(BlockStmt block){
        if(block.getStmts() == null)
            return 0;
        if(block.getStmts().size() == 0)
            return 0;
        int[] tempDepthLevels = new int[block.getStmts().size()];
        int i = 0;

        for (Statement s:block.getStmts()) {
            if(s.getClass().getSimpleName().toString().equals("ExpressionStmt")){
                tempDepthLevels[i] = 0;
                i++;
            }
            else if(s.getClass().getSimpleName().toString().equals("ReturnStmt") ){
                tempDepthLevels[i++] = 0;
            }
            else if (s.getClass().getSimpleName().toString().equals("ForStmt")){
                ForStmt forstmt = (ForStmt)s;
                BlockStmt blkstmt = (BlockStmt)forstmt.getBody();
                tempDepthLevels[i++] = GetHeighestDepthLevel(blkstmt);




            }
            else if (s.getClass().getSimpleName().toString().equals("WhileStmt")){
                WhileStmt whilestmt = (WhileStmt)s;
                tempDepthLevels[i++] = GetHeighestDepthLevel((BlockStmt)whilestmt.getBody());
            }
            else if (s.getClass().getSimpleName().toString().equals("IfStmt")){
                IfStmt ifStmt = (IfStmt)s;
                int ifLength;
                int elseLength;
                if(StatementTypeQuery.isExprStmt(ifStmt.getThenStmt()))
                    ifLength = 1;
                else
                    ifLength = GetHeighestDepthLevel((BlockStmt)ifStmt.getThenStmt());
                if(ifStmt.getElseStmt() != null && StatementTypeQuery.isExprStmt(ifStmt.getElseStmt()))
                    elseLength = 1;
                else
                    elseLength = ifStmt.getElseStmt() != null? GetHeighestDepthLevel((BlockStmt)ifStmt.getElseStmt()): 0;
                tempDepthLevels[i] = Math.max(ifLength,elseLength);
                i++;
            }
            else if(StatementTypeQuery.isSwitchStmt(s)){
                tempDepthLevels[i] = GetHeighestDepthLevel((SwitchStmt) s);
                i++;
            }
            else if(StatementTypeQuery.isForEachStmt(s)){
                tempDepthLevels[i] = GetHighestDepthLevel((ForeachStmt)s);
            }
            else{
                Debugger.log(s);
                Debugger.log("*************** NOT IMPLEMENTED ***************************");
                exit(-99);
            }


        }
        return 1 + FindMax(tempDepthLevels);
    }

    private int GetHeighestDepthLevel(SwitchStmt s) {

        Debugger.log("*********** in swich statement");
        List<SwitchEntryStmt> caseStmts =  s.getEntries();
        if(caseStmts == null || caseStmts.size() == 0){
            return 0;

        }
        int max = 0;
        for (SwitchEntryStmt casestmt:  caseStmts ){

            if(casestmt == null || casestmt.getStmts() == null){
                continue;
            }
            int maxInner = 0;
            for (Statement sInner: casestmt.getStmts()) {
                if(StatementTypeQuery.isExprStmt(sInner) || StatementTypeQuery.isBreakStmt(sInner))
                    maxInner = Math.max(maxInner,1);
                else if(StatementTypeQuery.isSForStmt(sInner))
                    maxInner = Math.max(maxInner,1 + GetHeighestDepthLevel((ForStmt) sInner));
                else if(StatementTypeQuery.isIfElseStmt(sInner)){

                     maxInner= Math.max(maxInner,1+GetHeighestDepthLevel((IfStmt)sInner)) ;
                }
                else{
                    Debugger.log("Not implemented : In TreeManipulator.java");
                    exit(-99);
                }

            }

            max = Math.max(max,maxInner);

        }
        return max;
    }

    private int GetHeighestDepthLevel(ForStmt forstmt){
        if(EmptyStatementChecker.isEmptyStatement(forstmt.getBody()))
                return 0;
        BlockStmt blkstmt = (BlockStmt)forstmt.getBody();
        return  GetHeighestDepthLevel(blkstmt);


    }

    private int GetHighestDepthLevel(ForeachStmt foreachStmt){
        if(EmptyStatementChecker.isEmptyStatement(foreachStmt.getBody()))
            return 0;
        BlockStmt blkstmt = (BlockStmt)foreachStmt.getBody();
        return GetHeighestDepthLevel(blkstmt);
    }

    private int GetHeighestDepthLevel(IfStmt ifStmt){

        int ifLength;
        int elseLength;
        if(StatementTypeQuery.isExprStmt(ifStmt.getThenStmt()))
            ifLength = 1;
        else
            ifLength = GetHeighestDepthLevel((BlockStmt)ifStmt.getThenStmt());
        if(ifStmt.getElseStmt() != null && StatementTypeQuery.isExprStmt(ifStmt.getElseStmt()))
            elseLength = 1;
        else
            elseLength = ifStmt.getElseStmt() != null? GetHeighestDepthLevel((BlockStmt)ifStmt.getElseStmt()): 0;
        return Math.max(ifLength,elseLength);

    }




    private int FindMax(int[] numbers){
        int largest = Integer.MIN_VALUE;
        for(int i =0;i<numbers.length;i++) {
            if(numbers[i] > largest) {
                largest = numbers[i];
            }
        }
        return largest;
    }

}
