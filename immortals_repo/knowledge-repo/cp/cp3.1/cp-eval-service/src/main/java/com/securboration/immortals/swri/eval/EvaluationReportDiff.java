package com.securboration.immortals.swri.eval;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.securboration.immortals.service.eos.api.types.EvaluationMetricAdaptationImpact;
import com.securboration.immortals.service.eos.api.types.EvaluationMetricCategory;

public class EvaluationReportDiff {
    
    private static String getAdjective(
            final String beforeVal, 
            final String afterVal
            ){
        if(beforeVal == null || afterVal == null){
            return null;
        }
        
        if(beforeVal.equals(afterVal)){
            return "unchanged";
        }
        
        if(isNumber(beforeVal) && isNumber(afterVal)){
            final double start = Double.parseDouble(beforeVal);
            final double end = Double.parseDouble(afterVal);
            
            final String percentage;
            if(start > 0){
                percentage = String.format(" by %1.2f%%",Math.abs(100 * (end - start) / start));
            } else {
                percentage = "";
            }
            
            if(end == start){
                return "unchanged";
            } else if(end > start){
                return "increased" + percentage;
            } else if(end < start){
                return "decreased" + percentage;
            } else {
                throw new RuntimeException("unhandled case");
            }
        }
        
        if(!beforeVal.equals(afterVal)){
            return "changed";
        }
        
        return null;
    }
    
    private static boolean isNumber(String s){
        return NumberUtils.isParsable(s);
    }
    
    public static EvaluationMetricCategory diff(
            final byte[] baselineReport, 
            final byte[] postAdaptationReport
            ) throws IOException{
        final Map<String,String> baseline = parseReport(baselineReport);
        final Map<String,String> postAdaptation = parseReport(postAdaptationReport);
        
        final Set<String> keys = new LinkedHashSet<>();
        keys.addAll(baseline.keySet());
        keys.addAll(postAdaptation.keySet());
        
        final StringBuilder sb = new StringBuilder();
        
//        for(String key:keys){
//            sb.append(
//                String.format(
//                    "%s\t%s\t%s\n", 
//                    key, 
//                    baseline.get(key),
//                    postAdaptation.get(key)
//                    )
//                );
//        }
        
        EvaluationMetricCategory category = new EvaluationMetricCategory();
        category.setCategoryDesc("impacts of adaptation");
        
        for(String key:keys){
            if(key.startsWith("starting test run with input dir")){
                continue;
            }
            
            if(key.startsWith("server URI")){
                continue;
            }
            
            final String before = baseline.get(key);
            final String after = postAdaptation.get(key);
            final String adjective = getAdjective(before,after);
            
            EvaluationMetricAdaptationImpact metric = new EvaluationMetricAdaptationImpact();
            metric.setMetricType(key);
            metric.setMetricDesc("a comparison of \"" + key + "\" before and after adaptation");
            metric.setMetricValueBefore(before);
            metric.setMetricValueAfter(after);
            metric.setImpactOfAdaptation(String.format("%s after adaptation", adjective));
            category.getMetricsForCategory().add(metric);
            
            sb.append(
                String.format(
                    "%s was %s by adaptation (%s --> %s)\n", 
                    key, 
                    getAdjective(before,after),
                    baseline.get(key),
                    postAdaptation.get(key)
                    )
                );
        }
        
        return category;
    }
    
    private static Map<String,String> parseReport(byte[] input) throws IOException{
        final Map<String,String> kvs = new LinkedHashMap<>();
        
        for(String line:IOUtils.readLines(new ByteArrayInputStream(input), StandardCharsets.UTF_8)){
            line = line.trim();
            
            if(line.isEmpty()){
                continue;
            }
            
            if(!line.contains("\t")){
                continue;
            }
            
            String[] parts = line.split("\t");
            
            final String k = parts[0].trim();
            final String v = parts[1].trim();
            
            if(k.startsWith("test")){
                continue;
            }
            
            if(kvs.containsKey(k)){
                System.err.printf("report contains multiple keys named \"%s\"\n", k);
            }
            
            kvs.put(k, v);
        }
        
        return kvs;
    }

}
