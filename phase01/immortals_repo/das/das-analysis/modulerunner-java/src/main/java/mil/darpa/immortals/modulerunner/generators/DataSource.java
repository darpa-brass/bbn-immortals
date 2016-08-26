package mil.darpa.immortals.modulerunner.generators;


import mil.darpa.immortals.modulerunner.ModuleConfiguration;
import mil.darpa.immortals.modulerunner.configuration.AnalysisConfig;
import mil.darpa.immortals.modulerunner.generators.sources.ExceptionSource;
import mil.darpa.immortals.modulerunner.generators.sources.FileSource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by awellman@bbn.com on 6/3/16.
 */
public class DataSource {

    private final DataGeneratorInterface generator;
    private final DataTransferUnit dataTransferUnit;
    private final SemanticTypeMapping dataType;
    private final int generationUnitByteCount;
    private final GeneratorJavaType generatorJavaType;
    private final String dataSourceFile;
    private FileSource fileSource;

    public DataSource(@Nonnull ModuleConfiguration moduleConfiguration, @Nonnull AnalysisConfig analysisConfig) throws IOException {
        this.dataType = analysisConfig.generatorSemanticTypeOverride;
        this.dataTransferUnit = analysisConfig.dataTransferUnit;
        this.generatorJavaType = analysisConfig.dataTransferJavaType;
        this.generationUnitByteCount = analysisConfig.dataTransferUnitBurstCount * generatorJavaType.byteSize;
        this.dataSourceFile = analysisConfig.generatorSourceDataFilepath;
        this.generator = produceGenerator();
    }

    private DataGeneratorInterface produceGenerator() throws IOException {
        DataGeneratorInterface gen;

        switch (dataType) {
            case GeneralText:
                final FileSource fs;
                if (dataSourceFile != null) {
                    fileSource = fs = new FileSource(dataSourceFile, generationUnitByteCount);
                } else {
                    fileSource = fs = new FileSource("pg2701-Moby_Dick.txt", generationUnitByteCount);
                }
                gen = new DataGeneratorInterface() {
                    @Override
                    public byte[] getBytes() throws IOException {
                        return fs.getBytes();
                    }
                };
                break;

            case CotLocationMessage:
                gen = new DataGeneratorInterface() {
                    @Override
                    public byte[] getBytes() {
                        System.out.println(CotGenerator.createMessage().asXML());
                        return CotGenerator.createMessage().asXML().getBytes();
                    }
                };

                break;

            case CotImageMessage:
                gen = new DataGeneratorInterface() {
                    @Override
                    public byte[] getBytes() {
                        return CotGenerator.createMessageWithImage().asXML().getBytes();
                    }
                };

                break;

            case ExceptionType:
                final ExceptionSource es = new ExceptionSource();
                gen = new DataGeneratorInterface() {
                    @Override
                    public byte[] getBytes() {
                        return es.getBytes();
                    }
                };
                break;

            case GeneralLogMessage:
                gen = new DataGeneratorInterface() {
                    @Override
                    public byte[] getBytes() throws IOException {
                        return "Debug log: General Occurance has occured. The occurance is working as expected. The stack trace isn't necessary in this case.".getBytes();
                    }
                };
                break;

            default:
                throw new RuntimeException("Unexpected SemanticTypeMapping '" + dataType.name() + "'!");
        }
        return gen;
    }

    public Object generate() throws IOException {
        if (generatorJavaType == GeneratorJavaType.Byte_Array) {
            return generator.getBytes();

        } else if (generatorJavaType == GeneratorJavaType.Char_Array) {
            byte[] bytes = generator.getBytes();
            return (new String(bytes).toCharArray());

        } else if (generatorJavaType == GeneratorJavaType.InputStream) {
            return fileSource.getInputStream();

        } else {
            throw new RuntimeException("Undefined GeneratorJavaType '" + generatorJavaType.name() + "' provided!");
        }
    }

    public enum GeneratorJavaType {
        Byte_Array(Byte.BYTES),
        InputStream(Byte.BYTES),
        OutputStream(Byte.BYTES),
        Char_Array(Character.BYTES);

        public final int byteSize;

        GeneratorJavaType(int byteSize) {
            this.byteSize = byteSize;
        }
    }

    public enum DataTransferUnit {
        Byte // Each number represented by a size is a single byte
    }

    public interface DataGeneratorInterface {
        public byte[] getBytes() throws IOException;
    }

    public class MultiGenerator implements DataGeneratorInterface {

        final DataGeneratorInterface generator;

        public MultiGenerator(SortedMap<DataSource, Integer> weightedGenerators) {
            final DataSource[] generators = (DataSource[]) weightedGenerators.values().toArray();
            final int[] generatorWeights = new int[generators.length];

            final SortedMap<DataSource, Integer> generatorMap = new TreeMap<>(weightedGenerators);
            int totalWeight = 0;


            for (int i = 0; i < generators.length; i++) {
                DataSource generator = generators[i];
                totalWeight += generatorMap.get(generator);
                generatorWeights[i] = totalWeight;

                if (generatorJavaType != generator.generatorJavaType) {
                    throw new RuntimeException("All generators placed into a multi-type generator must output the same java type!");
                }

                if (generator.dataTransferUnit != DataTransferUnit.Byte) {
                    throw new RuntimeException("Cannot have a datatype that does not send data in incomplete units in a multi-type generator!");
                }
            }

            final int finalWeight = totalWeight;
            final GeneratorJavaType javaType = generators[0].generatorJavaType;

            generator = new DataGeneratorInterface() {
                @Override
                public byte[] getBytes() throws IOException {
                    int randVal = ThreadLocalRandom.current().nextInt(finalWeight);

                    for (int i = generators.length; i >= 0; i--) {
                        if (generatorWeights[i] < randVal) {
                            return (byte[]) generators[i].generate();
                        }
                    }

                    throw new RuntimeException("Weight of " + randVal + "Is not between zero and " + finalWeight + "!");
                }
            };
        }

        public byte[] getBytes() throws IOException {
            return generator.getBytes();
        }
    }

}
