package com.securboration.immortals.utility;


import java.util.HashMap;
import java.util.Map;

public class StreamImplementationArchivist {
    
    protected static  Map<String, String> affectedMethodsToRepair;
    
    public static void initializeArchive(String streamType) {
        affectedMethodsToRepair = new HashMap<>();
        if (streamType.equals("java/net/Socket")) {
            affectedMethodsToRepair.put("getOutputStream()",
                    "{return this.outputstream;}");
            affectedMethodsToRepair.put("getInputStream()",
                    "{return this.inputstream;}");
            
        } else {
            
            affectedMethodsToRepair.put("write(ByteBuffer)",
                    "{OutputStream var2 = this.outputstream;\n" +
                            "\t\tvar2.write(var1.bytes());\n" +
                            "\t\treturn var1.length;}");
            affectedMethodsToRepair.put("read(ByteBuffer)",
                    "{byte[] arr = var1.array();\n" +
                            "\t\tInputStream is = this.inputstream;\n" +
                            "\t\tint bytesRead = is.read(arr);\n" +
                            "\t\tvar1.put(arr);\n" +
                            "\t\treturn bytesRead;}");
        }
    }
}
