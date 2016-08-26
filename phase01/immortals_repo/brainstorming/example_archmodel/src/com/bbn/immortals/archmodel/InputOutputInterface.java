package com.bbn.immortals.archmodel;

/**
 * Interface for concrete handlers in a chain of responsibility.
 *
 * @author petersamouelian
 */
public interface InputOutputInterface<INPUT,OUTPUT> extends InputProviderInterface<INPUT>, OutputProviderInterface<OUTPUT> {
}
