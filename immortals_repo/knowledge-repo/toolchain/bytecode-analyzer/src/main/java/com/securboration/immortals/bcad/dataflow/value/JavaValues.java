package com.securboration.immortals.bcad.dataflow.value;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

public class JavaValues {
    
    //map of type name to class name
    //TODO: this should be global
    private final Map<String,JavaValueClass> classes = new HashMap<>();
    
    private JavaValueClass acquireClass(final String internalName){
        JavaValueClass c = classes.get(internalName);
        
        if(c == null){
            c = new JavaValueClass(internalName);
            classes.put(internalName, c);
        }
        
        return c;
    }
    
    private JavaValue createNewValue(Type t){
        
        JavaValue v = null;
        
        if(t.getSort() == Type.OBJECT){
            //create new object
            v = new JavaValueObject(t.getDescriptor());
        } else if(t.getSort() == Type.ARRAY){
            //create new array
            v = new JavaValueArray(t.getDescriptor());
        } else {
            //create new primitive
            v = new JavaValuePrimitive(t.getDescriptor());
        }
        
//        objects.put(v.getUuid(), v);
        
        return v;
    }
    
    private JavaValue acquireFieldValue(
            final String fieldName,
            final Type fieldType,
            Map<String,JavaValue> fields
            ){
        
        JavaValue field = fields.get(fieldName);
        
        if(field == null){
            field = createNewValue(fieldType);
            fields.put(fieldName, field);
        }
        
        return field;
    }
    
    public JavaValue getField(
            final JavaValueObject instance, 
            final String fieldName,
            final Type type
            ){
        if(instance.getFields().containsKey(fieldName)){
            return instance.getFields().get(fieldName);
        }
        
        //if the field doesn't exist, create a new placeholder value 
        JavaValue v = newValue(type);
        setField(instance,fieldName,v);
        return v;
    }
    
    public void setField(
            final JavaValueObject instance,
            final String fieldName,
            final JavaValue value
            ){
        instance.getFields().put(fieldName, value);
    }
    
    public JavaValue getStatic(
            final String className, 
            final String fieldName,
            final Type type
            ){
        JavaValueClass c = acquireClass(className);
        
        Map<String,JavaValue> fields = c.getStaticFields();
        
        return acquireFieldValue(fieldName,type,fields);
    }
    
    public void putStatic(
            final String className,
            final String fieldName,
            final JavaValue value
            ){
        acquireClass(className).getStaticFields().put(fieldName, value);
    }
    
    public JavaValue newValue(Type t){
        return createNewValue(t);
    }
    
    public JavaValuePrimitive newPrimitive(Type t){
        return(JavaValuePrimitive) createNewValue(t);
    }
    
    public JavaValueArray newArray(Type t){
        return (JavaValueArray) createNewValue(t);
    }
    
    public JavaValueObject newObject(Type t){
        return (JavaValueObject) createNewValue(t);
    }
    
    public JavaValueClass getClassType(String internalName){
        return acquireClass(internalName);
    }
    
    
    
    

}
