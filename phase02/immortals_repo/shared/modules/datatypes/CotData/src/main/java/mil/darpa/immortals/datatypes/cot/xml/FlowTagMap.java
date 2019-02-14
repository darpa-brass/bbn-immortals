package mil.darpa.immortals.datatypes.cot.xml;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 8/9/17.
 */
@XmlType
@XmlJavaTypeAdapter(FlowTagAdapter.class)
public class FlowTagMap {
    Map<String, String> map = new HashMap<>();
    
    public void put(String key, String value) {
        map.put(key, value);
    }
}
