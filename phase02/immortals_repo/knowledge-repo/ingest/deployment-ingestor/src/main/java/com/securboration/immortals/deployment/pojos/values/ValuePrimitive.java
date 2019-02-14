package com.securboration.immortals.deployment.pojos.values;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A primitive value.  E.g., String, int
 * 
 * @author jstaples
 *
 */
public class ValuePrimitive<T> extends Value{
    
    /**
     * The Class<T> type of the field.  E.g., java.lang.String.class, int.class
     */
    protected Class<?> type;
    
    /**
     * The field's value
     */
    protected T value;

    protected ValuePrimitive() {}

    protected ValuePrimitive(T value)
    {
        this.value = value;
        this.type = value.getClass();
    }

    public Class<?> getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    private static final Set<Class<?>> primitiveTypes = 
            new HashSet<>(Arrays.asList(
                    int.class,Integer.class,
                    float.class,Float.class,
                    long.class,Long.class,
                    double.class,Double.class,
                    byte.class,Byte.class,
                    char.class,Character.class,
                    boolean.class,Boolean.class,
                    short.class,Short.class,
                    String.class
                    ));
    
    public static <T> ValuePrimitive<T> instantiatePrimitive(T o)
    {
        Class<?> c = o.getClass();
        
        if(!primitiveTypes.contains(c)){
            throw new RuntimeException(
                    "class " + c + " is not a primitive type");
        }
        
        return new ValuePrimitive<>(o);
    }

    @Override
    public Value copy() {
        return new ValuePrimitive<>(value);
    }
}
