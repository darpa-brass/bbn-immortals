package mil.darpa.immortals.javatypeconverters;

import mil.darpa.immortals.datatypes.Coordinates;
import mil.darpa.immortals.datatypes.cot.Detail;
import mil.darpa.immortals.datatypes.cot.Event;
import mil.darpa.immortals.datatypes.cot.Point;

import javax.annotation.Nonnull;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.util.GregorianCalendar;

/**
 * Converter...
 * <p>
 * Created by awellman@bbn.com on 7/24/17.
 */
public class CotDataCoordinateConverter {

    private static final long STALE_PERIOD_MS = 30000;

    public static Event toEvent(Coordinates coordinates, String uid) {
        try {
            DatatypeFactory dtf = DatatypeFactory.newInstance();
            Event e = new Event();
            
            if (e.getDetail() == null) {
                e.setDetail(new Detail());
            }
            
            if (e.getDetail().getContact() == null) {
                e.getDetail().setContact(new Detail.Contact());
            }

            if (e.getPoint() == null) {
                e.setPoint(new Point());
            }
            
            e.setVersion(BigDecimal.valueOf(2.0));
            e.getDetail().getContact().setEndpoint("0.0.0.0:-1:tcp");
            e.setType("a-f-G-U-C");
            
            e.setUid(uid);

            e.getPoint().setLat(new BigDecimal(coordinates.getLatitude()));
            e.getPoint().setLon(new BigDecimal(coordinates.getLongitude()));


            GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeInMillis(coordinates.getAcquisitionTime());
            e.setTime(dtf.newXMLGregorianCalendar(gc));
            e.setStart(dtf.newXMLGregorianCalendar(gc));

            GregorianCalendar gc_stale = new GregorianCalendar();
            gc_stale.setTimeInMillis(coordinates.getAcquisitionTime() + STALE_PERIOD_MS);
            e.setStale(dtf.newXMLGregorianCalendar(gc_stale));

            e.setHow(coordinates.getProvider());

            if (coordinates.hasAltitude()) {
                e.getPoint().setHae(new BigDecimal(coordinates.getAltitudeMSL()));
            } else {
                e.getPoint().setHae(new BigDecimal(0.0));
            }

            if (coordinates.hasAccuracy()) {
                e.getPoint().setCe(new BigDecimal(coordinates.getAccuracyMetric()));
            } else {
                e.getPoint().setCe(new BigDecimal(9999999));
            }

            e.getPoint().setLe(new BigDecimal(0));

            return e;

        } catch (DatatypeConfigurationException e) {
            // This shouldn't be possible short of a platform discrepancy...
            throw new RuntimeException(e);
        }
    }
    
    

    public static Coordinates toCoordinates(@Nonnull Event event) {
        return new Coordinates(
                event.getPoint().getLat().doubleValue(),
                event.getPoint().getLon().doubleValue(),
                event.getPoint().getHae().doubleValue(),
                event.getPoint().getCe().floatValue(),
                event.getTime().toGregorianCalendar().getTimeInMillis(),
                event.getHow()
        );
    }
}
