package com.bbn.cot;

import com.bbn.filter.Images;
import com.bbn.marti.remote.RemoteSubscription;
import com.bbn.marti.util.Assertion;
import mil.darpa.immortals.dfus.ElevationData;
import org.dom4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CotEventContainer {

    private static SimpleDateFormat dateFormat;

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        dateFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
    }

    public static final String PRIMARY_KEY = "primary_key";
    public static final String IMAGE_KEY = "image";
    public static final String THUMB_KEY = "thumb";
    protected Map<String, Object> context;
    protected Document doc;
    protected String uid = null;

    Logger logger = LoggerFactory.getLogger(CotEventContainer.class);

    public CotEventContainer() {
        context = new HashMap<String, Object>();
        doc = null;
    }

    public CotEventContainer(CotEventContainer src) {
        context = new HashMap<String, Object>(src.context);
        doc = (Document) src.doc.clone();
    }

    public CotEventContainer(Document xml) throws DocumentException {
        context = new HashMap<String, Object>();
        doc = xml;
    }

    public synchronized String asXml() {
        return doc.asXML();
    }

    public synchronized String getAccess() {
        return getRootAttribute("access");
    }

    public synchronized String getCallsign() {
        Attribute callsignAttr = (Attribute) doc.selectSingleNode("/event/detail/contact/@callsign");
        if (callsignAttr != null) {
            return callsignAttr.getValue();
        } else {
            return null;
        }
    }

    public synchronized Double getCe() {
        return Double.parseDouble(getPointAttribute("ce"));
    }

    public synchronized boolean hasContextKey(String key) {
        return context.containsKey(key);
    }

    public synchronized Object getContext(String key) {
        return context.get(key);
    }

    public synchronized <T> T getContext(String key, Class<T> type) {
        return (T) getContext(key);
    }

    public synchronized <T> T getContextOrElse(String key, T alternate) {
        T value = (T) getContext(key);

        if (value == null)
            value = alternate;

        return value;
    }

    public synchronized <T> T getContextOrStore(String key, T init) {
        T value = getContextOrElse(key, init);

        if (value == init) {
            setContextValue(key, init);
        }

        return value;
    }

    public synchronized Object getContextValue(String key) {
        return context.get(key);
    }

    public synchronized <T> T getContextValueOrElse(String key, T alternate) {
        T current = (T) getContextValue(key);
        if (current != null) {
            return current;
        } else {
            return alternate;
        }
    }

    public synchronized <T> T getOrStoreContextValue(String key, T init) {
        T current = (T) getContextValue(key);

        if (current == null) {
            setContextValue(key, init);
            current = init;
        }

        return current;
    }

    public synchronized String getDetailXml() {
        Element detailElem = (Element) doc.selectSingleNode("/event/detail");
        if (detailElem != null) {
            return detailElem.asXML();
        } else {
            return null;
        }
    }

    public Document getDocument() {
        return doc;
    }

    public synchronized String getEndpoint() {
        Attribute endpointAttr = (Attribute) doc.selectSingleNode("/event/detail/contact/@endpoint");
        if (endpointAttr != null) {
            return endpointAttr.getValue();
        } else {
            return null;
        }
    }

    public synchronized Double getHae() {
        return Double.parseDouble(getPointAttribute("hae"));
    }

    public synchronized void setElevationData(ElevationData elevationData) {
        setPointAddtribute("hae", Double.toString(elevationData.getHae()));
        setPointAddtribute("le", Double.toString(elevationData.getLe()));
    }

    public synchronized String getHow() {
        return getRootAttribute("how");
    }

    public synchronized Double getLat() {
        return Double.parseDouble(getPointAttribute("lat"));
    }

    public synchronized Double getLe() {
        return Double.parseDouble(getPointAttribute("le"));
    }

    /**
     * Gets all the &lt;link&gt; elements in the event.
     * The "link" element is a child of the "detail" element.
     *
     * @return a List of dom4j Elements, one for each link; or an empty List, if there are no links
     */

    private synchronized List<Element> getLinks() {
        List<Node> nodes = doc.selectNodes("/event/detail/link");

        if (nodes == null) {
            return null;
        } else {
            List<Element> elements = new ArrayList<>(nodes.size());
            for (Node n : nodes) {
                elements.add(n.getParent());
            }
            return elements;
        }
    }

    public synchronized double getLon() {
        return Double.parseDouble(getPointAttribute("lon"));
    }

    public String getOpex() {
        return getRootAttribute("opex");
    }

    private synchronized String getPointAttribute(String attribute) {
        return doc.getRootElement().element("point").attributeValue(attribute);
    }

    private synchronized void setPointAddtribute(String key, String value) {
        doc.getRootElement().element("point").attribute(key).setData(value);
    }

    public synchronized String getQos() {
        return getRootAttribute("qos");
    }

    private synchronized String getRootAttribute(String attribute) {
        return doc.getRootElement().attributeValue(attribute);
    }

    public synchronized String getStale() {
        return getRootAttribute("stale");
    }

    public synchronized String getStart() {
        return getRootAttribute("start");
    }

    public synchronized String getTime() {
        return getRootAttribute("time");
    }

    public synchronized String getType() {
        return getRootAttribute("type");
    }

    public synchronized String getUid() {
        return getRootAttribute("uid");
    }

    public synchronized boolean matchXPath(String b) {
        if (doc != null) {
            XPath xpath = DocumentHelper.createXPath(b);
            return xpath.booleanValueOf(doc);
        }
        return false;
    }

    public synchronized <T> T removeContext(String key, Class<T> type) {
        return (T) context.remove(key);
    }

    public synchronized Object removeContextValue(String key) {
        return context.remove(key);
    }

    public synchronized <T> T setContextAndCarry(String key, T value) {
        context.put(key, value);
        return value;
    }

    public synchronized <T> T setContext(String key, T value) {
        T old = (T) getContext(key);
        context.put(key, value);

        return old;
    }

    public synchronized Object setContextValue(String key, Object value) {
        Object old = getContext(key);
        context.put(key, value);

        return old;
    }

    public synchronized String toString() {
        return asXml();
    }

    public synchronized CotEventContainer copy() {
        return new CotEventContainer(this);
    }

    public enum ContextKey {
        PRIMARY_KEY("primary_key"),
        IMAGE_KEY("image"),
        THUMB("thumb");

        private final String key;

        ContextKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public interface ContextInitializer {
        Object initialize();
    }

    /**
     * Simple event handler for SAX parser that keeps track of parse errors.
     *
     * @author agronosk
     */
    private class CotHandler extends DefaultHandler {
        boolean valid = true;
        private ArrayList<String> errors = new ArrayList<String>();

        @Override
        public void error(SAXParseException ex) {
            errors.add(ex.toString());
            this.valid = false;
        }

        @Override
        public void fatalError(SAXParseException ex) {
            errors.add(ex.toString());
            this.valid = false;
        }

        public String getErrorMessage() {
            StringBuilder builder = new StringBuilder();
            for (Iterator<String> itr = this.errors.iterator(); itr.hasNext(); ) {
                builder.append(itr.next());
                if (itr.hasNext()) {
                    builder.append("\n");
                }
            }
            return builder.toString();
        }

        public boolean isValid() {
            return valid;
        }

        @Override
        public void skippedEntity(String name) {
            errors.add("Missing entity \"" + name + "\"");
            this.valid = false;
        }
    }


    @Nullable
    public synchronized BufferedImage getBufferedImage() {
        Element imageElem = (Element) doc.selectSingleNode("/event/detail/image");
        if (imageElem == null) {
            return null;
        } else {
            try {
                String rawDataString = imageElem.getText();
                byte[] decodedData = Images.decodeImageData(rawDataString);
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedData));
                return image;
            } catch (IOException e) {
                logger.error("Image Conversion Error: " + e.getMessage());
            }
            return null;
        }
    }

    public synchronized void setBufferedImage(@Nullable BufferedImage image) {
        // TODO: Add image?
        Element imageElem = (Element) doc.selectSingleNode("/event/detail/image");
        if (image == null) {
            if (imageElem != null) {
                doc.remove(imageElem);
            }

        } else {
            try {
                ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
                ImageIO.write(image, Images.IMAGE_FORMAT, imageStream);
                byte[] imageBytes = imageStream.toByteArray();
                String encodedData = Images.encodeImageData(imageBytes);

                imageElem.setText(encodedData);

            } catch (IOException e) {
                logger.error("Image Conversion Error: " + e.getMessage());
            }
        }
    }

    public synchronized boolean detachNodeIfExists(String xpathExpression) {
        Attribute serverAttr = (Attribute) doc.selectSingleNode(xpathExpression);
        if (serverAttr != null) {
            serverAttr.detach();
            return true;
        }
        return false;
    }

    public synchronized boolean containsElement(String name) {
        return doc.getRootElement().element(name) != null;
    }
    
    public synchronized String getDocumentValue(String xpathExpression) {
        return doc.valueOf(xpathExpression);
    }
    
    public synchronized String getDocumentNodeValue(String nodeName, String xpathExpression) {
        Node subNode = doc.selectSingleNode(nodeName);
        return subNode.valueOf(xpathExpression);
    }

    public synchronized void filter(String serverId) {
        // check to make sure detail field exists
        Element detailElem = doc.getRootElement().element("detail");

        if (detailElem != null) {
            Element flowTagElem = DocumentHelper.makeElement(detailElem, "/_flow-tags_");
            flowTagElem.addAttribute(serverId, dateFormat.format(System.currentTimeMillis()));
        }
    }
    
    public synchronized void filterImages(List<Images.ImageData> imageList, RemoteSubscription.ImagePref pref) {
        // get detail element out of document for attaching child images
        Element detailElem = DocumentHelper.makeElement(doc, Images.DETAIL_PATH);

        // remove any existing image elements
        List<Element> imageElems = (List<Element>) detailElem.elements(Images.IMAGE_ELEMENT_NAME);
        if (imageElems.size() > 0) {
            logger.warn("Trying to format cot message for images that already has image elements present -- stripping them out");
            for (Element imageElem : imageElems) {
                imageElem.detach();
            }
        }

        // attach image string data for each image
        for (Images.ImageData img : imageList) {
            Element imageElem = Images.generateElementFromData(img, pref);
            if (imageElem != null) {
                detailElem.add(imageElem);
            }
        }
    }
    
    public synchronized void imageProcessingFilter() {
        List<Node> imageElems = doc.selectNodes(Images.IMAGE_PATH);

        if (imageElems.size() > 0) {
            logger.debug("stripping " + imageElems.size() + " images from the cot message");

            // have some image elements to process
            // initialize ImageData image list
            List<Images.ImageData> dataList = getContextOrStore(CotEventContainer.IMAGE_KEY, new ArrayList<Images.ImageData>(imageElems.size()));

            // should have no ImageData already attached to the cot message -- process only once
            Assertion.zero(dataList.size());

            // convert each image element to an ImageData construct attached to the CoT message
            for (Node imageElem : imageElems) {
                // convert element to imageData
                Images.ImageData data = Images.imageDataFromElement((Element)imageElem);

                // add data to data list if nonnull (something terrible could have happened)
                if (data != null) {
                    // add to image data context list
                    dataList.add(data);
                }

                // remove image element from the document
                imageElem.detach();
            }
        }
    }
    
    public synchronized String getNodeType(String name) {
        Node event = doc.selectSingleNode(name);
        return event.valueOf("@type");
    }
}
