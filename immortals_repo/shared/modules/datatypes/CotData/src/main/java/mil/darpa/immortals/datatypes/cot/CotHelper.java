package mil.darpa.immortals.datatypes.cot;


import mil.darpa.immortals.datatypes.cot.xml.FlowTagMap;

import javax.xml.bind.*;
import java.io.*;
import java.nio.file.Path;

/**
 * Utility class to aid in marshalling/unmarshalling CoT data
 * <p>
 * Created by awellman@bbn.com on 5/30/17.
 */
public class CotHelper {

    private static JAXBContext jc; // = JAXBContext.newInstance(Event.class);
    
    private static JAXBContext getJC() throws JAXBException{
        if (jc == null) {
            jc = JAXBContext.newInstance(Event.class, FlowTagMap.class);
        }
        return jc;
    }

    public static Event unmarshalEvent(String cotString) throws JAXBException {
        return unmarshalEvent(new StringReader(cotString));
    }

    public static Event unmarshalEvent(Reader reader) throws JAXBException {
        Unmarshaller u = getJC().createUnmarshaller();
        return (Event) u.unmarshal(reader);
    }

    public static Event unmarshalEvent(Path path) throws FileNotFoundException, JAXBException {
        return unmarshalEvent(new FileReader(path.toFile()));
    }

    public static String marshalObject(Object obj) throws JAXBException {
        Marshaller m = getJC().createMarshaller();
        StringWriter sw = new StringWriter();
        m.marshal(obj, sw);
        return sw.toString();
    }
}
