package mil.darpa.immortals.core.api.ll.phase1;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
public class AdaptationState {

    public final Status adaptationStatus;
    public final AdaptationResult details;

    public AdaptationState(Status adaptationStatus, AdaptationResult details) {
        this.adaptationStatus = adaptationStatus;
        this.details = details;
    }
}
