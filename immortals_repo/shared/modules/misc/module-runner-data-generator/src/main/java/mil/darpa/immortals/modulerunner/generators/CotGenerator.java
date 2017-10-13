package mil.darpa.immortals.modulerunner.generators;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by austin on 06/06/2016.
 */

public class CotGenerator {

    public static final String DEFAULT_ERROR = "9999999";
    public static final String DEFAULT_HOW = "h-g-i-g-o";
    public static final String DEFAULT_TYPE = "a-f-G-U-C";
    // Rough mostly-continental chunk of the US
    public static final double MAX_RANDOM_LAT = 48.562068;
    public static final double MIN_RANDOM_LAT = 30.755641;
    public static final double MAX_RANDOM_LON = -81.490127;
    public static final double MIN_RANDOM_LON = -122.387100;
    public static final int DEFAULT_STALE_MS = 10800000; // 3 hours
    private static final String imageBase64String = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCAAGABADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD+ffw1+3sNR8CJ4x8SfCn4e6VqFzq/xD8F6Douk+Etd8R6Jr/iHw7a/CzWV1bxHc3vxU8Lan4Wi0rR/HEy6ZPpEviG21PUUa11bw3BBHHqj5Ph/wD4KMaN4ah8ZJ8Tf2cfh14uTXfBGq6J4B1Dw0+ueHLnwf8AESXU9E1zSvHGr6fea5rEXijQ9Og0rU/Bs/ggahprX/h7xTfa8/ieLxX4e0G6mKK7aHGfGNTKOJMZPiviF4ihmWHVCf8AbWZKNKPs6GPcKVJYpUoU3iG70owVN4dvAuDwP7h+TPhvhyGY5ThY5Bk3sauCqqrF5XgZSqNYjFYRTlVlh3VdRUYRtUc/aKqliedYpe2f/9k=";

    private static DocumentBuilder builder = null;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    public static String createMessage() {
        return stringifyDocument(createMessageDocument(Callsigns.getRandom(),
                ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LAT, MAX_RANDOM_LAT),
                ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LON, MAX_RANDOM_LON)));
    }

    public static String createMessageWithImage() {
        return stringifyDocument(createMessageWithImage(Callsigns.getRandom(),
                ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LAT, MAX_RANDOM_LAT),
                ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LON, MAX_RANDOM_LON)));
    }


    public static String createMessage(String uid, double currentLatitude, double currentLongitude) {
        return stringifyDocument(createMessageDocument(uid, currentLatitude, currentLongitude));
    }

    private static Document createMessageDocument(String uid, double currentLatitude, double currentLongitude) {
        try {
            if (builder == null) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                builder = factory.newDocumentBuilder();
            }


            Document cot = builder.newDocument();

            Element eventElement = cot.createElement("event");
            eventElement.setAttribute("version", "2.0");
            eventElement.setAttribute("type", DEFAULT_TYPE);
            eventElement.setAttribute("how", DEFAULT_HOW);

            // Set start, stale, and time values to something sensible
            Date now = new Date();
            Date stale = new Date(now.getTime() + DEFAULT_STALE_MS);

            eventElement.setAttribute("uid", uid);
            eventElement.setAttribute("time", sdf.format(now));
            eventElement.setAttribute("start", sdf.format(now));
            eventElement.setAttribute("stale", sdf.format(stale));

            Element pointElement = cot.createElement("point");

            pointElement.setAttribute("lat", Double.toString(currentLatitude));
            pointElement.setAttribute("lon", Double.toString(currentLongitude));
            pointElement.setAttribute("hae", DEFAULT_ERROR);
            pointElement.setAttribute("ce", DEFAULT_ERROR);
            pointElement.setAttribute("le", DEFAULT_ERROR);

            eventElement.appendChild(cot.createElement("detail"));

            eventElement.appendChild(pointElement);
            cot.appendChild(eventElement);


            return cot;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Document createMessageWithImage(String uid, double currentLatitude, double currentLongitude) {
        Document doc = createMessageDocument(uid,
                ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LAT, MAX_RANDOM_LAT),
                ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LON, MAX_RANDOM_LON));

        Element root = doc.getDocumentElement();

        Node detail = root.getElementsByTagName("detail").item(0);

        if (detail == null) {
            detail = doc.createElement("detail");
        }

        Element imageElement = doc.createElement("image");
        imageElement.setAttribute("height", "200");
        imageElement.setAttribute("width", "200");
        imageElement.setAttribute("mime", "image/jpeg");
        imageElement.setAttribute("resolution", "1");
        imageElement.setAttribute("type", "EO");
        imageElement.setAttribute("size", String.valueOf(imageBase64String.length()));
        imageElement.setTextContent(imageBase64String);

        detail.appendChild(imageElement);

        if (root.getElementsByTagName("detail").getLength() <= 0) {
            root.appendChild(detail);
        }
        return doc;
    }

    private static String stringifyDocument(Document document) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter out = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(out));
            return out.toString();
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a CoT Situational-Awareness (SA) message that includes
     *
     * @param uid       UID Value
     * @param latitude  latitude value
     * @param longitude longitude value
     * @param callsign  callsign value
     * @param endpoint  endpoint value
     * @return a well-formed CoT event
     */
