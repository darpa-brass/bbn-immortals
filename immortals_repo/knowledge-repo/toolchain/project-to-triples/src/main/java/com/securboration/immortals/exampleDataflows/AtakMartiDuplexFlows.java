package com.securboration.immortals.exampleDataflows;

import com.securboration.immortals.ontology.analysis.*;
import com.securboration.immortals.ontology.constraint.*;
import com.securboration.immortals.ontology.cp2.Analysis;
import com.securboration.immortals.ontology.functionality.ConfidentialProperty;
import com.securboration.immortals.ontology.functionality.dataproperties.Encrypted;
import com.securboration.immortals.ontology.functionality.datatype.BinaryData;
import com.securboration.immortals.ontology.functionality.datatype.InputStream;
import com.securboration.immortals.ontology.functionality.datatype.OutputStream;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.property.impact.*;
import com.securboration.immortals.ontology.types.CotMessage;

@Ignore
public class AtakMartiDuplexFlows {

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

            AbstractDataflowBindingSite clientToServerBinding = new AbstractDataflowBindingSite();
            clientToServerBinding.setSrc(Analysis.Atak.AtakPhone.class);
            clientToServerBinding.setDest(Analysis.Marti.MartiServer.class);
            clientToServerBinding.setHumanReadableDescription("Any data between an android mobile device and server");
            

            AbstractDataflowBindingSite serverToClientBinding = new AbstractDataflowBindingSite();{
                serverToClientBinding.setDest(Analysis.Atak.AtakPhone.class);
                serverToClientBinding.setSrc(Analysis.Marti.MartiServer.class);
                serverToClientBinding.setHumanReadableDescription("Any data between an android mobile device and server");
            }

            this.setCriterion(criterion);
            this.setAssertionBindingSites(new AssertionBindingSite[] {clientToServerBinding, serverToClientBinding});
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

    @ConceptInstance public static class CreateAtakStreams extends MethodInvocationDataflowNode {
        public CreateAtakStreams() {
            this.setJavaMethodName("open");
            this.setJavaClassName("java/nio/channels/SocketChannel");
            this.setJavaMethodPointer("unableToGenerateMethodPointer");
            this.setLineNumber(85);
        }
    }
    
    @ConceptInstance public static class CreateStreamsToConfigureBlocking extends MethodInvocationDataflowEdge {
        public CreateStreamsToConfigureBlocking() {
            this.setDataTypeCommunicated(OutputStream.class);
            DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
            frame1.setAnalysisFrameChild(null);
            frame1.setAnalysisFrameDataType(OutputStream.class);
            this.setDataflowAnalysisFrame(frame1);
            this.setProducer(new CreateAtakStreams());
            this.setConsumer(new ConfigureBlocking());
        }
    }
    
    @ConceptInstance public static class ConfigureBlocking extends MethodInvocationDataflowNode {
        public ConfigureBlocking() {
            this.setJavaMethodName("configureBlocking");
            this.setJavaClassName("java/nio/channels/SocketChannel");
            this.setJavaMethodPointer("unableToGenerateMethodPointer");
            this.setLineNumber(89);
        }
    }
    
    
    @ConceptInstance public static class ConfigureBlockingToFirstRegister extends MethodInvocationDataflowEdge {
        public ConfigureBlockingToFirstRegister() {
            this.setDataTypeCommunicated(OutputStream.class);
            DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
            frame1.setAnalysisFrameChild(null);
            frame1.setAnalysisFrameDataType(OutputStream.class);
            this.setDataflowAnalysisFrame(frame1);
            this.setProducer(new ConfigureBlocking());
            this.setConsumer(new FirstRegister());
        }
    }
    
    @ConceptInstance public static class FirstRegister extends MethodInvocationDataflowNode {
        public FirstRegister() {
            this.setJavaMethodName("register");
            this.setJavaClassName("java/nio/channels/SocketChannel");
            this.setJavaMethodPointer("unableToGenerateMethodPointer");
            this.setLineNumber(92);
        }
    }
    
    @ConceptInstance public static class FirstRegisterToSecondRegister extends MethodInvocationDataflowEdge {
        public FirstRegisterToSecondRegister() {
            this.setDataTypeCommunicated(OutputStream.class);
            DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
            frame1.setAnalysisFrameChild(null);
            frame1.setAnalysisFrameDataType(OutputStream.class);
            this.setDataflowAnalysisFrame(frame1);
            this.setProducer(new FirstRegister());
            this.setConsumer(new SecondRegister());
        }
    }
    
