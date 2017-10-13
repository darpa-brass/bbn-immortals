package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.property.Property;


/**
 * A directed dataflow edge connecting a producer to a consumer of an abstract
 * data type
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A directed dataflow edge connecting a producer to a consumer of an" +
    " abstract data type  @author jstaples ")
public class DataflowEdge {
    
    /**
     * The type of data being communicated
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The type of data being communicated")
    private Class<? extends DataType> dataTypeCommunicated;
    
    /**
     * The analysis frame of the data communicated
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
            "The analysis frame of the data communicated")
    private DataflowAnalysisFrame dataflowAnalysisFrame;
    
    /**
     * The producer of the data
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The producer of the data")
    private DataflowNode producer;
    
    /**
     * A template for the communication channel.  E.g., a NetworkConnection
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A template for the communication channel.  E.g., a" +
        " NetworkConnection")
    private Resource communicationChannelTemplate;
    
    /**
     * The consumer of the data
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The consumer of the data")
    private DataflowNode consumer;

    /**
     * Properties of the data flow edge
     */
    private Property[] edgeProperties;
    
    /**
     * A human-readable description of this object
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A human-readable description of this object")
    private String humanReadableDescription;
    
    
    public Class<? extends DataType> getDataTypeCommunicated() {
        return dataTypeCommunicated;
    }
    
    public void setDataTypeCommunicated(
            Class<? extends DataType> dataTypeCommunicated) {
        this.dataTypeCommunicated = dataTypeCommunicated;
    }
    
    public Resource getCommunicationChannelTemplate() {
        return communicationChannelTemplate;
    }
    
    public void setCommunicationChannelTemplate(
            Resource communicationChannelTemplate) {
        this.communicationChannelTemplate = communicationChannelTemplate;
    }
    
    public DataflowNode getProducer() {
        return producer;
    }
    
    public void setProducer(DataflowNode producer) {
        this.producer = producer;
    }
    
    public DataflowNode getConsumer() {
        return consumer;
    }
    
    public void setConsumer(DataflowNode consumer) {
        this.consumer = consumer;
    }

    
    public String getHumanReadableDescription() {
        return humanReadableDescription;
    }

    
    public void setHumanReadableDescription(String humanReadableDescription) {
        this.humanReadableDescription = humanReadableDescription;
    }

	public DataflowAnalysisFrame getDataflowAnalysisFrame() {
		return dataflowAnalysisFrame;
	}

	public void setDataflowAnalysisFrame(DataflowAnalysisFrame dataflowAnalysisFrame) {
		this.dataflowAnalysisFrame = dataflowAnalysisFrame;
	}

    public Property[] getEdgeProperties() {
        return edgeProperties;
    }

    public void setEdgeProperties(Property[] edgeProperties) {
        this.edgeProperties = edgeProperties;
    }
}
