package com.bbn.cot;

import com.bbn.filter.Images;
import mil.darpa.immortals.core.Semantics;
import mil.darpa.immortals.core.annotations.SemanticTypeBinding;
import mil.darpa.immortals.core.annotations.triple.Triple;
import mil.darpa.immortals.core.annotations.triple.Triples;
import org.dom4j.*;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@SemanticTypeBinding(semanticType = Semantics.Datatype_CotMessage)
@Triples({
        @Triple(predicateUri = Semantics.Predicate_DependsOn, objectUri = Semantics.Library_dom4j),
        @Triple(predicateUri = Semantics.Predicate_DependsOn, objectUri = Semantics.Library_awt)
})

public class CotEventContainer {
    public static final String PRIMARY_KEY = "primary_key";
    public static final String IMAGE_KEY = "image";
    public static final String THUMB_KEY = "thumb";
    protected Map<String, Object> context;
    protected Document doc;
    protected String uid = null;

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

    public String asXml() {
        return doc.asXML();
    }

    public String getAccess() {
        return getRootAttribute("access");
    }

    public String getCallsign() {
        Attribute callsignAttr = (Attribute) doc.selectSingleNode("/event/detail/contact/@callsign");
        if (callsignAttr != null) {
            return callsignAttr.getValue();
        } else {
            return null;
        }
    }

    public Double getCe() {
        return Double.parseDouble(getPointAttribute("ce"));
    }

    public boolean hasContextKey(String key) {
        return context.containsKey(key);
    }

    public Object getContext(String key) {
        return context.get(key);
    }

    public <T> T getContext(String key, Class<T> type) {
        return (T) getContext(key);
    }

    public <T> T getContextOrElse(String key, T alternate) {
        T value = (T) getContext(key);

        if (value == null)
            value = alternate;

        return value;
    }

    public <T> T getContextOrStore(String key, T init) {
        T value = getContextOrElse(key, init);

        if (value == init) {
            setContextValue(key, init);
        }

        return value;
    }

    public Object getContextValue(String key) {
        return context.get(key);
    }

    public <T> T getContextValueOrElse(String key, T alternate) {
        T current = (T) getContextValue(key);
        if (current != null) {
            return current;
        } else {
            return alternate;
        }
    }

    public <T> T getOrStoreContextValue(String key, T init) {
        T current = (T) getContextValue(key);

        if (current == null) {
            setContextValue(key, init);
            current = init;
        }

        return current;
    }

    public String getDetailXml() {
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

    public Document document() {
        return doc;
    }

    public String getEndpoint() {
        Attribute endpointAttr = (Attribute) doc.selectSingleNode("/event/detail/contact/@endpoint");
        if (endpointAttr != null) {
            return endpointAttr.getValue();
        } else {
            return null;
        }
    }

    public Double getHae() {
        return Double.parseDouble(getPointAttribute("hae"));
    }

    public String getHow() {
        return getRootAttribute("how");
    }

    public String getLat() {
        return getPointAttribute("lat");
    }

    public Double getLe() {
        return Double.parseDouble(getPointAttribute("le"));
    }

    /**
     * Gets all the &lt;link&gt; elements in the event.
     * The "link" element is a child of the "detail" element.
     *
     * @return a List of dom4j Elements, one for each link; or an empty List, if there are no links
     */

    public List<Element> getLinks() {
        return doc.selectNodes("/event/detail/link");
    }

    public String getLon() {
        return getPointAttribute("lon");
    }

    public String getOpex() {
        return getRootAttribute("opex");
    }

    private String getPointAttribute(String attribute) {
        return doc.getRootElement().element("point").attributeValue(attribute);
    }

    public String getQos() {
        return getRootAttribute("qos");
    }

    private String getRootAttribute(String attribute) {
        return doc.getRootElement().attributeValue(attribute);
    }

    public String getStale() {
        return getRootAttribute("stale");
    }

    public String getStart() {
        return getRootAttribute("start");
    }

    public String getTime() {
        return getRootAttribute("time");
    }

    public String getType() {
        return getRootAttribute("type");
    }

    public String getUid() {
        return getRootAttribute("uid");
    }

    public boolean matchXPath(String b) {
        if (doc != null) {
            XPath xpath = DocumentHelper.createXPath(b);
            return xpath.booleanValueOf(doc);
        }
        return false;
    }

    public <T> T removeContext(String key, Class<T> type) {
        return (T) context.remove(key);
    }

    public Object removeContextValue(String key) {
        return context.remove(key);
    }

    public <T> T setContextAndCarry(String key, T value) {
        context.put(key, value);
        return value;
    }

    public <T> T setContext(String key, T value) {
        T old = (T) getContext(key);
        context.put(key, value);

        return old;
    }

    public Object setContextValue(String key, Object value) {
        Object old = getContext(key);
        context.put(key, value);

        return old;
    }

    public String toString() {
        return asXml();
    }

    public CotEventContainer copy() {
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
    public BufferedImage getBufferedImage() {
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
                System.err.println("Image Conversion Error: " + e.getMessage());
            }
            return null;
        }
    }

    public void setBufferedImage(@Nullable BufferedImage image) {
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
                System.err.println("Image Conversion Error: " + e.getMessage());
            }
        }


    }
}