    @ConceptInstance public static class SecondRegister extends MethodInvocationDataflowNode {
        public SecondRegister() {
            this.setJavaMethodName("register");
            this.setJavaClassName("java/nio/channels/SocketChannel");
            this.setJavaMethodPointer("unableToGenerateMethodPointer");
            this.setLineNumber(100);
        }
    }

    /*@ConceptInstance public static class CreateStreamToCreateSelectionEvent extends MethodInvocationDataflowEdge {
        public CreateStreamToCreateSelectionEvent() {
            this.setDataTypeCommunicated(OutputStream.class);
            DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
            frame1.setAnalysisFrameChild(null);
            frame1.setAnalysisFrameDataType(OutputStream.class);
            this.setDataflowAnalysisFrame(frame1);
            this.setProducer(new CreateAtakStreams());
            this.setConsumer(new CreateSelectionEvent());
        }
    }*/
    
    @ConceptInstance public static class SecondRegisterToCreateSelectionEvent extends MethodInvocationDataflowEdge {
        public SecondRegisterToCreateSelectionEvent() {
            this.setDataTypeCommunicated(OutputStream.class);
            DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
            frame1.setAnalysisFrameChild(null);
            frame1.setAnalysisFrameDataType(OutputStream.class);
            this.setDataflowAnalysisFrame(frame1);
            this.setProducer(new SecondRegister());
            this.setConsumer(new CreateSelectionEvent());
        }
    }

    @ConceptInstance public static class CreateSelectionEvent extends MethodInvocationDataflowNode {
        public CreateSelectionEvent() {
            this.setJavaMethodName("<init>");
            this.setJavaClassName("com/bbn/ataklite/net/SelectionEvent");
            this.setJavaMethodPointer("DAboW+5MJBY4dZp3Fx0Qx7qZXK09C4xuDzoSmg3hrX4=/methods/<init>(Ljava/nio/channels/SelectionKey;Ljava/nio/channels/SocketChannel;Ljava/lang/Object;)V");
            this.setLineNumber(116);
        }
    }

    @ConceptInstance public static class CreateSelectionEventToHandleWriteEvent extends MethodInvocationDataflowEdge {
        public CreateSelectionEventToHandleWriteEvent() {
            this.setDataTypeCommunicated(OutputStream.class);
            DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
            frame1.setAnalysisFrameChild(null);
            frame1.setAnalysisFrameDataType(OutputStream.class);
            this.setDataflowAnalysisFrame(frame1);
            this.setProducer(new CreateSelectionEvent());
            this.setConsumer(new HandleWriteEvent());
        }
    }

    @ConceptInstance public static class HandleWriteEvent extends MethodInvocationDataflowNode {
        public HandleWriteEvent() {
            this.setJavaMethodName("handleEvent");
            this.setJavaClassName("com/bbn/ataklite/net/WriteHandler");
            this.setJavaMethodPointer("cuceBFnuDgKTTxGg17SNpUdiwwVKynyKc/lrZM1Dy5E=/methods/handleEvent(Lcom/bbn/ataklite/net/SelectionEvent;)V");
            this.setLineNumber(116);
        }
    }

    @ConceptInstance public static class HandleEventToWriteMessage extends MethodInvocationDataflowEdge {
        public HandleEventToWriteMessage() {
            this.setDataTypeCommunicated(OutputStream.class);
            DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
            frame1.setAnalysisFrameChild(null);
            frame1.setAnalysisFrameDataType(OutputStream.class);
            this.setDataflowAnalysisFrame(frame1);
            this.setProducer(new HandleWriteEvent());
            this.setConsumer(new DispatchMessage());
        }
    }

        @ConceptInstance public static class CreateCotMessage extends MethodInvocationDataflowNode {
            public CreateCotMessage() {
                this.setJavaMethodName("<init>");
                this.setJavaClassName("com/bbn/ataklite/CoTMessage");
                this.setJavaMethodPointer("uH9lGZmDYA3u0xK9tJXbcKuP9l6D5VAHdq1qeJppNSk=/methods/<init>(Landroid/location/Location;Ljava/lang/String;)V");
                this.setLineNumber(340);
            }
        }

        @ConceptInstance public static class CreateCotMessageToCotByter extends MethodInvocationDataflowEdge {
            public CreateCotMessageToCotByter() {
                this.setDataTypeCommunicated(CotMessage.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(CotMessage.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new CreateCotMessage());
                this.setConsumer(new ProcessByCotByter());
            }
        }


