package com.securboration.immortals.maven.etc;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Printing {
    
    public static String deepPrint(Object o) {
        return ToStringBuilder.reflectionToString(o,
                ToStringStyle.MULTI_LINE_STYLE);
    }

}
