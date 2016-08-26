package com.bbn.cot;

public class CotEvent extends CotEventContainer {

    private String event;

    private CotEvent(String event) {
        this.event = event;
    }

    public static CotEvent fromString(String event) {
        return new CotEvent(event);
    }

    @Override
    public boolean matchXPath(String xpath) {
        // TODO Auto-generated method stub
        return false;
    }

    public String toString() {
        return event;
    }

}
