package ASTManipulation;


import Helper.Debugger;
import Helper.Globals;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.stmt.*;
import ASTManipulation.*;

import java.util.ArrayList;
import java.util.List;




/**
 * Created by arpit on 5/16/16.
 */
public class ClassMethodLineManipulator {

    public CompilationUnit _cu;
    public CompilationUnit _newcu;
    String _methodName;
    ArrayList l;
    ArrayList<Statement> stmts = new ArrayList<Statement>();

    public ClassMethodLineManipulator(CompilationUnit cu, String methodName ){
        _cu= cu;
        _methodName = methodName;
        _newcu = new CompilationUnit(_cu.getPackageDeclaration().get(),_cu.getImports(),_cu.getTypes(),_cu.getModule().isPresent() ? _cu.getModule().get(): (ModuleDeclaration)null);

    }


    public ArrayList<Statement> GetExpressioinStatements(String methodName) {
        ArrayList<Statement> stmts = new ArrayList<Statement>();
        for (TypeDeclaration t : _cu.getTypes()) {
            for (Object b2 : t.getMembers()) {
                BodyDeclaration<?> b = (BodyDeclaration<?>)b2;
                System.out.println(b.getClass().getSimpleName());

                //MethodDeclaration m = (MethodDeclaration)b;

                if (b.getClass().getSimpleName().toString().equals("MethodDeclaration")) {
                    MethodDeclaration m = (MethodDeclaration)b;
                    if(m.getName().toString().equals(methodName))
                        stmts.addAll(GetExpressionFromBasicBlock(m.getBody().get()));

                }
            }
        }
        return  stmts;
    }



    public void ReduceMethodStatements(String methodName, List<Statement> statementsToBeReduced){
        NodeList<BodyDeclaration<?>> newBody = new NodeList<>();
        for (TypeDeclaration t : _cu.getTypes()) {
            System.out.println("class name " + t.getName());

            for (Object b2 : t.getMembers()) {
                BodyDeclaration<?> b = (BodyDeclaration<?>)b2;
                

                if (b.getClass().getSimpleName().toString().equals("MethodDeclaration")) {
                    MethodDeclaration m = (MethodDeclaration)b;
                    if(m.getName().toString().equals(methodName)) {
                        ArrayList<Statement> x = ReduceStatementsFromBlock(m.getBody().get());
                        BlockStmt b1 = new BlockStmt();
                        b1.setStatements(NodeList.nodeList(x));
                        m.setBody(b1);

                        System.out.println("Final " + (m.getBody().isPresent()? m.getBody().toString() : Globals.EmptyString));
                        newBody.add(m);

                    }
                    else{
                        newBody.add(m);
                    }


                }
                else if (b.getClass().getSimpleName().toString().equals("FieldDeclaration")){
                    FieldDeclaration f = (FieldDeclaration)b;
                    newBody.add(f);
                }
                //newBody.add(b);
            }




        }

        _newcu.setTypes(_cu.getTypes());
        NodeList<TypeDeclaration<?>> y = _newcu.getTypes();
        List<TypeDeclaration<?>> z = (List<TypeDeclaration<?>>)y;


        NodeList<TypeDeclaration<?>> tnew = _newcu.getTypes();

        tnew.get(0).setMembers((newBody));
        System.out.println("Final outcome \n" + _newcu.toString());
    }

    public void ReduceMethodStatements(String className, String methodName, List<Statement> statementToBeReduced){
        List<Statement> newSetOfStatement = new ArrayList<Statement>();
        ArrayList<BodyDeclaration> newBody = new ArrayList<BodyDeclaration>();
        for(TypeDeclaration  t : _cu.getTypes()){
            if(!TypeDeclarationTypeQuery.isNonRemovableType(t)){
                if(t.getName().toString().equals(className)){
                    for (Object b2 : t.getMembers()) {
                        BodyDeclaration<?> b = (BodyDeclaration<?>)b2;
                        if (b.getClass().getSimpleName().toString().equals("MethodDeclaration")) {
                            MethodDeclaration m = (MethodDeclaration) b;
                            if (m.getName().toString().equals(methodName)) {

                                //ArrayList<Statement> x = ReduceStatementsFromBlock(m.getBody(), statementToBeReduced);
                                Reducer reducer = new Reducer();
                                BlockStmt b1 =  reducer.reduce(m.getBody().get(),statementToBeReduced);



                                m.setBody(b1);

                                System.out.println("Final " + (m.getBody().isPresent() ? m.getBody().get().toString() : Globals.EmptyString));
                                newBody.add(m);

                            }
                            else{
                                newBody.add(m);
                            }
                        }
                    }
                }
            }

        }
    }

