package mil.darpa.immortals.analytics.events.data;

/**
 * Created by awellman@bbn.com on 10/26/16.
 */
public class ServerClientData {

    public final String clientAddress;
    public final int remotePort;
    public final int localPort;

    public ServerClientData(String clientAddress, int remotePort, int localPort) {
        this.clientAddress = clientAddress;
        this.remotePort = remotePort;
        this.localPort = localPort;
    }
}
