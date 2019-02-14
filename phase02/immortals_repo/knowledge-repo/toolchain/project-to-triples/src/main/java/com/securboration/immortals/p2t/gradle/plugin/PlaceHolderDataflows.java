package com.securboration.immortals.p2t.gradle.plugin;

import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.ontology.constraint.*;
import com.securboration.immortals.ontology.functionality.ConfidentialProperty;
import com.securboration.immortals.ontology.functionality.alg.encryption.aes.AES_256;
import com.securboration.immortals.ontology.functionality.compression.CompressionAlgorithm;
import com.securboration.immortals.ontology.functionality.dataproperties.*;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.Image;
import com.securboration.immortals.ontology.image.fidelity.ColorFidelity;
import com.securboration.immortals.ontology.image.fidelity.Greyscale8;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.property.impact.*;
import com.securboration.immortals.ontology.resources.FileSystemResource;
import com.securboration.immortals.ontology.resources.MobileAndroidDevice;
import com.securboration.immortals.ontology.resources.Server;
import com.securboration.immortals.ontology.resources.UsbResource;

public class PlaceHolderDataflows {

    @ConceptInstance
    public static class ColorFidelityConstraint extends ProscriptiveCauseEffectAssertion {

        public ColorFidelityConstraint() {
            AbstractPropertyCriterion criterion = new AbstractPropertyCriterion();
            {
                criterion.setProperty(ColorFidelity.class);
                criterion.setCriterion(PropertyCriterionType.PROPERTY_ABSENT);
            }

            ConstraintViolationImpact impact = new ConstraintViolationImpact();
            {
                impact.setConstraintViolationType(ConstraintImpactType.HARD_CONSTRAINT_VIOLATION);
                impact.setDirectionOfViolation(DirectionOfViolationType.UNDERSHOOT);
            }

            AbstractDataflowBindingSite abstractDataflowBindingSite = new AbstractDataflowBindingSite();
            {
                abstractDataflowBindingSite.setSrc(UsbResource.class);
                abstractDataflowBindingSite.setDest(FileSystemResource.class);
                abstractDataflowBindingSite.setHumanReadableDescription("Any data between a usb and file system");
            }

            this.setCriterion(criterion);
            this.setImpact(new ImpactStatement[] {impact});
            this.setApplicableDataType(Image.class);

        }
    }

    @ConceptInstance(name = "ConfidentialDataImplementationStrategy")
    public static class GreyScaleImageImplementationStrategy extends PrescriptiveCauseEffectAssertion {
        public GreyScaleImageImplementationStrategy() {
            ConstraintViolationCriterion criterion = new ConstraintViolationCriterion();{
                criterion.setConstraint(new ColorFidelityConstraint());
                criterion.setTriggeringConstraintCriterion(ConstraintCriterionType.WHEN_HARD_VIOLATED);
            }
            this.setCriterion(criterion);

            RemediationImpact impact = new RemediationImpact();{
                impact.setRemediationStrategy(new ImpactOfGreyScalingImages());
            }
            this.setImpact(new ImpactStatement[] {impact});
            this.setHumanReadableDescription("When the DataSafetyConstraint is \"hard\" violated, this strategy can " +
                    "mitigate the violation, while at the same time introducing ImpactOfEncryptingData.");
        }
    }

