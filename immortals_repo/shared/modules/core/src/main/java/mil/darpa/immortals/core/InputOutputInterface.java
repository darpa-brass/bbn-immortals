package mil.darpa.immortals.core;

/**
 * Interface for concrete handlers in a chain of responsibility.
 *
 * @author petersamouelian
 */
@Deprecated
public interface InputOutputInterface<INPUT,OUTPUT> extends InputProviderInterface<INPUT>, OutputProviderInterface<OUTPUT> {
}
