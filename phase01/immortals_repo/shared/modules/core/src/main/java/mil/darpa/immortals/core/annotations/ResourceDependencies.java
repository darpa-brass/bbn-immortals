package mil.darpa.immortals.core.annotations;

import mil.darpa.immortals.core.annotations.triple.Triple;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
public @interface ResourceDependencies {

    String[] dependencyUris() default {""};
}
