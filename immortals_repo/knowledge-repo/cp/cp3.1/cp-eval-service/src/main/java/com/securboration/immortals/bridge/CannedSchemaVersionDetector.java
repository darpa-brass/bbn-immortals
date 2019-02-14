package com.securboration.immortals.bridge;


public class CannedSchemaVersionDetector {
    
    /**
     * 
     * @param s
     * @return "v1" if only MDL 17 is detected, "v2" if only MDL 19 is detected, else throws an exception
     */
    public static String detectMdlTemplateVersion(String s){
        final boolean is17 = isMdlVersion17(s);
        final boolean is19 = isMdlVersion19(s);
        
        if(!is17 && !is19){
            throw new RuntimeException("unable to determine version of schema (neither 17 nor 19)");
        } else if(is17 && is19){
            throw new RuntimeException("unable to determine version of schema (matches both 17 and 19");
        } else if(!is17 && is19){
            return "v2";
        } else if(is17 && !is19){
            return "v1";
        }
        
        throw new RuntimeException("unable to determine version of schema");
    }
    
    private static boolean isMdlVersion17(String s){
        return doesContainMagicString(
            s,
            "Tmats_01-2011.xsd"
            );
    }
    
    private static boolean isMdlVersion19(String s){
        return doesContainMagicString(
            s,
            "TmatsCommonTypes.xsd"
            );
    }
    
    private static boolean doesContainMagicString(
            String s, 
            String...magics
            ){
        for(String magic:magics){
            if(s.contains(magic)){
                return true;
            }
            
            if(s.equals(magic)){
                return true;
            }
        }
        
        return false;
    }

}
