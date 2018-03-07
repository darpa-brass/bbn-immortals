package mil.darpa.immortals.core.api.ll.phase2;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP1;
import mil.darpa.immortals.core.api.annotations.P2CP2;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.ATAKLiteSubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.globalmodel.GlobalSubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.MartiSubmissionModel;

/**
 * Created by awellman@bbn.com on 9/12/17.
 */
@P2CP1
@P2CP2
@P2CP3
@Description("The main submission model")
public class SubmissionModel {
    
    @Description("The identifier for this scenario execution")
    public String sessionIdentifier;

    @P2CP1
    @P2CP3
    @Description("Marti server deployment model")
    public MartiSubmissionModel martiServerModel;

    @P2CP3
    @Description("ATAKLite client deployment model")
    public ATAKLiteSubmissionModel atakLiteClientModel;

    @P2CP2
    @Description("Global deployment model")
    public GlobalSubmissionModel globalModel;

    public SubmissionModel() {
    }

    public SubmissionModel(String sessionIdentifier, MartiSubmissionModel martiServerModel, ATAKLiteSubmissionModel atakLiteClientModel, GlobalSubmissionModel globalModel) {
        this.sessionIdentifier = sessionIdentifier;
        this.martiServerModel = martiServerModel;
        this.atakLiteClientModel = atakLiteClientModel;
        this.globalModel = globalModel;

    }

}
