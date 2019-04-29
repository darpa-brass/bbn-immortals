package mil.darpa.immortals.flitcons.datatypes.hierarchical;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates an object is immutable and is not required to implement {@link DuplicateInterface}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Immutable {
}
