package Driver;

import Helper.Debugger;
import Helper.Globals;
import JReduce.HierarchicalClassReducer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arpit on 9/28/16.
 */
public class CommonsValidatorDriver {
    public static void main(String args[]) throws Exception {



        Helper.CommandLineParser commandLineParser = null;


        try {
            commandLineParser = new Helper.CommandLineParser(args);
        }
        catch(org.apache.commons.cli.ParseException parseEx){
            System.out.println("Error in command line argument, provide correct command line arguments");
            System.out.println(parseEx.getMessage());
        }
        //List<String> testClasses = Helper.FileReaderUtil.ReadFileByLine("/home/arpit/research/HDD/src/Driver/CommonsValidatorTests.data");
        List<String> testClasses = Helper.FileReaderUtil.ReadFileByLine(commandLineParser.getTestClassesFilePath());
        testClasses.stream().forEach(x -> Debugger.log(x));
        String rootFolder = commandLineParser.getRootFolder();
        String relativeFilePath = commandLineParser.getRelativePath();
        String methodName = commandLineParser.getmehtodame();
        String className = commandLineParser.getClassName();
        String testJarFile = commandLineParser.getTestJarFileName();
        String jarFile = commandLineParser.getJarFileName();
        String buildCommand = commandLineParser.getBuildcommand();
        String packageName = commandLineParser.getPackageName();
        Globals.TEST_RUNNING_TIME_OUT = commandLineParser.gettimeoutForInfiniteLoop();
        System.out.println("*************************");
        System.out.println(buildCommand);
        System.out.println("*************************");


        HierarchicalClassReducer classReducer = new HierarchicalClassReducer(rootFolder,relativeFilePath,jarFile,packageName,
                testJarFile,testClasses,buildCommand,className);
        classReducer.ReduceClass();


    }
}
