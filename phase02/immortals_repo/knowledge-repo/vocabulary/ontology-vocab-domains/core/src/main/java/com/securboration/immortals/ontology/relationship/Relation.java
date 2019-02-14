package com.securboration.immortals.ontology.relationship;

import com.securboration.immortals.ontology.resources.logical.Table;

public class Relation {
    
    private RelationType relationType;
    
    private Table[] from;
    
    private Table[] to;
    
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    public Table[] getFrom() {
        return from;
    }

    public void setFrom(Table[] from) {
        this.from = from;
    }

    public Table[] getTo() {
        return to;
    }

    public void setTo(Table[] to) {
        this.to = to;
    }
}
