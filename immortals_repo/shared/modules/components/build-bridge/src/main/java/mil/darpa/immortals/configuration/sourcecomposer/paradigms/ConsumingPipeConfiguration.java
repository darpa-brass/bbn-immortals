package mil.darpa.immortals.configuration.sourcecomposer.paradigms;

import mil.darpa.immortals.configuration.sourcecomposer.DeploymentPlatform;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.das.sourcecomposer.CompositionException;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines the details needed to specify the usage of a ConsumingPipe
 */
public class ConsumingPipeConfiguration extends AbstractParadigm {

    public ConsumingPipeConfiguration(@Nonnull String classPackage, @Nonnull String dependencyString,
                                      @Nonnull ArrayList<GenericParameterConfiguration> constructorParameters) {
        super(dependencyString, classPackage);
        this.constructorParameters = constructorParameters;
    }

    /**
     * Information to determine the proper constructor to duplicate.
     * <p>
     * The format is { java-type : value }.
     * <p>
     * If the value is null, it is expected to be passed through from the DFU Constructor or next object in the
     * pipeline. Otherwise, the value should be coded in as a String-based parameter value.
     * each pair a definition of { java-type : value } such as {"java.lang.String" : "ThisIsAString"}.
     * The ordering must match the ordering in which they are passed to the constructor.
     * <p>
     * If it's a DFU that doesn't take any parameters, this will be empty or omitted.
     * <p>
     * If it is a DFU that only has a next, it would be something like this depending on the object type being passed:
     * { "mil.darpa.immortals.core.synthesis.interfaces.WriteableObjectPipeInterface<android.graphics.Bitmap>" : null }
     * <p>
     * If it is a DFU that contains an integer parameter from the das, a String value passed from the
     * app/constructor, and a next from the app/constructor, it would be something like the following:
     * { "java.lang.Integer" : 54, "java.lang.String" : null,  "mil.darpa.immortals.core.synthesis.interfaces.WriteableObjectPipeInterface<android.graphics.Bitmap>" : null }
     */
    // This should ideally be final!!
    @Nonnull
    public final ArrayList<GenericParameterConfiguration> constructorParameters;

    private transient Class mClazz = null;
    private transient Type mInputJavaType = null;
    private transient Type mOutputJavaType = null;
    private transient Constructor mMatchingConstructor = null;

    public Class getDfuClassType() throws CompositionException {
        try {
            if (mClazz == null) {
                mClazz = Class.forName(classPackage);
            }
            return mClazz;
        } catch (ClassNotFoundException e) {
            throw new CompositionException.InvalidClaspathSpecifiedException(classPackage);
        }
    }

    public Type getInputJavaType() throws CompositionException {
        if (mInputJavaType != null) {
            return mInputJavaType;
        }

        Class clazz = getDfuClassType();

        for (Type t : clazz.getGenericInterfaces()) {
            if (t instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) t;
                if (pt.getRawType() == ConsumingPipe.class) {
                    mInputJavaType = pt.getActualTypeArguments()[0];
                    return mInputJavaType;
                }
            }
        }
        throw new CompositionException.ConsumingPipeUnexpectedException("Unable to parse generic input type from '" + classPackage + "");
    }

    public Type getOutputJavaType() throws CompositionException {
        if (mOutputJavaType != null) {
            return mOutputJavaType;
        }

        Constructor constructor = getConstructor();
        Type[] constructorParameters = constructor.getGenericParameterTypes();

        if (constructorParameters.length > 0) {
            Type lastParameter = constructorParameters[constructorParameters.length - 1];

            if (lastParameter instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) lastParameter;

                if (pt.getRawType() == ConsumingPipe.class) {
                    mOutputJavaType = pt.getActualTypeArguments()[0];
                }
            }
        }

        return mOutputJavaType;
    }

    public Constructor getConstructor() throws CompositionException {
        if (mMatchingConstructor != null) {
            return mMatchingConstructor;
        }

        try {
            Class classType = Class.forName(classPackage);

            for (Constructor c : classType.getConstructors()) {

                Parameter[] cParams = c.getParameters();
                if (cParams == null) {
                    cParams = new Parameter[0];
                }

                if (cParams.length != constructorParameters.size()) {
                    continue;
                }

                if (cParams.length == 0 && constructorParameters.size() == 0) {
                    mMatchingConstructor = c;
                    return mMatchingConstructor;
                }

                for (int i = 0; i < constructorParameters.size(); i++) {

                    if (cParams[i].getParameterizedType().toString().replaceAll(" ", "").equals(constructorParameters.get(i).classType.replaceAll(" ", ""))) {
                        mMatchingConstructor = c;
                        return mMatchingConstructor;
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            throw new CompositionException.InvalidClaspathSpecifiedException(classPackage);
        }

        throw new CompositionException.InvalidSpecifiedParametersException(classPackage, "...", "...");
    }

    public Set<ConsumingPipeConfiguration> produceAnalysisConfigurations() {

        Set<ArrayList<GenericParameterConfiguration>> parameterListSet = new HashSet<>();

        if (constructorParameters == null || constructorParameters.isEmpty()) {
            parameterListSet.add(new ArrayList<>());

        } else {

            for (GenericParameterConfiguration parameter : constructorParameters) {
                Set<GenericParameterConfiguration> parameterSet = parameter.produceAnalysisParameters();

                if (parameterSet != null) {
                    if (parameterListSet.isEmpty()) {
                        for (GenericParameterConfiguration param : parameterSet) {
                            ArrayList<GenericParameterConfiguration> newList = new ArrayList(1);
                            newList.add(param);
                            parameterListSet.add(newList);
                        }

                    } else {
                        Set<ArrayList<GenericParameterConfiguration>> newParameterListSet = new HashSet<>(parameterListSet.size() * parameterSet.size());

                        for (List<GenericParameterConfiguration> currentParamList : parameterListSet) {
                            for (GenericParameterConfiguration newParam : parameterSet) {
                                ArrayList<GenericParameterConfiguration> newList = new ArrayList<GenericParameterConfiguration>(currentParamList.size() + 1);
                                newList.addAll(currentParamList);
                                newList.add(newParam);
                                newParameterListSet.add(newList);
                            }
                        }
                        parameterListSet = newParameterListSet;
                    }
                }
            }
        }

        Set<ConsumingPipeConfiguration> returnSet = new HashSet<>(parameterListSet.size());
        for (ArrayList<GenericParameterConfiguration> parameters : parameterListSet) {
            returnSet.add(new ConsumingPipeConfiguration(classPackage, dependencyString, parameters));
        }
        return returnSet;
    }
}
