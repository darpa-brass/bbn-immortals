package com.bbn.marti.immortals.data;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.net.CotChannel;

import javax.annotation.Nonnull;

/**
 * Cot data container
 *
 * Created by awellman@bbn.com on 12/14/15.
 */
public class CotData {

    public final CotEventContainer cotEventContainer;

    public final CotChannel channel;

    public CotData(@Nonnull CotEventContainer cotEventContainer, @Nonnull CotChannel channel) {
        this.cotEventContainer = cotEventContainer;
        this.channel = channel;
    }
}
