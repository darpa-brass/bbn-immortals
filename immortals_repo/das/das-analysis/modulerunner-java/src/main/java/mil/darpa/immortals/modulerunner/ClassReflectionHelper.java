package mil.darpa.immortals.modulerunner;

import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherCleanup;
import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherInitialize;
import com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorCleanup;
import com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorInitialize;
import com.securboration.immortals.ontology.functionality.logger.AspectLoggerCleanup;
import com.securboration.immortals.ontology.functionality.logger.AspectLoggerInitialize;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.core.annotations.SemanticTypeBinding;
import mil.darpa.immortals.core.synthesis.annotations.dfu.SynthesisWork;

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

    public static final String FILL_IN_VALUE_KEY = "$PROVIDED_BY_APPLICATION!";

    // TODO: Cache the results from reflection invocations to minimize excessive cpu cycles
    private final Class clazz;

    private final Constructor<?>[] constructors;
    private final LinkedList<Method> initMethods;
    private final LinkedList<Method> workMethods;
    private final Method cleanupMethod;

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


                if (functionalAspectAnnotation instanceof AspectCipherCleanup ||
                        functionalAspectAnnotation instanceof AspectLoggerCleanup ||
                        functionalAspectAnnotation instanceof AspectImageProcessorCleanup) {
                    initMethods.add(candidateMethod);

                } else if (functionalAspectAnnotation instanceof AspectCipherInitialize ||
                        functionalAspectAnnotation instanceof AspectLoggerInitialize ||
                        functionalAspectAnnotation instanceof AspectImageProcessorInitialize) {
                    mutableCleanupMethod = candidateMethod;

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
    private static List<Method> getMatchingMethodsWithParameterAnnotationMap(@Nonnull Class parentClass, @Nullable Map<String, Object> baseAnnotationParameterObjectMap, List<Method> candidateMethodList) {
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


    private static boolean doesParameterAnnotationMapMatch(@Nullable Map<String, Object> parameterMap, @Nullable Parameter[] parameters) {
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

        HashMap<String, Object> availableParameterMap = new HashMap<>(parameterMap);

        for (Parameter parameter : parameters) {


            Annotation[]  annotations = parameter.getAnnotations();

            if (annotations == null || annotations.length <= 0) {
                return false;

            } else {


                for (Annotation annotation : annotations) {
                    String semanticType = annotation.annotationType().getName();
                    if (availableParameterMap.containsKey(semanticType)) {
                        if (parameter.getType().isAssignableFrom(availableParameterMap.get(semanticType).getClass()) ||
                                FILL_IN_VALUE_KEY.equals(availableParameterMap.get(semanticType))) {
                            availableParameterMap.remove(annotation.getClass().getSimpleName());
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public List<Constructor> getMatchingConstructors(@Nullable Map<String, Object> parameterMap) {
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

    public static Object[] createValidParameterArray(@Nonnull Constructor parentConstructor, @Nullable Map<String, Object> baseParameterMap, @Nullable Map<String, Object> parameterOverrideMap) {
        return createValidParameterArray(parentConstructor, parentConstructor.getParameters(), baseParameterMap, parameterOverrideMap);
    }

    public static Object[] createValidParameterArray(@Nonnull Method parentMethod, @Nullable Map<String, Object> baseParameterMap, @Nullable Map<String, Object> parameterOverrideMap) {
        return createValidParameterArray(parentMethod, parentMethod.getParameters(), baseParameterMap, parameterOverrideMap);
    }


    @Nullable
    private static Object[] createValidParameterArray(@Nonnull Object parentMethodOrConstructor, @Nullable Parameter[] parameters, @Nullable Map<String, Object> baseParameterMap, @Nullable Map<String, Object> parameterOverrideMap) {
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

                if (FILL_IN_VALUE_KEY.equals(baseParameterValue)) {
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

    public List<Method> getMatchingInitMethods(@Nullable Map<String, Object> parameterMap) {
        return getMatchingMethodsWithParameterAnnotationMap(clazz, parameterMap, initMethods);
    }

    public List<Method> getMatchingWorkMethods(@Nullable Map<String, Object> parameterMap) {
        return getMatchingMethodsWithParameterAnnotationMap(clazz, parameterMap, workMethods);
    }

    public Method getCleanupMethod() {
        return cleanupMethod;
    }
}
