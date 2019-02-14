package com.securboration.immortals.ontology.resources.logical;

public class Record<T> {
    
    private RecordType recordType;
    
    private T record;

    public RecordType getRecordType() {
        return recordType;
    }

    public void setRecordType(RecordType recordType) {
        this.recordType = recordType;
    }

    public T getRecord() {
        return record;
    }

    public void setRecord(T record) {
        this.record = record;
    }
}
