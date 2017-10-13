import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.ontology.bytecode.AMethod;
import com.securboration.immortals.ontology.bytecode.AnAnnotation;
import com.securboration.immortals.ontology.bytecode.AnnotationKeyValuePair;
import com.securboration.immortals.ontology.constraint.*;
import com.securboration.immortals.ontology.functionality.ConfidentialProperty;
import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherDecrypt;
import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherEncrypt;
import com.securboration.immortals.ontology.functionality.alg.encryption.Cipher;
import com.securboration.immortals.ontology.functionality.alg.encryption.aes.AES_128;
import com.securboration.immortals.ontology.functionality.alg.encryption.aes.AES_256;
import com.securboration.immortals.ontology.functionality.compression.AspectDeflate;
import com.securboration.immortals.ontology.functionality.compression.AspectInflate;
import com.securboration.immortals.ontology.functionality.compression.CompressionAlgorithm;
import com.securboration.immortals.ontology.functionality.dataproperties.Compressed;
import com.securboration.immortals.ontology.functionality.dataproperties.Encrypted;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.functionality.imagescaling.EnlargeImage;
import com.securboration.immortals.ontology.functionality.imagescaling.NumberOfPixels;
import com.securboration.immortals.ontology.functionality.logger.AspectLog;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.property.impact.*;
import com.securboration.immortals.ontology.resources.MobileAndroidDevice;
import com.securboration.immortals.ontology.resources.Server;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

@DfuAnnotation(functionalityBeingPerformed = Cipher.class)
public class ClientServerConstraintEnvironment {

    @ConceptInstance(name = "DataSafetyConstraint")
    public static class DataSafetyConstraint extends ProscriptiveCauseEffectAssertion {
        public DataSafetyConstraint() {

            AbstractPropertyCriterion criterion = new AbstractPropertyCriterion();{
                criterion.setProperty(ConfidentialProperty.class);
                criterion.setCriterion(PropertyCriterionType.PROPERTY_ABSENT);
                criterion.setHumanReadableDescription("This criterion specifies a situation where a confidential property is absent.");
            }

            ConstraintViolationImpact impact = new ConstraintViolationImpact();{
                impact.setConstraintViolationType(ConstraintImpactType.HARD_CONSTRAINT_VIOLATION);
                impact.setDirectionOfViolation(DirectionOfViolationType.UNDERSHOOT);
            }

            AbstractDataflowBindingSite abstractDataflowBindingSite = new AbstractDataflowBindingSite();{
                abstractDataflowBindingSite.setSrc(MobileAndroidDevice.class);
                abstractDataflowBindingSite.setDest(Server.class);
                abstractDataflowBindingSite.setHumanReadableDescription("Any data between an android mobile device and server");
            }

            this.setCriterion(criterion);
            this.setAssertionBindingSite(abstractDataflowBindingSite);
            this.setImpact(new ImpactStatement[] {impact});
            this.setApplicableDataType(DataType.class);
            this.setHumanReadableDescription("All data being transmitted between MobileAndroidDevices and FileSystemResources " +
                    "must be confidential");
        }
    }

    @ConceptInstance(name = "ConfidentialDataImplementationStrategy")
    public static class ConfidentialDataImplementationStrategy extends PrescriptiveCauseEffectAssertion {
        public ConfidentialDataImplementationStrategy() {
            ConstraintViolationCriterion criterion = new ConstraintViolationCriterion();{
                criterion.setConstraint(new ClientServerConstraintEnvironment.DataSafetyConstraint());
                criterion.setTriggeringConstraintCriterion(ConstraintCriterionType.WHEN_HARD_VIOLATED);
            }
            this.setCriterion(criterion);

            RemediationImpact impact = new RemediationImpact();{
                impact.setRemediationStrategy(new ClientServerConstraintEnvironment.ImpactOfEncryptingData());
            }
            this.setImpact(new ImpactStatement[] {impact});
            this.setHumanReadableDescription("When the DataSafetyConstraint is \"hard\" violated, this strategy can " +
                    "mitigate the violation, while at the same time introducing ImpactOfEncryptingData.");
        }
    }
    
