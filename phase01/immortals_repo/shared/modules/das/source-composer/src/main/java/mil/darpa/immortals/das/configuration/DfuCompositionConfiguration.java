package mil.darpa.immortals.das.configuration;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.das.sourcecomposer.CompositionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A configuration used to define the composition of DFUs
 * <p>
 * Created by awellman@bbn.com on 9/27/16.
 */
public class DfuCompositionConfiguration {

    public String getProductClasspath() {
        EnvironmentConfiguration ec = EnvironmentConfiguration.getInstance();
        return ec.getSynthesizedDfuPackage() + (ec.getSynthesizedDfuPackage().endsWith(".") ? "" : ".") + getProductBaseIdentifier();
    }

    public String getProductDependencyIdentifier() {
        EnvironmentConfiguration ec = EnvironmentConfiguration.getInstance();
        return ec.getSynthesizedDfuPackage() + ":" + getProductBaseIdentifier() + ":+";
    }

    public String getProductBaseIdentifier() {
        if (sessionIdentifier == null) {
            sessionIdentifier = "S" + Long.toString(System.currentTimeMillis());
        }

        if (compositionIdentifier == null) {

            if (originalDfu.consumingPipeSpecification != null) {

                String newName = "";
                for (DfuCompositionConfiguration.DfuConfigurationContainer dfu : dfuCompositionSequence) {
                    String[] packageIdentifier = dfu.consumingPipeSpecification.classPackageIdentifier.split("\\.");
                    newName += packageIdentifier[packageIdentifier.length - 1];

                }
                newName += "_" + sessionIdentifier;
                compositionIdentifier = newName;
            }
        }
        return compositionIdentifier;
    }

    public String getSessionIdentifier() {
        return sessionIdentifier;
    }

    public Path getProductDirectory() {
        return EnvironmentConfiguration.getInstance().getSynthesizedDfuProjectFilepath(sessionIdentifier).resolve(getProductBaseIdentifier());
    }

    public Path getProductSourceDirectory() {
        return getProductDirectory().resolve(EnvironmentConfiguration.getInstance().getSynthesizedDfuSourceSubdirectory());
    }

    /**
     * The identifier for the newly-formed DFU
     */
    @Nullable
    private String compositionIdentifier;

    /**
     * The session identifier
     */
     @Nullable
     String sessionIdentifier;

    /**
     * The DFU whom the newly created DFU must adhere to in terms of interface
     */
    @Nonnull
    public final DfuConfigurationContainer originalDfu;

    public final DeploymentPlatform targetPlatform;


    /**
     * The sequence of DFUs to be used to construct the new DFU
     */
    @Nonnull
    public final ArrayList<DfuConfigurationContainer> dfuCompositionSequence;

    public DfuCompositionConfiguration(@Nonnull String sessionIdentifier, @Nonnull DeploymentPlatform targetPlatform, @Nonnull DfuConfigurationContainer originalDfu, @Nonnull ArrayList<DfuConfigurationContainer> dfuCompositionSequence) {
        this.sessionIdentifier = sessionIdentifier;
        this.targetPlatform = targetPlatform;
        this.originalDfu = originalDfu;
        this.dfuCompositionSequence = dfuCompositionSequence;
    }

    public static class DfuParameter {

        public DfuParameter(boolean providedByApplication, String classType, String value) {
            this.providedByApplication = providedByApplication;
            this.classType = classType;
            this.value = value;
        }

        public final boolean providedByApplication;
        public final String classType;
        // This should ideally be final!!
        public String value;
    }

    public static class DfuPermutationParameter {

        public DfuPermutationParameter(boolean providedByApplication, String classType, String[] values) {
            this.providedByApplication = providedByApplication;
            this.classType = classType;
            this.values = values;
        }

        public final boolean providedByApplication;
        public final String classType;
        public final String[] values;

        public Set<DfuParameter> generateParameters() {
            Set<DfuParameter> parameterList;

            if (values == null) {
                parameterList = new HashSet<>(1);
                parameterList.add(new DfuParameter(providedByApplication, classType, null));

            } else {
                parameterList = new HashSet<>(values.length);
                for (String value : values) {
                    parameterList.add(new DfuParameter(providedByApplication, classType, value));
                }
            }

            return parameterList;
        }
    }


