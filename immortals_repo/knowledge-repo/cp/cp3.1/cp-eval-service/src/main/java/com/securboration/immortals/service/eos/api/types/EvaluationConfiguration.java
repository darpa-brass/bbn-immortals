package com.securboration.immortals.service.eos.api.types;

import java.util.ArrayList;
import java.util.List;

/**
 * This type describes the configuration for a single CP Evaluation run
 * 
 * @author jstaples
 *
 */
public class EvaluationConfiguration extends EosType {
    
    /**
     * The definition of the XML schema used by the client
     */
    private SchemaDefinition clientSchemaDefinition;
    
    /**
     * The definition of the XML schema used by the server. Evolutionary
     * pressure arises when this is different from
     * {@link EvaluationConfiguration#clientSchemaDefinition}.
     */
    private SchemaDefinition serverSchemaDefinition;
    
    /**
     * The definition of the XML schema used by the datasource. Evolutionary
     * pressure arises when this is different from
     * {@link EvaluationConfiguration#clientSchemaDefinition}.
     */
    private SchemaDefinition datasourceSchemaDefinition;
    
    /**
     * A list of XML documents conformant with
     * {@link #datasourceSchemaDefinition}
     */
    private final List<Document> datasourceXmls = new ArrayList<>();
    
    
    /**
     * A zip archive containing data that is typically derived during adaptation
     * but is instead provided explicitly. The presence of cheat data may result
     * in skipping certain costly or error-prone analysis steps (e.g., dynamic
     * analysis)
     */
    private byte[] cheatZip;
    
    
    public EvaluationConfiguration(){
        
    }
    
    public EvaluationConfiguration(String s){
        super(s);
    }

    
    public SchemaDefinition getClientSchemaDefinition() {
        return clientSchemaDefinition;
    }

    
    public void setClientSchemaDefinition(SchemaDefinition clientSchemaDefinition) {
        this.clientSchemaDefinition = clientSchemaDefinition;
    }

    
    public SchemaDefinition getServerSchemaDefinition() {
        return serverSchemaDefinition;
    }

    
    public void setServerSchemaDefinition(SchemaDefinition serverSchemaDefinition) {
        this.serverSchemaDefinition = serverSchemaDefinition;
    }

    
    public SchemaDefinition getDatasourceSchemaDefinition() {
        return datasourceSchemaDefinition;
    }

    
    public void setDatasourceSchemaDefinition(
            SchemaDefinition datasourceSchemaDefinition) {
        this.datasourceSchemaDefinition = datasourceSchemaDefinition;
    }

    
    public List<Document> getDatasourceXmls() {
        return datasourceXmls;
    }

    
    public byte[] getCheatZip() {
        return cheatZip;
    }

    
    public void setCheatZip(byte[] cheatZip) {
        this.cheatZip = cheatZip;
    }

    
    
    
    

}
