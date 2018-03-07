package mil.darpa.immortals.das.sourcecomposer.dfucomposers;

import mil.darpa.immortals.analytics.profilers.GenericConsumingPipelineProfiler;
import mil.darpa.immortals.das.sourcecomposer.configuration.DeploymentPlatform;
import mil.darpa.immortals.das.sourcecomposer.configuration.paradigms.ConfigurationContainer;
import mil.darpa.immortals.das.sourcecomposer.configuration.paradigms.ConsumingPipeConfiguration;
import mil.darpa.immortals.das.sourcecomposer.configuration.paradigms.GenericParameterConfiguration;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.das.sourcecomposer.configuration.DfuCompositionConfiguration;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;
import mil.darpa.immortals.das.sourcecomposer.CompositionException;
import mil.darpa.immortals.datagenerators.DevNullConsumingPipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static mil.darpa.immortals.das.sourcecomposer.configuration.paradigms.GenericParameterConfiguration.parameterIroner;

/**
 * Created by awellman@bbn.com on 9/27/16.
 */
public class ConsumingPipeComposer {
    public final String packagePath;
    public String className;
    public String classPath;
    public DfuCompositionConfiguration compositionConfiguration;
    public ConsumingPipeConfiguration originalDfu;
    public ConsumingPipeConfiguration headDfu;
    public final String headPipeIdentifier;


    public ConsumingPipeComposer(EnvironmentConfiguration environmentConfiguration, DfuCompositionConfiguration moduleCompositionConfiguration) {
        this.compositionConfiguration = moduleCompositionConfiguration;
        this.originalDfu = compositionConfiguration.controlPoint.originalDfu.consumingPipeSpecification;
        this.headDfu = compositionConfiguration.dfuCompositionSequence.get(0).consumingPipeSpecification;

        className = moduleCompositionConfiguration.compositionIdentifier;
        packagePath = environmentConfiguration.synthesizedDfuPackage;
        classPath = moduleCompositionConfiguration.getProductClasspath();

        headPipeIdentifier = "head_" + UUID.randomUUID().toString().substring(0, 4);
    }


    public List<String> createFileLines() throws CompositionException {
        validateJavaTypes();

        List<String> outputLines = new LinkedList<>();

        outputLines.add(createPackageHeaderLine());
        outputLines.add(createClassHeaderLine());
        outputLines.add(createFieldDeclarationLine());
        outputLines.add(produceConstructorLine());
        outputLines.add(produceConsumeLine());
        outputLines.add(produceFlushLine());
        outputLines.add(produceCloseLine());
        outputLines.add(createClassFooterLine());

        return outputLines;
    }

    public void validateJavaTypes() throws CompositionException {
        int sequenceSize = compositionConfiguration.dfuCompositionSequence.size();
        for (int i = 0; i < sequenceSize; i++) {

            ConsumingPipeConfiguration currentDfu = compositionConfiguration.dfuCompositionSequence.get(i).consumingPipeSpecification;

            // If first, check that input matches the original Dfu
            if (i == 0) {

                if (originalDfu.getInputJavaType() != currentDfu.getInputJavaType()) {
                    // Otherwise, if it is not and the input types do not match, throw an exception
                    throw new CompositionException.ConsumingPipeInvalidHeadException();
                }
            }

            // If last, check that the output (if any matches the original Dfu
            if (i == sequenceSize - 1) {
                ConsumingPipeConfiguration originalDfu = compositionConfiguration.controlPoint.originalDfu.consumingPipeSpecification;
                if (originalDfu.getOutputJavaType() != currentDfu.getOutputJavaType()) {
                    throw new CompositionException.ConsumingPipeInvalidTailException();
                }
            }

            // It has following items, compare types.
            if (i < sequenceSize - 1) {
                ConsumingPipeConfiguration trailingDfu = compositionConfiguration.dfuCompositionSequence.get(i + 1).consumingPipeSpecification;

                // If it's DevNull, ignore it, it's just an object consumer that intentionally does nothing.
                if (trailingDfu.getDfuClassType() != DevNullConsumingPipe.class) {
                    if (currentDfu.getOutputJavaType() != trailingDfu.getInputJavaType()) {
                        throw new CompositionException.ConsumingPipeMissmatchedTypesException(
                                currentDfu.classPackage,
                                trailingDfu.classPackage);
                    }
                }
            }

            for (GenericParameterConfiguration param : currentDfu.constructorParameters) {
                if (param.value != null && param.value.length > 1) {
                    throw new RuntimeException("Cannot produce code with multiple parameters!");
                }
            }
        }
    }

