package com.bbn.filter;

import com.bbn.marti.remote.RemoteSubscription.ImagePref;
import com.bbn.marti.service.CoreConfig;
import com.bbn.marti.util.Assertion;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 */
public class Images {
    // default values
    public static final int DEFAULT_PIXELS = 10;
    public static final ImagePref DEFAULT_IMAGE_PREF = ImagePref.THUMBNAIL;

    // XPath values
    public static final String DETAIL_PATH = "/event/detail";
    public static final String IMAGE_PATH = "/event/detail/image";
    public static final String IMAGE_ELEMENT_NAME = "image";
    public static final String IMAGE_FORMAT = "jpg";

    // keys
    public static final String PIXELS_INT_KEY = "filter.thumbnail.pixels";

    private static Logger log = Logger.getLogger(Images.class);

    static {
        CoreConfig ctx = CoreConfig.getInstance();

        if (ctx.getAttributeInteger(PIXELS_INT_KEY) == null) {
            ctx.setAttribute(PIXELS_INT_KEY,
                    DEFAULT_PIXELS);
        }
    }

    /**
     * Pure (side effect free) conversion from the given element to an ImageData wrapper.
     * <p>
     * The image text is decoded into a byte[], which is transformed into a thumb byte[] as well.
     * A copy of the given element is stored with the data into an ImageData object. The given element
     * becomes the canonical version of that element, generating all later formats of the image.
     */
    public static ImageData imageDataFromElement(Element imageElem) {
        ImageData imageData = null;
        try {
            // decode data from element text, clear string
            String rawDataString = imageElem.getText();
            byte[] rawData = decodeImageData(rawDataString);

            // process into full/thumb byte arrays
            BufferedImage fullImg = ImageIO.read(new ByteArrayInputStream(rawData));
            BufferedImage thumbImg = createThumbNail(fullImg);

            // write out to an array
            ByteArrayOutputStream fullStream = new ByteArrayOutputStream();
            ByteArrayOutputStream thumbStream = new ByteArrayOutputStream();
            ImageIO.write(fullImg, IMAGE_FORMAT, fullStream);
            ImageIO.write(thumbImg, IMAGE_FORMAT, thumbStream);

            // create copy (obtuse logic with temporarily stripping out the text, we want to avoid potentially allocating
            // another text node with the encoded image data copied, though this might not be the case)
            imageElem.setText("");
            Element clonedElem = imageElem.createCopy();
            imageElem.setText(rawDataString);

            // create ImageData wrapper
            imageData = new ImageData()
                    .fullBytes(fullStream.toByteArray())
                    .thumbBytes(thumbStream.toByteArray())
                    .element(clonedElem);

        } catch (Exception e) {
            log.warn("  Image Conversion error: " + e.getMessage(), e);
        }

        return imageData;
    }

    /**
     * Generates an element formatted for the given Image Preference
     */
    public static Element generateElementFromData(ImageData imageData, ImagePref pref) {
        Assertion.notNull(pref);

        Element newImageElem = imageData.element().createCopy();

        switch (pref) {
            case FULL_IMAGE:
                setContent(newImageElem, imageData.getFullResImage());
                break;
            case THUMBNAIL:
                setContent(newImageElem, imageData.getThumbnail());
                break;
            case URL_ONLY:
                break;
            case DATABASE:
                Assertion.notNull(imageData.getPrimaryKey());
                setContent(newImageElem, imageData.getPrimaryKey());
                break;
            default:
                // DEBUG line
                Assertion.fail();
        }

        return newImageElem;
    }

    /**
     * A mutator method to set the image text to the given byte array, encoding the image data in the process
     */
    public static void setContent(Element imageElem, byte[] imageData) {
        String imageText = encodeImageData(imageData);
        imageElem.setText(imageText);
    }

    /**
     * Formats the given element to have the database primary key in the text field of the element
     */
    public static void setContent(Element imageElem, Integer primaryKey) {
        imageElem.setText(primaryKey.toString());
    }

