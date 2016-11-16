//package mil.darpa.immortals.modulerunner.analyzers;
//
//import mil.darpa.immortals.core.synthesis.ObjectPipe;
//import mil.darpa.immortals.core.synthesis.interfaces.ProducingPipe;
//import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
//
///**
// * Created by awellman@bbn.com on 8/19/16.
// */
//public abstract class AbstractAnalyzer<DataType> extends ObjectPipe<DataType, DataType> {
//
//    public abstract DataType analyzeData(DataType data);
//
//    public AbstractAnalyzer(ConsumingPipe<DataType> next) {
//        super(next);
//    }
//
//    public AbstractAnalyzer(ProducingPipe<DataType> previous) {
//        super(previous);
//    }
//
//    @Override
//    protected DataType process(DataType data) {
//        return analyzeData(data);
//    }
//
//    @Override
//    protected DataType flushToOutput() {
//        return null;
//    }
//
//    @Override
//    protected void preNextClose() {
//
//    }
//
//    @Override
//    public int getBufferSize() {
//        throw new RuntimeException("Not supported!");
//    }
//}
