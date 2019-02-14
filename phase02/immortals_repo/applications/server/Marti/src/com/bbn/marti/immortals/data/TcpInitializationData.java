package com.bbn.marti.immortals.data;

import javax.annotation.Nonnull;
import java.net.InetAddress;

/**
 * Data to be used to initialize a transport
 *
 * Created by awellman@bbn.com on 12/21/15.
 */
public class TcpInitializationData {

    public final String name;
    public final String protocol;
    public final InetAddress address;
    public final int port;

    public TcpInitializationData(@Nonnull String name, @Nonnull String protocol, @Nonnull InetAddress address, int port) {
        this.name = name;
        this.protocol = protocol;
        this.address = address;
        this.port = port;
    }
}
