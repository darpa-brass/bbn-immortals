package mil.darpa.immortals.modulerunner;

import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherCleanup;
import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherInitialize;
import com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorCleanup;
import com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorInitialize;
import com.securboration.immortals.ontology.functionality.locationprovider.CleanupAspect;
import com.securboration.immortals.ontology.functionality.logger.AspectLoggerCleanup;
import com.securboration.immortals.ontology.functionality.logger.AspectLoggerInitialize;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.modulerunner.configuration.AnalysisModuleConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Assumptions: No parameter in an annotated method can be null
 * Each parameter can only have a single annotation
 * <p>
 * Created by awellman@bbn.com on 6/9/16.
 */
public class ClassReflectionHelper {


    // TODO: Cache the results from reflection invocations to minimize excessive cpu cycles
    private final Class clazz;

    private final Constructor<?>[] constructors;
    private final LinkedList<Method> initMethods;
    private final LinkedList<Method> workMethods;
    private final Method cleanupMethod;

    public ClassReflectionHelper(@Nonnull AnalysisModuleConfiguration configuration) throws ClassNotFoundException {
        this(configuration.classPackageIdentifier, configuration.aspect, configuration.functionalityBeingPerformed);
    }

    public ClassReflectionHelper(@Nonnull String classIdentifier,
                                 @Nonnull String aspect,
                                 @Nonnull String functionalityBeingPerformed
    ) throws ClassNotFoundException {

        clazz = Class.forName(classIdentifier);
        initMethods = new LinkedList<>();
        workMethods = new LinkedList<>();
        Method mutableCleanupMethod = null;

        constructors = clazz.getConstructors();

        Method[] candidateMethods = clazz.getMethods();

        for (Method candidateMethod : candidateMethods) {
            FunctionalAspectAnnotation functionalAspectAnnotation = candidateMethod.getAnnotation(FunctionalAspectAnnotation.class);


            if (functionalAspectAnnotation != null) {

                String meh = functionalAspectAnnotation.aspect().getTypeName();
                String meh2 = functionalAspectAnnotation.aspect().getClass().getName();
                String meh3 = functionalAspectAnnotation.aspect().getName();


                if (functionalAspectAnnotation.aspect() == CleanupAspect.class ||
                        functionalAspectAnnotation instanceof AspectCipherCleanup ||
                        functionalAspectAnnotation instanceof AspectLoggerCleanup ||
                        functionalAspectAnnotation instanceof AspectImageProcessorCleanup) {
                    mutableCleanupMethod = candidateMethod;

                } else if (functionalAspectAnnotation.aspect() == com.securboration.immortals.ontology.functionality.InitializeAspect.class ||
                        functionalAspectAnnotation.aspect() == com.securboration.immortals.ontology.functionality.locationprovider.InitializeAspect.class ||
                        functionalAspectAnnotation instanceof AspectCipherInitialize ||
                        functionalAspectAnnotation instanceof AspectLoggerInitialize ||
                        functionalAspectAnnotation instanceof AspectImageProcessorInitialize) {
                    initMethods.add(candidateMethod);

                } else if (functionalAspectAnnotation.aspect().getTypeName().equals(aspect)) {
                    workMethods.add(candidateMethod);
                }
            }
        }
        cleanupMethod = mutableCleanupMethod;
    }

    /**
     * Gets a list of matching methods, ordered by how closely they match based on total parameters from the parameter
     * map utilized
     *
     * @param candidateMethodList
     * @return
     */
    @Nullable
    private static List<Method> getMatchingMethodsWithParameterAnnotationMap(@Nonnull Class parentClass, @Nullable Map<String, String> baseAnnotationParameterObjectMap, List<Method> candidateMethodList) {
        LinkedList<Method> candidateMethods = new LinkedList<>();

        for (Method candidateMethod : candidateMethodList) {

            if (doesParameterAnnotationMapMatch(baseAnnotationParameterObjectMap, candidateMethod.getParameters())) {
                candidateMethods.add(candidateMethod);
            }
        }

        // If there is only one, return it.
        if (candidateMethods.size() == 1) {
            return candidateMethods;

        } else if (candidateMethods.size() == 0) {
            if (baseAnnotationParameterObjectMap != null && baseAnnotationParameterObjectMap.size() > 0) {
                throw new RuntimeException("Parameters have been specified for the constructor or method in a DFU of type '" + parentClass.toString() + "' but no matching method or constructor could be found!");
            } else {
                return null;
            }

        } else {

            // Otherwise, create a new list sorted by the number of available parameters utilized
            LinkedList<Method> newCandidateList = new LinkedList<>();

            for (int i = baseAnnotationParameterObjectMap.keySet().size(); i >= 0; --i) {
                final int parameterCount = i;

                List<Method> sizeList = candidateMethods.stream().filter(
                        m -> m.getParameters().length == parameterCount).collect(Collectors.toList());

                newCandidateList.addAll(sizeList);
            }

            if ((newCandidateList == null || newCandidateList.isEmpty()) && (baseAnnotationParameterObjectMap != null && !baseAnnotationParameterObjectMap.isEmpty())) {
                throw new RuntimeException("Parameters have been specified for the constructor or method in a DFU of type '" + parentClass.toString() + "' but no matching method or constructor could be found!");
            }
            return newCandidateList;
        }
    }

