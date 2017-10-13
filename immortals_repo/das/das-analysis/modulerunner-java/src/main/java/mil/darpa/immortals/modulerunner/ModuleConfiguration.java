package mil.darpa.immortals.modulerunner;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.core.annotations.FunctionalDfuAspect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 6/1/16.
 */
public class ModuleConfiguration {

    private final String classPackageIdentifier;

    private final String aspect;
    private final String functionalityBeingPerformed;

    private transient ClassReflectionHelper classReflectionHelper;
    private final HashMap<String, Object> constructorParameterMap;
    private final HashMap<String, Object> initializationParameterMap;
    private final HashMap<String, Object> executionParameterMap;

    public ModuleConfiguration(@Nonnull String classPackageIdentifier,
                               @Nonnull String aspect,
                               @Nonnull String functionalityBeingPerformed,
                               @Nullable Map<String, Object> constructorParameterMap,
                               @Nullable Map<String, Object> initializationParameterMap,
                               @Nullable Map<String, Object> executionParameterMap) throws ClassNotFoundException {
        this.classPackageIdentifier = classPackageIdentifier;

        this.aspect = aspect;
        this.functionalityBeingPerformed = functionalityBeingPerformed;

        if (constructorParameterMap == null) {
            this.constructorParameterMap = new HashMap<>();
        } else {
            this.constructorParameterMap = new HashMap<>(constructorParameterMap);
        }

        if (initializationParameterMap == null) {
            this.initializationParameterMap = new HashMap<>();
        } else {
            this.initializationParameterMap = new HashMap<>(initializationParameterMap);
        }

        if (executionParameterMap == null) {
            this.executionParameterMap = new HashMap<>();
        } else {
            this.executionParameterMap = new HashMap<>(executionParameterMap);
        }
    }

    public boolean hasMatchForOneTimeUseParameter(@Nonnull String identifier) {
        return (constructorParameterMap != null && constructorParameterMap.containsKey(identifier)) ||
                (initializationParameterMap != null && initializationParameterMap.containsKey(identifier)) ||
                (executionParameterMap != null && executionParameterMap.containsKey(identifier));
    }

    public synchronized Object createInstance(@Nullable Map<String, Object> parameterOverrides) throws ReflectiveOperationException {
        if (classReflectionHelper == null) {
            classReflectionHelper = new ClassReflectionHelper(classPackageIdentifier, aspect, functionalityBeingPerformed/*, returnSemanticTypeUri*/);
        }

        Constructor constructor = classReflectionHelper.getMatchingConstructors(constructorParameterMap).get(0);
        Object[] params = classReflectionHelper.createValidParameterArray(constructor, constructorParameterMap, parameterOverrides);
        return constructor.newInstance(params);
    }


    public synchronized void init(@Nonnull Object instance, @Nullable Map<String, Object> parameterOverrides) throws ReflectiveOperationException {
        if (classReflectionHelper == null) {
            classReflectionHelper = new ClassReflectionHelper(classPackageIdentifier, aspect, functionalityBeingPerformed/*, returnSemanticTypeUri*/);
        }

        List<Method> methods = classReflectionHelper.getMatchingInitMethods(initializationParameterMap);

        if (methods != null && methods.size() > 0) {
            Method method = classReflectionHelper.getMatchingInitMethods(initializationParameterMap).get(0);
            Object[] params = classReflectionHelper.createValidParameterArray(method, initializationParameterMap, parameterOverrides);
            method.invoke(instance, params);
        }
    }

    public synchronized Object execute(Object instance, Map<String, Object> parameterOverrides) throws ReflectiveOperationException {
        if (classReflectionHelper == null) {
            classReflectionHelper = new ClassReflectionHelper(classPackageIdentifier, aspect, functionalityBeingPerformed/*, returnSemanticTypeUri*/);
        }

        List<Method> methods = classReflectionHelper.getMatchingWorkMethods(executionParameterMap);

        Method executionMethod = null;
        for (Method candidateMethod : methods) {
            FunctionalAspectAnnotation annotation = candidateMethod.getAnnotation(FunctionalAspectAnnotation.class);
            if (annotation != null) {
                if (annotation.aspect().getName().equals(aspect)) {
                    executionMethod = candidateMethod;
                    break;
                }
            }
        }

        Object[] params = classReflectionHelper.createValidParameterArray(executionMethod, executionParameterMap, parameterOverrides);
        return executionMethod.invoke(instance, params);
    }


    public synchronized void cleanup(Object instance) throws ReflectiveOperationException {
        if (classReflectionHelper == null) {
            classReflectionHelper = new ClassReflectionHelper(classPackageIdentifier, aspect, functionalityBeingPerformed/*, returnSemanticTypeUri*/);
        }

        if (classReflectionHelper.getCleanupMethod() != null) {
            classReflectionHelper.getCleanupMethod().invoke(instance);
        }
    }
}
