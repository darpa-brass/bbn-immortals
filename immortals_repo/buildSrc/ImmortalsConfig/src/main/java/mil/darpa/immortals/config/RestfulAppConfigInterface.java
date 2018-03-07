package mil.darpa.immortals.config;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by awellman@bbn.com on 1/25/18.
 */
public interface RestfulAppConfigInterface extends AppConfigInterface {
    public String getProtocol();

    // TODO: Rename this to "gethost"
    public String getUrl();

    public int getPort();

    public URI getFullUrl();

    static URI toFullUrl(RestfulAppConfigInterface c) {
        try {
            return new URI(c.getProtocol(), null, c.getUrl(), c.getPort(), null, null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
