package PostProcessing;

import ASTManipulation.ClassIterator;
import Helper.Debugger;

import Helper.FileWriterUtil;
import com.sun.deploy.ref.Helpers;

import japa.parser.ast.body.MethodDeclaration;
import sun.swing.plaf.synth.DefaultSynthStyle;

import java.lang.instrument.ClassDefinition;
import java.util.List;

/**
 * Created by root on 1/22/17.
 */
public class ClassDiffCalculator {

    private String originalFile,reducedFile;

    public ClassDiffCalculator(String originalFile, String reducedFile){
        this.originalFile = originalFile;
        this.reducedFile = reducedFile;
    }

    public StatInfo FindReductionInfo(){
        int toatalNodes = 0;
        int highestFromLeaf = 0;
        ClassIterator classIteratorOriginal = new ClassIterator(originalFile);
        ClassIterator classIternatorReduced = new ClassIterator(reducedFile);

        List<MethodDeclaration> methodsOriginal = classIteratorOriginal.GetAllMethods();
        List<MethodDeclaration> methodsReduced = classIternatorReduced.GetAllMethods();

        for (MethodDeclaration m1 : methodsOriginal
             ) {
            for ( MethodDeclaration m2: methodsOriginal
                 ) {
                if (classIteratorOriginal.ComapreMethods(m1, m2)) {
                    try {


                        if (m1.getName().equals("checkFile")) {

                            Debugger.log("stop");
                        }
                        MethodDiffCalculator methodDiff = new MethodDiffCalculator(originalFile, reducedFile, m1.getName());
                        StatInfo statInfo = methodDiff.Calculate();
                        toatalNodes += statInfo.numberOfNodesReduced;
                        highestFromLeaf = Math.max(highestFromLeaf, statInfo.highestlevelFromLeafNode);
                    } catch (Exception ex) {
                        Debugger.log(ex.toString());


                    }

                }
            }
        }
        StatInfo statInfo = new StatInfo();
        statInfo.highestlevelFromLeafNode = highestFromLeaf;
        statInfo.numberOfNodesReduced = toatalNodes;

        return statInfo;



    }


    public static void main(String[] args){
        int percentage = 10 ;
        String className = "OptionGroup";




        FileWriterUtil.writeLine("/home/ubuntu/temp.txt","\n");
        for(int i = 1; i<=10;i++){
            String filename = className + "_" + i + "_" + percentage + ".java";
            String firstFileName = "/home/ubuntu//results/reduced/" + className + "/" + className + ".java";
            String secondFileName = "/home/ubuntu//results/reduced/" + className + "/" + filename;
            ClassDiffCalculator diff = new ClassDiffCalculator(firstFileName,secondFileName);
            StatInfo statInfo =  diff.FindReductionInfo();
            Debugger.log(statInfo.highestlevelFromLeafNode);
            if(i == 1){
                FileWriterUtil.appendLine("/home/ubuntu/tempStatements.txt",Integer.toString(statInfo.numberOfNodesReduced));
                FileWriterUtil.appendLine("/home/ubuntu/tempLevel.txt",Integer.toString(statInfo.highestlevelFromLeafNode));

            }


            else{
                FileWriterUtil.appendLine("/home/ubuntu/tempStatements.txt",Integer.toString(statInfo.numberOfNodesReduced));
                FileWriterUtil.appendLine("/home/ubuntu/tempLevel.txt",Integer.toString(statInfo.highestlevelFromLeafNode));
            }



        }

    }
}