        @ConceptInstance public static class ProcessByCotByter extends MethodInvocationDataflowNode {
            public ProcessByCotByter() {
                this.setJavaClassName("com/bbn/ataklite/pipes/CotByter");
                this.setJavaMethodName("process");
                this.setJavaMethodPointer("jFRqNEnAgyPfFq6cwBUtVywcZcSAlJ+4pZ7JcRClXoQ=/methods/process(Lcom/bbn/ataklite/CoTMessage;)[B");
                this.setLineNumber(38);
            }
        }

        @ConceptInstance public static class ProcessToDispatch extends MethodInvocationDataflowEdge {
            public ProcessToDispatch() {
                this.setDataTypeCommunicated(BinaryData.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(CotMessage.class);
                DataflowAnalysisFrame frame2 = new DataflowAnalysisFrame();
                frame2.setAnalysisFrameChild(frame1);
                frame2.setAnalysisFrameDataType(BinaryData.class);
                this.setDataflowAnalysisFrame(frame2);
                this.setProducer(new ProcessByCotByter());
                this.setConsumer(new DispatchMessage());
            }
        }

        @ConceptInstance public static class DispatchMessage extends InterMethodDataflowNode {
            public DispatchMessage() {
                this.setJavaClassName("java/nio/channels/SocketChannel");
                this.setJavaMethodName("write");
                this.setJavaMethodPointer("unableToGenerateMethodPointer");
                this.setLineNumber(30);
                Analysis.Atak.AtakPhone atakPhone = new Analysis.Atak.AtakPhone();
                this.setResourceTemplate(atakPhone);
            }
        }

        @ConceptInstance public static class DispatchToReceive extends InterMethodDataflowEdge {
            public DispatchToReceive() {
                this.setDataTypeCommunicated(BinaryData.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(CotMessage.class);
                DataflowAnalysisFrame frame2 = new DataflowAnalysisFrame();
                frame2.setAnalysisFrameChild(frame1);
                frame2.setAnalysisFrameDataType(BinaryData.class);
                this.setDataflowAnalysisFrame(frame2);
                this.setProducer(new DispatchMessage());
                this.setConsumer(new ReceiveMessage());
            }
        }

        @ConceptInstance public static class ReceiveMessage extends InterMethodDataflowNode {
            public ReceiveMessage() {
                this.setJavaClassName("java/io/InputStream");
                this.setJavaMethodName("read");
                this.setJavaMethodPointer("unableToGenerateMethodPointer");
                this.setLineNumber(154);
                Analysis.Marti.MartiServer martiServer = new Analysis.Marti.MartiServer();
                this.setResourceTemplate(martiServer);
            }
        }

        @ConceptInstance public static class GetInputToReadMessage extends MethodInvocationDataflowEdge {
            public GetInputToReadMessage() {
                this.setDataTypeCommunicated(InputStream.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(InputStream.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new GetInputStream());
                this.setConsumer(new ReceiveMessage());
            }
        }

        @ConceptInstance public static class GetInputStream extends MethodInvocationDataflowNode {
            public GetInputStream() {
                this.setJavaMethodName("getInputStream");
                this.setJavaClassName("java/net/Socket");
                this.setJavaMethodPointer("unableToGenerateMethodPointer");
                this.setLineNumber(151);
            }
        }

        @ConceptInstance public static class CreateSocketToGetInputStream extends MethodInvocationDataflowEdge {
            public CreateSocketToGetInputStream() {
                this.setDataTypeCommunicated(InputStream.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(InputStream.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new CreateSocket());
                this.setConsumer(new GetInputStream());
            }
        }
        
        @ConceptInstance
        public static class CreateSocket extends MethodInvocationDataflowNode {
            public CreateSocket() {
                this.setJavaMethodName("<init>");
                this.setJavaClassName("java/net/Socket");
                this.setJavaMethodPointer("unableToGenerateMethodPointer");
                this.setLineNumber(60);
            }
        }

        @ConceptInstance public static class ReceiveToByteBuffer extends MethodInvocationDataflowEdge {
            public ReceiveToByteBuffer() {
                this.setDataTypeCommunicated(BinaryData.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(CotMessage.class);
                DataflowAnalysisFrame frame2 = new DataflowAnalysisFrame();
                frame2.setAnalysisFrameChild(frame1);
                frame2.setAnalysisFrameDataType(BinaryData.class);
                this.setDataflowAnalysisFrame(frame2);
                this.setProducer(new ReceiveMessage());
                this.setConsumer(new PassMessageToByteBuffer());
            }
        }

