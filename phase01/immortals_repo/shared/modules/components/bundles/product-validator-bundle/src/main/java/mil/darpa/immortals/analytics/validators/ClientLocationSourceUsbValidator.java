package mil.darpa.immortals.analytics.validators;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 11/10/16.
 */
public class ClientLocationSourceUsbValidator extends AbstractClientLocationSourceValidator {

    private List<String> tags = null;

    @Override
    public String getValidatorName() {
        return "client-location-source-usb";
    }

    @Override
    List<String> getValidLocationTags() {
        if (tags == null) {
            tags = new LinkedList<>();
            tags.add("m-g-s-u");
        }
        return tags;
    }

    public ClientLocationSourceUsbValidator(@Nonnull Set<String> clientIdentifiers) {
        super(clientIdentifiers);
    }
}
