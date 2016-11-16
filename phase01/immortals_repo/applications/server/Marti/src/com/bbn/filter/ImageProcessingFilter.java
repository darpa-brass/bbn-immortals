package com.bbn.filter;

import com.bbn.cot.CotEventContainer;
import com.bbn.filter.Images.ImageData;
import com.bbn.marti.util.Assertion;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class ImageProcessingFilter implements Filter<CotEventContainer> {
    private static Logger log = Logger.getLogger(ImageFormattingFilter.class);

    public CotEventContainer filter(CotEventContainer c) {
        List<Element> imageElems = (List<Element>) c.getDocument().selectNodes(Images.IMAGE_PATH);

        if (imageElems.size() > 0) {
            log.debug("stripping " + imageElems.size() + " images from the cot message");

            // have some image elements to process
            // initialize ImageData image list
            List<ImageData> dataList = c.getContextOrStore(CotEventContainer.IMAGE_KEY, new ArrayList<ImageData>(imageElems.size()));

            // should have no ImageData already attached to the cot message -- process only once
            Assertion.zero(dataList.size());

            // convert each image element to an ImageData construct attached to the CoT message
            for (Element imageElem : imageElems) {
                // convert element to imageData
                ImageData data = Images.imageDataFromElement(imageElem);

                // add data to data list if nonnull (something terrible could have happened)
                if (data != null) {
                    // add to image data context list
                    dataList.add(data);
                }

                // remove image element from the document
                imageElem.detach();
            }
        }

        return c;
    }
}