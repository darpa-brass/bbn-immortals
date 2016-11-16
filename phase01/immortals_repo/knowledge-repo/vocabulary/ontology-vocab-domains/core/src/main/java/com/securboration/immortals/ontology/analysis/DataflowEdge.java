package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "A directed dataflow edge connecting a producer to a consumer of " +
        "an abstract data type"
        )
    )
public class DataflowEdge {
    
    private Class<? extends DataType> dataTypeCommunicated;
    
    private DataflowNode producer;
    private Resource communicationChannelTemplate;
    private DataflowNode consumer;
    
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
    
}
