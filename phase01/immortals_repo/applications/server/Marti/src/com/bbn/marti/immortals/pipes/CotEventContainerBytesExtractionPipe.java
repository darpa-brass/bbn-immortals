package com.bbn.marti.immortals.pipes;

import com.bbn.cot.CotEventContainer;
import mil.darpa.immortals.core.AbstractInputOutputProvider;
import mil.darpa.immortals.core.AbstractOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;
import org.jetbrains.annotations.NotNull;

/**
 * This DFU extracts the CoT message as bytes from a CotEventContainer
 * Created by awellman@bbn.com on 1/11/16.
 */
public class CotEventContainerBytesExtractionPipe extends AbstractInputOutputProvider<CotEventContainer, byte[]> {
    @Override
    public synchronized void handleData(@NotNull CotEventContainer data) {
        distributeResult(data.asXml().getBytes());
    }
}
