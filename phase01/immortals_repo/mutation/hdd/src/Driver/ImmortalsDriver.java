package Driver;

import Helper.Debugger;
import JReduce.HierarchicalClassReducer;
import JReduce.ImmortalsTester;

import java.util.List;

/**
 * Created by arpit on 9/28/16.
 */
public class ImmortalsDriver {
    public static void main(String args[]) throws Exception {
        // specify build command
        // specify tester
        // pass builder and tester to Heirarchical class or method reducer based on your requirement
        Helper.CommandLineParser commandLineParser = null;
        try {
            commandLineParser = new Helper.CommandLineParser(args);
        }
        catch(org.apache.commons.cli.ParseException parseEx){
            System.out.println("Error in command line argument, provide correct command line arguments");
            System.out.println(parseEx.getMessage());
        }

        String rootFolder = commandLineParser.getRootFolder();
        String relativeFilePath = commandLineParser.getRelativePath();
        String methodName = commandLineParser.getmehtodame();
        String className = commandLineParser.getClassName();
        String testJarFile = commandLineParser.getTestJarFileName();
        String jarFile = commandLineParser.getJarFileName();
        String buildCommand = commandLineParser.getBuildcommand();
        String packageName = commandLineParser.getPackageName();
        System.out.println("*************************");
        System.out.println(buildCommand);
        System.out.println("*************************");
        String immortalsTesterCommand= commandLineParser.getImmortalsTester();
        ImmortalsTester tester = new ImmortalsTester(immortalsTesterCommand, new String[]{"-w", "-t", "30", "-v", "-r", "client-test-location"});
        //buildCommand = "/root/immortals_repo/./gradlew";
        //-w -t 30 -v -r client-test-images

        HierarchicalClassReducer classReducer = new HierarchicalClassReducer(rootFolder,relativeFilePath,jarFile,packageName,
                tester,buildCommand,className);
        classReducer.ReduceClass();

    }
}
