package com.bbn.marti.immortals.data;


import javax.annotation.Nonnull;
import java.net.Socket;

/**
 * Created by awellman@bbn.com on 1/11/16.
 */
public class SocketData {

    public final String protocol;
    public final String sourceServerIdentifier;
    public final Socket socket;

    public SocketData(@Nonnull String protocol, @Nonnull String sourceServerIdentifier, @Nonnull Socket socket) {
        this.protocol = protocol;
        this.sourceServerIdentifier = sourceServerIdentifier;
        this.socket = socket;
    }
}
