package mil.darpa.immortals.core.api.ll.phase1;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
public class AdaptationResult {
    public final String adaptationStatusValue;
    public final LinkedList<String> audits;
    public final String auditsAsString;
    public final String details;
    public final String selectedDfu;
    public final String sessionIdentifier;

    public AdaptationResult(String adaptationStatusValue, LinkedList<String> audits, String auditsAsString,
                            String details, String selectedDfu, String sessionIdentifier) {
        this.adaptationStatusValue = adaptationStatusValue;
        this.audits = audits;
        this.auditsAsString = auditsAsString;
        this.details = details;
        this.selectedDfu = selectedDfu;
        this.sessionIdentifier = sessionIdentifier;
    }
}