    public String createPackageHeaderLine() {
        return "package " + packagePath + ";\n\n";
    }

    public String createClassHeaderLine() throws CompositionException {
        return "public class " + className + " implements " + ConsumingPipe.class.getName() +
                "<" + originalDfu.getInputJavaType().getTypeName() + "> {\n\n";
    }

    public String createClassFooterLine() {
        return "}";
    }

    public String produceConsumeLine() throws CompositionException {
        return "    public void consume(" + originalDfu.getInputJavaType().getTypeName() + " input) {\n" +
                "        " + headPipeIdentifier + ".consume(input);\n" +
                "    }\n\n";
    }

    public String produceFlushLine() {
        return "    public void flushPipe() {\n" +
                "        " + headPipeIdentifier + ".flushPipe();\n" +
                "    }\n\n";
    }

    public String produceCloseLine() {
        return
                "    public void closePipe() {\n" +
                        "        " + headPipeIdentifier + ".closePipe();\n" +
                        "    }\n\n";
    }

    public String createFieldDeclarationLine() throws CompositionException {
        String returnString;

        if (compositionConfiguration.performAnalysis) {

            // TODO: This should really be more flexible and not depend on the head DFU matching this input type...
            returnString = "    private mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe<Object> " +
                    headPipeIdentifier + ";\n\n";

            if (compositionConfiguration.applicationProfile.deploymentPlatform == DeploymentPlatform.Java) {
                returnString += "    private " + GenericConsumingPipelineProfiler.class.getName() +
                        " consumingPipelineProfiler = new " + GenericConsumingPipelineProfiler.class.getName() +
                        "(\"" + compositionConfiguration.compositionIdentifier + "\");\n\n";
            } else {
              throw new RuntimeException("Unsupported profile!");
            }
        } else {
            returnString = "    private " + headDfu.classPackage + " " + headPipeIdentifier + ";\n\n";
        }
        return returnString;

    }

    public String produceConstructorLine() throws CompositionException {
        Constructor originalConstructor = originalDfu.getConstructor();

        String tailPipeIdentifier = "tail" + UUID.randomUUID().toString().substring(0, 4);


        return "    public " + className + "(" + getConstructorHeaderParameterString(originalConstructor, tailPipeIdentifier) + ") {\n" +
                "        " + headPipeIdentifier + " = " + constructPipeline(compositionConfiguration.dfuCompositionSequence, tailPipeIdentifier) + ";\n" +
                "    }\n\n";
    }

    public String constructPipeline(List<ConfigurationContainer> dfuList, String tailIdentifier) throws CompositionException {
        return recursivePipelineBuilder(dfuList, 0, tailIdentifier);
    }

    public String recursivePipelineBuilder(List<ConfigurationContainer> dfuList, int currentIndex, String tailIdentifier) throws CompositionException {
        if (currentIndex == dfuList.size()) {
            return tailIdentifier;

        } else {
            if (dfuList.get(currentIndex).consumingPipeSpecification != null) {
                ConsumingPipeConfiguration dfu = dfuList.get(currentIndex).consumingPipeSpecification;

                Constructor c = dfu.getConstructor();

                if (compositionConfiguration.performAnalysis) {
                    return "consumingPipelineProfiler.insertPipe(" + currentIndex +
                            ", new " + dfu.classPackage + "(" + getConstructorParameterString(
                            c,
                            dfu.constructorParameters,
                            recursivePipelineBuilder(dfuList, currentIndex + 1, tailIdentifier)) + "))";
                } else {
                    return "new " + dfu.classPackage + "(" + getConstructorParameterString(
                            c,
                            dfu.constructorParameters,
                            recursivePipelineBuilder(dfuList, currentIndex + 1, tailIdentifier)) + ")";
                }


            } else {
                throw new CompositionException.InvalidParadigmMappingException("Undefined", "ConsumingPipe");
            }

        }
    }


