package com.securboration.immortals.ontology.resources.logical;

public class Column extends LogicalType {
    
    private String name;
    
    private Record[] records;

    public Record[] getRecords() {
        return records;
    }

    public void setRecords(Record[] records) {
        this.records = records;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