    /**
     * Converts the unencoded byte array to an encoded xml text string
     */
    public static String encodeImageData(byte[] unencodedData) {
        byte[] encodedData = Base64.encodeBase64(unencodedData);
        return new String(encodedData);
    }

    /**
     * Converts the encoded xml text string to a byte[]
     */
    public static byte[] decodeImageData(String imageText) {
        byte[] decodedData = Base64.decodeBase64(imageText.getBytes());
        return decodedData;
    }

    /**
     * Tries to parse an integer out of the element text (primary key of the image should be
     * stored in the element text)
     */
    public static Integer parseTextToPrimaryKey(Element imageElem) {
        Integer primaryKey = null;
        String primaryText = imageElem.getText();

        if (!primaryText.equals("")) {
            // backward compatibility, will read databases
            try {
                primaryKey = Integer.valueOf(primaryText);
            } catch (NumberFormatException e) {
                log.error("Error reading image element text: couldn't parse to an integer");
            }
        }

        return primaryKey;
    }

    /**
     * Creates a thumbnail-sized image from a full-size image
     */
    private static BufferedImage createThumbNail(BufferedImage fullSizeImg)
            throws Exception {
        int width = fullSizeImg.getWidth();
        int height = fullSizeImg.getHeight();
        if (width <= 0 || height <= 0) {
            throw new Exception(
                    "At least one dimension is invalid.  No image writen to message.");
        }
        int pixels = CoreConfig.getInstance().getAttributeInteger(
                PIXELS_INT_KEY);
        if (width > height) { // Ensure ratio is kept
            height = (int) ((double) (height) / (width) * pixels);
            width = pixels;
        } else {
            width = (int) ((double) (width) / (height) * pixels);
            height = pixels;
        }
        BufferedImage thumb = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_BGR);
        Graphics2D g = thumb.createGraphics();
        g.drawImage(fullSizeImg, 0, 0, width, height, null); // Shrink Image
        g.dispose();
        return thumb;
    }

    /**
     * Image data structure for storing the data associated with an image
     * - full/thumbnail, decoded byte arrays
     * - generating element (private, canonical reference)
     * - primary key (database entry)
     */
    public static class ImageData {
        private byte[] fullResImg = null;
        private byte[] thumbnail = null;
        private Element imageElem = null;
        private Integer primaryKey = null;

        // fluent setter
        public ImageData fullBytes(byte[] fullRes) {
            this.fullResImg = fullRes;
            return this;
        }

        // get full bytes
        public byte[] fullBytes() {
            return this.fullResImg;
        }

        // fluent setter
        public ImageData thumbBytes(byte[] thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        // get thumb bytes
        public byte[] thumbBytes() {
            return this.thumbnail;
        }

        // fluent setter
        public ImageData element(Element imageElement) {
            this.imageElem = imageElement;
            return this;
        }

        // returns a reference to the mutable element held by this data wrapper
        // users should copy before modifying, as changes will be reflected internally
        public Element element() {
            return this.imageElem;
        }

        // fluent setter for the db primary key
        public ImageData primaryKey(Integer primaryKey) {
            this.primaryKey = primaryKey;
            return this;
        }

        // get primary key
        public Integer primaryKey() {
            return this.primaryKey;
        }

        // creates shallow copy of this object
        public ImageData copy() {
            return new ImageData()
                    .fullBytes(this.fullBytes())
                    .thumbBytes(this.thumbBytes())
                    .element(this.element())
                    .primaryKey(this.primaryKey());
        }

        // preexisting code
        public byte[] getFullResImage() {
            return fullResImg;
        }

        public byte[] getThumbnail() {
            return thumbnail;
        }

        public Element getElement() {
            Assertion.notNull(imageElem);
            return imageElem;
        }

        public Integer getPrimaryKey() {
            Assertion.notNull(primaryKey);
            return primaryKey;
        }
    }
}
