package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
@Unstable
@Description("The current state of an adaptation")
public class AdaptationDetails extends AbstractAdaptationDetails {

    @Description("Messages indicating the reasons for failure")
    public LinkedList<String> errorMessages;

    @Description("Messages indicating the reasons for success")
    public LinkedList<String> detailMessages;

    public AdaptationDetails() {
        super();
    }

    public AdaptationDetails(@Nonnull String adaptorIdentifier, @Nonnull DasOutcome dasOutcome, @Nonnull String adaptationIdentifier) {
        super(adaptorIdentifier, dasOutcome, adaptationIdentifier);
        this.errorMessages = new LinkedList<>();
        this.detailMessages = new LinkedList<>();
        this.adaptationValidationsPerformed = 0;
        this.passingAdaptationValidations = 0;
    }

    public AdaptationDetails(@Nonnull String adaptorIdentifier, @Nonnull DasOutcome dasOutcome, @Nonnull String adaptationIdentifier, @Nonnull LinkedList<String> errorMessages, @Nonnull LinkedList<String> detailMessages) {
        super(adaptorIdentifier, dasOutcome, adaptationIdentifier);
        this.errorMessages = new LinkedList<>(errorMessages);
        this.detailMessages = new LinkedList<>(detailMessages);
        this.adaptationValidationsPerformed = 0;
        this.passingAdaptationValidations = 0;
    }

    private AdaptationDetails duplicate() {
        return new AdaptationDetails(adaptorIdentifier, dasOutcome, adaptationIdentifier, errorMessages, detailMessages);
    }

    public synchronized AdaptationDetails produceUpdate(@Nonnull DasOutcome dasOutcome, @Nullable List<String> errorMessages, @Nullable List<String> detailMessages, @Nonnull int adaptationValidationsPerformed, @Nonnull int passingAdaptationValidations) {
        AdaptationDetails ad = duplicate();
        ad.dasOutcome = dasOutcome;
        if (errorMessages != null) {
            ad.errorMessages.addAll(errorMessages);
        }
        if (detailMessages != null) {
            ad.detailMessages.addAll(detailMessages);
        }
        ad.adaptationValidationsPerformed = adaptationValidationsPerformed;
        ad.passingAdaptationValidations = passingAdaptationValidations;
        return ad;
    }
}
