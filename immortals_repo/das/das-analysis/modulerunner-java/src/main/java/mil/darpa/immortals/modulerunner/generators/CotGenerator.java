package mil.darpa.immortals.modulerunner.generators;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
    public static final int DEFAULT_STALE_HOURS = 24;
    private static final String imageBase64String = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCAAGABADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD+ffw1+3sNR8CJ4x8SfCn4e6VqFzq/xD8F6Douk+Etd8R6Jr/iHw7a/CzWV1bxHc3vxU8Lan4Wi0rR/HEy6ZPpEviG21PUUa11bw3BBHHqj5Ph/wD4KMaN4ah8ZJ8Tf2cfh14uTXfBGq6J4B1Dw0+ueHLnwf8AESXU9E1zSvHGr6fea5rEXijQ9Og0rU/Bs/ggahprX/h7xTfa8/ieLxX4e0G6mKK7aHGfGNTKOJMZPiviF4ihmWHVCf8AbWZKNKPs6GPcKVJYpUoU3iG70owVN4dvAuDwP7h+TPhvhyGY5ThY5Bk3sauCqqrF5XgZSqNYjFYRTlVlh3VdRUYRtUc/aKqliedYpe2f/9k=";

    public static Document createMessage() {
        return createMessage(Callsigns.getRandom(),
                ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LAT, MAX_RANDOM_LAT),
                ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LON, MAX_RANDOM_LON));
    }

    public static Document createMessageWithImage() {
        return createMessageWithImage(Callsigns.getRandom(),
                ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LAT, MAX_RANDOM_LAT),
                ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LON, MAX_RANDOM_LON));
    }

    public static Document createMessage(String uid, double currentLatitude, double currentLongitude) {
        Document cot = DocumentHelper.createDocument();
        Element eventElement = cot.addElement("event")
                .addAttribute("version", "2.0")
                .addAttribute("type", DEFAULT_TYPE)
                .addAttribute("how", DEFAULT_HOW);

        // Set start, stale, and time values to something sensible
        LocalDateTime time = LocalDateTime.now();
        LocalDateTime stale = time.plusHours(DEFAULT_STALE_HOURS);

        eventElement.addAttribute("uid", uid);
        eventElement.addAttribute("time", time.toString() + "Z");
        eventElement.addAttribute("start", time.toString() + "Z");
        eventElement.addAttribute("stale", stale.toString() + "Z");

        eventElement.addElement("point")
                .addAttribute("lat", Double.toString(currentLatitude))
                .addAttribute("lon", Double.toString(currentLongitude))
                .addAttribute("hae", DEFAULT_ERROR)
                .addAttribute("ce", DEFAULT_ERROR)
                .addAttribute("le", DEFAULT_ERROR);

        eventElement.addElement("detail");

        return cot;
    }

    public static Document createMessageWithImage(String uid, double currentLatitude, double currentLongitude) {
        Document doc = createMessage(uid,
                ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LAT, MAX_RANDOM_LAT),
                ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LON, MAX_RANDOM_LON));

        Element root = doc.getRootElement();

        Element detail = root.element("detail");

        if (detail == null) {
            detail = root.addElement("detail");
        }

        Element imageElement = detail.addElement("image");
        imageElement.addAttribute("height", "200");
        imageElement.addAttribute("width", "200");
        imageElement.addAttribute("mime", "image/jpeg");
        imageElement.addAttribute("resolution", "1");
        imageElement.addAttribute("type", "EO");
        imageElement.addAttribute("size", String.valueOf(imageBase64String.length()));
        imageElement.setText(imageBase64String);

        return doc;
    }

    /**
     * Creates a CoT Situational-Awareness (SA) message that includes
     *
     * @param uid
     * @param latitude
     * @param longitude
     * @param callsign
     * @param endpoint
     * @return a well-formed CoT event
     */
    @SuppressWarnings("rawtypes")
    public static Document createMessageWithCallsign(String uid,
                                                     double latitude,
                                                     double longitude,
                                                     String callsign,
                                                     String endpoint) {

        Document cot = createMessage(uid, latitude, longitude);
        Element rootElement = cot.getRootElement();
        for (Iterator itr = rootElement.elementIterator("detail"); itr.hasNext(); ) {
            Element detailElement = (Element) itr.next();
            Element contactElement = detailElement.addElement("contact");
            contactElement.addAttribute("callsign", callsign);
            contactElement.addAttribute("endpoint", endpoint);
        }
        return cot;
    }

    public static String parseUID(String cotMessage) {
        try {
            Document doc = DocumentHelper.parseText(cotMessage);

            Element event = doc.getRootElement();

            Element detail = event.element("detail");
            if (detail != null) {
                Element link = detail.element("link");
                if (link != null) {
                    String uid = link.attributeValue("uid");
                    if (uid != null) {
                        return uid;
                    }
                }
            }

            String identifier = event.attributeValue("uid");
            return identifier;

        } catch (DocumentException e) {
            System.err.println(cotMessage);
            throw new RuntimeException(e);
        }
    }

    public static List<String> parseMessages(String receivedData) {
        try {
            List<String> returnList = new LinkedList<>();

            String[] stringList;

            if (receivedData.contains("\n")) {
                stringList = receivedData.split("\n");
            } else {
                stringList = new String[]{receivedData};
            }

            for (String string : stringList) {

                if (string.startsWith("<?xml")) {
                    string = string.substring(string.indexOf("?>") + 2);
                } else if (string.endsWith("?>")) {
                    string = string.substring(0, string.lastIndexOf("<?xml"));
                }
                if (string == null || string.equals("")) {
                    Document doc = DocumentHelper.parseText(string);
                    returnList.add(doc.asXML());
                }
            }

            return returnList;
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

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
