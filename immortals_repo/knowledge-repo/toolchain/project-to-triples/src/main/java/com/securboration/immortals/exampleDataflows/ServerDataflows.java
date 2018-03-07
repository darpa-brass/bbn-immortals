package com.securboration.immortals.exampleDataflows;

import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.ontology.constraint.*;
import com.securboration.immortals.ontology.functionality.ConfidentialProperty;
import com.securboration.immortals.ontology.functionality.alg.encryption.aes.AES_256;
import com.securboration.immortals.ontology.functionality.compression.CompressionAlgorithm;
import com.securboration.immortals.ontology.functionality.dataproperties.Compressed;
import com.securboration.immortals.ontology.functionality.dataproperties.Encrypted;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.Image;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.property.impact.*;
import com.securboration.immortals.ontology.resources.MobileAndroidDevice;
import com.securboration.immortals.ontology.resources.Server;

public class ServerDataflows {

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
            this.setApplicableDataType(BinaryData.class);
            this.setHumanReadableDescription("All data being transmitted between MobileAndroidDevices and Servers " +
                    "must be confidential");
        }
    }

    @ConceptInstance(name = "ConfidentialDataImplementationStrategy")
    public static class ConfidentialDataImplementationStrategy extends PrescriptiveCauseEffectAssertion {
        public ConfidentialDataImplementationStrategy() {
            ConstraintViolationCriterion criterion = new ConstraintViolationCriterion();{
                criterion.setConstraint(new DataSafetyConstraint());
                criterion.setTriggeringConstraintCriterion(ConstraintCriterionType.WHEN_HARD_VIOLATED);
            }
            this.setCriterion(criterion);

            RemediationImpact impact = new RemediationImpact();{
                impact.setRemediationStrategy(new ImpactOfEncryptingData());
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
    public static class ReceiveDataFromClient extends InterMethodDataflowNode {
        public ReceiveDataFromClient() {
            this.setJavaMethodName("receiveFromClient");
            this.setJavaClassName("MonolithicAtakApplication$Dfus$SimpleAesCipher");
            Server server = new Server();
            this.setResourceTemplate(server);
        }
    }
    
    @ConceptInstance
    public static class SendDataToServer extends InterMethodDataflowNode {
        public SendDataToServer() {
            this.setJavaMethodName("sendToServer");
            this.setJavaClassName("MonolithicAtakApplication$Dfus$SimpleAesCipher");
            MobileAndroidDevice clientDevice = new MobileAndroidDevice();
            this.setResourceTemplate(clientDevice);
        }
    }

    @ConceptInstance
    public static class ServerWriteImage extends MethodInvocationDataflowNode {
        public ServerWriteImage() {
            this.setJavaMethodName("writeImage");
            this.setJavaClassName("MonolithicAtakApplication$Dfus$SimpleAesCipher");
        }
    }

    @ConceptInstance
    public static class ServerDecompressImage extends MethodInvocationDataflowNode {
        public ServerDecompressImage() {
            this.setJavaMethodName("decompressImage");
            this.setJavaClassName("MonolithicAtakApplication$Dfus$SimpleAesCipher");
        }
    }

    @ConceptInstance
    public static class ServerDecryptImage extends MethodInvocationDataflowNode {
        public ServerDecryptImage() {
            this.setJavaMethodName("decryptImage");
            this.setJavaClassName("MonolithicAtakApplication$Dfus$SimpleAesCipher");
        }
    }

    @ConceptInstance
    public static class ReceiveToDecrypt extends MethodInvocationDataflowEdge {
        public ReceiveToDecrypt() {
            this.setProducer(new ReceiveDataFromClient());
            this.setConsumer(new ServerDecryptImage());
            this.setDataTypeCommunicated(BinaryData.class);
        }
    }

    @ConceptInstance
    public static class DecryptToDecompress extends MethodInvocationDataflowEdge {
        public DecryptToDecompress() {
            this.setProducer(new ServerDecryptImage());
            this.setConsumer(new ServerDecompressImage());
            this.setDataTypeCommunicated(BinaryData.class);
        }
    }

    @ConceptInstance
    public static class DecompressToWrite extends MethodInvocationDataflowEdge {
        public DecompressToWrite() {
            this.setProducer(new ServerDecompressImage());
            this.setConsumer(new ServerWriteImage());
            this.setDataTypeCommunicated(Image.class);
        }
    }
    
    public static class ServerGoodDataflows {

        @ConceptInstance
        public static class ClientToServer extends InterMethodDataflowEdge {
            public ClientToServer() {
                this.setProducer(new SendDataToServer());
                this.setConsumer(new ReceiveDataFromClient());
                this.setDataTypeCommunicated(BinaryData.class);

                Compressed compressedProperty = new Compressed();
                compressedProperty.setCompressionAlgorithm(CompressionAlgorithm.class);
                Encrypted encryptedProperty = new Encrypted();
                encryptedProperty.setEncryptionAlgorithm(AES_256.class);
                this.setEdgeProperties(new Property[] {encryptedProperty, compressedProperty});

                DataflowAnalysisFrame compressFrame = new DataflowAnalysisFrame();
                compressFrame.setAnalysisFrameDataType(BinaryData.class);
                compressFrame.setFrameProperties(new Compressed[] {compressedProperty});
                DataflowAnalysisFrame encryptFrame = new DataflowAnalysisFrame();
                encryptFrame.setAnalysisFrameDataType(BinaryData.class);
                encryptFrame.setFrameProperties(new Encrypted[] {encryptedProperty});
                DataflowAnalysisFrame readImageFrame = new DataflowAnalysisFrame();
                readImageFrame.setAnalysisFrameDataType(Image.class);

                encryptFrame.setAnalysisFrameChild(compressFrame);
                compressFrame.setAnalysisFrameChild(readImageFrame);
                this.setDataflowAnalysisFrame(encryptFrame);
            }
        }
      
    }
    
    public static class ServerBadDataflows {

        @ConceptInstance
        public static class ClientToServer extends InterMethodDataflowEdge {
            public ClientToServer() {
                this.setProducer(new SendDataToServer());
                this.setConsumer(new ReceiveDataFromClient());
                this.setDataTypeCommunicated(BinaryData.class);

                Compressed compressedProperty = new Compressed();
                compressedProperty.setCompressionAlgorithm(CompressionAlgorithm.class);
                Encrypted encryptedProperty = new Encrypted();
                encryptedProperty.setEncryptionAlgorithm(AES_256.class);
                this.setEdgeProperties(new Property[] {encryptedProperty, compressedProperty});

                DataflowAnalysisFrame compressFrame = new DataflowAnalysisFrame();
                compressFrame.setAnalysisFrameDataType(BinaryData.class);
                compressFrame.setFrameProperties(new Compressed[] {compressedProperty});
                DataflowAnalysisFrame encryptFrame = new DataflowAnalysisFrame();
                encryptFrame.setAnalysisFrameDataType(BinaryData.class);
                encryptFrame.setFrameProperties(new Encrypted[] {encryptedProperty});
                DataflowAnalysisFrame readImageFrame = new DataflowAnalysisFrame();
                readImageFrame.setAnalysisFrameDataType(Image.class);

                encryptFrame.setAnalysisFrameChild(readImageFrame);
                compressFrame.setAnalysisFrameChild(encryptFrame);
                this.setDataflowAnalysisFrame(compressFrame);
            }
        }
        
    }
    
}
