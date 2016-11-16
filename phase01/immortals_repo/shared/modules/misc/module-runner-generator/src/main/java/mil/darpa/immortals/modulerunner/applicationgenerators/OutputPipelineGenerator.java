//package mil.darpa.immortals.modulerunner.applicationgenerators;
//
//import mil.darpa.immortals.modulerunner.configuration.AnalysisGeneratorConfiguration;
//import mil.darpa.immortals.modulerunner.configuration.AnalysisModuleConfiguration;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.util.*;
//
///**
// * Created by awellman@bbn.com on 8/19/16.
// */
//public class OutputPipelineGenerator extends AbstractGenerator {
//
//    public OutputPipelineGenerator(@Nonnull List<AnalysisModuleConfiguration> moduleConfigurations, @Nonnull Class insertionClass, @Nullable AnalysisGeneratorConfiguration generatorConfiguration) throws ClassNotFoundException {
//        super(moduleConfigurations, insertionClass, generatorConfiguration);
//    }
//
//    @Override
//    protected List<String> createDeclarationLines() throws ReflectiveOperationException {
//        List<String> returnList = new ArrayList(1);
//        return returnList;
//    }
//
//    @Override
//    protected List<String> createInitializationLines() throws ReflectiveOperationException {
//        // For the Input pipleine, The element opposite to the data source will be responsible for triggering. For example:
//        // JpegFilepath generator = new JpegFilepath(SemanticTypeUnit.JpegFilepath.constructDefaultGenerator(),
//        //         new BitmapReader(
//        //                 new BitmapDownsizer(
//        //                         1.0,
//        //                         new BitmapWriter()
//        //                 )
//        //         )
//        // );
//
//        List<String> returnList = new LinkedList<>();
//
//        if (generatorConfig != null) {
//            returnList.add(generatorIdName + " = mil.darpa.immortals.modulerunner.generators.GeneratorFactory.produceGenerator(");
//            returnList.add(generatorConfigIdentifier + ",");
//
//            String[] parameterLines = recursivePipelineConstructor(moduleConfigurations, 0).split("\n");
//
//            for (String str : parameterLines) {
//                returnList.add(str);
//            }
//
//            returnList.add(");");
//
//        } else {
//            throw new RuntimeException("Cannot utilize an OutputPipe without a generator to submit data to it!");
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
//
//            customParameters.put("mil.darpa.immortals.annotation.dsl.ontology.functionality.Output", recursivePipelineConstructor(moduleConfigurations, currentIdx + 1));
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
//        executionLines.add(generatorIdName + ".generate();");
//        return encloseInTimedGeneratorIterator(executionLines);
//    }
//
//    @Override
//    protected List<String> createCleanupLines() throws ReflectiveOperationException {
//        List<String> returnList = new ArrayList(1);
//        returnList.add(generatorIdName + ".cleanup();");
//        return returnList;
//    }
//}
