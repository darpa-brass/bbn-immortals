package mil.darpa.immortals.datatypes.cot.xml;

import mil.darpa.immortals.datatypes.cot.Detail;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 8/9/17.
 */
public class FlowTagAdapter extends XmlAdapter<Detail.FlowTags, FlowTagMap> {
    
    public FlowTagAdapter() {
        System.out.println("MEH");
    }

    private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    
    @Override
    public FlowTagMap unmarshal(Detail.FlowTags v) throws Exception {
        FlowTagMap rval = new FlowTagMap();
        for (Element e : v.getAny()) {
            rval.map.put(e.getTagName(), e.getTextContent());
        }
        return rval;
    }

    @Override
    public Detail.FlowTags marshal(FlowTagMap v) throws Exception {
        if (v == null) {
            return null;
        }
        // This should be pooled or something to make it more efficient...
        DocumentBuilder db = dbf.newDocumentBuilder();
        Detail.FlowTags ft = new Detail.FlowTags();

        List<Element> elements = new ArrayList<>();
        for (Map.Entry<String, String> property : v.map.entrySet()) {
            Document d = db.newDocument();
            Element e = d.createElement(property.getKey());
            e.setTextContent(property.getValue());
            ft.getAny().add(e);
        }
        return ft;
    }


    //    @Override
//    public Map<String, String> unmarshal(MapWrapper v) throws Exception {
//        Map<String, String> rval = new HashMap<>();
//
//        for (Element e : v.getAny()) {
//            rval.put(e.getTagName(), e.getNodeValue());
//        }
//        return rval;
//    }
//
//    @Override
//    public MapWrapper marshal(Map<String, String> v) throws Exception {
//        Detail.FlowTags ft = new Detail.FlowTags();
//        
//        List<Element> elements = new ArrayList<>();
//        for (Map.Entry<String,String> property: v.entrySet()) {
//            Document d = documentBuilder.newDocument();
//            Element e = d.createElement(property.getKey());
//            e.setNodeValue(property.getValue());
//            ft.getAny().add(e);
//        }
//        return ft;
//    }
}