    private static boolean isMethodFunctionalityMatch(@Nonnull String functionalityBeingPerformed, @Nonnull String aspect, /*@Nullable String semanticTypeUri, */@Nonnull Method candidateMethod, @Nonnull Class candidateMethodClass) {
        FunctionalAspectAnnotation functionalAspectAnnotation = candidateMethod.getAnnotation(FunctionalAspectAnnotation.class);
        DfuAnnotation dfuAnnotation = (DfuAnnotation) candidateMethodClass.getAnnotation(DfuAnnotation.class);

        // If there is no functionalDfuAsepct this is not a usable method
        if (aspect == null) {
            return false;
        }

        String methodAspect = functionalAspectAnnotation.aspect().getName();
        String methodFunctionalityBeingPerformed = dfuAnnotation.functionalityBeingPerformed().getName();

        if (methodAspect.equals(aspect) &&
                methodFunctionalityBeingPerformed.equals(functionalityBeingPerformed)) {
            return true;
        } else {
            return false;
        }
    }


    private static boolean doesParameterAnnotationMapMatch(@Nullable Map<String, String> parameterMap, @Nullable Parameter[] parameters) {
        if (parameterMap != null) {
            if (parameters != null) {
                if (parameters.length == parameterMap.size()) {
                    // Continue on with parameter type validation
                } else {
                    return false;
                }
            } else {
                return false;
            }

        } else {
            if (parameters != null) {
                return (parameters.length == 0);
            } else {
                return true;
            }
        }

        HashMap<String, String> availableParameterMap = new HashMap<>(parameterMap);

        for (Parameter parameter : parameters) {


            Annotation[] annotations = parameter.getAnnotations();

            if (annotations == null || annotations.length <= 0) {
                return false;

            } else {

                for (Annotation annotation : annotations) {
                    String annotationSemanticType = annotation.annotationType().getName();
                    String matchingAnnotationValue = availableParameterMap.get(annotationSemanticType);

                    Class parameterType = parameter.getType();

                    if ((matchingAnnotationValue != null) && (parameterType.isAssignableFrom(matchingAnnotationValue.getClass()) ||
                            matchingAnnotationValue.toString().startsWith(AnalysisModuleConfiguration.FILL_IN_VALUE_KEY) ||
                            matchingAnnotationValue.toString().startsWith(AnalysisModuleConfiguration.DATA_SOURCE) ||
                            matchingAnnotationValue.toString().startsWith(AnalysisModuleConfiguration.ATTACHED_DFU) ||
                            MappingHelper.doesMap(parameter, matchingAnnotationValue))
                            ) {
                        availableParameterMap.remove(annotation.getClass().getSimpleName());
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public List<Constructor> getMatchingConstructors(@Nullable Map<String, String> parameterMap) {
        // Methods that may have @Nullable annotation to indicate possible matches
        LinkedList<Constructor> candidateConstructors = new LinkedList<>();

        for (Constructor candidateConstructor : constructors) {
            if (doesParameterAnnotationMapMatch(parameterMap, candidateConstructor.getParameters())) {
                candidateConstructors.add(candidateConstructor);
            }
        }

        // If there is only one, return it.
        if (candidateConstructors.size() == 1) {
            return candidateConstructors;

        } else {

            // Otherwise, create a new list sorted by the number of available parameters utilized
            LinkedList<Constructor> newConstructorList = new LinkedList<>();

            for (int i = parameterMap.size(); i >= 0; --i) {
                final int parameterCount = i;

                List<Constructor> sizeList = candidateConstructors.stream().filter(
                        m -> m.getParameters().length == parameterCount).collect(Collectors.toList());

                newConstructorList.addAll(sizeList);
            }
            return newConstructorList;
        }
    }

    public static Object[] createValidParameterArray(@Nonnull Constructor parentConstructor, @Nullable Map<String, String> baseParameterMap, @Nullable Map<String, String> parameterOverrideMap) {
        return createValidParameterArray(parentConstructor, parentConstructor.getParameters(), baseParameterMap, parameterOverrideMap);
    }

    public static Object[] createValidParameterArray(@Nonnull Method parentMethod, @Nullable Map<String, String> baseParameterMap, @Nullable Map<String, String> parameterOverrideMap) {
        return createValidParameterArray(parentMethod, parentMethod.getParameters(), baseParameterMap, parameterOverrideMap);
    }


    @Nullable
    private static Object[] createValidParameterArray(@Nonnull Object parentMethodOrConstructor, @Nullable Parameter[] parameters, @Nullable Map<String, String> baseParameterMap, @Nullable Map<String, String> parameterOverrideMap) {
        boolean paramsNone = (parameters == null || parameters.length <= 0);
        boolean paramValuesNone = (baseParameterMap == null || baseParameterMap.size() <= 0);

        Object[] parameterArray = null;

        boolean failure = false;

        if (paramsNone && paramValuesNone) {
            // If they are both empty, return null
            return null;

        } else if (paramsNone != paramValuesNone) {
            // If they are not equal, we appear to have a missmatch, so failure
            failure = true;
        }

        if (!failure) {
            // Otherwise, analyze the contents
            parameterArray = new Object[parameters.length];
            String parameterAnnotation = null;
            Object baseParameterValue = null;
            Object parameterOverrideValue = null;

            forloop:
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];

                if (parameter.getAnnotations() != null && parameter.getAnnotations().length > 1) {
                    throw new RuntimeException("Parameters with multiple annotations are not currently supported!");
                }
                parameterAnnotation = parameter.getAnnotations()[0].annotationType().getName();
                baseParameterValue = baseParameterMap.get(parameterAnnotation);
                parameterOverrideValue = parameterOverrideMap.get(parameterAnnotation);

                if (AnalysisModuleConfiguration.FILL_IN_VALUE_KEY.equals(baseParameterValue) ||
                        baseParameterValue.toString().startsWith(AnalysisModuleConfiguration.DATA_SOURCE)) {

                    if (parameterOverrideValue != null) {
                        // If the value needs an override and it is available, validates the type and use it
                        if (parameter.getType().isAssignableFrom(parameterOverrideValue.getClass())) {
                            parameterArray[i] = parameterOverrideValue;
                        } else {
                            // If the value needs an override and it is not available, failure.
                            failure = true;
                            break forloop;
                        }

                    } else {
                        // If the value needs an override and it is not available, failure
                        failure = true;
                        break forloop;
                    }
                } else {
                    if (parameter.getType().isAssignableFrom(baseParameterValue.getClass())) {
                        parameterArray[i] = baseParameterValue;
                    } else {
                        failure = true;
                        break forloop;
                    }
                }

                // If an override has been offered but is not applicable, ignore it
            }
        }

        if (failure) {
            String exceptionString = "Constructor or Method '" + parentMethodOrConstructor + "' with parameters '" +
                    (paramsNone ? "" : Arrays.toString(parameters)) + "' does not match with parameterMap containing '" +
                    (paramValuesNone ? "" : Arrays.toString(baseParameterMap.keySet().toArray())) + "'.";
            throw new RuntimeException(exceptionString);
        }

        return parameterArray;
    }


    public static String[] createParameterIdentifierArray(@Nonnull Constructor parentConstructor, @Nullable Map<String, String> baseParameterMap, @Nullable Map<String, String> parameterOverrideMap) {
        return createParameterIdentifierArray(parentConstructor, parentConstructor.getParameters(), baseParameterMap, parameterOverrideMap);
    }

    public static String[] createParameterIdentifierArray(@Nonnull Method parentMethod, @Nullable Map<String, String> baseParameterMap, @Nullable Map<String, String> parameterOverrideMap) {
        return createParameterIdentifierArray(parentMethod, parentMethod.getParameters(), baseParameterMap, parameterOverrideMap);
    }

    @Nullable
    private static String[] createParameterIdentifierArray(@Nonnull Object parentMethodOrConstructor, @Nullable Parameter[] parameters, @Nullable Map<String, String> baseParameterMap, @Nullable Map<String, String> parameterOverrideMap) {
        boolean paramsNone = (parameters == null || parameters.length <= 0);
        boolean paramValuesNone = (baseParameterMap == null || baseParameterMap.size() <= 0);

        String[] parameterArray = null;

        boolean failure = false;

        if (paramsNone && paramValuesNone) {
            // If they are both empty, return null
            return null;

        } else if (paramsNone != paramValuesNone) {
            // If they are not equal, we appear to have a missmatch, so failure
            failure = true;
        }

        if (!failure) {
            // Otherwise, analyze the contents
            parameterArray = new String[parameters.length];
            String parameterAnnotation = null;
            String baseParameterValue = null;
            String parameterOverrideValue = null;

            forloop:
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];

                if (parameter.getAnnotations() != null && parameter.getAnnotations().length > 1) {
                    throw new RuntimeException("Parameters with multiple annotations are not currently supported!");
                }
                parameterAnnotation = parameter.getAnnotations()[0].annotationType().getName();
                baseParameterValue = baseParameterMap.get(parameterAnnotation);
                parameterOverrideValue = parameterOverrideMap.get(parameterAnnotation);

                if (baseParameterValue != null && (AnalysisModuleConfiguration.FILL_IN_VALUE_KEY.equals(baseParameterValue) ||
                        baseParameterValue.startsWith(AnalysisModuleConfiguration.DATA_SOURCE) ||
                        baseParameterValue.startsWith(AnalysisModuleConfiguration.ATTACHED_DFU))) {

                    if (parameterOverrideValue != null) {
                        parameterArray[i] = parameterOverrideValue;
                    } else {
                        // If the value needs an override and it is not available, failure
                        failure = true;
                        break forloop;
                    }
                } else {
                    if (MappingHelper.doesMap(parameter, baseParameterValue)) {
//                    if (parameter.getType().isAssignableFrom(baseParameterValue.getClass())) {
                        parameterArray[i] = baseParameterValue;
                    } else {
                        failure = true;
                        break forloop;
                    }
                }

                // If an override has been offered but is not applicable, ignore it
            }
        }

        if (failure) {
            String exceptionString = "Constructor or Method '" + parentMethodOrConstructor + "' with parameters '" +
                    (paramsNone ? "" : Arrays.toString(parameters)) + "' does not match with parameterMap containing '" +
                    (paramValuesNone ? "" : Arrays.toString(baseParameterMap.keySet().toArray())) + "'.";
            throw new RuntimeException(exceptionString);
        }

        return parameterArray;
    }

    public List<Method> getMatchingInitMethods(@Nullable Map<String, String> parameterMap) {
        return getMatchingMethodsWithParameterAnnotationMap(clazz, parameterMap, initMethods);
    }

    public List<Method> getMatchingWorkMethods(@Nullable Map<String, String> parameterMap) {
        return getMatchingMethodsWithParameterAnnotationMap(clazz, parameterMap, workMethods);
    }

    public Method getCleanupMethod() {
        return cleanupMethod;
    }


    public static class MappingHelper {

        public static boolean doesMap(Parameter parameter, String value) {
            String parameterTypeName = parameter.getType().toString();

            if (parameterTypeName.equals("double")) {
                if (tryParseDouble(value) != null) {
                    return true;
                }
            }

            if (parameterTypeName.equals("int")) {
                if (tryParseInt(value) != null) {
                    return true;
                }
            }

            if (parameterTypeName.equals("float")) {
                if (tryParseFloat(value) != null) {
                    return true;
                }
            }

            if (parameterTypeName.equals("short")) {
                if (tryParseShort(value) != null) {
                    return true;
                }
            }

            if (parameterTypeName.equals("long")) {
                if (tryParseLong(value) != null) {
                    return true;
                }
            }

            if (parameterTypeName.equals("byte")) {
                if (tryParseByte(value) != null) {
                    return true;
                }
            }

            if (parameterTypeName.equals("char")) {
                if (tryParseChar(value) != null) {
                    return true;
                }
            }

            if (parameterTypeName.equals("boolean")) {
                if (tryParseBoolean(value) != null) {
                    return true;
                }
            }

            return false;
        }

        private static Double tryParseDouble(String value) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                // I don't always catch exceptions as expected code, but when I do, it's when stack overflow indicates it is reasonable speed-wise
                return null;
            }
        }

