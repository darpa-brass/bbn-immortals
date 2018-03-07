package mil.darpa.immortals.analytics.validators;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 11/10/16.
 */
public class ClientLocationSourceBluetoothValidator extends AbstractClientLocationSourceValidator {

    private List<String> tags = null;

    @Override
    List<String> getValidLocationTags() {
        if (tags == null) {
            tags = new LinkedList<>();
            tags.add("m-g-s-b");
        }
        return tags;
    }

    public ClientLocationSourceBluetoothValidator(@Nonnull Set<String> clientIdentifiers, boolean haltUponInitialValidation) {
        super(clientIdentifiers, haltUponInitialValidation);
    }
}
