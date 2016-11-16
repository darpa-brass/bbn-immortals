//package mil.darpa.immortals.modulerunner.applicationgenerators;
//
//import mil.darpa.immortals.core.synthesis.interfaces.ProducingPipe;
//import mil.darpa.immortals.modulerunner.configuration.AnalysisGeneratorConfiguration;
//import mil.darpa.immortals.modulerunner.configuration.AnalysisModuleConfiguration;
//import mil.darpa.immortals.modulerunner.generators.ControlPointFormat;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.util.*;
//
///**
// * Created by awellman@bbn.com on 8/19/16.
// */
//public class InputPipelineGenerator extends AbstractGenerator {
//
//    private String headIdentifier;
//
//    public InputPipelineGenerator(@Nonnull List<AnalysisModuleConfiguration> moduleConfigurations, @Nonnull Class insertionClass, @Nullable AnalysisGeneratorConfiguration generatorConfiguration) throws ClassNotFoundException {
//        super(moduleConfigurations, insertionClass, generatorConfiguration);
//    }
//
//    @Override
//    protected List<String> createDeclarationLines() throws ReflectiveOperationException {
//        List<String> returnList = new ArrayList(1);
//
//        headIdentifier = "head_" + UUID.randomUUID().toString().substring(0, 4);
//        returnList.add("mil.darpa.immortals.core.synthesis.interfaces.ReadableObjectPipeInterface " + headIdentifier + ";");
//
//        return returnList;
//    }
//
//    @Override
//    protected List<String> createInitializationLines() throws ReflectiveOperationException {
//        List<String> returnList = new LinkedList<>();
//
//        if (generatorConfig != null) {
//            returnList.add(generatorIdName + " = mil.darpa.immortals.modulerunner.generators.GeneratorFactory.produceGenerator(" + generatorConfigIdentifier + ");");
//        }
//
//        returnList.add(headIdentifier + " = ");
//
//        String[] parameterLines = recursivePipelineConstructor(moduleConfigurations, 0).split("\n");
//        parameterLines[parameterLines.length - 1] = parameterLines[parameterLines.length - 1] + ";";
//
//        for (String str : parameterLines) {
//            returnList.add(str);
//        }
//
//        return returnList;
//    }
//
//    public String recursivePipelineConstructor(List<AnalysisModuleConfiguration> configurations, int currentIdx) throws ReflectiveOperationException {
//
//        if (currentIdx == configurations.size()) {
//            return generatorIdName;
//        } else {
//
//            AnalysisModuleConfiguration currentConfig = configurations.get(currentIdx);
//
//            Set<String> missingConstructorParamters = currentConfig.getMissingConstructorParameters();
//
//            Map<String, String> customParameters = new HashMap<>();
//            if (currentConfig.controlPointFormat == ControlPointFormat.InputPipe) {
//                customParameters.put("mil.darpa.immortals.annotation.dsl.ontology.functionality.Input", recursivePipelineConstructor(moduleConfigurations, currentIdx + 1));
//            }
//
//            Map<String, String> providingConstructorParameters = fieldFiller(missingConstructorParamters, insertionClass, customParameters);
//
//            return createConstructorLineRightSide(currentConfig, providingConstructorParameters);
//        }
//    }
//
//    @Override
//    protected List<String> createExecutionLines() throws ReflectiveOperationException {
//        List<String> executionLines = new ArrayList<>(1);
//        ProducingPipe<String> ropi;
//        executionLines.add(headIdentifier + ".produce();");
//        return encloseInTimedGeneratorIterator(executionLines);
//    }
//
//    @Override
//    protected List<String> createCleanupLines() throws ReflectiveOperationException {
//        List<String> returnList = new ArrayList(1);
//        returnList.add(headIdentifier + ".closePipe();");
//        return returnList;
//    }
//}
