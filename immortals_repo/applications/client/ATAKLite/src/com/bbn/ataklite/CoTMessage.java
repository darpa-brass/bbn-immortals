package com.bbn.ataklite;

import android.graphics.Bitmap;
import android.location.Location;
import android.util.Base64;
import android.util.Log;
import mil.darpa.immortals.core.Semantics;
import mil.darpa.immortals.core.annotations.SemanticTypeBinding;
import mil.darpa.immortals.core.annotations.triple.Triple;
import mil.darpa.immortals.core.annotations.triple.Triples;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@SemanticTypeBinding(semanticType = Semantics.Datatype_CotMessage)
@Triples({
        @Triple(predicateUri = Semantics.Predicate_DependsOn, objectUri = Semantics.Ecosystem_Platform_Android),
        @Triple(predicateUri = Semantics.Predicate_DependsOn, objectUri = Semantics.Library_dom4j)
})
public class CoTMessage implements Serializable {

    private static final long STALE_PERIOD_MS = 30000;

    private static final long serialVersionUID = 239885679680454860L;
    private String uid;
    private String type;
    private long time;
    private long start;
    private long stale;
    private String how;
    private double latitude;
    private double longitude;
    private double altitude;            // 'hae'
    private double cylinderRadius;        //circular error, 'ce'
    private double cyclinderHalfHeight; //linear error, 'le'

    private byte[] imageData;
    private Integer imageHeight;
    private Integer imageWidth;
    private Bitmap.CompressFormat imageMime;
    private int imageSize;
    
    private static final AtomicInteger imageCounter = new AtomicInteger();

    public void setImage(byte[] imageData, int imageHeight, int imageWidth, Bitmap.CompressFormat imageMime) {
        this.imageData = imageData;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        this.imageSize = imageData.length;
        this.imageMime = imageMime;
    }

    public void updateTimestamp() {
        this.time = System.currentTimeMillis();
        this.start = this.time;
        this.stale = this.time + STALE_PERIOD_MS;
    }

    public CoTMessage(Location location, String uid) {
        this.uid = uid;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();

        this.time = location.getTime();
        this.start = location.getTime();
        this.stale = time + STALE_PERIOD_MS;

        this.how = location.getProvider();

        this.altitude = location.hasAltitude() ? location.getAltitude() : 0.0;

        //Semantics seem the same; investigate date type conversion issues though
        this.cylinderRadius = location.hasAccuracy() ? location.getAccuracy() : 9999999;

        //This is more complex; address later
        this.cyclinderHalfHeight = 0.0;
    }

