package mil.darpa.immortals.das.evaluationbridge;

import mil.darpa.immortals.das.configuration.EvaluationConfiguration;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 10/10/16.
 */
public class EvaluationBridge {

    public final EvaluationConfiguration evaluationConfiguration;

    public EvaluationBridge(@Nonnull EvaluationConfiguration evaluationConfiguration) {
        this.evaluationConfiguration = evaluationConfiguration;
    }

    /**
     * Performs a blocking basic evaluation limited eto two clients and one server
     */
    public void performBasicEvaluation() {

    }

    /**
     * Performs a full evaluation with all clients specified
     */
    public void performFullEvaluation() {

    }
}

