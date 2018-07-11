package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
@Unstable
public abstract class AbstractAdaptationDetails {
    
    @Description("The current state of the adaptation")
    public DasOutcome dasOutcome;

    @Description("The adaptor in use")
    public String adaptorIdentifier;

    @Description("The identifier for the adaptation instance")
    public String adaptationIdentifier;

    // TODO: Add the adaptation validation counters to the constructor parameters and constructor usage sites
    @Description("The total number of validations performed by the adaptation")
    public int adaptationValidationsPerformed;

    @Description("The total number of passing validations performed by the adaptation")
    public int passingAdaptationValidations;
    
    public AbstractAdaptationDetails() {
    }

    public AbstractAdaptationDetails(@Nonnull String adaptorIdentifier, @Nonnull DasOutcome dasOutcome, @Nonnull String adaptationIdentifier) {
        this.adaptorIdentifier = adaptorIdentifier;
        this.dasOutcome = dasOutcome;
        this.adaptationIdentifier = adaptationIdentifier;
        this.adaptationValidationsPerformed = 0;
        this.passingAdaptationValidations = 0;
    }
}
