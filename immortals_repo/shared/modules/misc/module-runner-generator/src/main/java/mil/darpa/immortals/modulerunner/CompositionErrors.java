//package mil.darpa.immortals.modulerunner;
//
///**
// * Created by awellman@bbn.com on 9/29/16.
// */
//public class CompositionErrors {
//
//
//
//
//
//    public static void throwInvalidDfuClassSpecified(String dfuIdentifier) throws CompositionException {
//        throw new CompositionException(Tags.INVALID_CLASS_SPECIFIED, "Invalid DFU of type '" + dfuIdentifier + "' specified!");
//    }
//
//    public static void throwConsumingPipeInvalidHeadError() throws CompositionException {
//        throw new CompositionException(Tags.CONSUMINGPIPE_INVALID_HEAD,
//                "Dfu Composition pipelines that do not begin with the original Dfu are currently not supported!");
//    }
//
//    public static void throwConsumingPipeInvalidParameterMappingError(String desiredParadigm, String currentParadigm) throws CompositionException {
//        throw new CompositionException(Tags.CONSUMINGPIPE_INVALID_PARADIGM_MAPPING, "The paradigm \" + " + currentParadigm + "\" cannot " +
//                "be mapped to a \"" + desiredParadigm + "\" paradigm!");
//    }
//
//    public static void throwConsumingPipeInvalidParameters() throws CompositionException {
//        throw new CompositionException(Tags.CONSUMINGPIPE_INVALID_SPECIFIED_PARAMETERS, "Invalid parameters!");
//    }
//
//    public static void throwConsumingPipeInvalidSpecifiedParameters(String className, String expectedType, String actualType) throws CompositionException {
//        throw new CompositionException(Tags.CONSUMINGPIPE_INVALID_SPECIFIED_PARAMETERS,
//                "Class '" + className + "' was provided a parameter type of '" + actualType + "' but expected a " +
//                        "parameter type of '" + expectedType + "'!");
//
//    }
//}
