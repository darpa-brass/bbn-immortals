package com.bbn.marti.immortals.data;

import org.jetbrains.annotations.NotNull;

import java.net.Socket;

/**
 * Created by awellman@bbn.com on 1/11/16.
 */
public class SocketData {

    public final String protocol;
    public final String sourceServerIdentifier;
    public final Socket socket;

    public SocketData(@NotNull String protocol, @NotNull String sourceServerIdentifier, @NotNull Socket socket) {
        this.protocol = protocol;
        this.sourceServerIdentifier = sourceServerIdentifier;
        this.socket = socket;
    }
}
