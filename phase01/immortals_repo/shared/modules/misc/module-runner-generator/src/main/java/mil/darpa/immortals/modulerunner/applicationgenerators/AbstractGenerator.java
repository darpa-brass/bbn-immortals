//package mil.darpa.immortals.modulerunner.applicationgenerators;
//
//import mil.darpa.immortals.modulerunner.ClassReflectionHelper;
//import mil.darpa.immortals.modulerunner.configuration.AnalysisGeneratorConfiguration;
//import mil.darpa.immortals.modulerunner.configuration.AnalysisModuleConfiguration;
//import mil.darpa.immortals.modulerunner.generators.SemanticType;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.io.IOException;
//import java.lang.reflect.Constructor;
//import java.lang.reflect.Field;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.*;
//
///**
// * Created by awellman@bbn.com on 8/19/16.
// */
//public abstract class AbstractGenerator {
//
//    protected final String CONTROL_POINT_BASIC_DECLARATION = "$B49219A8-493B-4639-8F94-73EEC496C523-declaration";
//    protected final String CONTROL_POINT_BASIC_INITIALIZATION = "$B49219A8-493B-4639-8F94-73EEC496C523-init";
//    protected final String CONTROL_POINT_BASIC_USAGE = "$B49219A8-493B-4639-8F94-73EEC496C523-work";
//    protected final String CONTROL_POINT_BASIC_CLEANUP = "$B49219A8-493B-4639-8F94-73EEC496C523-cleanup";
//
//    private final Map<AnalysisModuleConfiguration, ClassReflectionHelper> moduleReflectionHelpers = new HashMap<>();
//
//    protected final Class insertionClass;
//
//    protected final List<AnalysisModuleConfiguration> moduleConfigurations;
//
//    protected final AnalysisGeneratorConfiguration generatorConfig;
//    protected final String generatorConfigIdentifier;
//    protected final String generatorIdName;
//
//
//    public AbstractGenerator(@Nonnull List<AnalysisModuleConfiguration> moduleConfigurations, @Nonnull Class insertionClass, @Nullable AnalysisGeneratorConfiguration generatorConfiguration) throws ClassNotFoundException {
//        this.moduleConfigurations = moduleConfigurations;
//        this.insertionClass = insertionClass;
//
//        if (generatorConfiguration != null) {
//            this.generatorConfig = generatorConfiguration;
//            this.generatorConfigIdentifier = "generatorConfiguration_" + UUID.randomUUID().toString().substring(0, 4);
//            this.generatorIdName = "dataGenerator_" + UUID.randomUUID().toString().substring(0, 4);
//
//        } else {
//            SemanticType generatorType = null;
//
//            for (AnalysisModuleConfiguration moduleConfiguration : moduleConfigurations) {
//                SemanticType innerGeneratorType = moduleConfiguration.getNeededGeneratorType();
//                if (innerGeneratorType != null) {
//                    if (generatorType != null) {
//                        throw new RuntimeException("Only a single generator is supported at this time!");
//                    }
//                    generatorType = innerGeneratorType;
//                }
//            }
//
//            if (generatorType == null) {
//                this.generatorConfig = null;
//                this.generatorConfigIdentifier = null;
//                this.generatorIdName = null;
//
//            } else {
//                this.generatorConfig = generatorType.constructDefaultGenerator();
//                this.generatorConfigIdentifier = "generatorConfiguration_" + UUID.randomUUID().toString().substring(0, 4);
//                this.generatorIdName = "dataGenerator_" + UUID.randomUUID().toString().substring(0, 4);
//            }
//        }
//
//    }
//
//    protected synchronized ClassReflectionHelper getReflectionHelper(AnalysisModuleConfiguration moduleConfiguration) throws ClassNotFoundException {
//        ClassReflectionHelper classReflectionHelper = moduleReflectionHelpers.get(moduleConfiguration);
//
//        if (classReflectionHelper == null) {
//            classReflectionHelper = new ClassReflectionHelper(moduleConfiguration);
//            moduleReflectionHelpers.put(moduleConfiguration, classReflectionHelper);
//        }
//        return classReflectionHelper;
//
//    }
//
//    public final void regenerateFile(@Nonnull String filepath) throws IOException, ReflectiveOperationException {
//
//        boolean declarationAdded = false;
//        boolean initializationAdded = false;
//        boolean usageAdded = false;
//        boolean cleanupAdded = false;
//
//        List<String> sourceLines = Files.readAllLines(Paths.get(filepath));
//        List<String> targetLines = new LinkedList<>();
//
//        for (String sourceLine : sourceLines) {
//            if (sourceLine.contains(CONTROL_POINT_BASIC_DECLARATION)) {
//                if (!declarationAdded) {
//                    declarationAdded = true;
//
//                    if (generatorConfigIdentifier != null && generatorIdName != null) {
//                        targetLines.add("mil.darpa.immortals.modulerunner.configuration.AnalysisGeneratorConfiguration " + generatorConfigIdentifier + "; // " + CONTROL_POINT_BASIC_DECLARATION);
//                        targetLines.add("mil.darpa.immortals.sourcecomposer.generators.javaclasstypes.AbstractDataGenerator " + generatorIdName + "; // " + CONTROL_POINT_BASIC_DECLARATION);
//                    }
//
//                    List<String> declarationLines = createDeclarationLines();
//                    for (String line : declarationLines) {
//                        targetLines.add(line + " // " + CONTROL_POINT_BASIC_DECLARATION);
//                    }
//                }
//
//
//            } else if (sourceLine.contains(CONTROL_POINT_BASIC_INITIALIZATION)) {
//                if (!initializationAdded) {
//                    initializationAdded = true;
//
//                    if (generatorConfigIdentifier != null) {
//                        targetLines.add(generatorConfigIdentifier + " = " + generatorConfig.AnalysisGeneratorConfigurationDeclarationClone() + "; // " + CONTROL_POINT_BASIC_INITIALIZATION);
//                    }
//
//                    List<String> initializationLines = createInitializationLines();
//                    for (String line : initializationLines) {
//                        targetLines.add(line + " // " + CONTROL_POINT_BASIC_INITIALIZATION);
//                    }
//                }
//
//            } else if (sourceLine.contains(CONTROL_POINT_BASIC_USAGE)) {
//                if (!usageAdded) {
//                    usageAdded = true;
//
//                    List<String> executionLines = createExecutionLines();
//                    for (String line : executionLines) {
//                        targetLines.add(line + " // " + CONTROL_POINT_BASIC_USAGE);
//                    }
//                }
//
//            } else if (sourceLine.contains(CONTROL_POINT_BASIC_CLEANUP)) {
//                if (!cleanupAdded) {
//                    cleanupAdded = true;
//
//                    List<String> cleanupLines = createCleanupLines();
//                    for (String line : cleanupLines) {
//                        targetLines.add(line + " // " + CONTROL_POINT_BASIC_CLEANUP);
//                    }
//                }
//
//            } else {
//                targetLines.add(sourceLine);
//            }
//        }
//
//        Files.write(Paths.get(filepath), targetLines);
//    }
//
//    protected abstract List<String> createDeclarationLines() throws ReflectiveOperationException;
//
//    protected abstract List<String> createInitializationLines() throws ReflectiveOperationException;
//
//    protected abstract List<String> createExecutionLines() throws ReflectiveOperationException;
//
//    protected abstract List<String> createCleanupLines() throws ReflectiveOperationException;
//
//    protected final Map<String, String> fieldFiller(Set<String> missingParameters, Class clazz, Map<String, String> customParameters) throws ClassNotFoundException {
//        Map<String, String> availableParameters = new HashMap<>();
//
//        for (String missing : missingParameters) {
//
//            if (customParameters != null && customParameters.containsKey(missing)) {
//                availableParameters.put(missing, customParameters.get(missing));
//
//            } else {
//                Class annotationClass = Class.forName(missing);
//                boolean found = false;
//
//                for (Field field : clazz.getFields()) {
//                    if (field.getAnnotation(annotationClass) != null) {
//                        availableParameters.put(missing, field.getName());
//                        found = true;
//                        break;
//                    }
//
//                }
//
//                if (!found) {
//                    throw new RuntimeException("No public field is available in '" + clazz.getName() + "' that matches the annotation '" + annotationClass.getName() + "'!");
//                }
//            }
//        }
//        return availableParameters;
//    }
//
//
//    protected final List<String> encloseInTimedGeneratorIterator(List<String> linesToEnclose) {
//        List<String> returnList = new LinkedList<>();
//
//        returnList.add("try {");
//        returnList.add("    while (" + generatorIdName + ".hasMore()) {");
//        for (String line : linesToEnclose) {
//            returnList.add("        " + line);
//        }
//        returnList.add("        Thread.sleep(" + generatorIdName + ".getIntervalMS());");
//        returnList.add("    }");
//        returnList.add("} catch (InterruptedException e) {");
//        returnList.add("    throw new RuntimeException(e);");
//        returnList.add("}");
//
//        return returnList;
//    }
//
//    public synchronized String createConstructorLineRightSide(@Nonnull AnalysisModuleConfiguration moduleConfiguration, @Nonnull Map<String, String> parameterOverrides) throws ReflectiveOperationException {
//        ClassReflectionHelper classReflectionHelper = getReflectionHelper(moduleConfiguration);
//
//        List<Constructor> constructors = classReflectionHelper.getMatchingConstructors(moduleConfiguration.constructorParameterMap);
//
//        if (constructors == null || constructors.isEmpty()) {
//            throw new RuntimeException("No matching constructors were found for the provided configuration!");
//        }
//
//        Constructor constructor = constructors.get(0);
//
//        String paramList = "\n";
//
//        if (constructor.getParameters() != null && constructor.getParameters().length > 0) {
//            String[] params = classReflectionHelper.createParameterIdentifierArray(constructor, moduleConfiguration.constructorParameterMap, parameterOverrides);
//
//            for (String param : params) {
//                if (paramList.equals("\n")) {
//                    paramList = param;
//                } else {
//                    paramList = paramList + ", \n" + param;
//                }
//            }
//        }
//
//        return "new " + moduleConfiguration.classPackageIdentifier + "(\n" + paramList + "\n)";
//    }
//}