    @ConceptInstance(name = "ImpactOfEncryptingData")
    public static class ImpactOfGreyScalingImages extends PredictiveCauseEffectAssertion {
        public ImpactOfGreyScalingImages() {
            AbstractPropertyCriterion criterion = new AbstractPropertyCriterion();{
                criterion.setProperty(Greyscale8.class);
                criterion.setCriterion(PropertyCriterionType.PROPERTY_ADDED);
            }
            this.setCriterion(criterion);

            PropertyImpact impact = new PropertyImpact();{
                impact.setImpactedProperty(ColorFidelity.class);
                impact.setImpactOnProperty(PropertyImpactType.ADDS);
            }
            this.setImpact(new ImpactStatement[] {impact});
        }
    }

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
    public static class SendDataToServer extends InterMethodDataflowNode {
        public SendDataToServer() {
            this.setJavaMethodName("sendToServer");
            this.setJavaClassName("MonolithicAtakApplication$Dfus$SimpleAesCipher");
            MobileAndroidDevice clientDevice = new MobileAndroidDevice();
            this.setResourceTemplate(clientDevice);
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

    @ConceptInstance
    public static class ReadToCompress extends MethodInvocationDataflowEdge {
        public ReadToCompress() {
            this.setProducer(new ClientReadImage());
            this.setConsumer(new ClientCompressImage());
            this.setDataTypeCommunicated(Image.class);
        }
    }

    @ConceptInstance
    public static class CompressToEncrypt extends MethodInvocationDataflowEdge {
        public CompressToEncrypt() {
            this.setProducer(new ClientCompressImage());
            this.setConsumer(new ClientEncryptImage());
            this.setDataTypeCommunicated(BinaryData.class);
        }
    }

    @ConceptInstance
    public static class EncryptToSend extends MethodInvocationDataflowEdge {
        public EncryptToSend() {
            this.setProducer(new ClientEncryptImage());
            this.setConsumer(new SendDataToServer());
            this.setDataTypeCommunicated(BinaryData.class);
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

    @ConceptInstance
    public static class ClientReadImage extends MethodInvocationDataflowNode {
        public ClientReadImage() {
            this.setJavaMethodName("readImage");
            this.setJavaClassName("MonolithicAtakApplication$Dfus$SimpleAesCipher");
        }
    }

    @ConceptInstance
    public static class ClientCompressImage extends MethodInvocationDataflowNode {
        public ClientCompressImage() {
            this.setJavaMethodName("compressImage");
            this.setJavaClassName("MonolithicAtakApplication$Dfus$SimpleAesCipher");
        }
    }

    @ConceptInstance
    public static class ClientEncryptImage extends MethodInvocationDataflowNode {
        public ClientEncryptImage() {
            this.setJavaMethodName("encryptImage");
            this.setJavaClassName("MonolithicAtakApplication$Dfus$SimpleAesCipher");
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
    
    //////////////////////////

    @ConceptInstance
    public static class ClientToFileSystem extends InterMethodDataflowEdge {
        public ClientToFileSystem() {
            this.setProducer(new SendDataToFileSystem());
            this.setConsumer(new FileSystemReceiveImage());
            this.setDataTypeCommunicated(BinaryData.class);

            GrayScale grayScale = new GrayScale();
            Pixelated pixelated = new Pixelated();
            this.setEdgeProperties(new Property[] {pixelated, grayScale});

            DataflowAnalysisFrame mainFrame = new DataflowAnalysisFrame();
            mainFrame.setAnalysisFrameDataType(Image.class);
            mainFrame.setFrameProperties(new Property[] {pixelated, grayScale});
            DataflowAnalysisFrame readImageFrame = new DataflowAnalysisFrame();
            readImageFrame.setAnalysisFrameDataType(Image.class);
            
            readImageFrame.setAnalysisFrameChild(mainFrame);
            this.setDataflowAnalysisFrame(readImageFrame);
        }
    }

    @ConceptInstance
    public static class SendDataToFileSystem extends InterMethodDataflowNode {
        public SendDataToFileSystem() {
            this.setJavaMethodName("sendToFileSystem");
            this.setJavaClassName("MonolithicAtakApplication$Dfus$SimpleAesCipher");
            MobileAndroidDevice clientDevice = new MobileAndroidDevice();
            this.setResourceTemplate(clientDevice);
        }
    }

    @ConceptInstance
    public static class FileSystemReceiveImage extends InterMethodDataflowNode {
        public FileSystemReceiveImage() {
            this.setJavaMethodName("fileSysReceiveImage");
            this.setJavaClassName("MonolithicAtakApplication$Dfus$SimpleAesCipher");
            FileSystemResource fileSystemResource = new FileSystemResource();
            this.setResourceTemplate(fileSystemResource);
        }
    }

    @ConceptInstance
    public static class FileSystemWriteImage extends MethodInvocationDataflowNode {
        public FileSystemWriteImage() {
            this.setJavaMethodName("fileSysWriteImage");
            this.setJavaClassName("MonolithicAtakApplication$Dfus$SimpleAesCipher");
        }
    }

    @ConceptInstance
    public static class FileSystemRemoveBadImageProps extends MethodInvocationDataflowNode {
        public FileSystemRemoveBadImageProps() {
            this.setJavaMethodName("fileSysRemoveBadImageProps");
            this.setJavaClassName("MonolithicAtakApplication$Dfus$SimpleAesCipher");
        }
    }
    
    @ConceptInstance
    public static class FileSysReceiveToRemoveBadImageProps extends MethodInvocationDataflowEdge {
        public FileSysReceiveToRemoveBadImageProps() {
            this.setProducer(new FileSystemReceiveImage());
            this.setConsumer(new FileSystemRemoveBadImageProps());
            this.setDataTypeCommunicated(Image.class);
        }
    }

    @ConceptInstance
    public static class FileSysReceiveToWriteImage extends MethodInvocationDataflowEdge {
        public FileSysReceiveToWriteImage() {
            this.setProducer(new FileSystemReceiveImage());
            this.setConsumer(new FileSystemWriteImage());
            this.setDataTypeCommunicated(Image.class);
        }
    }

    @ConceptInstance
    public static class FileSysWriteImageToRemoveProps extends MethodInvocationDataflowEdge {
        public FileSysWriteImageToRemoveProps() {
            this.setProducer(new FileSystemWriteImage());
            this.setConsumer(new FileSystemRemoveBadImageProps());
            this.setDataTypeCommunicated(Image.class);
        }
    }
    
    @ConceptInstance
    public static class FileSysRemovePropsToWrite extends MethodInvocationDataflowEdge {
        public FileSysRemovePropsToWrite() {
            this.setProducer(new FileSystemRemoveBadImageProps());
            this.setConsumer(new FileSystemWriteImage());
            this.setDataTypeCommunicated(Image.class);
        }
    }

}

