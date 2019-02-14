package com.securboration.immortals.deployment.Main;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class MainHelper {

    public static String printObject(Object o){
        
      return ReflectionToStringBuilder.toString(
              o,
              RecursiveToStringStyle.MULTI_LINE_STYLE
              );
    }
    
}