    @ConceptInstance(name = "ImpactOfEncryptingData")
    public static class ImpactOfEncryptingData extends PredictiveCauseEffectAssertion {
        public ImpactOfEncryptingData() {
            AbstractPropertyCriterion criterion = new AbstractPropertyCriterion();{
                criterion.setProperty(Encrypted.class);
                criterion.setCriterion(PropertyCriterionType.PROPERTY_ADDED);
            }
            this.setCriterion(criterion);

            PropertyImpact impact = new PropertyImpact();{
                impact.setImpactedProperty(ConfidentialProperty.class);
                impact.setImpactOnProperty(PropertyImpactType.ADDS);
            }
            this.setImpact(new ImpactStatement[] {impact});
        }
    }
    
    @ConceptInstance
    public static class ClientDevice extends MobileAndroidDevice {
        public ClientDevice() {
            
        }
    }
    
    @ConceptInstance
    public static class ServerDevice extends Server {
        public ServerDevice() {
            
        }
    }
    
    @ConceptInstance
    public static class ReceiveFromClientDataflowNode extends InterMethodDataflowNode {
        public ReceiveFromClientDataflowNode() {
            this.setJavaMethodPointer("receiveFromClientMethod");
            this.setResourceTemplate(new ServerDevice());
        }
    }
    
    @ConceptInstance
    public static class ReceiveFromClient extends AMethod {
        public ReceiveFromClient() {
            this.setBytecodePointer("receiveFromClientMethod");
        }
    }
    
    @ConceptInstance
    public static class ServerDecryptDataflowNode extends MethodInvocationDataflowNode {
        public ServerDecryptDataflowNode() {
            this.setJavaMethodPointer("serverDecryptMethod");
        }
    }

    @ConceptInstance
    @FunctionalAspectAnnotation(aspect = AspectCipherDecrypt.class)
    public static class ServerDecrypt extends AMethod {
        public ServerDecrypt() {
            AnnotationKeyValuePair keyValuePair = new AnnotationKeyValuePair();{
                keyValuePair.setValue("TYPE(com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherDecrypt)");
            }
            AnAnnotation annotation = new AnAnnotation();{
                annotation.setAnnotationClassName("TYPE(mil/darpa/immortals/annotation/dsl/ontology/dfu/annotation/FunctionalAspectAnnotation)");
                annotation.setKeyValuePairs(new AnnotationKeyValuePair[] {keyValuePair});
            }
            this.setAnnotations(new AnAnnotation[] {annotation});
            this.setBytecodePointer("serverDecryptMethod");
        }
    }
    
    @ConceptInstance
    public static class ServerDecompressDataflowNode extends MethodInvocationDataflowNode {
        public ServerDecompressDataflowNode() {
            this.setJavaMethodPointer("serverDecompressMethod");
        }
    }

    @ConceptInstance
    @FunctionalAspectAnnotation(aspect = AspectInflate.class)
    public static class ServerDecompress extends AMethod {
        public ServerDecompress() {
            AnnotationKeyValuePair keyValuePair = new AnnotationKeyValuePair();{
                keyValuePair.setValue("TYPE(com.securboration.immortals.ontology.functionality.compression.AspectDeflate)");
            }
            AnAnnotation annotation = new AnAnnotation();{
                annotation.setAnnotationClassName("TYPE(mil/darpa/immortals/annotation/dsl/ontology/dfu/annotation/FunctionalAspectAnnotation)");
                annotation.setKeyValuePairs(new AnnotationKeyValuePair[] {keyValuePair});
            }
            this.setAnnotations(new AnAnnotation[] {annotation});
            this.setBytecodePointer("serverDecompressMethod");
            
        }
    }

    @ConceptInstance
    public static class ServerLogDataflowNode extends MethodInvocationDataflowNode {
        public ServerLogDataflowNode() {
            this.setJavaMethodPointer("serverLogMethod");
        }
    }

    @ConceptInstance
    @FunctionalAspectAnnotation(aspect = AspectLog.class)
    public static class ServerLog extends AMethod {
        public ServerLog() {
            AnnotationKeyValuePair keyValuePair = new AnnotationKeyValuePair();{
                keyValuePair.setValue("TYPE(com.securboration.immortals.ontology.functionality.logger.AspectLog)");
            }
            AnAnnotation annotation = new AnAnnotation();{
                annotation.setAnnotationClassName("TYPE(mil/darpa/immortals/annotation/dsl/ontology/dfu/annotation/FunctionalAspectAnnotation)");
                annotation.setKeyValuePairs(new AnnotationKeyValuePair[] {keyValuePair});
            }
            this.setAnnotations(new AnAnnotation[] {annotation});
            this.setBytecodePointer("serverLogMethod");
        }
    }
    
