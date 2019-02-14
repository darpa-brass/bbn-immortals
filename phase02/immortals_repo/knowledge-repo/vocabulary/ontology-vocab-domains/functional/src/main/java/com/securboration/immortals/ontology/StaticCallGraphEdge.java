package com.securboration.immortals.ontology;

public class StaticCallGraphEdge {

    private String callerHash;
    private String calledHash;
    private int order;

    private String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCallerHash() {
        return callerHash;
    }

    public String getCalledHash() {
        return calledHash;
    }

    public void setCalledHash(String calledHash) {
        this.calledHash = calledHash;
    }

    public void setCallerHash(String callerHash) {
        this.callerHash = callerHash;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
