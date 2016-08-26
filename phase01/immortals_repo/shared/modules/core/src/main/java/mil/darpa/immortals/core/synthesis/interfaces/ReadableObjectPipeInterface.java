package mil.darpa.immortals.core.synthesis.interfaces;

/**
 * Object-base "InputStream" style pipeline for dealing with blocking reads
 *
 * Created by awellman@bbn.com on 7/15/16.
 */
public interface ReadableObjectPipeInterface<OutputType> {

    /**
     * Produces an object of type OutputType. Will block if data is available
     * @return The next object in the pipeline, or null if the end of the
     * stream has been reached
     */
    public OutputType produce();

    /**
     * Closes thee pipe.
     */
    public void closePipe();

    /**
     * Returns the buffer size of this object for use by wrappers if necessary
     */
    public int getBufferSize();
}