    @ConceptInstance
    public static class EnlargeImageDataNode extends MethodInvocationDataflowNode {
        public EnlargeImageDataNode() {
            this.setJavaMethodPointer("clientEnlargeImageMethod");
        }
    }

    @DfuAnnotation(functionalityBeingPerformed = Cipher.class)
    @FunctionalAspectAnnotation(aspect = EnlargeImage.class)
    @ConceptInstance
    public static class ClientEnlargeImage extends AMethod {
        public ClientEnlargeImage() {
            this.setBytecodePointer("clientEnlargeImageMethod");
        }
    }
    
    @ConceptInstance
    public static class ClientCompressDataNode extends MethodInvocationDataflowNode {
        public ClientCompressDataNode() {
            this.setJavaMethodPointer("clientCompressMethod");
        }
    }
    
    @FunctionalAspectAnnotation(aspect = AspectDeflate.class)
    @ConceptInstance
    public static class ClientCompress extends AMethod {
        public ClientCompress() {
            this.setBytecodePointer("clientCompressMethod");
        }
    }
    
    @ConceptInstance
    public static class ClientEncryptDataflowNode extends MethodInvocationDataflowNode {
        public ClientEncryptDataflowNode() {
            this.setJavaMethodPointer("clientEncryptMethod");
        }
    }
    
    @FunctionalAspectAnnotation(aspect = AspectCipherEncrypt.class)
    @ConceptInstance
    public static class ClientEncrypt extends AMethod {
        public ClientEncrypt() {
            this.setBytecodePointer("clientEncryptMethod");
        }
    }
    
    @ConceptInstance
    public static class SendToServerDataflowNode extends InterMethodDataflowNode {
        public SendToServerDataflowNode() {
            this.setJavaMethodPointer("sendToServerMethod");
            this.setResourceTemplate(new ClientDevice());
        }
    }
    
    @ConceptInstance
    public static class SendToServer extends AMethod {
        public SendToServer() {
            this.setBytecodePointer("sendToServerMethod");
        }
    }
    
    @ConceptInstance
    public static class EnlargeToCompressDataflowEdge extends MethodInvocationDataflowEdge {
        public EnlargeToCompressDataflowEdge() {
            this.setProducer(new EnlargeImageDataNode());
            this.setConsumer(new ClientCompressDataNode());
        }
    }
    
    @ConceptInstance
    public static class CompressToEncryptDataflowEdge extends MethodInvocationDataflowEdge {
        public CompressToEncryptDataflowEdge() {
            this.setProducer(new ClientCompressDataNode());
            this.setConsumer(new ClientEncryptDataflowNode());
        }
    }
    
    @ConceptInstance
    public static class EncryptToSendDataflowEdge extends MethodInvocationDataflowEdge {
        public EncryptToSendDataflowEdge() {
            this.setProducer(new ClientEncryptDataflowNode());
            this.setConsumer(new SendToServerDataflowNode());
        }
    }
    
    @ConceptInstance
    public static class ReceiveDataToDecryptDataflowEdge extends MethodInvocationDataflowEdge {
        public ReceiveDataToDecryptDataflowEdge() {
            this.setProducer(new ReceiveFromClientDataflowNode());
            this.setConsumer(new ServerDecryptDataflowNode());
        }
    }
    
    @ConceptInstance
    public static class DecryptToDecompressDataflowEdge extends MethodInvocationDataflowEdge {
        public DecryptToDecompressDataflowEdge() {
            this.setProducer(new ServerDecryptDataflowNode());
            this.setConsumer(new ServerDecompressDataflowNode());
        }
    }
    
    @ConceptInstance
    public static class DecompressToLoggingDataflowEdge extends MethodInvocationDataflowEdge {
        public DecompressToLoggingDataflowEdge() {
            this.setProducer(new ServerDecompressDataflowNode());
            this.setConsumer(new ServerLogDataflowNode());
        }
    }
    
