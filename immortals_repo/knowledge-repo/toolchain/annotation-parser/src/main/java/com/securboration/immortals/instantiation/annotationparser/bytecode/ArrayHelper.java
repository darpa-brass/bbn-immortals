package com.securboration.immortals.instantiation.annotationparser.bytecode;

import java.lang.reflect.Array;

public class ArrayHelper {
    
    public static <T> T[] append(Class<T> t, T[] first, T[] second){
        
        if(second == null || second.length == 0){
            return first;
        }
        
        T[] newArray = (T[])Array.newInstance(t, first.length + second.length);
        
        System.arraycopy(first, 0, newArray, 0, first.length);
        System.arraycopy(second, 0, newArray, first.length, second.length);
        
        return newArray;
    }
    
    public static <T> T[] append(Class<T> t, T[] first, T second){
        T[] newArray = (T[])Array.newInstance(t, first.length + 1);
        
        System.arraycopy(first, 0, newArray, 0, first.length);
        newArray[first.length]=second;
        
        return newArray;
    }
    
    

}
