package com.bbn.marti.immortals.data;

import com.bbn.marti.immortals.net.CotChannel;

import javax.annotation.Nonnull;
import java.net.InetAddress;

/**
 * Created by awellman@bbn.com on 1/11/16.
 */
public class CotChannelData {
    public final CotChannel cotChannel;
    public final InetAddress host;
    public final String protocolName;
    public final int port;


    public CotChannelData(@Nonnull CotChannel cotChannel, @Nonnull InetAddress host, @Nonnull String protocolName, int port) {
        this.cotChannel = cotChannel;
        this.host = host;
        this.protocolName = protocolName;
        this.port = port;
    }
}
