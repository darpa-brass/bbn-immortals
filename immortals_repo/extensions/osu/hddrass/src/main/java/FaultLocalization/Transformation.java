package FaultLocalization;

import ASTManipulation.MethodRemover;
import Helper.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by root on 2/12/18.
 *
 * transforms labeled and unlabeled tests into Matrix for spectrum based ranking.
 * Input
 * // labeled (compliment) test path, filename and class name
 // unlabled (reduced) test path, filename and class name

 Output:

 */
public class Transformation {
    String labeledTestFile = "/home/ubuntu/results/complement/URIBuilder/URIBuilderTest_1_10_.java";
    String ClassName= "TestURIBuilder";
    String unLabeledTestFile = "/home/ubuntu/results/reduced/URIBuilder/TestURIBuilder_1_10.java";
    String coverageFilesPath = "/home/ubuntu/results/coverage/URIBuilder/testcoverage/";
    Set<BodyDeclaration<?>> labeldTests;
    Set<BodyDeclaration<?>> unlabledTests;
    Dictionary<String,Set<String>> testCoverage;
    Dictionary<String,Boolean> testPassFail;
    List<String> testFiles = new ArrayList<>();

    public Transformation(String labeledTestFile, String unLabeledTestFile, String coverageFilesPath){
        testPassFail = new Hashtable<>();
        testCoverage = new Hashtable<>();
        this.labeledTestFile = labeledTestFile;
        this.unLabeledTestFile = unLabeledTestFile;
        this.coverageFilesPath = coverageFilesPath;
    }

    public void transform(){
        CompilationUnit cuLabeled = CompilationUnitHelper.CreateCompilationUnit(labeledTestFile);
        CompilationUnit cuUnLabeled = CompilationUnitHelper.CreateCompilationUnit(unLabeledTestFile);
        MethodRemover rm = new MethodRemover(labeledTestFile);
        labeldTests = rm.getAvaibleTestMethods().stream().distinct().collect(Collectors.toSet());
        rm = new MethodRemover(unLabeledTestFile);
        unlabledTests = rm.getAvaibleTestMethods().stream().distinct().collect(Collectors.toSet());

        labeldTests.forEach(x -> {
            testPassFail.put(x.asMethodDeclaration().getName().asString(),Boolean.FALSE);
        });
        unlabledTests.forEach(x -> {
            testPassFail.put(x.asMethodDeclaration().getName().asString(),Boolean.TRUE);
        });

        testFiles = FileOperationUtil.getAllFileNames(coverageFilesPath,"");

        Debugger.log(testPassFail);
        Debugger.log(testFiles);

        testFiles.forEach(y -> {
            try {
                Set<String> coverageInfo = FileReaderUtil.ReadFileByLine(y).stream().collect(Collectors.toSet());
                String testname = Stuffs.DeriveFilenameWithoutExtensionFromFullPath(y);
                testCoverage.put(testname,coverageInfo);



            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        Debugger.log(testCoverage);
    }





    public static void main(String[] args){
        Transformation t = new Transformation("/home/ubuntu/results/complement/URIBuilder/URIBuilderTest_1_10_.java","/home/ubuntu/results/reduced/URIBuilder/TestURIBuilder_1_10.java", "/home/ubuntu/results/coverage/URIBuilder/testcoverage/");
        t.transform();
    }


}
