package mil.darpa.immortals.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A functional aspect of a DFU.
 * <p>
 * For example, a DFU abstraction of a counter might have three inseparable
 * functional aspects:
 * <ol>
 * <li>increment (add 1 to the current counter value)</li>
 * <li>reset (set the current counter value to 0)</li>
 * <li>get (retrieve the current counter value)</li>
 * </ol>
 * A code construct annotated with the counter DFU would contain functional
 * aspect annotations on these methods. State (in this example the value of the
 * counter) is identified using the {@link StatefulDfuAspect} annotation.
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface FunctionalDfuAspect {

    /**
     * 
     * @return a unique identifier for this instance of a DFU. This identifier
     *         is used to link external content to the annotation model (e.g.,
     *         so an element in an external DSL file can reference a specific
     *         DFU instance).
     */
    String uuid() default "";

    /**
     * 
     * @return a URI for a concept in an ontology indicating the functionality being performed.
     * E.g., locationProvider:CurrentLocation vs locationProvider:LastLocation.  If no uri is
     * provided, it is assumed that there is only one functional aspect associated with the DFU and
     * it is fulfilled by the target method.
     */
    String functionalAspectUri() default "";

    /**
     *
     * @return a URI for the parent functionality to which this functional aspect belongs.  E.g.,
     * functionality:LocationProvider
     */
    String functionalityUri();

    /**
     *
     * @return any resource dependencies specific to this functional aspect of the DFU
     */
    ResourceDependencies resourceDependencies() default @ResourceDependencies();
}