//    @SuppressWarnings("rawtypes")
    private static Document createMessageWithCallsign(String uid,
                                                     double latitude,
                                                     double longitude,
                                                     String callsign,
                                                     String endpoint) {

        Document cot = createMessageDocument(uid, latitude, longitude);
        Element rootElement = cot.getDocumentElement();


        NodeList elementList = rootElement.getElementsByTagName("detail");
        for (int i = 0; i < elementList.getLength(); i++) {
            Node detailNode = elementList.item(i);
            Element contactElement = cot.createElement("contact");
            contactElement.setAttribute("callsign", callsign);
            contactElement.setAttribute("endpoint", endpoint);
            detailNode.appendChild(contactElement);

        }
        return cot;


    }
//
//    public static String parseUID(String cotMessage) {
//        try {
//            Document doc = DocumentHelper.parseText(cotMessage);
//
//            Element event = doc.getDocumentElement();
//
//            Element detail = event.element("detail");
//            if (detail != null) {
//                Element link = detail.element("link");
//                if (link != null) {
//                    String uid = link.attributeValue("uid");
//                    if (uid != null) {
//                        return uid;
//                    }
//                }
//            }
//
//            String identifier = event.attributeValue("uid");
//            return identifier;
//
//        } catch (DocumentException e) {
//            System.err.println(cotMessage);
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static List<String> parseMessages(String receivedData) {
//        try {
//            List<String> returnList = new LinkedList<>();
//
//            String[] stringList;
//
//            if (receivedData.contains("\n")) {
//                stringList = receivedData.split("\n");
//            } else {
//                stringList = new String[]{receivedData};
//            }
//
//            for (String string : stringList) {
//
//                if (string.startsWith("<?xml")) {
//                    string = string.substring(string.indexOf("?>") + 2);
//                } else if (string.endsWith("?>")) {
//                    string = string.substring(0, string.lastIndexOf("<?xml"));
//                }
//                if (string == null || string.equals("")) {
//                    Document doc = DocumentHelper.parseText(string);
//                    returnList.add(doc.asXML());
//                }
//            }
//
//            return returnList;
//        } catch (DocumentException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public enum Callsigns {
        RedEagle,
        WhiteEagle,
        BlueEagle,
        OrangeIguana,
        PurpleHippo,
        TangyTaco,
        BeanyBurrito,
        CaffienatedCoffee;

        public static String getRandom() {
            return (Callsigns.values()[ThreadLocalRandom.current().nextInt(8)]).name();
        }
    }

}