        @ConceptInstance public static class PassMessageToByteBuffer extends MethodInvocationDataflowNode {
            public PassMessageToByteBuffer() {
                this.setJavaClassName("mil/darpa/immortals/core/synthesis/AbstractMultisuccessorConsumingPipe");
                this.setJavaMethodName("consume");
                this.setJavaMethodPointer("/VjdXmX9wvvpde4v3KvK1RpLEb7rh8qs2Br78Gf5cTg=/methods/consume(Ljava/lang/Object;)V");
                this.setLineNumber(157);
            }
        }

        @ConceptInstance public static class ByteBufferToProcess extends MethodInvocationDataflowEdge {
            public ByteBufferToProcess() {
                this.setDataTypeCommunicated(BinaryData.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(CotMessage.class);
                DataflowAnalysisFrame frame2 = new DataflowAnalysisFrame();
                frame2.setAnalysisFrameChild(frame1);
                frame2.setAnalysisFrameDataType(BinaryData.class);
                this.setDataflowAnalysisFrame(frame2);
                this.setProducer(new PassMessageToByteBuffer());
                this.setConsumer(new ProcessByteBuffer());
            }
        }

        @ConceptInstance public static class ProcessByteBuffer extends MethodInvocationDataflowNode {
            public ProcessByteBuffer() {
                this.setJavaClassName("com/bbn/marti/immortals/pipes/CotByteBufferPipe");
                this.setJavaMethodName("process");
                this.setJavaMethodPointer("2yBtUZohoZzDg50IdKkexAjiumwzbIyrdhKk+G+hg1c=/methods/process(Ljava/lang/Object;)Ljava/lang/Object;");
                this.setLineNumber(57);
            }
        }

        @ConceptInstance public static class ProcessToMessageConversion extends MethodInvocationDataflowEdge {
            public ProcessToMessageConversion() {
                this.setDataTypeCommunicated(BinaryData.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(CotMessage.class);
                DataflowAnalysisFrame frame2 = new DataflowAnalysisFrame();
                frame2.setAnalysisFrameChild(frame1);
                frame2.setAnalysisFrameDataType(BinaryData.class);
                this.setDataflowAnalysisFrame(frame2);
                this.setProducer(new ProcessByteBuffer());
                this.setConsumer(new ConvertToCotMessage());
            }
        }

        @ConceptInstance public static class ConvertToCotMessage extends MethodInvocationDataflowNode {
            public ConvertToCotMessage() {
                this.setJavaClassName("mil/darpa/immortals/datatypes/cot/dom4j/StreamingCotProcessor");
                this.setJavaMethodName("add");
                this.setJavaMethodPointer("1iuV2HNQvhout5vLQ4oIhHcL+ISu5ovlvd+W48yoQk4=/methods/add(Ljava/lang/String;Ljava/lang/Object;)Ljava/util/List;");
                this.setLineNumber(38);
            }
        }

        @ConceptInstance public static class CreateSocketToGetOutputStream extends MethodInvocationDataflowEdge {
            public CreateSocketToGetOutputStream() {
                this.setDataTypeCommunicated(OutputStream.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(OutputStream.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new CreateSocket());
                this.setConsumer(new GetOutputStream());
            }
        }

        @ConceptInstance public static class GetOutputStream extends MethodInvocationDataflowNode {
            public GetOutputStream() {
                this.setJavaMethodName("getOutputStream");
                this.setJavaClassName("java/net/Socket");
                this.setJavaMethodPointer("unableToGenerateMethodPointer");
                this.setLineNumber(84);
            }
        }

        @ConceptInstance public static class GetOutputToCreateDataOutputStream extends MethodInvocationDataflowEdge {
            public GetOutputToCreateDataOutputStream() {
                this.setDataTypeCommunicated(OutputStream.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(OutputStream.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new GetOutputStream());
                this.setConsumer(new CreateDataOutputStream());
            }
        }

        @ConceptInstance public static class CreateDataOutputStream extends MethodInvocationDataflowNode {
            public CreateDataOutputStream() {
                this.setJavaMethodName("<init>");
                this.setJavaClassName("java/io/DataOutputStream");
                this.setJavaMethodPointer("unableToGenerateMethodPointer");
                this.setLineNumber(84);
            }
        }

