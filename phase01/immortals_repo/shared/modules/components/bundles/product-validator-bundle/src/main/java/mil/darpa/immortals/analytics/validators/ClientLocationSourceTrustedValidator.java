package mil.darpa.immortals.analytics.validators;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 11/10/16.
 */
public class ClientLocationSourceTrustedValidator extends AbstractClientLocationSourceValidator {

    private List<String> tags = null;

    @Override
    public String getValidatorName() {
        return "client-location-source-trusted";
    }

    @Override
    List<String> getValidLocationTags() {
        if (tags == null) {
            tags = new LinkedList<>();
            tags.add("m-r-p");
            tags.add("m-r-e");
            tags.add("m-r-t");
        }
        return tags;
    }

    public ClientLocationSourceTrustedValidator(@Nonnull Set<String> clientIdentifiers) {
        super(clientIdentifiers);
    }
}
