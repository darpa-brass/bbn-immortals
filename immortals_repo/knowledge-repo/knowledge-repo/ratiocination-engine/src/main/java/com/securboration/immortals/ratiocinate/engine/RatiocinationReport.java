package com.securboration.immortals.ratiocinate.engine;


public class RatiocinationReport {
    
    private final StringBuilder sb = new StringBuilder();

    private int triplesAdded = -1;
    
    public void print(String format,Object...args){
        sb.append(String.format(format, args));
    }

    public String getReportText(){
        return sb.toString();
    }

    public void setTriplesAdded(int triplesAdded) {
        this.triplesAdded = triplesAdded;
    }

    public int getTriplesAdded() {
        return triplesAdded;
    }
}
