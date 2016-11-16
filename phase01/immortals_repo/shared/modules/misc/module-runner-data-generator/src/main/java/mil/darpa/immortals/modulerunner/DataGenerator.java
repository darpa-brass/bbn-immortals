//package mil.darpa.immortals.modulerunner;
//
//import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
//import mil.darpa.immortals.modulerunner.generators.ControlPointFormat;
//import mil.darpa.immortals.modulerunner.generators.SemanticType;
//
///**
// * Created by awellman@bbn.com on 10/6/16.
// */
//public class DataGenerator implements ConsumingPipe {
//
//    public final ControlPointFormat controlPointFormat;
//    public final String javaType;
//    public final SemanticType semanticType;
//    public final int dataTransferUnitsPerBurst;
//    public final int dataTransferBurstCount;
//    public final int dataTransferBurstIntervalMS;
//    public final int overallTestIterations;
//
//    public DataGenerator(
//            ControlPointFormat controlPointFormat,
//            String javaType,
//            SemanticType semanticType,
//            int dataTransferUnitsPerBurst,
//            int dataTransferBurstCount,
//            int dataTransferBurstIntervalMS,
//            int overallTestIterations,
//    ) {
//        this.controlPointFormat = controlPointFormat;
//        this.javaType = javaType;
//        this.semanticType = semanticType;
//        this.dataTransferUnitsPerBurst = dataTransferUnitsPerBurst;
//        this.dataTransferBurstCount = dataTransferBurstCount;
//        this.dataTransferBurstIntervalMS = dataTransferBurstIntervalMS;
//        this.overallTestIterations = overallTestIterations;
//
//    }
//
//    @Override
//    public void consume(Object input) {
//
//    }
//
//    @Override
//    public void flushPipe() {
//
//    }
//
//    @Override
//    public void closePipe() {
//
//    }
//}
