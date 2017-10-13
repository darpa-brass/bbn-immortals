package mil.darpa.immortals.core.api.websockets;

/**
 * Created by awellman@bbn.com on 8/9/17.
 */
public class WebsocketObject {

    public final String data;

    public final String endpoint;

    public WebsocketObject(String endpoint, String data) {
        this.endpoint = endpoint;
        this.data = data;
    }


}