    /**
     * Defines the details needed to specify the usage of a ConsumingPipe
     */
    public static class ConsumingPipeSpecification {

        public ConsumingPipeSpecification(String classPackageIdentifier, ArrayList<DfuParameter> constructorParameters) {
            this.classPackageIdentifier = classPackageIdentifier;
            this.constructorParameters = constructorParameters;
        }

        /**
         * The class package identifier
         */
        // This should ideally be final!!
        @Nonnull
        public String classPackageIdentifier;

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
        public final ArrayList<DfuParameter> constructorParameters;

        private transient Class mClazz = null;
        private transient Type mInputJavaType = null;
        private transient Type mOutputJavaType = null;
        private transient Constructor mMatchingConstructor = null;

        public Class getDfuClassType() throws CompositionException {
            try {
                if (mClazz == null) {
                    mClazz = Class.forName(classPackageIdentifier);
                }
                return mClazz;
            } catch (ClassNotFoundException e) {
                throw new CompositionException.InvalidClaspathSpecifiedException(classPackageIdentifier);
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
            throw new CompositionException.ConsumingPipeUnexpectedException("Unable to parse generic input type from '" + classPackageIdentifier + "");
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
                Class classType = Class.forName(classPackageIdentifier);

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
                throw new CompositionException.InvalidClaspathSpecifiedException(classPackageIdentifier);
            }

            throw new CompositionException.InvalidSpecifiedParametersException(classPackageIdentifier, "...", "...");
        }
    }


    /**
     * Specifies the details necessary to use a DFU
     */
    public static class DfuConfigurationContainer {
        /**
         * A string that can be put into a gradle dependency field to import the DFU
         * <p>
         * It is expected in the "group:package:version" format, such as "mil.darpa.immortals.dfus:myAwesomeDfu:1.3.7"
         */
        @Nonnull
        // This should ideally be final!!
        public String dependencyString;

        @Nullable
        public final boolean performAnalysis;

        /**
         * The specification of the DFU in the form of an {@link ConsumingPipeSpecification}
         * <p>
         * In the future, a configuration in the form of this or another paradigm will be required, but for the moment,
         * this is the only type supported and is mandatory
         */
        @Nonnull
        public final ConsumingPipeSpecification consumingPipeSpecification;

        public DfuConfigurationContainer(@Nonnull String dependencyString, @Nonnull ConsumingPipeSpecification consumingPipeSpecification, boolean performAnalysis) {
            this.dependencyString = dependencyString;
            this.performAnalysis = performAnalysis;
            this.consumingPipeSpecification = consumingPipeSpecification;
        }
    }
}

/* JSON Example
{
    "originalDfu": {
        "dependencyString": "mil.darpa.immortals.dfus:ImageUtilsAndroid:+",
        "consumingPipeSpecification": {
            "classPackageIdentifier": "mil.darpa.immortals.dfus.images.BitmapReader",
            "constructorParameters": [
                {
                    "providedByApplication": true,
                    "classType": "mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe<android.graphics.Bitmap>"
                }
            ]
        }
    },
    "dfuCompositionSequence": [
        {
            "dependencyString": "mil.darpa.immortals.dfus:ImageUtilsAndroid:+",
            "consumingPipeSpecification": {
                "classPackageIdentifier": "mil.darpa.immortals.dfus.images.BitmapReader",
                "constructorParameters": [
                    {
                        "providedByApplication": true,
                        "classType": "mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe<android.graphics.Bitmap>"
                    }
                ]
            }
        },
        {
            "dependencyString": "mil.darpa.immortals.dfus:ImageUtilsAndroid:+",
            "consumingPipeSpecification": {
                "classPackageIdentifier": "mil.darpa.immortals.dfus.images.BitmapScaler",
                "constructorParameters": [
                    {
                        "providedByApplication": false,
                        "classType": "java.lang.Integer",
                        "value": "0.85"
                    },
                    {
                        "providedByApplication": true,
                        "classType": "mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe<android.graphics.Bitmap>"
                    }
                ]
            }
        }
    ]
}
 */



