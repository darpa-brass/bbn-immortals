package com.securboration.immortals.bcad.transformers;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;

public class BytecodePrintHelper {
    
    private static Map<Integer,String> opcodeToString = new HashMap<>();
    
    static{
        opcodeToString.put(Opcodes.INVOKEDYNAMIC,   "INVOKEDYNAMIC");
        opcodeToString.put(Opcodes.INVOKEINTERFACE, "INVOKEINTERFACE");
        opcodeToString.put(Opcodes.INVOKESPECIAL,   "INVOKESPECIAL");
        opcodeToString.put(Opcodes.INVOKESTATIC,    "INVOKESTATIC");
        opcodeToString.put(Opcodes.INVOKEVIRTUAL,   "INVOKEVIRTUAL");
    }
    
    public static String getStringForm(int opcode){
        
        if(!opcodeToString.containsKey(opcode)){
            throw new RuntimeException("no string form for opcode " + opcode);
        }
        
        return opcodeToString.get(opcode);
    }

}
