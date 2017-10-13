//package mil.darpa.immortals.modulerunner.analyzers;
//
//import mil.darpa.immortals.core.synthesis.interfaces.ProducingPipe;
//import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
//
//import javax.annotation.Nonnull;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
///**
// * Created by awellman@bbn.com on 8/19/16.
// */
//public class FileLogger<DataType> extends AbstractAnalyzer<DataType> {
//
//    private final FileOutputStream fileOutputStream;
//
//    @Override
//    public DataType analyzeData(DataType data) {
//        try {
//            if (data instanceof String) {
//                fileOutputStream.write(((String) data).getBytes());
//
//            } else if (data.getClass().getComponentType().isPrimitive() && byte.class.isAssignableFrom(data.getClass().getComponentType())) {
//                fileOutputStream.write((byte[]) data);
//
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return data;
//    }
//
//    public FileLogger(ConsumingPipe<DataType> next, @Nonnull String outputFilepath) throws IOException {
//        super(next);
//        fileOutputStream = new FileOutputStream(outputFilepath);
//    }
//
//    public FileLogger(ProducingPipe<DataType> previous, @Nonnull String outputFilepath) throws IOException {
//        super(previous);
//        fileOutputStream = new FileOutputStream(outputFilepath);
//    }
//}
