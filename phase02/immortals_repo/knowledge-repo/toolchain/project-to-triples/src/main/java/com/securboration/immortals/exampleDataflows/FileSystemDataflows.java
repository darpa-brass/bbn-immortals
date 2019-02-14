package com.securboration.immortals.exampleDataflows;

import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.ontology.functionality.dataproperties.GrayScale;
import com.securboration.immortals.ontology.functionality.dataproperties.Pixelated;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.Image;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.resources.FileSystemResource;
import com.securboration.immortals.ontology.resources.MobileAndroidDevice;

public class FileSystemDataflows {

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

            mainFrame.setAnalysisFrameChild(readImageFrame);
            this.setDataflowAnalysisFrame(mainFrame);
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


    public static class FileSystemGoodDataflows {
        
        @ConceptInstance
        public static class FileSysReceiveToRemoveBadImageProps extends MethodInvocationDataflowEdge {
            public FileSysReceiveToRemoveBadImageProps() {
                this.setProducer(new FileSystemReceiveImage());
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
    
    public static class FileSystemBadDataflows {

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
        
    }
    
}
