//package mil.darpa.immortals.modulerunner.configuration;
//
//import mil.darpa.immortals.modulerunner.generators.ControlPointFormat;
//import mil.darpa.immortals.modulerunner.generators.DeploymentPlatform;
//import mil.darpa.immortals.modulerunner.generators.SemanticType;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.util.*;
//
///**
// * Created by awellman@bbn.com on 6/1/16.
// */
//public class AnalysisModuleConfiguration extends RuntimeEnvironmentConfiguration {
//
//
//    public static transient final String FILL_IN_VALUE_KEY = "$PROVIDED_BY_APPLICATION!";
//    public static transient final String DATA_SOURCE = "$CONSTRUCTED_DATA_SOURCE!";
//    public static transient final String ATTACHED_DFU = "$CONSTRUCTED_DFU!";
//
//    public final String classPackageIdentifier;
//
//    public final String aspect;
//    public final String functionalityBeingPerformed;
//
////    public final ControlPointFormat controlPointFormat;
////    public final DeploymentPlatform deploymentPlatform;
//
//    public final String dataGeneratorClassPackageIdentifier;
//    public final String dataGeneratorAspect;
//
//    //    private transient ClassReflectionHelper classReflectionHelper;
//    public final HashMap<String, String> constructorParameterMap;
//    public final HashMap<String, String> initializationParameterMap;
//    public final HashMap<String, String> executionParameterMap;
//
//
//    public AnalysisModuleConfiguration(@Nonnull String classPackageIdentifier,
//                                       @Nonnull String aspect,
//                                       @Nonnull String functionalityBeingPerformed,
//                                       @Nonnull ControlPointFormat controlPointFormat,
//                                       @Nonnull DeploymentPlatform deploymentPlatform,
//                                       @Nullable Map<String, String> constructorParameterMap,
//                                       @Nullable Map<String, String> initializationParameterMap,
//                                       @Nullable Map<String, String> executionParameterMap,
//                                       @Nullable String dataGeneratorClassPackageIdentifier,
//                                       @Nullable String dataGeneratorAspect) throws ClassNotFoundException {
//        super(controlPointFormat, deploymentPlatform);
//        this.classPackageIdentifier = classPackageIdentifier;
//
//        this.aspect = aspect;
//        this.functionalityBeingPerformed = functionalityBeingPerformed;
//
//        if (constructorParameterMap == null) {
//            this.constructorParameterMap = new HashMap<>();
//        } else {
//            this.constructorParameterMap = new HashMap<>(constructorParameterMap);
//        }
//
//        if (initializationParameterMap == null) {
//            this.initializationParameterMap = new HashMap<>();
//        } else {
//            this.initializationParameterMap = new HashMap<>(initializationParameterMap);
//        }
//
//        if (executionParameterMap == null) {
//            this.executionParameterMap = new HashMap<>();
//        } else {
//            this.executionParameterMap = new HashMap<>(executionParameterMap);
//        }
//
//        this.dataGeneratorClassPackageIdentifier = dataGeneratorClassPackageIdentifier;
//        this.dataGeneratorAspect = dataGeneratorAspect;
//    }
//
//    public void collapseInRuntimeEnvironmentConfiguration(RuntimeEnvironmentConfiguration configuration) {
//        if (deploymentPlatform == null) {
//            deploymentPlatform = configuration.deploymentPlatform;
//        }
//        if (deploymentPlatform != configuration.deploymentPlatform) {
//            throw new RuntimeException("Deployment platform missmatch error!");
//        }
//
//        if (controlPointFormat == null) {
//            controlPointFormat = configuration.controlPointFormat;
//        }
//        if (controlPointFormat != configuration.controlPointFormat) {
//            throw new RuntimeException("Control point format missmatch error!");
//        }
//    }
//
////    public ControlPointFormat getControlPointFormat() {
////        return controlPointFormat;
////    }
//
////    public void setControlPointFormat(@Nonnull ControlPointFormat controlPointFormat) {
////        this.controlPointFormat = controlPointFormat;
////    }
//
//    public Set<String> getMissingConstructorParameters() {
//        Set<String> returnSet = new HashSet<>();
//
//        for (String key : constructorParameterMap.keySet()) {
//            Object o = constructorParameterMap.get(key);
//            if (o instanceof String && (o.equals(FILL_IN_VALUE_KEY) || ((String) o).startsWith(DATA_SOURCE) || ((String) o).startsWith(ATTACHED_DFU))) {
//                returnSet.add(key);
//            }
//        }
//        return returnSet;
//    }
//
//    public Set<String> getMissingInitializationParameters() {
//        Set<String> returnSet = new HashSet<>();
//
//        for (String key : initializationParameterMap.keySet()) {
//            Object o = initializationParameterMap.get(key);
//            if (o instanceof String && (o.equals(FILL_IN_VALUE_KEY) || ((String) o).startsWith(DATA_SOURCE) || ((String) o).startsWith(ATTACHED_DFU))) {
//                returnSet.add(key);
//            }
//        }
//        return returnSet;
//    }
//
//    public Set<String> getMissingExecutionParameters() {
//        Set<String> returnSet = new HashSet<>();
//
//        for (String key : executionParameterMap.keySet()) {
//            Object o = executionParameterMap.get(key);
//            if (o instanceof String && (o.equals(FILL_IN_VALUE_KEY) || ((String) o).startsWith(DATA_SOURCE) || ((String) o).startsWith(ATTACHED_DFU))) {
//                returnSet.add(key);
//            }
//        }
//        return returnSet;
//    }
//
//    @Nullable
//    public SemanticType getNeededGeneratorType() {
//        Set<SemanticType> generatorSet = getRequiredGenerators(constructorParameterMap);
//        generatorSet.addAll(getRequiredGenerators(initializationParameterMap));
//        generatorSet.addAll(getRequiredGenerators(executionParameterMap));
//
//        if (generatorSet.size() > 1) {
//            throw new RuntimeException("Only a single generator is supported at this time!");
//        }
//
//        if (generatorSet.size() == 0) {
//            return null;
//        }
//        return generatorSet.iterator().next();
//    }
//
//
//    private static Set<SemanticType> getRequiredGenerators(Map<String, String> paramMap) {
//        Set<SemanticType> generators = new HashSet<>();
//
//        for (String key : paramMap.keySet()) {
//            SemanticType stu = null;
//
//            // TODO: put something in the function it references
//            // If the actual semantic type doesn't match one defined with a generator, Check what the value declared in the configuration is
//            if ((stu = SemanticType.getByOntologySemanticType(key)) == null) {
//                Object o = paramMap.get(key);
//
//                if (o instanceof String) {
//                    String val = (String) o;
//
//                    if (val.startsWith(DATA_SOURCE)) {
//                        val = val.substring(DATA_SOURCE.length(), val.length());
//                        stu = SemanticType.valueOf(val);
//
//                        if (stu == null) {
//                            throw new RuntimeException("Could not find semantic type for key='" + key + "', value='" + val + "'!");
//                        }
//                    }
//                }
//            }
//            if (generators.contains(stu)) {
//                throw new RuntimeException("Only a single generator is supported at this time!");
//            }
//            if (stu != null) {
//                generators.add(stu);
//            }
//        }
//        return generators;
//    }
//
//
////    public String getAspect() {
////        return aspect;
////    }
//
////    public String getClassPackageIdentifier() {
////        return classPackageIdentifier;
////    }
//
////    public String getDataGeneratorClassPackageIdentifier() {
////        return dataGeneratorClassPackageIdentifier;
////    }
////
////    public String getDataGeneratorAspect() {
////        return dataGeneratorAspect;
////    }
//
////    public String getFunctionalityBeingPerformed() {
////        return functionalityBeingPerformed;
////    }
//
////    public ControlPointFormat getControlPointFormat() {
////        return controlPointFormat;
////    }
//
////    public boolean hasMatchForOneTimeUseParameter(@Nonnull String identifier) {
////        return (constructorParameterMap != null && constructorParameterMap.containsKey(identifier)) ||
////                (initializationParameterMap != null && initializationParameterMap.containsKey(identifier)) ||
////                (executionParameterMap != null && executionParameterMap.containsKey(identifier));
////    }
//
////    public synchronized Object createInstance(@Nullable Map<String, Object> parameterOverrides) throws ReflectiveOperationException {
////        if (classReflectionHelper == null) {
////            classReflectionHelper = new ClassReflectionHelper(classPackageIdentifier, aspect, functionalityBeingPerformed/*, returnSemanticTypeUri*/);
////        }
////
////        Constructor constructor = classReflectionHelper.getMatchingConstructors(constructorParameterMap).get(0);
////        Object[] params = classReflectionHelper.createValidParameterArray(constructor, constructorParameterMap, parameterOverrides);
////        return constructor.newInstance(params);
////    }
////
////
////    public synchronized void init(@Nonnull Object instance, @Nullable Map<String, Object> parameterOverrides) throws ReflectiveOperationException {
////        if (classReflectionHelper == null) {
////            classReflectionHelper = new ClassReflectionHelper(classPackageIdentifier, aspect, functionalityBeingPerformed/*, returnSemanticTypeUri*/);
////        }
////
////        List<Method> methods = classReflectionHelper.getMatchingInitMethods(initializationParameterMap);
////
////        if (methods != null && methods.size() > 0) {
////            Method method = classReflectionHelper.getMatchingInitMethods(initializationParameterMap).get(0);
////            Object[] params = classReflectionHelper.createValidParameterArray(method, initializationParameterMap, parameterOverrides);
////            method.invoke(instance, params);
////        }
////    }
//
////    public static synchronized String createConstructorLineRightSide(AnalysisModuleConfiguration moduleConfiguration, String objectName, Map<String, String> parameterOverrides) throws ReflectiveOperationException {
////        if (classReflectionHelper == null) {
////            classReflectionHelper = new ClassReflectionHelper(moduleConfiguration.classPackageIdentifier, moduleConfiguration.aspect, moduleConfiguration.functionalityBeingPerformed);
////        }
////
////        List<Constructor> constructors = classReflectionHelper.getMatchingConstructors(moduleConfiguration.constructorParameterMap);
////
////        if (constructors == null || constructors.isEmpty()) {
////            throw new RuntimeException("No matching constructors were found for the provided configuration!");
////        }
////
////        Constructor constructor = constructors.get(0);
////
////        String paramList = "";
////
////        if (constructor.getParameters() != null && constructor.getParameters().length > 0) {
////            String[] params = classReflectionHelper.createParameterIdentifierArray(constructor, moduleConfiguration.constructorParameterMap, parameterOverrides);
////
////            for (String param : params) {
////                if (paramList.equals("")) {
////                    paramList = param;
////                } else {
////                    paramList = paramList + ", " + param;
////                }
////            }
////        }
////
////        return "new " + moduleConfiguration.classPackageIdentifier + "(" + paramList + ");";
////    }
//
////    public synchronized String createInitializationLine(String objectName, Map<String, String> parameterOverrides) throws ReflectiveOperationException {
////        if (classReflectionHelper == null) {
////            classReflectionHelper = new ClassReflectionHelper(classPackageIdentifier, aspect, functionalityBeingPerformed);
////        }
////
////        List<Method> initMethods = classReflectionHelper.getMatchingInitMethods(initializationParameterMap);
////
////        if (initMethods == null || initMethods.isEmpty()) {
////            throw new RuntimeException("No matching constructors were found for the provided configuration!");
////        }
////
////        Method initMethod = initMethods.get(0);
////
////        String paramList = "";
////
////        if (initMethod.getParameters() != null && initMethod.getParameters().length > 0) {
////            String[] params = classReflectionHelper.createParameterIdentifierArray(initMethod, initializationParameterMap, parameterOverrides);
////
////            for (String param : params) {
////                if (paramList.equals("")) {
////                    paramList = param;
////                } else {
////                    paramList = paramList + ", " + param;
////                }
////            }
////        }
////
////        return objectName + "." + initMethod.getName() + "(" + paramList + ");";
////    }
//
////    public synchronized String createCleanupLine(String objectName) throws ReflectiveOperationException {
////        if (classReflectionHelper == null) {
////            classReflectionHelper = new ClassReflectionHelper(classPackageIdentifier, aspect, functionalityBeingPerformed);
////        }
////
////        Method cleanupMethod = classReflectionHelper.getCleanupMethod();
////
////        if (cleanupMethod == null) {
////            return "";
////        } else {
////            return objectName + "." + cleanupMethod.getName() + "();";
////        }
////    }
////
////    public synchronized String createExecutionLine(String objectName, Map<String,String> parameterOverrides) throws ReflectiveOperationException {
////        if (classReflectionHelper == null) {
////            classReflectionHelper = new ClassReflectionHelper(classPackageIdentifier, aspect, functionalityBeingPerformed);
////        }
////
////        List<Method> executionMethods = classReflectionHelper.getMatchingWorkMethods(executionParameterMap);
////
////        if (executionMethods == null || executionMethods.isEmpty()) {
////            throw new RuntimeException("No matching execution methods were found for the provided configuration!");
////        }
////
////        Method executionMethod = executionMethods.get(0);
////
////        String paramList = "";
////
////        if (executionMethod.getParameters() != null && executionMethod.getParameters().length > 0) {
////            String[] params = classReflectionHelper.createParameterIdentifierArray(executionMethod, executionParameterMap, parameterOverrides);
////
////            for (String param : params) {
////                if (paramList.equals("")) {
////                    paramList = param;
////                } else {
////                    paramList = paramList + ", " + param;
////                }
////            }
////        }
////
////        return objectName + "." + executionMethod.getName() + "(" + paramList + ");";
////    }
//
////    public synchronized Object execute(Object instance, Map<String, Object> parameterOverrides) throws ReflectiveOperationException {
////        if (classReflectionHelper == null) {
////            classReflectionHelper = new ClassReflectionHelper(classPackageIdentifier, aspect, functionalityBeingPerformed/*, returnSemanticTypeUri*/);
////        }
////
////        List<Method> methods = classReflectionHelper.getMatchingWorkMethods(executionParameterMap);
////
////        Method executionMethod = null;
////        for (Method candidateMethod : methods) {
////            FunctionalAspectAnnotation annotation = candidateMethod.getAnnotation(FunctionalAspectAnnotation.class);
////            if (annotation != null) {
////                if (annotation.aspect().getName().equals(aspect)) {
////                    executionMethod = candidateMethod;
////                    break;
////                }
////            }
////        }
////
////        Object[] params = classReflectionHelper.createValidParameterArray(executionMethod, executionParameterMap, parameterOverrides);
////        return executionMethod.invoke(instance, params);
////    }
//
//
////    public synchronized void cleanup(Object instance) throws ReflectiveOperationException {
////        if (classReflectionHelper == null) {
////            classReflectionHelper = new ClassReflectionHelper(classPackageIdentifier, aspect, functionalityBeingPerformed/*, returnSemanticTypeUri*/);
////        }
////
////        if (classReflectionHelper.getCleanupMethod() != null) {
////            classReflectionHelper.getCleanupMethod().invoke(instance);
////        }
////    }
//}
