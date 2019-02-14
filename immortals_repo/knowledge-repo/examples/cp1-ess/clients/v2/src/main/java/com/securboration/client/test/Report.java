package com.securboration.client.test;

import java.util.LinkedHashMap;
import java.util.Map;

public class Report{
    private final Map<String,Object> report = new LinkedHashMap<>();
    
    public void put(String k) {
        put(k,null);
    }
    
    public void put(String k, Object v) {
        if(report.containsKey(k)) {
            throw new RuntimeException("attempted to overwrite key " + k);
        }
        
        report.put(k, v);
    }
    
    private static String stripNewlinesAndTabs(String in) {
        return in.replace("\n","").replace("\r", "").replace("\t", "  ");
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        for(final String key:report.keySet()) {
            final Object value = report.get(key);
            sb.append(String.format("%s\t%s\n", key, value == null ? "":stripNewlinesAndTabs(value.toString())));
        }
        
        return sb.toString();
    }
}
