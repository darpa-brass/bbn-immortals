package mil.darpa.immortals.config;

import java.net.URI;

/**
 * Created by awellman@bbn.com on 1/25/18.
 */
public interface RestfulAppConfigInterface extends AppConfigInterface {
    public String getProtocol();

    // TODO: Rename this to "gethost"
    public String getUrl();

    public int getPort();

    public URI getFullUrl();
}
