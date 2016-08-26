package mil.darpa.immortals.core.synthesis.annotations.dfu;

import mil.darpa.immortals.core.Semantics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by awellman@bbn.com on 2/29/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface SynthesisWork {

    String functionalAspectUri = Semantics.Functionality_LocationProvider;
}