    public CoTMessage(String xml) {
        //The code below is very direct, but preferable to a complex XML binding solution for a prototype;
        //we can certainly move some of the more expensive objects for better performance (e.g., to the handlers)
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            Element event = (Element) document.getElementsByTagName("event").item(0);

            if (event != null) {
                this.uid = event.getAttribute("uid");
                this.type = event.getAttribute("type");

                String rawTime = event.getAttribute("time");
                this.time = sdf.parse(rawTime).getTime();

                rawTime = event.getAttribute("stale");
                this.stale = sdf.parse(rawTime).getTime();

                rawTime = event.getAttribute("start");
                this.start = sdf.parse(rawTime).getTime();

                this.how = event.getAttribute("how");

                Element point = (Element) document.getElementsByTagName("point").item(0);

                if (point != null) {
                    this.latitude = Double.parseDouble(point.getAttribute("lat"));
                    this.longitude = Double.parseDouble(point.getAttribute("lon"));
                    this.cylinderRadius = Double.parseDouble(point.getAttribute("ce"));
                    this.cyclinderHalfHeight = Double.parseDouble(point.getAttribute("le"));
                    this.altitude = Double.parseDouble(point.getAttribute("hae"));
                }

                Element detail = (Element) event.getElementsByTagName("detail").item(0);

                if (detail != null) {
                    Element imageElement = (Element) detail.getElementsByTagName("image").item(0);
                    if (imageElement != null) {

                        if (imageElement.hasAttribute("height")) {
                            this.imageHeight = Integer.valueOf(imageElement.getAttribute("height"));
                        }

                        if (imageElement.hasAttribute("width")) {
                            this.imageWidth = Integer.valueOf(imageElement.getAttribute("width"));
                        }

                        if (imageElement.hasAttribute("mime")) {
                            String mimeType = imageElement.getAttribute("mime");

                            if (mimeType.equals("image/jpeg")) {
                                imageMime = Bitmap.CompressFormat.JPEG;

                            } else if (mimeType.equals("image/png")) {
                                imageMime = Bitmap.CompressFormat.PNG;

                            } else if (mimeType.equals("image/webp")) {
                                imageMime = Bitmap.CompressFormat.WEBP;

                            } else {
                                Log.e("CoTMessage", "Invalid or unknown MIME type '" + mimeType + "' received!");
                            }
                        }

                        if (imageElement.hasAttribute("size")) {
                            this.imageSize = Integer.valueOf(imageElement.getAttribute("size"));
                        }

                        imageData = Base64.decode(imageElement.getTextContent(), Base64.DEFAULT);
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getAsXML() {
        return getAsXML(this);
    }


    private static String getAsXML(CoTMessage coTMessage) {

        //The code below is very direct, but preferable to a complex XML binding solution for a prototype;
        //we can certainly move some of the more expensive objects for better performance (e.g., to the handlers)
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        String time = null;
        StringWriter out = null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

        Calendar cal = Calendar.getInstance();
        FileInputStream fis = null;

        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            document.setXmlStandalone(true);
            document.setXmlVersion("1.0");


            // Create all mandatory elements
            Element event = document.createElement("event");
            Element point = document.createElement("point");
            Element contact = document.createElement("contact");
            Element detail = document.createElement("detail");


            // Add event attributes
            event.setAttribute("version", "2.0");

            //Set time of event attribute (when event occurred)
            time = sdf.format(new Date(coTMessage.getTime()));
            event.setAttribute("time", time);

            //Set start time attribute (when event becomes valid)
            event.setAttribute("start", time);

            //Set end time attribute (when event is no longer valid)
            event.setAttribute("stale", sdf.format(new Date(coTMessage.getStale())));

            //Set how attribute
            event.setAttribute("how", coTMessage.getHow());


            // Add point attributes
            point.setAttribute("lat", String.valueOf(coTMessage.getLatitude()));
            point.setAttribute("lon", String.valueOf(coTMessage.getLongitude()));
            point.setAttribute("hae", String.valueOf(coTMessage.getAltitude()));
            point.setAttribute("ce", String.valueOf(coTMessage.getCylinderRadius()));
            point.setAttribute("le", String.valueOf(coTMessage.getCyclinderHalfHeight()));

            // Add precisionlocation attributes
//            if (coTMessage.getHow().equals("m-g")) { // AndroidGPSBuiltIn
//                Element precisionLocation = document.createElement("precisionlocation");
//                precisionLocation.setAttribute("geopointsrc", "GPS");
//                precisionLocation.setAttribute("altsrc", "GPS");
//                detail.appendChild(precisionLocation);
//
//            } else if (coTMessage.getHow().equals("m-g-s-b")) {// BluetoothGPSSimulated
//                Element precisionLocation = document.createElement("precisionlocation");
//                precisionLocation.setAttribute("geopointsrc", "GPS");
//                precisionLocation.setAttribute("altsrc", "GPS");
//                detail.appendChild(precisionLocation);
//
//            } else if (coTMessage.getHow().equals("h-e-s")) { // ManualSimulated
//                Element precisionLocation = document.createElement("precisionlocation");
//                precisionLocation.setAttribute("geopointsrc", "???");
//                precisionLocation.setAttribute("altsrc", "???");
//                detail.appendChild(precisionLocation);
//
//            } else if (coTMessage.getHow().equals("m-r-p")) { // SAASM
//                Element precisionLocation = document.createElement("precisionlocation");
//                precisionLocation.setAttribute("geopointsrc", "GPS_PPS");
//                precisionLocation.setAttribute("altsrc", "GPS_PPS");
//                detail.appendChild(precisionLocation);
//
//            } else if (coTMessage.getHow().equals("m-g-s-u")) { // USBSimulated
//                Element precisionLocation = document.createElement("precisionlocation");
//                precisionLocation.setAttribute("geopointsrc", "GPS");
//                precisionLocation.setAttribute("altsrc", "GPS");
//                detail.appendChild(precisionLocation);
//            }

            if (coTMessage.imageData == null) {
                String uid = ATAKLite.getConfigInstance().callsign;
                String trimmedUid = uid.substring(uid.length() - 3, uid.length());
                String type = "a-f-G-U-C";

                event.setAttribute("uid", uid);
                event.setAttribute("type", type);

                contact.setAttribute("callsign", trimmedUid);
                contact.setAttribute("endpoint", "0.0.0.0:-1:tcp");

                Element group = document.createElement("__group");
                group.setAttribute("name", "Cyan");
                detail.appendChild(group);


            } else {
                String uid = ATAKLite.getConfigInstance().callsign + "-i" + Integer.toString(imageCounter.incrementAndGet());
                String trimmedUid = uid.substring(uid.length() - 5, uid.length());
//                String type = "b-m-p-s-p-loc";
                String type = "a-u-G";

                event.setAttribute("uid", uid);
                event.setAttribute("type", type);

                contact.setAttribute("callsign", trimmedUid);

//                Element link = document.createElement("link");
//                link.setAttribute("relation", "p-p");
//                link.setAttribute("type", type);
//                link.setAttribute("uid", uid);
//                detail.appendChild(link);

                // Add sensor attributes
                Element sensor = document.createElement("sensor");
                sensor.setAttribute("displayMagneticReference", "0");
                sensor.setAttribute("fov", "45");
                sensor.setAttribute("fovRed", "1.0");
                sensor.setAttribute("fovBlue", "1.0");
                sensor.setAttribute("fovAlpha", "0.3");
                sensor.setAttribute("fovGreen", "1.0");
                sensor.setAttribute("azimuth", "270");
                sensor.setAttribute("range", "100");
                detail.appendChild(sensor);

                Element image = document.createElement("image");

                if (coTMessage.imageHeight != null) {
                    image.setAttribute("height", Integer.toString(coTMessage.imageHeight));
                }

                if (coTMessage.imageWidth != null) {
                    image.setAttribute("width", Integer.toString(coTMessage.imageWidth));
                }

                if (coTMessage.imageMime != null) {
                    if (coTMessage.imageMime == Bitmap.CompressFormat.JPEG) {
                        image.setAttribute("mime", "image/jpeg");

                    } else if (coTMessage.imageMime == Bitmap.CompressFormat.PNG) {
                        image.setAttribute("mime", "image/png");

                    } else if (coTMessage.imageMime == Bitmap.CompressFormat.WEBP) {
                        image.setAttribute("mime", "image/webp");
                    }
                }

                image.setAttribute("type", "EO");
                image.setAttribute("size", String.valueOf(coTMessage.imageData.length));
                image.setTextContent(new String(Base64.encode(coTMessage.imageData, Base64.DEFAULT)));
                detail.appendChild(image);
            }

            detail.appendChild(contact);
            event.appendChild(point);
            event.appendChild(detail);
            document.appendChild(event);

            //Render the final XML
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            //tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //??? causes problems
            //tf.setOutputProperty(OutputKeys.INDENT, "yes"); //add whitespace when debugging
            out = new StringWriter();

            tf.transform(new DOMSource(document), new StreamResult(out));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    //Do nothing
                }
            }
        }

        return out.toString();
    }

    public byte[] getImageData() {
        return imageData;
    }

    public String getUid() {
        return uid;
    }

    public String getType() {
        return type;
    }

    public long getTime() {
        return time;
    }

    public long getStart() {
        return start;
    }

    public long getStale() {
        return stale;
    }

    public String getHow() {
        return how;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getCylinderRadius() {
        return cylinderRadius;
    }

    public double getCyclinderHalfHeight() {
        return cyclinderHalfHeight;
    }

}
