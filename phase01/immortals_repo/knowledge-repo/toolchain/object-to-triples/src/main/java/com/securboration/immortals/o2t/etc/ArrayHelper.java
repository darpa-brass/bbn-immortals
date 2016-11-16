package com.securboration.immortals.o2t.etc;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ArrayHelper {
    
    private static final Logger logger = 
            LogManager.getLogger(ArrayHelper.class);
    
    private static AtomicLong counter = new AtomicLong(0l);
    private static final long WARN_THRESHOLD = 1024*16l;
    
    public static Object append(
            Object array, 
            Class<?> arrayType, 
            Object element
            ){
        
        {
            final long count = counter.incrementAndGet();
            if((count > WARN_THRESHOLD) && (count % WARN_THRESHOLD == 0)){
                System.err.println(
                    "WARNING: Excessive use of O(N) appending algorithm, " +
                    "count = " + count
                    );
            }
        }
        
        final int arraySize;
        if(array == null){
            arraySize = 0;
        } else {
            arraySize = Array.getLength(array);
        }
        
        Object newArray = Array.newInstance(arrayType, arraySize+1);
        
        if(array != null){
            System.arraycopy(array, 0, newArray, 0, arraySize);
        }
        
        Array.set(newArray, arraySize, element);
        
        return newArray;
    }

}
