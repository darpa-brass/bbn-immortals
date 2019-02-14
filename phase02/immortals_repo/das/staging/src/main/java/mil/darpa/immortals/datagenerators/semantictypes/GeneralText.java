package mil.darpa.immortals.datagenerators.semantictypes;//package mil.darpa.immortals.sourcecomposer.generators.semantictypes;
//
//import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
//import mil.darpa.immortals.modulerunner.configuration.AnalysisGeneratorConfiguration;
//import AbstractDataGenerator;
//import mil.darpa.immortals.modulerunner.generators.sources.raw.FileSource;
//
//import javax.annotation.Nonnull;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//
///**
// * Created by awellman@bbn.com on 8/5/16.
// */
//public class GeneralText extends AbstractDataGenerator<String> {
//
//    private final FileSource fileSource;
//
//    public GeneralText(AnalysisGeneratorConfiguration configuration) {
//        super(configuration);
//        try {
//            this.fileSource = new FileSource("pg2701-Moby_Dick.txt", configuration.dataTransferUnitsPerBurst);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public GeneralText(AnalysisGeneratorConfiguration configuration, @Nonnull ConsumingPipe<String> next) {
//        super(configuration, next);
//        try {
//            this.fileSource = new FileSource("pg2701-Moby_Dick.txt", configuration.dataTransferUnitsPerBurst);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    protected String innerProduce() {
//        try {
//            return new String(fileSource.getBytes());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
