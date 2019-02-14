package mil.darpa.immortals.das.sourcecomposer;


import javax.annotation.Nullable;

/**
 * Created by awellman@bbn.com on 10/3/16.
 */
public class CompositionException extends Exception {

    public enum Tags {
        INVALID_CONTROL_POINT_IDENTIFIER_SPECIFIED,
        INVALID_CLASSPATH_SPECIFIED,
        INVALID_PARADIGM_MAPPING,
        INVALID_SPECIFIED_PARAMETERS,
        INVALID_FILEPATH_SPECIFIED,
        CONSUMINGPIPE_INVALID_HEAD,
        CONSUMINGPIPE_INVALID_TAIL,
        CONSUMINGPIPE_DANGLING_TAIL,
        CONSUMINGPIPE_UNEXPECTED,
        CONSUMINGPIPE_MISSMATCHED_TYPES,
        CONSUMINGPIPE_UNANNOTATED_PARAMETER,
        SUBSTITUTION_UNDEFINED_PARAMETER,
        SUBSTITUTION_IMPROPERLY_FORMATTED_CONTROL_POINT,
        DFU_UUID_EXCEPTION,
        CONTROLPOINT_UUID_EXCEPTION,
        APPLICATION_UUID_EXCEPTION,
    }

    public CompositionException(Tags tag, String details) {
        super("{\"" + tag + "\" : \"" + details + "\" }");
    }

    public static class InvalidControlPointIdentifierSpecifiedException extends CompositionException {
        public InvalidControlPointIdentifierSpecifiedException(String controlPointIdentifier) {
            super(Tags.INVALID_CONTROL_POINT_IDENTIFIER_SPECIFIED, "Invalid ControlPointIdentifier '" + controlPointIdentifier + "' provided!");
        }
    }
    
    public static class ConsumingPipeUnannotatedParameterException extends CompositionException {
        public ConsumingPipeUnannotatedParameterException(String method) {
            super(Tags.CONSUMINGPIPE_UNANNOTATED_PARAMETER, "Cannot determine which parameter to insert into " + method
             + " since it is not properly annotated!");
        }
    }

    public static class InvalidClaspathSpecifiedException extends CompositionException {
        public InvalidClaspathSpecifiedException(String dfuIdentifier) {
            super(Tags.INVALID_CLASSPATH_SPECIFIED, "Invalid DFU of type '" + dfuIdentifier + "' specified!");
        }
    }

    public static class InvalidFilepathSpecifiedException extends CompositionException {
        public InvalidFilepathSpecifiedException(String filepath) {
            super(Tags.INVALID_FILEPATH_SPECIFIED, "Invalid filepath '" + filepath + "' specified!");
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


    public static class SubstitutionUndefinedParameterException extends CompositionException {
        public SubstitutionUndefinedParameterException(String method, String parameter, @Nullable String semanticType) {
            super(Tags.SUBSTITUTION_UNDEFINED_PARAMETER,
                    "No parameter value has been defined for parameter '" + parameter +
                            (semanticType == null ? "" : "of semantic type '" + semanticType + "' ") +
                            "' of method " + method + "!");
        }
    }

    public static class SubstitutionImproperlyFormattedControlPoint extends CompositionException {
        public SubstitutionImproperlyFormattedControlPoint(String filepath, String controlPoint) {
            super(Tags.SUBSTITUTION_IMPROPERLY_FORMATTED_CONTROL_POINT,
                    "The file '" + filepath + "' Contains an improperly formatted control point '" + controlPoint + "'!");
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
        public ConsumingPipeMissmatchedTypesException(String consumingPipeConfigurationLeaderClassPackage,
                                                      String consumingPipeConfigurationTailClassPackage) {
            super(Tags.CONSUMINGPIPE_MISSMATCHED_TYPES, "Output type of '" +
                    consumingPipeConfigurationLeaderClassPackage + "' does not match input type of '" +
                    consumingPipeConfigurationTailClassPackage + "'!");

        }
    }

    public static class DfuUuidException extends CompositionException {
        public DfuUuidException(String uuid) {
            super(Tags.DFU_UUID_EXCEPTION, "No DFU exists with the UUID '" + uuid + "'!");
        }
    }

    public static class ControlPointUuidException extends CompositionException {
        public ControlPointUuidException(String uuid) {
            super(Tags.CONTROLPOINT_UUID_EXCEPTION, "No ControlPoint exists with the UUID '" + uuid + "'!");
        }
    }

    public static class ApplicationUuidException extends CompositionException {
        public ApplicationUuidException(String uuid) {
            super(Tags.APPLICATION_UUID_EXCEPTION, "No Application exists with the UUID '" + uuid + "'!");
        }
    }
}
