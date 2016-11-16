package com.bbn.marti.immortals.cot;

import com.bbn.cot.CotEventContainer;
import com.bbn.filter.Images;
import mil.darpa.immortals.core.AbstractOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;
import org.dom4j.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Strips images from a cot message
 *
 * Created by awellman@bbn.com on 1/8/16.
 */
public class ImageStripper extends AbstractOutputProvider<CotEventContainer> implements InputProviderInterface<CotEventContainer> {

    @Override
    public void handleData(@NotNull CotEventContainer data) {
        List<Element> images = (List<Element>) data.getDocument().selectNodes(Images.IMAGE_PATH);

        if (images != null && images.size() > 0) {
            for (Element e : images) {
                data.getDocument().remove(e);
            }
        }
    }
}
