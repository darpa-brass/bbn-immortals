package com.bbn.marti.immortals.cot;

import com.bbn.cot.CotEventContainer;
import com.bbn.filter.Images;
import mil.darpa.immortals.core.AbstractOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;
import org.dom4j.Node;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Strips images from a cot message
 * <p>
 * Created by awellman@bbn.com on 1/8/16.
 */
public class ImageStripper extends AbstractOutputProvider<CotEventContainer> implements InputProviderInterface<CotEventContainer> {

    @Override
    public void handleData(@Nonnull CotEventContainer data) {
        List<Node> images = data.getDocument().selectNodes(Images.IMAGE_PATH);

        if (images != null && images.size() > 0) {
            for (Node e : images) {
                data.getDocument().remove(e);
            }
        }
    }
}
