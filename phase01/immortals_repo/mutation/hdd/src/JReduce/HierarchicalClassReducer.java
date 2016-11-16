package JReduce;

import Helper.ClassEnumerator;
import Helper.Globals;
import Helper.MethodEnumerator;
import japa.parser.ParseException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by arpit on 9/28/16.
 */
public class HierarchicalClassReducer {
    String rootFolder = Globals.EmptyString;
    String relativeFilePath = Globals.EmptyString;
    List<String> testClasses = new ArrayList<String>();
    int highestDepthLevel;
    public String fullJarFilePath = Globals.EmptyString;
    public String packageName = Globals.EmptyString;
    String buildCommand;
    String className;
    public String testJarFilePath = Globals.EmptyString;
    public ITester tester;

    public HierarchicalClassReducer(String rootFolder,String relativeFilePath, String jarFilePath, String packageName,String testJarFileName,  List<String> testClasses, String buildCommand, String className){
        this.rootFolder = rootFolder;
        this.relativeFilePath = relativeFilePath;
        this.testClasses = testClasses;
        this.fullJarFilePath = jarFilePath;
        this.buildCommand = buildCommand;
        this.testJarFilePath = testJarFileName;
        this.testClasses = testClasses;
        this.packageName = packageName;
        this.className = className;
    }

    public HierarchicalClassReducer(String rootFolder,String relativeFilePath, String jarFilePath, String packageName,ITester tester, String buildCommand, String className){
        this.rootFolder = rootFolder;
        this.relativeFilePath = relativeFilePath;
        this.testClasses = testClasses;
        this.fullJarFilePath = jarFilePath;
        this.buildCommand = buildCommand;
        this.tester = tester;
        this.packageName = packageName;
        this.className = className;
    }

    private void Initialize(){

    }
    public void ReduceClass(){
        List<Class<?>> classes = ClassEnumerator.getClassedFromThisJarFile(this.fullJarFilePath,packageName);
        if(classes.stream().anyMatch(x -> x.getSimpleName().equals(className))){

            Arrays
            .asList(MethodEnumerator.GetDeclaedMethods(classes.stream().filter(x -> x.getSimpleName().equals(className))
            .findFirst().get()))
            .stream().forEach(p -> {
                JavaMethod javaMethod = new JavaMethod(rootFolder,relativeFilePath,p.getName(),className,this.fullJarFilePath);
                HierarchicalReducer hReducer = null;
                if(testClasses.isEmpty() || testJarFilePath.isEmpty()){
                    hReducer = new HierarchicalReducer
                            (javaMethod,rootFolder,relativeFilePath,p.getName(),tester,buildCommand,className);
                }else{
                    hReducer = new HierarchicalReducer
                            (javaMethod,rootFolder,relativeFilePath,p.getName(),testJarFilePath,testClasses,buildCommand,className);
                }

                try {
                    hReducer.Reduce();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });

        }

    }
}