        @ConceptInstance public static class CreateDataOutputStreamToWriteData extends MethodInvocationDataflowEdge {
            public CreateDataOutputStreamToWriteData() {
                this.setDataTypeCommunicated(OutputStream.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(OutputStream.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new CreateDataOutputStream());
                this.setConsumer(new WriteData());
            }
        }

        @ConceptInstance public static class WriteData extends InterMethodDataflowNode {
            public WriteData() {
                this.setJavaMethodName("write");
                this.setJavaClassName("java/io/DataOutputStream");
                this.setJavaMethodPointer("unableToGenerateMethodPointer");
                this.setLineNumber(85);
                Analysis.Marti.MartiServer martiServer = new Analysis.Marti.MartiServer();
                this.setResourceTemplate(martiServer);
            }
        }

        @ConceptInstance public static class RetrieveCoTEvent extends MethodInvocationDataflowNode {
            public RetrieveCoTEvent() {
                this.setJavaMethodName("consume");
                this.setJavaClassName("com/bbn/marti/immortals/pipes/CotEventContainerBytesExtractionPipe");
                this.setJavaMethodPointer("/VjdXmX9wvvpde4v3KvK1RpLEb7rh8qs2Br78Gf5cTg=/methods/consume(Ljava/lang/Object;)V");
                this.setLineNumber(71);
            }
        }

        @ConceptInstance public static class RetrieveCoTEventToSendToNetwork extends MethodInvocationDataflowEdge {
            public RetrieveCoTEventToSendToNetwork() {
                this.setDataTypeCommunicated(BinaryData.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(BinaryData.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new RetrieveCoTEvent());
                this.setConsumer(new SendToNetworkPipe());
            }
        }

        @ConceptInstance public static class SendToNetworkPipe extends MethodInvocationDataflowNode {
            public SendToNetworkPipe() {
                this.setJavaMethodName("consume");
                this.setJavaClassName("com/bbn/marti/immortals/net/tcp/Transport$SendToNetworkPipe");
                this.setJavaMethodPointer("/VjdXmX9wvvpde4v3KvK1RpLEb7rh8qs2Br78Gf5cTg=/methods/consume(Ljava/lang/Object;)V");
                this.setLineNumber(71);
            }
        }

        @ConceptInstance public static class SendToNetworkPipeToHandleData extends MethodInvocationDataflowEdge {
            public SendToNetworkPipeToHandleData() {
                this.setDataTypeCommunicated(BinaryData.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(BinaryData.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new SendToNetworkPipe());
                this.setConsumer(new HandleData());
            }
        }

        @ConceptInstance public static class HandleData extends MethodInvocationDataflowNode {
            public HandleData() {
                this.setJavaMethodName("handleData");
                this.setJavaClassName("com/bbn/marti/immortals/net/tcp/TcpTransport");
                this.setJavaMethodPointer("TrSeCkTeGU2+hdurzhBrlKOaSG6MdTZOU8RWpYUrbqo=/methods/handleData([B)V");
                this.setLineNumber(118);
            }
        }

        @ConceptInstance public static class HandleDataToWriteToClient extends MethodInvocationDataflowEdge {
            public HandleDataToWriteToClient() {
                this.setDataTypeCommunicated(BinaryData.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(BinaryData.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new HandleData());
                this.setConsumer(new WriteData());
            }
        }

        @ConceptInstance public static class WriteToClientToReceiveFromServer extends InterMethodDataflowEdge {
            public WriteToClientToReceiveFromServer() {
                this.setDataTypeCommunicated(BinaryData.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(CotMessage.class);
                DataflowAnalysisFrame frame2 = new DataflowAnalysisFrame();
                frame2.setAnalysisFrameChild(frame1);
                frame2.setAnalysisFrameDataType(BinaryData.class);
                this.setDataflowAnalysisFrame(frame2);
                this.setProducer(new WriteData());
                this.setConsumer(new ReceiveFromServer());
            }
        }

        @ConceptInstance public static class ReceiveFromServer extends InterMethodDataflowNode {
            public ReceiveFromServer() {
                this.setJavaMethodName("read");
                this.setJavaClassName("java/nio/channels/SocketChannel");
                this.setJavaMethodPointer("unableToGenerateMethodPointer");
                this.setLineNumber(23);
                Analysis.Atak.AtakPhone atakPhone = new Analysis.Atak.AtakPhone();
                this.setResourceTemplate(atakPhone);
            }
        }

