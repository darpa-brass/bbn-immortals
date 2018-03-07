package mil.darpa.immortals.das;

/**
 * A general purpose interface to check for applicability and execute the provided function
 * 
 * Created by awellman@bbn.com on 10/16/17.
 */
public interface KnowledgeInterchangeInterface {

    /**
     * 
     * @param deploymentModelUri The Knowledge Repository URI of the deployment model
     * @param knowledgeUri THe knowledge repository information store URI
     * @return Whether or not this component is capable of resolving any detected issues
     */
    boolean init(String deploymentModelUri, String knowledgeUri);

    /**
     * Executes the operation of the implementing class
     */
    void execute();

    /**
     * Tears down the previously initialized session
     */
    void teardown();
}
