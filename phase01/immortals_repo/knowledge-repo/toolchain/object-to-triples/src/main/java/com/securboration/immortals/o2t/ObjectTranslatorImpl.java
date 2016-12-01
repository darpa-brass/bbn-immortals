package com.securboration.immortals.o2t;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs the following translations:
 * 
 * converts byte[]   --> base64 string
 * converts char[]   --> string
 * 
 * converts List<>   --> array
 * converts Map<?,?> --> KeyValue[]
 * 
 * @author jstaples
 *
 */
public class ObjectTranslatorImpl implements ObjectTranslator{
    private Map<Object,Object> previouslyTranslated = new HashMap<>();

    @Override
    public Object translate(Object input) {
        if(previouslyTranslated.containsKey(input)){
            return previouslyTranslated.get(input);
        }
        
        Object translatedValue = translateInternal(input);
        
        previouslyTranslated.put(input, translatedValue);
        
        return translatedValue;
    }
    
    private Object translateInternal(Object input){
        if(input == null){
            return null;
        }
        
        if(input instanceof char[]){
            return new String((char[])input);
        }
        
        if(input instanceof byte[]){
            return new String(Base64.getEncoder().encode((byte[])input));
        }
        
        input = arrayify(input);

        return input;
    }
    
    private Object arrayify(
            Object input
            ){
        
        if(previouslyTranslated.containsKey(input)){
            return previouslyTranslated.get(input);
        }
        
        List<Object> resultArray = new ArrayList<>();
        
        if(input instanceof Collection){
            Collection<?> c = (Collection<?>)input;
            
            for(Object o:c){
                resultArray.add(arrayify(o));
            }
        } else if(input instanceof Map){
            Map<?,?> m = (Map<?,?>)input;
            
            for(Object key:m.keySet()){
                Object value = m.get(key);
                
                resultArray.add(
                        new KeyValue(
                                arrayify(key),
                                arrayify(value)));
            }
        } else {
            return input;
        }
        
        Object[] array = resultArray.toArray();
        
        previouslyTranslated.put(input, array);
        
        return array;
    }
    
    private static class KeyValue{
        Object key;
        Object value;
        
        KeyValue(Object k,Object v){
            this.key = k;
            this.value = v;
        }
    }
}
