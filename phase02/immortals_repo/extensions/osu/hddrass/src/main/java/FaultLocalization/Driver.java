package FaultLocalization;

import Helper.Debugger;

import java.text.MessageFormat;

/**
 * Created by root on 2/12/18.
 */
public class Driver {

    public static void main(String[] args){
        String className = "URIBuilder";
        String complementFileName = "URIBuilderTest";
        String reducedFileName = "TestURIBuilder";
        int labelingScheme = 10;
        String labeledTestFileTemplate = "/home/ubuntu/results/complement/{0}/{1}_{2}_{3}_.java";
        String unLabeledTestFileTemplate = "/home/ubuntu/results/reduced/{0}/{1}_{2}_{3}.java";
        String coverageFilesTemplate =  "/home/ubuntu/results/coverage/{0}/testcoverage/";

        for(int i = 1; i<= 1; i++){
            String labeledTestFile = MessageFormat.format(labeledTestFileTemplate,className,complementFileName,i,labelingScheme);
            String unLabledTestFile = MessageFormat.format(unLabeledTestFileTemplate,className,reducedFileName,i,labelingScheme);
            String coverageFiles = MessageFormat.format(coverageFilesTemplate,className);
            Debugger.log(labeledTestFile);
            Debugger.log(unLabledTestFile);
            Debugger.log(coverageFiles);

            Transformation t = new Transformation(labeledTestFile,unLabledTestFile,coverageFiles);
            t.transform();

            Rank r = new Rank(t.testCoverage,t.testPassFail);
            r.calculateTarantula();
            r.sortByRanking();
            Debugger.log(r.tarantulaRank);
            for (String s: r.finalRanking
                 ) {
                Debugger.log(s);
            }
        }




    }
}
