package com.securboration.immortals.exampleDataflows;

import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.ontology.functionality.dataproperties.GrayScale;
import com.securboration.immortals.ontology.functionality.dataproperties.Pixelated;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.Image;
import com.securboration.immortals.ontology.functionality.datatype.OutputStream;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.ontology.resources.FileSystemResource;
import com.securboration.immortals.ontology.resources.MobileAndroidDevice;
import com.securboration.immortals.ontology.types.CotMessage;

public class FileSystemDataflows {

    @ConceptInstance
    public static class ClientToFileSystem extends InterMethodDataflowEdge {
        public ClientToFileSystem() {
            this.setProducer(new SendDataToServer());
            this.setConsumer(new ReceiveFromClient());
            this.setDataTypeCommunicated(BinaryData.class);
            
            DataflowAnalysisFrame mainFrame = new DataflowAnalysisFrame();
            mainFrame.setAnalysisFrameDataType(BinaryData.class);
            DataflowAnalysisFrame readImageFrame = new DataflowAnalysisFrame();
            readImageFrame.setAnalysisFrameDataType(CotMessage.class);

            mainFrame.setAnalysisFrameChild(readImageFrame);
            this.setDataflowAnalysisFrame(mainFrame);
        }
    }
    
    @ConceptInstance
    public static class EnterToSendMessage extends MethodInvocationDataflowEdge {
        public EnterToSendMessage() {
            this.setProducer(new EnterClient());
            this.setConsumer(new SendDataToServer());
            this.setDataTypeCommunicated(BinaryData.class);
        }
    }

    @ConceptInstance
    public static class SomewhereToSendOutputStream extends MethodInvocationDataflowEdge {
        public SomewhereToSendOutputStream() {
            this.setProducer(new EnterClient());
            this.setConsumer(new SendDataToServer());
            this.setDataTypeCommunicated(OutputStream.class);
        }
    }
    
    
    
    @ConceptInstance 
    public static class ReceiveToExit extends MethodInvocationDataflowEdge {
        public ReceiveToExit() {
            this.setProducer(new ReceiveFromClient());
            this.setConsumer(new ExitServer());
        }
    }
    

    @ConceptInstance
    public static class SendDataToServer extends InterMethodDataflowNode {
        public SendDataToServer() {
            this.setJavaMethodName("sendToServer");
            this.setJavaClassName("com.securboration.miniatakapp.AndroidClientTest");
            
        }
    }

    @ConceptInstance
    public static class ReceiveFromClient extends InterMethodDataflowNode {
        public ReceiveFromClient() {
            this.setJavaMethodName("receiveFromClient");
            this.setJavaClassName("com.securboration.miniatakapp.AndroidClientTest");
        }
    }

    @ConceptInstance
    public static class EnterClient extends MethodInvocationDataflowNode {
        public EnterClient() {
            this.setJavaMethodName("enterClient");
            this.setJavaClassName("com.securboration.miniatakapp.AndroidClientTest");
        }
    }

    @ConceptInstance
    public static class ExitServer extends MethodInvocationDataflowNode {
        public ExitServer() {
            this.setJavaMethodName("exitServer");
            this.setJavaClassName("com.securboration.miniatakapp.AndroidClientTest");
        }
    }
    
}
