//package mil.darpa.immortals.modulerunner.applicationgenerators;
//
//import mil.darpa.immortals.modulerunner.ClassReflectionHelper;
//import mil.darpa.immortals.modulerunner.configuration.AnalysisModuleConfiguration;
//import mil.darpa.immortals.modulerunner.configuration.ModuleCompositionConfiguration;
//
//import java.util.LinkedList;
//import java.util.List;
//
///**
// * Created by awellman@bbn.com on 9/26/16.
// */
//public class InputPipeDfuComposer {
//
//    public static final String PACKAGE_PATH = "mil.darpa.immortals.dfus.generated.";
//
//    public String className;
//    public String classPath;
//
//    private final ModuleCompositionConfiguration moduleCompositionConfiguration;
//
//    public InputPipeDfuComposer(ModuleCompositionConfiguration moduleCompositionConfiguration) {
//        this.moduleCompositionConfiguration = moduleCompositionConfiguration;
//    }
//
//
//    public List<String> createFileLines() throws ClassNotFoundException {
//        List<String> outputLines = new LinkedList<>();
//
//        className = moduleCompositionConfiguration.compositionIdentifier;
//        classPath = PACKAGE_PATH + className;
//
//        AnalysisModuleConfiguration baseConfiguration = moduleCompositionConfiguration.compositionSequence.get(0);
//        ClassReflectionHelper crh = new ClassReflectionHelper(baseConfiguration);
//
////        outputLines.add("public class " + className + " implements " + WriteableObjectPipeInterface.class.getName() +
////        "<" + originalDfu. + "," +  + ">");
//
//
//
//
//        return outputLines;
//    }
//}
