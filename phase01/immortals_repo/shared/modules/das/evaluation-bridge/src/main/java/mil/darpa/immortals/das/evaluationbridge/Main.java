package mil.darpa.immortals.das.evaluationbridge;

import mil.darpa.immortals.das.configuration.EvaluationClientConfiguration;
import mil.darpa.immortals.das.configuration.EvaluationConfiguration;

/**
 * Created by awellman@bbn.com on 10/17/16.
 */
public class Main {

    public static void main(String args[]) {

        // Create a configueation with the specified session identifier
        EvaluationConfiguration evaluationConfiguration = new EvaluationConfiguration("MySessionIdentifier");

        // Create a client configuration that broadcasts latestSA at an interval of 1000 ms and images at an interval of 20000 ms
        EvaluationClientConfiguration clientConfiguration = new EvaluationClientConfiguration(1000, 20000);

        // Add ten instances of that client to the configuration
        evaluationConfiguration.addIdenticalClients(clientConfiguration, 10);

        // Construct a bridge to perform the evaluation
        EvaluationBridge evaluationBridge = new EvaluationBridge(evaluationConfiguration);

        // Run the basic sanity test evaluation. It is blocking. This will probably return something later on, but it is a void function for now
        evaluationBridge.performBasicEvaluation();
    }
}
