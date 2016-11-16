//package mil.darpa.immortals.das.datagenerators.semantictypes;
//
//import AbstractDataGenerator;
//import mil.darpa.immortals.modulerunner.configuration.AnalysisGeneratorConfiguration;
//
//import java.io.File;
//
///**
// * Created by awellman@bbn.com on 8/19/16.
// */
//public class JpegFilepath extends AbstractDataGenerator<String> {
//
//    private final File[] fileList;
//
//    private int currentIdx = 0;
//
//    public JpegFilepath(AnalysisGeneratorConfiguration configuration) {
//        super(configuration);
//
//        fileList = new File("sample_images").listFiles();
//    }
//
//    @Override
//    protected String innerProduce() {
//        String returnValue = fileList[currentIdx].getAbsolutePath();
//        currentIdx = (currentIdx+1 == fileList.length ? 0 : currentIdx+1);
//        return returnValue;
//    }
//}
