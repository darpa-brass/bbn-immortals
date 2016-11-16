package com.bbn.marti.immortals.pipelines;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.pipes.BufferedImageFileWriter;
import mil.darpa.immortals.core.AbstractOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;
import org.jetbrains.annotations.NotNull;

/**
 * Created by awellman@bbn.com on 2/29/16.
 */

public class OutToNetworkImageProcessor extends AbstractOutputProvider<CotEventContainer> implements InputProviderInterface<CotEventContainer> {

    // SIImages-declaration: 7A4A45C9-8569-4264-A2B2-44759167B43E
    BufferedImageFileWriter bufferedImageFileWriter;
    // SIImages-declaration-end

    public OutToNetworkImageProcessor() {
        // SIImages-init: 7A4A45C9-8569-4264-A2B2-44759167B43E
        bufferedImageFileWriter = new BufferedImageFileWriter();
        // SIImages-init-end
    }

    public void handleData(@NotNull com.bbn.cot.CotEventContainer data) {
        // SIImages-work: 7A4A45C9-8569-4264-A2B2-44759167B43E
        java.awt.image.BufferedImage object0 = data.getBufferedImage();
        bufferedImageFileWriter.consume(object0);
        // SIImages-work-end
        distributeResult(data);
    }
}