        @ConceptInstance public static class CreateInputStreamToHandleRead extends MethodInvocationDataflowEdge {
            public CreateInputStreamToHandleRead() {
                this.setDataTypeCommunicated(InputStream.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(InputStream.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new CreateAtakStreams());
                this.setConsumer(new HandleRead());
            }
        }

        @ConceptInstance public static class HandleRead extends MethodInvocationDataflowNode {
            public HandleRead() {
                this.setJavaMethodName("handleEvent");
                this.setJavaClassName("com/bbn/ataklite/net/ReadHandler");
                this.setJavaMethodPointer("saxtKqX/Tc28fV9qfluPVPD7e0glqYbFa/4xv6Z1ZO0=/methods/handleEvent(Lcom/bbn/ataklite/net/SelectionEvent;)V");
                this.setLineNumber(114);
            }
        }
        
        @ConceptInstance public static class HandleReadToReceiveFromServer extends MethodInvocationDataflowEdge {
            public HandleReadToReceiveFromServer() {
                this.setDataTypeCommunicated(InputStream.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(InputStream.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new HandleRead());
                this.setConsumer(new ReceiveFromServer());
            }
        }

        @ConceptInstance public static class ReceiveFromServerToOfferData extends MethodInvocationDataflowEdge {
            public ReceiveFromServerToOfferData() {
                this.setDataTypeCommunicated(BinaryData.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(BinaryData.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new ReceiveFromServer());
                this.setConsumer(new OfferData());
            }
        }

        @ConceptInstance public static class OfferData extends MethodInvocationDataflowNode {
            public OfferData() {
                this.setJavaMethodName("offer");
                this.setJavaClassName("java/util/concurrent/LinkedBlockingQueue");
                this.setJavaMethodPointer("unableToGenerateMethodPointer");
                this.setLineNumber(33);
            }
        }

        @ConceptInstance public static class OfferDataToDequeue extends MethodInvocationDataflowEdge {
            public OfferDataToDequeue() {
                this.setDataTypeCommunicated(BinaryData.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(BinaryData.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new OfferData());
                this.setConsumer(new DequeueData());
            }
        }

        @ConceptInstance public static class DequeueData extends MethodInvocationDataflowNode {
            public DequeueData() {
                this.setJavaMethodName("produce");
                this.setJavaClassName("com/bbn/ataklite/net/Dispatcher");
                this.setJavaMethodPointer("2Qh/VHMYP3GTPNXpNLX4ZNG/YIAIsru1za5ZDhYdcNw=/methods/produce()Ljava/lang/Object;");
                this.setLineNumber(39);
            }
        }

        @ConceptInstance public static class DequeueToDecode extends MethodInvocationDataflowEdge {
            public DequeueToDecode() {
                this.setDataTypeCommunicated(BinaryData.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(BinaryData.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new DequeueData());
                this.setConsumer(new DecodeData());
            }
        }

        @ConceptInstance public static class DecodeData extends MethodInvocationDataflowNode {
            public DecodeData() {
                this.setJavaMethodName("<init>");
                this.setJavaClassName("java/lang/String");
                this.setJavaMethodPointer("unableToGenerateMethodPointer");
                this.setLineNumber(40);
            }
        }

        @ConceptInstance public static class DecodeToCreateCoordinate extends MethodInvocationDataflowEdge {
            public DecodeToCreateCoordinate() {
                this.setDataTypeCommunicated(CotMessage.class);
                DataflowAnalysisFrame frame1 = new DataflowAnalysisFrame();
                frame1.setAnalysisFrameChild(null);
                frame1.setAnalysisFrameDataType(CotMessage.class);
                this.setDataflowAnalysisFrame(frame1);
                this.setProducer(new DecodeData());
                this.setConsumer(new OfferData());
            }
        }

        @ConceptInstance public static class CreateCoordinate extends MethodInvocationDataflowNode {
            public CreateCoordinate() {
                this.setJavaMethodName("<init>");
                this.setJavaClassName("mil/darpa/immortals/datatypes/Coordinates");
                this.setJavaMethodPointer("QH/F+KSG+4XdmBHRR0Q5xt+jvs3RBh8QUy84cv682nU=/methods/<init>(DDLjava/lang/Double;Ljava/lang/Float;JLjava/lang/String;)V");
                this.setLineNumber(212);
            }
        }
        
    }