        private static Integer tryParseInt(String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // I don't always catch exceptions as expected code, but when I do, it's when stack overflow indicates it is reasonable speed-wise
                return null;
            }
        }

        private static Float tryParseFloat(String value) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                // I don't always catch exceptions as expected code, but when I do, it's when stack overflow indicates it is reasonable speed-wise
                return null;
            }
        }

        private static Short tryParseShort(String value) {
            try {
                return Short.parseShort(value);
            } catch (NumberFormatException e) {
                // I don't always catch exceptions as expected code, but when I do, it's when stack overflow indicates it is reasonable speed-wise
                return null;
            }
        }

        private static Long tryParseLong(String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                // I don't always catch exceptions as expected code, but when I do, it's when stack overflow indicates it is reasonable speed-wise
                return null;
            }
        }

        private static Byte tryParseByte(String value) {
            try {
                return Byte.parseByte(value);
            } catch (NumberFormatException e) {
                // I don't always catch exceptions as expected code, but when I do, it's when stack overflow indicates it is reasonable speed-wise
                return null;
            }
        }

        private static Character tryParseChar(String value) {
            if (value.length() == 1) {
                return value.charAt(0);
            }
            return null;
        }

        private static Boolean tryParseBoolean(String value) {
            return Boolean.parseBoolean(value);
        }

    }

}
