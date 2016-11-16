package mil.darpa.immortals.das.sourcecomposer;

import mil.darpa.immortals.das.configuration.DfuCompositionConfiguration;

/**
 * Created by awellman@bbn.com on 10/3/16.
 */
public class CompositionException extends Exception {

    public enum Tags {
        INVALID_CLASSPATH_SPECIFIED,
        INVALID_PARADIGM_MAPPING,
        INVALID_SPECIFIED_PARAMETERS,
        CONSUMINGPIPE_INVALID_HEAD,
        CONSUMINGPIPE_INVALID_TAIL,
        CONSUMINGPIPE_DANGLING_TAIL,
        CONSUMINGPIPE_UNEXPECTED,
        CONSUMINGPIPE_MISSMATCHED_TYPES
    }

    public CompositionException(Tags tag, String details) {
        super("{\"" + tag + "\" : \"" + details + "\" }");
    }

    public static class InvalidClaspathSpecifiedException extends CompositionException {
        public InvalidClaspathSpecifiedException(String dfuIdentifier) {
            super(Tags.INVALID_CLASSPATH_SPECIFIED, "Invalid DFU of type '" + dfuIdentifier + "' specified!");
        }
    }

    public static class ConsumingPipeInvalidHeadException extends CompositionException {
        public ConsumingPipeInvalidHeadException() {
            super(Tags.CONSUMINGPIPE_INVALID_HEAD,
                    "The input of the original DFU does not fit with the input of the first DFU to be composed!");
        }
    }

    public static class ConsumingPipeInvalidTailException extends CompositionException {
        public ConsumingPipeInvalidTailException() {
            super(Tags.CONSUMINGPIPE_INVALID_TAIL,
                    "The output of the original DFU does not fit with the output of the last DFU to be composed!");
        }
    }

    public static class ConsumingPipeDanglingTailException extends CompositionException {
        public ConsumingPipeDanglingTailException(String constructorIdentifier) {
            super(Tags.CONSUMINGPIPE_DANGLING_TAIL,
                    "A tail value has been provided to '" + constructorIdentifier + "' but it is not used!");
        }
    }

    public static class InvalidParadigmMappingException extends CompositionException {
        public InvalidParadigmMappingException(String desiredParadigm, String currentParadigm) {
            super(Tags.INVALID_PARADIGM_MAPPING, "The paradigm \" + " + currentParadigm + "\" cannot " +
                    "be mapped to a \"" + desiredParadigm + "\" paradigm!");
        }
    }

    public static class InvalidSpecifiedParametersException extends CompositionException {
        public InvalidSpecifiedParametersException(String methodName, String expectedType, String actualType) {
            super(Tags.INVALID_SPECIFIED_PARAMETERS,
                    "Method or constructor '" + methodName + "' was provided a parameter type of '" + actualType +
                            "' but expected a parameter type of '" + expectedType + "'!");
        }
    }

    public static class ConsumingPipeUnexpectedException extends CompositionException {
        public ConsumingPipeUnexpectedException(String detail) {
            super(Tags.CONSUMINGPIPE_UNEXPECTED, detail);
        }
    }

    public static class ConsumingPipeMissmatchedTypesException extends CompositionException {
        public ConsumingPipeMissmatchedTypesException(DfuCompositionConfiguration.ConsumingPipeSpecification leader, DfuCompositionConfiguration.ConsumingPipeSpecification tail) {
            super(Tags.CONSUMINGPIPE_MISSMATCHED_TYPES, "Output type of '" + leader.classPackageIdentifier + "' does not match input type of '" + tail.classPackageIdentifier + "'!");

        }
    }
}
