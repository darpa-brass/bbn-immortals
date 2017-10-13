//package mil.darpa.immortals.modulerunner.applicationgenerators;
//
//import mil.darpa.immortals.modulerunner.ClassReflectionHelper;
//import mil.darpa.immortals.modulerunner.configuration.AnalysisGeneratorConfiguration;
//import mil.darpa.immortals.modulerunner.configuration.AnalysisModuleConfiguration;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.lang.reflect.Method;
//import java.util.*;
//
///**
// * Created by awellman@bbn.com on 8/9/16.
// */
//public class BasicGenerator extends AbstractGenerator {
//
//    private final AnalysisModuleConfiguration moduleConfiguration;
//    private String headIdentifier;
//
//    public BasicGenerator(@Nonnull List<AnalysisModuleConfiguration> moduleConfigurations, @Nonnull Class insertionClass, @Nullable AnalysisGeneratorConfiguration generatorConfiguration) throws ClassNotFoundException {
//        super(moduleConfigurations, insertionClass, generatorConfiguration);
//        if (moduleConfigurations.size() > 1) {
//            throw new RuntimeException("A Basic synthesis point can currently only be constructed with a single module!");
//        } else if (generatorConfiguration != null) {
//            throw new RuntimeException("A Basic synthesis point does not currently support usage of a generator!");
//        } else {
//            moduleConfiguration = moduleConfigurations.get(0);
//        }
//    }
//
//    @Override
//    protected List<String> createDeclarationLines() {
//        List<String> returnList = new LinkedList<>();
//
//        headIdentifier = "head" + UUID.randomUUID().toString().substring(0, 4);
//        returnList.add(moduleConfiguration.classPackageIdentifier + " " + headIdentifier + ";");
//        return returnList;
//    }
//
//    @Override
//    protected List<String> createInitializationLines() throws ReflectiveOperationException {
//        List<String> returnList = new LinkedList<>();
//
//        Set<String> missingConstructorParamters = moduleConfiguration.getMissingConstructorParameters();
//        Map<String, String> providingConstructorParameters = fieldFiller(missingConstructorParamters, insertionClass, null);
//        returnList.add(headIdentifier + " = " + createConstructorLineRightSide(moduleConfiguration, providingConstructorParameters) + ";");
//
//        Set<String> missingInitializationParameters = moduleConfiguration.getMissingInitializationParameters();
//        Map<String, String> providingInitializationParameters = fieldFiller(missingInitializationParameters, insertionClass, null);
//        returnList.add(createInitializationLine(headIdentifier, providingInitializationParameters));
//
//        return returnList;
//    }
//
//    @Override
//    protected List<String> createExecutionLines() throws ReflectiveOperationException {
//        List<String> returnList = new LinkedList<>();
//
//        Set<String> missingExecutionParameters = moduleConfiguration.getMissingExecutionParameters();
//        Map<String, String> providingExecutionParameters = fieldFiller(missingExecutionParameters, insertionClass, null);
//        returnList.add(createExecutionLine(headIdentifier, providingExecutionParameters));
//
//        return returnList;
//    }
//
//    @Override
//    protected List<String> createCleanupLines() throws ReflectiveOperationException {
//        List<String> returnList = new LinkedList<>();
//
//        returnList.add(createCleanupLine(headIdentifier));
//
//        return returnList;
//    }
//
//    public synchronized String createInitializationLine(String objectName, Map<String, String> parameterOverrides) throws ReflectiveOperationException {
//        ClassReflectionHelper classReflectionHelper = getReflectionHelper(moduleConfiguration);
//
//        List<Method> initMethods = classReflectionHelper.getMatchingInitMethods(moduleConfiguration.initializationParameterMap);
//
//        if (initMethods == null || initMethods.isEmpty()) {
//            throw new RuntimeException("No matching constructors were found for the provided configuration!");
//        }
//
//        Method initMethod = initMethods.get(0);
//
//        String paramList = "";
//
//        if (initMethod.getParameters() != null && initMethod.getParameters().length > 0) {
//            String[] params = classReflectionHelper.createParameterIdentifierArray(initMethod, moduleConfiguration.initializationParameterMap, parameterOverrides);
//
//            for (String param : params) {
//                if (paramList.equals("")) {
//                    paramList = param;
//                } else {
//                    paramList = paramList + ", " + param;
//                }
//            }
//        }
//
//        return objectName + "." + initMethod.getName() + "(" + paramList + ");";
//    }
//
//
//    public synchronized String createCleanupLine(String objectName) throws ReflectiveOperationException {
//        ClassReflectionHelper classReflectionHelper = getReflectionHelper(moduleConfiguration);
//
//        Method cleanupMethod = classReflectionHelper.getCleanupMethod();
//
//        if (cleanupMethod == null) {
//            return "";
//        } else {
//            return objectName + "." + cleanupMethod.getName() + "();";
//        }
//    }
//
//    public synchronized String createExecutionLine(String objectName, Map<String, String> parameterOverrides) throws ReflectiveOperationException {
//        ClassReflectionHelper classReflectionHelper = getReflectionHelper(moduleConfiguration);
//
//        List<Method> executionMethods = classReflectionHelper.getMatchingWorkMethods(moduleConfiguration.executionParameterMap);
//
//        if (executionMethods == null || executionMethods.isEmpty()) {
//            throw new RuntimeException("No matching execution methods were found for the provided configuration!");
//        }
//
//        Method executionMethod = executionMethods.get(0);
//
//        String paramList = "";
//
//        if (executionMethod.getParameters() != null && executionMethod.getParameters().length > 0) {
//            String[] params = classReflectionHelper.createParameterIdentifierArray(executionMethod, moduleConfiguration.executionParameterMap, parameterOverrides);
//
//            for (String param : params) {
//                if (paramList.equals("")) {
//                    paramList = param;
//                } else {
//                    paramList = paramList + ", " + param;
//                }
//            }
//        }
//
//        return objectName + "." + executionMethod.getName() + "(" + paramList + ");";
//    }
//
//}
