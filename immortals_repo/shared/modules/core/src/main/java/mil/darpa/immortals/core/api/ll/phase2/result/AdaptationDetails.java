package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
@Unstable
public class AdaptationDetails {
    public DasOutcome dasOutcome;
    public String adaptationStatusValue;
    public LinkedList<String> audits;
    public String auditsAsString;
    public String details;
    public String selectedDfu;
    public String sessionIdentifier;

    public AdaptationDetails() {
    }

    public AdaptationDetails(DasOutcome dasOutcome, String adaptationStatusValue, LinkedList<String> audits,
                             String auditsAsString, String details, String selectedDfu, String sessionIdentifier) {
        this.dasOutcome = dasOutcome;
        this.adaptationStatusValue = adaptationStatusValue;
        this.audits = audits;
        this.auditsAsString = auditsAsString;
        this.details = details;
        this.selectedDfu = selectedDfu;
        this.sessionIdentifier = sessionIdentifier;
    }
}