    public List<Statement> GetLeafStatements(String className, String methodName){
        List<Statement> newSetOfStatement = new ArrayList<Statement>();
        ArrayList<BodyDeclaration> newBody = new ArrayList<BodyDeclaration>();
        for(TypeDeclaration  t : _cu.getTypes()){
            if(t.getName().toString().equals(className)){
                for (Object b2 : t.getMembers()) {
                    BodyDeclaration<?> b = (BodyDeclaration<?>)b2;
                    if (b.getClass().getSimpleName().toString().equals("MethodDeclaration")) {
                        MethodDeclaration m = (MethodDeclaration) b;
                        if (m.getName().toString().equals(methodName)) {

                            //ArrayList<Statement> x = ReduceStatementsFromBlock(m.getBody(), statementToBeReduced);
                            TreeManipulator treeManipulator = new TreeManipulator(this._cu,m.getName().asString());
                            newSetOfStatement.addAll(treeManipulator.GetAllLeafNodes());


                            newSetOfStatement.addAll(treeManipulator.GetAllLeafPlusOneNodes(m.getBody().get()));


                        }
                        else{

                        }
                    }
                }
            }
        }
        return newSetOfStatement;
    }

    public ArrayList<Statement> ReduceStatementsFromBlock(BlockStmt block){
        ArrayList<Statement> statementToBeReduced = new ArrayList<Statement>();
        ArrayList<Statement> newSetOfStatement = new ArrayList<Statement>();
        for(Statement s: block.getStatements()){
            //System.out.println("****" + s.toString());
            //System.out.println("****" + s.getClass().getSimpleName());
            if(s.getClass().getSimpleName().equals("ExpressionStmt")){
                System.out.println("****" + ((ExpressionStmt)s).getExpression().toString());
                ExpressionStmt a = (ExpressionStmt)s;
                System.out.println(a.getExpression().getClass().getSimpleName());
                Expression e = a.getExpression();
                System.out.println(e.getClass().getSimpleName());


            }
            else{
                newSetOfStatement.add(s);
            }


        }
        return newSetOfStatement;
    }

    public ArrayList<Statement> ReduceStatementsFromBlock(BlockStmt block, List<Statement> statementToBeReduced){

        ArrayList<Statement> newSetOfStatement = new ArrayList<Statement>();
        for(Statement s: block.getStatements()){
            //System.out.println("****" + s.toString());
            //System.out.println("****" + s.getClass().getSimpleName());

            if(statementToBeReduced.contains(s)){
                System.out.println(s);


            }
            else{
                newSetOfStatement.add(s);

            }


        }
        return newSetOfStatement;
    }

