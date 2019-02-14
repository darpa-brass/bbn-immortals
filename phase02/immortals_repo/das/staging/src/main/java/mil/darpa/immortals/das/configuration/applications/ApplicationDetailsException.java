package mil.darpa.immortals.das.configuration.applications;

/**
 * Created by awellman@bbn.com on 10/30/17.
 */
public class ApplicationDetailsException extends Exception {
    
    public ApplicationDetailsException(String msg) {
        super(msg);
    }
    
    public ApplicationDetailsException(Exception e) {
        super(e);
    }
}
