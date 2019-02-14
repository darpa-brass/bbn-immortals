package com.securboration.immortals.ontology.resources.logical;

import com.securboration.immortals.ontology.core.HumanReadable;
import com.securboration.immortals.ontology.relationship.Relation;

public class DBSchema extends Schema implements HumanReadable {
    
    private String name;
    
    private String version;
    
    private Table[] tables;
    
    private Relation[] relations;
    
    private String humanReadableDesc;
    
    public String getHumanReadableDesc() {
        return humanReadableDesc;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Relation[] getRelations() {
        return relations;
    }

    public void setRelations(Relation[] relations) {
        this.relations = relations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Table[] getTables() {
        return tables;
    }

    public void setTables(Table[] tables) {
        this.tables = tables;
    }

    public void setHumanReadableDesc(String humanReadableDesc) {
        this.humanReadableDesc = humanReadableDesc;
    }
}
