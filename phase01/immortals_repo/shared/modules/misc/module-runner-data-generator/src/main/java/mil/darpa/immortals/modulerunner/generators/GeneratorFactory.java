//package mil.darpa.immortals.modulerunner.generators;
//
//import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
//import mil.darpa.immortals.modulerunner.configuration.AnalysisGeneratorConfiguration;
//import mil.darpa.immortals.sourcecomposer.generators.javaclasstypes.AbstractByteArrayGenerator;
//import AbstractDataGenerator;
//import mil.darpa.immortals.sourcecomposer.generators.javaclasstypes.AbstractStringGenerator;
//import mil.darpa.immortals.sourcecomposer.generators.semantictypes.GeneralText;
//import JpegFilepathGenerator;
//
//import javax.annotation.Nonnull;
//
//import static mil.darpa.immortals.modulerunner.generators.SemanticType.JpegFilepath;
//
///**
// * Created by awellman@bbn.com on 8/8/16.
// */
//public class GeneratorFactory {
//
//    public static AbstractDataGenerator produceGenerator(@Nonnull AnalysisGeneratorConfiguration configuration, @Nonnull ConsumingPipe next) {
//        SemanticType semanticType = configuration.semanticType;
//        JavaClassType producedType = configuration.javaType;
//
//        final TypeConverter typeConverter;
//        final AbstractDataGenerator generator;
//
//        if (semanticType == SemanticType.GeneralText) {
//            generator = new GeneralText(configuration, next);
//
//            if (semanticType.producedType == producedType) {
//                return generator;
//            } else {
//                typeConverter = TypeConverter.ConstructTypeConverter(semanticType.producedType, producedType);
//
//            }
//
//        } else if (semanticType == JpegFilepath) {
//            generator = new JpegFilepath(configuration, next);
//
//            if (semanticType.producedType == producedType) {
//                return generator;
//            } else {
//                typeConverter = TypeConverter.ConstructTypeConverter(semanticType.producedType, producedType);
//            }
//
//        } else {
//            throw new RuntimeException("No generator exists for SemanticTypeUnit '" + semanticType + "'!");
//        }
//
//        if (producedType == JavaClassType.String) {
//            return new AbstractStringGenerator(configuration, next) {
//                @Override
//                protected String innerProduce() {
//                    return (String) typeConverter.convert(generator.produce());
//                }
//            };
//
//        } else if (producedType == JavaClassType.ByteArray) {
//            return new AbstractByteArrayGenerator(configuration, next) {
//                @Override
//                protected byte[] innerProduce() {
//                    return (byte[]) typeConverter.convert(generator.produce());
//                }
//            };
//
//        } else {
//            throw new RuntimeException("Cannot convert from SemanticTypeUnit '" + semanticType + "' JavaClassType '" + semanticType.producedType + "' to JavaClassType '" + producedType + "'!");
//        }
//
//    }
//
//    public static AbstractDataGenerator produceGenerator(@Nonnull AnalysisGeneratorConfiguration configuration) {
//        SemanticType semanticType = configuration.semanticType;
//        JavaClassType producedType = configuration.javaType;
//
//        final TypeConverter typeConverter;
//        final AbstractDataGenerator generator;
//
//        if (semanticType == SemanticType.GeneralText) {
//            generator = new GeneralText(configuration);
//
//            if (semanticType.producedType == producedType) {
//                return generator;
//            } else {
//                typeConverter = TypeConverter.ConstructTypeConverter(semanticType.producedType, producedType);
//
//            }
//
//        } else if (semanticType == JpegFilepath) {
//            generator = new JpegFilepath(configuration);
//
//            if (semanticType.producedType == producedType) {
//                return generator;
//            } else {
//                typeConverter = TypeConverter.ConstructTypeConverter(semanticType.producedType, producedType);
//            }
//
//        } else {
//            throw new RuntimeException("No generator exists for SemanticTypeUnit '" + semanticType + "'!");
//        }
//
//        if (producedType == JavaClassType.String) {
//            return new AbstractStringGenerator(configuration) {
//                @Override
//                protected String innerProduce() {
//                    return (String) typeConverter.convert(generator.produce());
//                }
//            };
//
//        } else if (producedType == JavaClassType.ByteArray) {
//            return new AbstractByteArrayGenerator(configuration) {
//                @Override
//                protected byte[] innerProduce() {
//                    return (byte[]) typeConverter.convert(generator.produce());
//                }
//            };
//
//        } else {
//            throw new RuntimeException("Cannot convert from SemanticTypeUnit '" + semanticType + "' JavaClassType '" + semanticType.producedType + "' to JavaClassType '" + producedType + "'!");
//        }
//    }
//
////
////    public static AbstractGenerator produceGeneratorFromFile(@Nonnull String filePath, @Nonnull JavaClassType producedType, int dataTransferUnitsPerBurst) {
////        try {
////            final FileSource fs = new FileSource(filePath, dataTransferUnitsPerBurst);
////
////            AbstractByteArrayGenerator generator = new AbstractByteArrayGenerator() {
////                @Override
////                public byte[] produceBytes() {
////                    try {
////                        return fs.getBytes();
////                    } catch (IOException e) {
////                        throw new RuntimeException(e);
////                    }
////                }
////            };
////
////            if (producedType == JavaClassType.ByteArray) {
////                return generator;
////            } else {
////                throw new RuntimeException("A file-based generator supporting the JavaClassType '" + producedType + "' Is not currently supported!");
////            }
////
////        } catch (IOException e) {
////            throw new RuntimeException(e);
////        }
////    }
//}