    @ConceptInstance
    public static class ClientToServerDataflowEdgeGood extends InterMethodDataflowEdge {
        public ClientToServerDataflowEdgeGood() {
            this.setProducer(new SendToServerDataflowNode());
            this.setConsumer(new ReceiveFromClientDataflowNode());

            Property enlargedImage = new NumberOfPixels();
            Compressed compressedProperty = new Compressed();
            compressedProperty.setCompressionAlgorithm(CompressionAlgorithm.class);
            Encrypted encryptedProperty = new Encrypted();
            encryptedProperty.setEncryptionAlgorithm(AES_256.class);
            this.setEdgeProperties(new Property[] {enlargedImage, encryptedProperty, compressedProperty});

            DataflowAnalysisFrame enlargeImageFrame = new DataflowAnalysisFrame();
            enlargeImageFrame.setAnalysisFrameDataType(BinaryData.class);
            enlargeImageFrame.setFrameProperties(new Property[] {new NumberOfPixels()});
            DataflowAnalysisFrame compressFrame = new DataflowAnalysisFrame();
            compressFrame.setAnalysisFrameDataType(BinaryData.class);
            compressFrame.setFrameProperties(new Compressed[] {compressedProperty});
            DataflowAnalysisFrame encryptFrame = new DataflowAnalysisFrame();
            encryptFrame.setAnalysisFrameDataType(BinaryData.class);
            encryptFrame.setFrameProperties(new Encrypted[] {encryptedProperty});
            
            encryptFrame.setAnalysisFrameChild(compressFrame);
            compressFrame.setAnalysisFrameChild(enlargeImageFrame);
            
            this.setDataflowAnalysisFrame(encryptFrame);
        }
    }

    @ConceptInstance
    public static class ClientToServerDataflowEdgeBadAlgorithm extends InterMethodDataflowEdge {
        public ClientToServerDataflowEdgeBadAlgorithm() {
            this.setProducer(new SendToServerDataflowNode());
            this.setConsumer(new ReceiveFromClientDataflowNode());

            Property enlargedImage = new NumberOfPixels();
            Compressed compressedProperty = new Compressed();
            compressedProperty.setCompressionAlgorithm(CompressionAlgorithm.class);
            Encrypted encryptedProperty = new Encrypted();
            encryptedProperty.setEncryptionAlgorithm(AES_128.class);
            this.setEdgeProperties(new Property[] {enlargedImage, encryptedProperty, compressedProperty});

            DataflowAnalysisFrame enlargeImageFrame = new DataflowAnalysisFrame();
            enlargeImageFrame.setAnalysisFrameDataType(BinaryData.class);
            enlargeImageFrame.setFrameProperties(new Property[] {new NumberOfPixels()});
            DataflowAnalysisFrame compressFrame = new DataflowAnalysisFrame();
            compressFrame.setAnalysisFrameDataType(BinaryData.class);
            compressFrame.setFrameProperties(new Compressed[] {compressedProperty});
            DataflowAnalysisFrame encryptFrame = new DataflowAnalysisFrame();
            encryptFrame.setAnalysisFrameDataType(BinaryData.class);
            encryptFrame.setFrameProperties(new Encrypted[] {encryptedProperty});

            encryptFrame.setAnalysisFrameChild(compressFrame);
            compressFrame.setAnalysisFrameChild(enlargeImageFrame);

            this.setDataflowAnalysisFrame(encryptFrame);
        }
    }

    @ConceptInstance
    public static class ClientToServerDataflowEdgeBadOrder extends InterMethodDataflowEdge {
        public ClientToServerDataflowEdgeBadOrder() {
            this.setProducer(new SendToServerDataflowNode());
            this.setConsumer(new ReceiveFromClientDataflowNode());

            Property enlargedImage = new NumberOfPixels();
            Compressed compressedProperty = new Compressed();
            compressedProperty.setCompressionAlgorithm(CompressionAlgorithm.class);
            Encrypted encryptedProperty = new Encrypted();
            encryptedProperty.setEncryptionAlgorithm(AES_128.class);
            this.setEdgeProperties(new Property[] {enlargedImage, encryptedProperty, compressedProperty});

            DataflowAnalysisFrame enlargeImageFrame = new DataflowAnalysisFrame();
            enlargeImageFrame.setAnalysisFrameDataType(BinaryData.class);
            enlargeImageFrame.setFrameProperties(new Property[] {new NumberOfPixels()});
            DataflowAnalysisFrame compressFrame = new DataflowAnalysisFrame();
            compressFrame.setAnalysisFrameDataType(BinaryData.class);
            compressFrame.setFrameProperties(new Compressed[] {compressedProperty});
            DataflowAnalysisFrame encryptFrame = new DataflowAnalysisFrame();
            encryptFrame.setAnalysisFrameDataType(BinaryData.class);
            encryptFrame.setFrameProperties(new Encrypted[] {encryptedProperty});

            compressFrame.setAnalysisFrameChild(enlargeImageFrame);
            enlargeImageFrame.setAnalysisFrameChild(encryptFrame);

            this.setDataflowAnalysisFrame(compressFrame);
        }
    }
}
