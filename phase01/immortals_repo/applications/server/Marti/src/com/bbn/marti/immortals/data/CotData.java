package com.bbn.marti.immortals.data;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.net.CotChannel;
import org.jetbrains.annotations.NotNull;

/**
 * Cot data container
 *
 * Created by awellman@bbn.com on 12/14/15.
 */
public class CotData {

    public final CotEventContainer cotEventContainer;

    public final CotChannel channel;

    public CotData(@NotNull CotEventContainer cotEventContainer, @NotNull CotChannel channel) {
        this.cotEventContainer = cotEventContainer;
        this.channel = channel;
    }
}