    public List<Statement> GetNthLevelStatementsFromMethodName(String methodName, int n){
        for (TypeDeclaration t : _cu.getTypes()) {

            if(!TypeDeclarationTypeQuery.isNonRemovableType(t)){
                for (Object b2 : t.getMembers()) {
                    BodyDeclaration<?> b = (BodyDeclaration<?>)b2;
                    if (b.getClass().getSimpleName().toString().equals("MethodDeclaration")) {
                        MethodDeclaration m = (MethodDeclaration)b;
                        if(m.getName().toString().equals(methodName)) {
                            List<Statement> blockStatements = m.getBody().get().getStatements();

                            List<Statement> nthLevelStmnts = null;
                            try {
                                nthLevelStmnts = GetNthLevelFromBlock(blockStatements,n);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if(nthLevelStmnts != null){
                                for (Statement s: nthLevelStmnts) {
                                    System.out.print(s.hashCode());
                                    System.out.println(" " + s);
                                    return nthLevelStmnts;

                                }
                            }

                        }

                    }

                }
            }

        }
        return new ArrayList<Statement>();
    }

    public boolean IsHighestDepthLevel(String methodName, int n){
        for (TypeDeclaration t : _cu.getTypes()) {
            if(!TypeDeclarationTypeQuery.isNonRemovableType(t)){
                for (Object b2 : t.getMembers()) {
                    BodyDeclaration<?> b = (BodyDeclaration<?>)b2;
                    if (b.getClass().getSimpleName().toString().equals("MethodDeclaration")) {
                        MethodDeclaration m = (MethodDeclaration)b;
                        if(m.getName().toString().equals(methodName)) {
                            List<Statement> blockStatements = m.getBody().get().getStatements();
                            List<Statement> nthLevelStmnts = null;
                            try {
                                nthLevelStmnts = GetNthLevelFromBlock(blockStatements,n);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if(nthLevelStmnts != null){
                                for (Statement s: nthLevelStmnts) {
                                    System.out.println(s);

                                }
                            }
                            else{
                                return true;
                            }

                        }

                    }

                }
            }
            return false;

        }
        return false;
    };

    public int GetHeigestDepthLevel() {
        for (TypeDeclaration t : this._cu.getTypes()) {

            if(!TypeDeclarationTypeQuery.isNonRemovableType(t)){
                for (Object b2 : t.getMembers()) {
                    BodyDeclaration<?> b = (BodyDeclaration<?>)b2;
                    if (b.getClass().getSimpleName().toString().equals("MethodDeclaration")) {
                        MethodDeclaration m = (MethodDeclaration) b;
                        if (m.getName().toString().equals(_methodName)) {
                            return GetHeighestDepthLevel(m.getBody().get());

                        }
                    }
                }
            }

        }
        return 0;
    }

    public int GetHeighestDepthLevel(BlockStmt block){
        int[] tempDepthLevels = new int[block.getStatements().size()];
        int i = 0;
        for (Statement s:block.getStatements()) {
            if(s.getClass().getSimpleName().toString().equals("ExpressionStmt")){
                tempDepthLevels[i++] = 0;
            }
            else if(s.getClass().getSimpleName().toString().equals("ReturnStmt") ){
                tempDepthLevels[i++] = 0;
            }
            else if(StatementTypeQuery.isLeafLevelStatement(s)){
                tempDepthLevels[i++] =  0;
            }
            else if (s.getClass().getSimpleName().toString().equals("ForStmt")){
                ForStmt forstmt = (ForStmt)s;
                BlockStmt blkstmt = (BlockStmt)forstmt.getBody();
                tempDepthLevels[i++] = GetHeighestDepthLevel(blkstmt);



                System.out.println("Special hash code " + forstmt.getBody().hashCode());
            }
            else if (s.getClass().getSimpleName().toString().equals("WhileStmt")){
                WhileStmt whilestmt = (WhileStmt)s;
                tempDepthLevels[i++] = GetHeighestDepthLevel((BlockStmt)whilestmt.getBody());
            }
            else if(s.getClass().getSimpleName().toString().equals("IfStmt")){
                IfStmt ifStmt = (IfStmt)s;
                int ifParthDepth = 0;
                int elsePartDepth = 0;
                Statement elsePart = ifStmt.getElseStmt().get();

                if(StatementTypeQuery.isBlockStmt(ifStmt.getThenStmt())){
                    ifParthDepth = GetHeighestDepthLevel((BlockStmt)ifStmt.getThenStmt());
                    //tempDepthLevels[i++] = GetHeighestDepthLevel((BlockStmt)ifStmt.getThenStmt());
                }
                else if(StatementTypeQuery.isLeafLevelStatement(ifStmt.getThenStmt()))
                    ifParthDepth = 1;
                else{
                    ifParthDepth = 0;
                    //tempDepthLevels[i++] = 0;
                }

                if(elsePart != null){
                    if(StatementTypeQuery.isLeafLevelStatement(elsePart))
                        elsePartDepth = 1;
                    else if(StatementTypeQuery.isBlockStmt(elsePart)){
                        elsePartDepth = GetHeighestDepthLevel((BlockStmt) elsePart);
                    }
                    else
                        elsePartDepth = 0;
                }

                tempDepthLevels[i++] = Math.max(ifParthDepth,elsePartDepth);




            }
            else if(StatementTypeQuery.isTryStmt(s)){

                //todo: assumption, try block is the deepest block. remove assumption and fix.
                TryStmt tryStmt = (TryStmt)s;
                BlockStmt tryBlock = tryStmt.getTryBlock();
                tempDepthLevels[i++] = GetHeighestDepthLevel(tryBlock);


            }
        }
        return 1 + FindMax(tempDepthLevels);
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


    private List<Statement> GetNthLevelFromBlock(List<Statement> statements,int n) throws Exception {
        if(statements.isEmpty())
            return null;
        else if(n == 1)
            return statements;
        else{
            List<Statement> levelStatements = new ArrayList<Statement>();
            for(Statement s : statements){


                if(s.getClass().getSimpleName().toString().equals("ExpressionStmt")){

                }
                else if(s.getClass().getSimpleName().toString().equals("ReturnStmt") ){

                }
                else if(StatementTypeQuery.isBreakStmt(s)){

                }
                else if(StatementTypeQuery.isContinueStmt(s)){

                }
                else if(StatementTypeQuery.isThrowStmt(s)){

                }
                else if (s.getClass().getSimpleName().toString().equals("ForStmt")){
                    ForStmt forstmt = (ForStmt)s;
                    BlockStmt blkstmt = (BlockStmt)forstmt.getBody();


                    levelStatements.addAll(blkstmt.getStatements());

                }
                else if (s.getClass().getSimpleName().toString().equals("WhileStmt")){
                    levelStatements.addAll(((BlockStmt)((WhileStmt) s).getBody()).getStatements());

                }
                else if(StatementTypeQuery.isIfElseStmt(s)){

                    Statement ifpart = ((IfStmt)s).getThenStmt();
                    Statement elsePart =((IfStmt)s).getElseStmt().isPresent() ? ((IfStmt)s).getElseStmt().get() : null;
                    if(ifpart != null){
                        if(StatementTypeQuery.isExprStmt(ifpart)){
                            levelStatements.add(ifpart);
                        }
                        else if(StatementTypeQuery.isReturnStmt(ifpart)){
                            levelStatements.add(ifpart);
                        }
                        else{
                            List<Statement> stmts = ((BlockStmt)((IfStmt) s).getThenStmt()).getStatements();
                            if(stmts != null)
                                levelStatements.addAll(stmts);
                        }
                    }

                    if(elsePart != null){
                        if(StatementTypeQuery.isExprStmt(elsePart)){
                            levelStatements.add(elsePart);
                        }
                        else{
                            if(StatementTypeQuery.isBlockStmt(elsePart)){
                                List<Statement> stmts = ((BlockStmt)((IfStmt) s).getElseStmt().get()).getStatements();
                                if(stmts != null)
                                    levelStatements.addAll(stmts);
                            }
                            else if(StatementTypeQuery.isIfElseStmt(elsePart)){
                                levelStatements.add(elsePart);

                            }

                        }

                    }




                }
                else if(StatementTypeQuery.isTryStmt(s)){
                    TryStmt tryStmt = (TryStmt)s;
                    BlockStmt tryBlock = tryStmt.getTryBlock();
                    if(tryBlock != null && tryBlock.getStatements() != null){
                        levelStatements.addAll(tryBlock.getStatements());
                    }
                    if(tryStmt.getCatchClauses() != null){
                        for (CatchClause catchClause: tryStmt.getCatchClauses()  ) {
                            if(catchClause.getBody().getStatements() != null){
                                levelStatements.addAll(catchClause.getBody().getStatements());
                            }

                        }
                    }

                    BlockStmt finallyBlock = tryStmt.getFinallyBlock().isPresent() ? tryStmt.getFinallyBlock().get(): null;
                    if(finallyBlock != null && finallyBlock.getStatements() != null){
                        levelStatements.addAll(finallyBlock.getStatements());
                    }

                }
                else if(StatementTypeQuery.isSwitchStmt(s)){
                    Debugger.log("In switch statement");
                    SwitchStmt switchstmt = (SwitchStmt)s;
                    for (SwitchEntryStmt switchEntryStmt:switchstmt.getEntries() ) {

                        levelStatements.addAll(switchEntryStmt.getStatements());

                    }

                }

                else if(StatementTypeQuery.isForEachStmt(s)){
                    ForeachStmt foreachstmt = (ForeachStmt) s;
                    BlockStmt blkstmt = (BlockStmt)foreachstmt.getBody();


                    levelStatements.addAll(blkstmt.getStatements());
                }
                else if(StatementTypeQuery.isSynchronizedStmt(s)){
                    SynchronizedStmt syncStmt = (SynchronizedStmt)s;
                    if(!EmptyStatementChecker.isEmptyStatement(syncStmt.getBody())){
                        if(syncStmt.getBody().getStatements() != null){
                            levelStatements.addAll(syncStmt.getBody().getStatements());
                        }

                    }
                }
                else{
                    throw new Exception("not implemented");
                }
            }
            return GetNthLevelFromBlock(levelStatements, n-1);
        }

    }




    public ArrayList<Statement> GetExpressionFromBasicBlock(BlockStmt block){

        for (Statement s: block.getStatements()) {
            if(s.getClass().getSimpleName().toString().equals("ExpressionStmt")){
                stmts.add(s);
            }
            else if(s.getClass().getSimpleName().toString().equals("ForStmt")){
                ForStmt f = (ForStmt)s;
                GetExpressionFromBasicBlock((BlockStmt) f.getBody());

            }


        }
        return stmts;
    }

    public void RemoveStatement(BlockStmt blockStmt,ExpressionStmt exprStmt){
        BlockStmt newBlockStmt = new BlockStmt();
        NodeList<Statement> newStatements = new NodeList<>();
        NodeList<Statement> originalStatements =  blockStmt.getStatements();
        for (Statement s: originalStatements) {
            if(!((ExpressionStmt)s).equals(exprStmt))
                newStatements.add(s);
        }
        newBlockStmt.setStatements(newStatements);
        for (Statement s: newBlockStmt.getStatements()) {
            //System.out.println(s);
        }


    }


    public static class Main {

        public static void main(String[] args) {
            System.out.println("Hello World!");

        }
    }
}
