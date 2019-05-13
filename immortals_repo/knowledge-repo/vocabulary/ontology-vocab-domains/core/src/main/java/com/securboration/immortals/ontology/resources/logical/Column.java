package com.securboration.immortals.ontology.resources.logical;

public class Column extends LogicalType {

    private Record[] records;

    public Record[] getRecords() {
        return records;
    }

    public void setRecords(Record[] records) {
        this.records = records;
    }
}
