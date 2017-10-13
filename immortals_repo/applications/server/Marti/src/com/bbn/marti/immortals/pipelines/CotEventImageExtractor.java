package com.bbn.marti.immortals.pipelines;

import com.bbn.cot.CotEventContainer;
import mil.darpa.immortals.core.synthesis.AbstractFunctionConsumingPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import java.awt.image.BufferedImage;

/**
 * Created by awellman@bbn.com on 2/29/16.
 */

public class CotEventImageExtractor extends AbstractFunctionConsumingPipe<CotEventContainer, BufferedImage> {
    public CotEventImageExtractor(ConsumingPipe<BufferedImage> next) {
        super(false, next);
    }

    @Override
    public BufferedImage process(CotEventContainer input) {
        return input.getBufferedImage();
    }
}

