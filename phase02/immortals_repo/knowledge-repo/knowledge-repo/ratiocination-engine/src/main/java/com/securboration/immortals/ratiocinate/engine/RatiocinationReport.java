package com.securboration.immortals.ratiocinate.engine;


public class RatiocinationReport {
    
    private final StringBuilder sb = new StringBuilder();
    
    public void print(String format,Object...args){
        sb.append(String.format(format, args));
    }

    public String getReportText(){
        return sb.toString();
    }
}
