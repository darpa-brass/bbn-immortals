package FaultLocalization;
import ASTManipulation.MethodRemover;
import Helper.Command;
import Helper.Stuffs;
import PreProcessig.PrepareClassBasedOnLabeling;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.text.MessageFormat;
/**
 * Created by root on 2/10/18.
 * Runs individual tests and collects coverage information per test.
 * inputs: test class file, intermediate coverage file name.
 * output: test1.coverage, test2.coverage ..... testN.coverage files.
 */
public class Spectrum {
    public String testFile;
    public String mavenOrAntCommandTemplate;
    public String intermediateCoverageFileName;

    public Spectrum(String testFile, String mavenOrAntCommandTemplate, String intermediateCoverageFileName){
        this.testFile = testFile;
        this.mavenOrAntCommandTemplate = mavenOrAntCommandTemplate;
        this.intermediateCoverageFileName = intermediateCoverageFileName;
    }

    public void computeCoverage(){
        MethodRemover mr = new MethodRemover(testFile);
        NodeList<BodyDeclaration<?>> testMethods = mr.getAvaibleTestMethods();

        mr.getAvaibleTestMethods().forEach(x -> {
            String command = MessageFormat.format(mavenOrAntCommandTemplate,x.asMethodDeclaration().getName().asString());
            Command.exec(new String[]{"bash","-c",command });

            String renameCommand = "mv " + intermediateCoverageFileName + " " + Stuffs.DerivePathWithoutFileName(intermediateCoverageFileName) +  x.asMethodDeclaration().getName().asString() + ".coverage";
            Command.exec(new String[]{"bash","-c",renameCommand });


        });

    }


    public static void main(String[] args){
        String mavenTemplate = "mvn -Dtest=TestURIBuilder#{0} -f /home/ubuntu/research/httpcore/httpcore5/pom.xml -DfailIfNoTests=false test";
        String testFile = "/home/ubuntu/research//httpcore/httpcore5/src/test/java/org/apache/hc/core5/net/TestURIBuilder.java";
        String intermediateCoverageFileName = "/home/ubuntu/results/coverage/URIBuilder/URIBuilder_1_10.coverage";
        Spectrum sp = new Spectrum(testFile,mavenTemplate,intermediateCoverageFileName);
        sp.computeCoverage();

    }

}
