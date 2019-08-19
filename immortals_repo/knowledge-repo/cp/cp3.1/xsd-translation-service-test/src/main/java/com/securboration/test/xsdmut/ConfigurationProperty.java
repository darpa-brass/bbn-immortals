package com.securboration.test.xsdmut;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation to decorate classes that extend ConfigurableTypeBase
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationProperty{
    /**
     * @return a human readable description of the property and its legal values
     */
    String desc();
}