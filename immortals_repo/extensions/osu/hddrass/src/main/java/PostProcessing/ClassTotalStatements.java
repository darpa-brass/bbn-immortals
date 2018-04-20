package PostProcessing;

import ASTManipulation.ClassIterator;
import ASTManipulation.ClassMethodLineManipulator;
import ASTManipulation.StatementTypeQuery;
import Helper.CompilationUnitHelper;
import Helper.Debugger;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import java.util.List;

/**
 * Created by root on 4/25/17.
 */
public class ClassTotalStatements {
    String originalFile;
    int highestDepthLevel = 0;
    int statementAboveLevel2Statements = 0;
    int expressionIfAndReturnStatements = 0;

    public ClassTotalStatements(String originalFile){
        this.originalFile = originalFile;

    }

    public int FindTotalSTatemetns(){
        ClassIterator classIteratorOriginal = new ClassIterator(originalFile);
        List<MethodDeclaration> methodsOriginal = classIteratorOriginal.GetAllMethods();
        int sum = 0;
        for (MethodDeclaration m1 : methodsOriginal){
            ClassMethodLineManipulator clm = new ClassMethodLineManipulator(CompilationUnitHelper.CreateCompilationUnit(originalFile),m1.getName().asString());

            int n = clm.GetHeigestDepthLevel();
            this.highestDepthLevel = Math.max(n,this.highestDepthLevel);
            int count = 1;
            for(int i =n;i>0;i--){


                List<Statement> statements = clm.GetNthLevelStatementsFromMethodName(m1.getName().asString(),i);
                sum += statements.size();
                if((i >= n - 1) ){
                    statementAboveLevel2Statements += statements.size();
                }
                for (Statement s : statements
                     ) {
                    if(StatementTypeQuery.isIfElseStmt(s) || StatementTypeQuery.isExprStmt(s) || StatementTypeQuery.isReturnStmt(s)){
                        expressionIfAndReturnStatements++;
                    }

                }
                count++;




            }
        }

        return sum;
    }

    public int FindTotalMethods(){
        ClassIterator classIteratorOriginal = new ClassIterator(originalFile);
        List<MethodDeclaration> methodsOriginal = classIteratorOriginal.GetAllMethods();
        return methodsOriginal.size();
    }



    public static void main(String[] args){
        ClassTotalStatements cts = new ClassTotalStatements("/home/ubuntu/results/reduced/ZipArchiveEntry/ZipArchiveEntry.java");

        int total = cts.FindTotalSTatemetns();

        System.out.println(total);
        System.out.println(cts.highestDepthLevel);
        System.out.println(cts.statementAboveLevel2Statements);
        System.out.println(cts.expressionIfAndReturnStatements);
    }
}
