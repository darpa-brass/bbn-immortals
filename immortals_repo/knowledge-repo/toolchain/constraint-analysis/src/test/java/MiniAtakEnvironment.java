import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.ontology.functionality.alg.encryption.aes.AES_256;
import com.securboration.immortals.ontology.functionality.compression.CompressionAlgorithm;
import com.securboration.immortals.ontology.functionality.dataproperties.Compressed;
import com.securboration.immortals.ontology.functionality.dataproperties.Encrypted;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.Image;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.resources.MobileAndroidDevice;
import com.securboration.immortals.ontology.resources.Server;

public class MiniAtakEnvironment {
    
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
    
}
