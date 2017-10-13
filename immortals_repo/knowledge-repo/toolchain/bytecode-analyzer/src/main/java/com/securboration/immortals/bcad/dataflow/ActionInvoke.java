package com.securboration.immortals.bcad.dataflow;

import com.securboration.immortals.bcad.dataflow.value.JavaValue;

/**
 * the movement of data from a src to a dest
 * 
 * @author jstaples
 *
 */
public class ActionInvoke extends Action {
    
    private final DataLocationStack objectRef;
    private final JavaValue objectRefValue;
    private final DataLocationStack [] args;
    private final JavaValue[] argsValues;
    private final DataLocationStack returnValue;
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("INVOKE ");
        
        if(objectRefValue != null){
            sb.append("instance method on " + objectRefValue + " (came from " + objectRef + ")");
        } else {
            sb.append("static method");
        }
        
        sb.append(" with args " );
        
        
        int argIndex = 0;
        for(DataLocation arg:args){
            JavaValue value = argsValues[argIndex];
            
            sb.append(argIndex + " " + value + "(came from " + arg + "),");
            argIndex++;
        }
        
        sb.append(" then return " + returnValue);
        
        return sb.toString();
    }


    public ActionInvoke(
            DataLocationStack returnValue,
            DataLocationStack objectRef,
            JavaValue objectRefValue,
            DataLocationStack[] args,
            JavaValue[] argsValues
            ) {
        super();
        this.args = args;
        this.returnValue = returnValue;
        this.objectRef = objectRef;
        this.objectRefValue = objectRefValue;
        this.argsValues = argsValues;
    }


    
    public DataLocationStack getObjectRef() {
        return objectRef;
    }


    
    public JavaValue getObjectRefValue() {
        return objectRefValue;
    }


    
    public DataLocationStack[] getArgs() {
        return args;
    }


    
    public JavaValue[] getArgsValues() {
        return argsValues;
    }


    
    public DataLocationStack getReturnValue() {
        return returnValue;
    }

}