    public static String getConstructorHeaderParameterString(Constructor constructor, String tailPipeIdentifier) {
        String parameterString = null;
        Parameter[] parameters = constructor.getParameters();

        int argCounter = 0;

        if (parameters.length == 0) {
            return "";
        }

        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];

            if (p.getType() == ConsumingPipe.class && i + 1 == parameters.length) {

                parameterString = (parameterString == null ? "" : parameterString + ", ") +
                        p.getParameterizedType().getTypeName() + " " + tailPipeIdentifier;

            } else {
                parameterString = (parameterString == null ? "" : parameterString + ", ") +
                        p.getParameterizedType().getTypeName() + " arg" + argCounter++;
            }
        }
        return parameterString;
    }

    public static String getConstructorParameterString(@Nonnull Constructor constructor,
                                                       @Nullable List<GenericParameterConfiguration> parameterList,
                                                       @Nullable String tailValue) throws CompositionException {

        String parameterString = null;
        Parameter[] parameters = constructor.getParameters();

        // If there are no parameters, return an empty String
        if ((parameters == null || parameters.length == 0) && (parameterList == null || parameterList.size() == 0)) {
            return "";
        }

        // Throw error if size counts do not match
        if (parameters.length != parameterList.size()) {
            throw new CompositionException.InvalidSpecifiedParametersException(constructor.getName(),
                    "size=" + Integer.toString(parameters.length), "size=" + Integer.toString(parameterList.size()));
        }

        // Iterate through each parameter
        for (int i = 0; i < parameters.length; i++) {
            GenericParameterConfiguration pParam = parameterList.get(i);

            String cParamIdentifier = parameterIroner(parameters[i].getParameterizedType().getTypeName().replaceAll(" ", ""));
            String pParamIdentifier = parameterIroner(parameterList.get(i).classType.replaceAll(" ", ""));

            // Validate their parameter type
            if (!cParamIdentifier.equals(cParamIdentifier)) {
                throw new CompositionException.InvalidSpecifiedParametersException(constructor.getName(),
                        cParamIdentifier, pParamIdentifier);
            }


            // Add the parameter if omitNext is false or the parameter is the last and a WriteableObjectPipeInterface
            // If there is a tail value or it is not at the last parameter,

            // if the last item is being evaluated and a tail value is defined, try and use it
            if (i + 1 == parameters.length && tailValue != null) {
                // TODO: check generic type?
                if (parameters[i].getType() == ConsumingPipe.class) {
                    // If the constructor has the right parameter value, use it
                    parameterString = (parameterString == null ? "" : parameterString + ", ") + tailValue;

                } else {
                    // Otherwise, throw an exception
                    throw new CompositionException.ConsumingPipeDanglingTailException(constructor.getName());
                }
            } else {

                // Othrewise, evaluate the parameter for insertion
                parameterString = (parameterString == null ? "" : parameterString + ", ") +
                        (pParam.providedByApplication ?
                                (pParamIdentifier + " arg" + Integer.toString(i)) :
                                (parseParameter(pParam))
                        );
            }
        }
        return parameterString;
    }

    public static String parseParameter(GenericParameterConfiguration parameter) {
        if (parameter.classType.equals("java.lang.String")) {
            return "\"" + parameter.value[0] + "\"";
        } else if (parameter.value == null) {
            return null;
        } else {
            return parameter.value[0];
        }

    }
}
