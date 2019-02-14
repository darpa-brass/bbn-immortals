//package com.securboration.immortals.ontology.action;
//
//import com.securboration.immortals.ontology.constraint.PropertyConstraintType;
//import com.securboration.immortals.ontology.core.Resource;
//import com.securboration.immortals.ontology.functionality.FunctionalAspect;
//import com.securboration.immortals.ontology.functionality.Functionality;
//import com.securboration.immortals.ontology.functionality.datatype.DataType;
//import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;
//import com.securboration.immortals.ontology.property.Property;
//import com.securboration.immortals.ontology.property.impact.ImpactStatement;
//import com.securboration.immortals.ontology.resources.FileResource;
//import com.securboration.immortals.ontology.resources.NetworkResource;
//import com.securboration.immortals.ontology.resources.Platform;
//
///**
// * Aggregation of various indefinite concept specifications
// * 
// * These will be moved into their own files as they solidify
// * 
// * @author jstaples
// *
// */
//public class Monolithic {
//    
//    public class AbstractSoftwareAction{
//        
//        private ImpactStatement[] impactOfAction;
//        private String humanReadableDesc;
//        
//        public ImpactStatement[] getImpactOfAction() {
//            return impactOfAction;
//        }
//        
//        public void setImpactOfAction(ImpactStatement[] impactOfAction) {
//            this.impactOfAction = impactOfAction;
//        }
//        
//        public String getHumanReadableDesc() {
//            return humanReadableDesc;
//        }
//        
//        public void setHumanReadableDesc(String humanReadableDesc) {
//            this.humanReadableDesc = humanReadableDesc;
//        }
//        
//    }
//    
//    
//    
//    @GenerateAnnotation
//    public class DataflowAnalysis{
//        private String humanReadableDesc;
//        private DfuProvenance[] provenance;
//        private PropertyConstraintAssertion[] propertyConstraints;
//        
//        public String getHumanReadableDesc() {
//            return humanReadableDesc;
//        }
//        
//        public void setHumanReadableDesc(String humanReadableDesc) {
//            this.humanReadableDesc = humanReadableDesc;
//        }
//        
//        public DfuProvenance[] getProvenance() {
//            return provenance;
//        }
//        
//        public void setProvenance(DfuProvenance[] provenance) {
//            this.provenance = provenance;
//        }
//        
//        public PropertyConstraintAssertion[] getPropertyConstraints() {
//            return propertyConstraints;
//        }
//        
//        public void setPropertyConstraints(
//                PropertyConstraintAssertion[] propertyConstraints) {
//            this.propertyConstraints = propertyConstraints;
//        }
//    }
//    
//    public class DfuProvenance{
//        
//        private Class<?> classUsingFunctionality;
//        private Class<? extends Functionality> functionalityImplemented;
//        private Class<? extends FunctionalAspect> aspectOfFunctionality;
//        
//        public Class<?> getClassUsingFunctionality() {
//            return classUsingFunctionality;
//        }
//        
//        public void setClassUsingFunctionality(Class<?> classUsingFunctionality) {
//            this.classUsingFunctionality = classUsingFunctionality;
//        }
//        
//        public Class<? extends Functionality> getFunctionalityImplemented() {
//            return functionalityImplemented;
//        }
//        
//        public void setFunctionalityImplemented(
//                Class<? extends Functionality> functionalityImplemented) {
//            this.functionalityImplemented = functionalityImplemented;
//        }
//        
//        public Class<? extends FunctionalAspect> getAspectOfFunctionality() {
//            return aspectOfFunctionality;
//        }
//        
//        public void setAspectOfFunctionality(
//                Class<? extends FunctionalAspect> aspectOfFunctionality) {
//            this.aspectOfFunctionality = aspectOfFunctionality;
//        }
//    }
//    
//    public class PropertyConstraintAssertion{
//        private PropertyConstraintType constraint;
//        private Class<? extends Property> property;
//        
//        public PropertyConstraintType getConstraint() {
//            return constraint;
//        }
//        
//        public void setConstraint(PropertyConstraintType constraint) {
//            this.constraint = constraint;
//        }
//        
//        public Class<? extends Property> getProperty() {
//            return property;
//        }
//        
//        public void setProperty(Class<? extends Property> property) {
//            this.property = property;
//        }
//    }
//    
//    @GenerateAnnotation
//    public class TransformationAnalysis{
//        private Class<? extends DataType> dataType;
//        private TransformationStep[] transformationsApplied;
//        
//        public Class<? extends DataType> getDataType() {
//            return dataType;
//        }
//        
//        public void setDataType(Class<? extends DataType> dataType) {
//            this.dataType = dataType;
//        }
//        
//        public TransformationStep[] getTransformationsApplied() {
//            return transformationsApplied;
//        }
//        
//        public void setTransformationsApplied(
//                TransformationStep[] transformationsApplied) {
//            this.transformationsApplied = transformationsApplied;
//        }
//    }
//    
//    public class TransformationStep {
//        private int order;
//        private Transformation transformation;
//        
//        public int getOrder() {
//            return order;
//        }
//        
//        public void setOrder(int order) {
//            this.order = order;
//        }
//        
//        public Transformation getTransformation() {
//            return transformation;
//        }
//        
//        public void setTransformation(Transformation transformation) {
//            this.transformation = transformation;
//        }
//    }
//    
//    public class Transformation {
//        private Class<? extends FunctionalAspect> causeOfTransformation;
//        
//        private Property impactOfTransformation;
//
//        
//        public Class<? extends FunctionalAspect> getCauseOfTransformation() {
//            return causeOfTransformation;
//        }
//
//        
//        public void setCauseOfTransformation(
//                Class<? extends FunctionalAspect> causeOfTransformation) {
//            this.causeOfTransformation = causeOfTransformation;
//        }
//
//        
//        public Property getImpactOfTransformation() {
//            return impactOfTransformation;
//        }
//
//        
//        public void setImpactOfTransformation(Property impactOfTransformation) {
//            this.impactOfTransformation = impactOfTransformation;
//        }
//    }
//    
//    @GenerateAnnotation
//    public class DataExitpoint {
//        private Class<? extends DataType> dataType;
//        private MethodId dfuProducingData;
//        
//        public Class<? extends DataType> getDataType() {
//            return dataType;
//        }
//        
//        public void setDataType(Class<? extends DataType> dataType) {
//            this.dataType = dataType;
//        }
//        
//        public MethodId getDfuProducingData() {
//            return dfuProducingData;
//        }
//        
//        public void setDfuProducingData(MethodId dfuProducingData) {
//            this.dfuProducingData = dfuProducingData;
//        }
//    }
//    
//    @GenerateAnnotation
//    public class DataEntrypoint {
//        private Class<? extends DataType> dataType;
//        private MethodId dfuConsumingData;
//        
//        public Class<? extends DataType> getDataType() {
//            return dataType;
//        }
//        
//        public void setDataType(Class<? extends DataType> dataType) {
//            this.dataType = dataType;
//        }
//        
//        public MethodId getDfuConsumingData() {
//            return dfuConsumingData;
//        }
//        
//        public void setDfuConsumingData(MethodId dfuConsumingData) {
//            this.dfuConsumingData = dfuConsumingData;
//        }
//    }
//    
//    public class MethodId {
//        private Class<?> methodOwner;
//        private String methodName;
//        private Class<?>[] methodArgTypes;
//        
//        public Class<?> getMethodOwner() {
//            return methodOwner;
//        }
//        
//        public void setMethodOwner(Class<?> methodOwner) {
//            this.methodOwner = methodOwner;
//        }
//        
//        public String getMethodName() {
//            return methodName;
//        }
//        
//        public void setMethodName(String methodName) {
//            this.methodName = methodName;
//        }
//        
//        public Class<?>[] getMethodArgTypes() {
//            return methodArgTypes;
//        }
//        
//        public void setMethodArgTypes(Class<?>[] methodArgTypes) {
//            this.methodArgTypes = methodArgTypes;
//        }
//    }
//    
//    @GenerateAnnotation
//    public class AbstractCommunication extends AbstractSoftwareAction {
//        private Class<? extends DataType> dataTypeCommunicated;
//        private CommunicationChannel communicationChannel;
//        
//        private PropertyBinding[] assertionAboutDatatypeCommunicated;
//
//        
//        public Class<? extends DataType> getDataTypeCommunicated() {
//            return dataTypeCommunicated;
//        }
//
//        
//        public void setDataTypeCommunicated(
//                Class<? extends DataType> dataTypeCommunicated) {
//            this.dataTypeCommunicated = dataTypeCommunicated;
//        }
//
//        
//        public CommunicationChannel getCommunicationChannel() {
//            return communicationChannel;
//        }
//
//        
//        public void setCommunicationChannel(CommunicationChannel communicationChannel) {
//            this.communicationChannel = communicationChannel;
//        }
//
//        
//        public PropertyBinding[] getAssertionAboutDatatypeCommunicated() {
//            return assertionAboutDatatypeCommunicated;
//        }
//
//        
//        public void setAssertionAboutDatatypeCommunicated(
//                PropertyBinding[] assertionAboutDatatypeCommunicated) {
//            this.assertionAboutDatatypeCommunicated =
//                assertionAboutDatatypeCommunicated;
//        }
//    }
//    
//    public class CommunicationChannel{
//        private Class<? extends Resource> thisNodeType;
//        private DataflowDirection operationOnChannel;
//        private Class<? extends Resource> remoteNodeType;
//        
//        public Class<? extends Resource> getThisNodeType() {
//            return thisNodeType;
//        }
//        
//        public void setThisNodeType(Class<? extends Resource> thisNodeType) {
//            this.thisNodeType = thisNodeType;
//        }
//        
//        public DataflowDirection getOperationOnChannel() {
//            return operationOnChannel;
//        }
//        
//        public void setOperationOnChannel(DataflowDirection operationOnChannel) {
//            this.operationOnChannel = operationOnChannel;
//        }
//        
//        public Class<? extends Resource> getRemoteNodeType() {
//            return remoteNodeType;
//        }
//        
//        public void setRemoteNodeType(Class<? extends Resource> remoteNodeType) {
//            this.remoteNodeType = remoteNodeType;
//        }
//    }
//    
//    public class PersistentCommunicationChannel extends CommunicationChannel{
//        
//    }
//    
//    public class OneOffCommunicationChannel extends CommunicationChannel{
//        
//    }
//    
//    public enum DataflowDirection{
//        READS_FROM,
//        WRITES_TO,
//        READS_FROM_AND_WRITES_TO
//    }
//    
//    public class PropertyBinding{
//        private Class<? extends Property> property;
//
//        
//        public Class<? extends Property> getProperty() {
//            return property;
//        }
//
//        
//        public void setProperty(Class<? extends Property> property) {
//            this.property = property;
//        }
//    }
//    
//    public class AbstractProcess extends AbstractSoftwareAction {
//        
//    }
//    
//    public class StatefulProcess extends AbstractProcess{
//        
//    }
//    
//    public class StatelessProcess extends AbstractProcess{
//        
//    }
//    
//    public class FileIO extends AbstractCommunication{
//        private FileResource file;
//
//        
//        public FileResource getFile() {
//            return file;
//        }
//
//        
//        public void setFile(FileResource file) {
//            this.file = file;
//        }
//        
//    }
//    
//    public class FileRead extends FileIO{
//        
//    }
//    
//    public class FileWrite extends FileIO{
//        
//    }
//    
//    public class FileReadWrite extends FileIO{
//        
//    }
//    
//    public class NetworkIO extends AbstractCommunication{
//        private Class<? extends NetworkResource> communicationNetworkTemplate;
//        private Class<? extends Platform> thisNodeTemplate;
//        private Class<? extends Platform> remoteNodeTemplate;
//        
//        public Class<? extends NetworkResource> getCommunicationNetworkTemplate() {
//            return communicationNetworkTemplate;
//        }
//        
//        public void setCommunicationNetworkTemplate(
//                Class<? extends NetworkResource> communicationNetworkTemplate) {
//            this.communicationNetworkTemplate = communicationNetworkTemplate;
//        }
//        
//        public Class<? extends Platform> getThisNodeTemplate() {
//            return thisNodeTemplate;
//        }
//        
//        public void setThisNodeTemplate(Class<? extends Platform> thisNodeTemplate) {
//            this.thisNodeTemplate = thisNodeTemplate;
//        }
//        
//        public Class<? extends Platform> getRemoteNodeTemplate() {
//            return remoteNodeTemplate;
//        }
//        
//        public void setRemoteNodeTemplate(
//                Class<? extends Platform> remoteNodeTemplate) {
//            this.remoteNodeTemplate = remoteNodeTemplate;
//        }
//    }
//    
//    public class NetworkSend extends NetworkIO{
//    }
//    
//    public class NetworkReceive extends NetworkIO{
//    }
//
//}
