package com.bbn.filter;

import com.bbn.cot.CotEventContainer;
import com.bbn.filter.Images.ImageData;
import com.bbn.marti.util.Assertion;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;

public class ImageProcessingFilter implements Filter<CotEventContainer> {
    private static Logger log = Logger.getLogger(ImageFormattingFilter.class);

    public CotEventContainer filter(CotEventContainer c) {
        c.imageProcessingFilter();
        return c;
    }
}