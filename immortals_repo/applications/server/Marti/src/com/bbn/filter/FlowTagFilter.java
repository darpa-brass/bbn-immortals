package com.bbn.filter;

import com.bbn.cot.CotEventContainer;
import org.apache.log4j.Logger;
import org.dom4j.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SimpleTimeZone;

public class FlowTagFilter implements Filter<CotEventContainer> {
    public static String DEFAULT_FLOWTAG_KEY = "filter.flowtag.text";
    public static String flowTagXPath = "/event/detail/_flow-tags_"; // xpath for getting to the flow tags element
    public static String flowTagAttrXPath = flowTagXPath + "/@*";     // xpath for getting to the flow tag serverId/timestamp attribute pair
    public static Logger log = Logger.getLogger(FlowTagFilter.class.getCanonicalName());
    private boolean warned = false; // flag for logging an empty server Id warning only once
    private String serverId;

    public FlowTagFilter(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Removes the flow tag filter for the given server id.
     *
     * @return whether an attribute with the given serverId name was actually removed ie, whether the xpath /event/detail/_flow-tags_/@serverId existed
     * @note In the case that the given xpath is malformed due to a flawed serverId, the error is caught, and false is returned.
     */
//    public static boolean unfilter(CotEventContainer c, String serverId) {
//        // select the _flow-tags_ attribute with the matching server id, if any
//        boolean modified = false;
//
//        try {
//            String serverXPath = flowTagXPath + "/@" + serverId;
//            Attribute serverAttr = (Attribute) c.getDocument().selectSingleNode(serverXPath);
//            if (serverAttr != null) {
//                serverAttr.detach();
//                modified = true;
//            }
//        } catch (InvalidXPathException e) {
//            log.error("Invalid xpath for serverID in unfilterExplicit: " + serverId);
//        }
//
//        return modified;
//    }

    // TODO: figure out where we are stopping messages if we aren't filtering them out here because of the empty detail element... and shouldn't we add a server tag?
    @Override
    public CotEventContainer filter(CotEventContainer c) {
        if (!serverId.isEmpty()) {
            c.filter(serverId);
        } else if (!warned) {
            log.error("Flow tag filter not working due to an empty serverId -- unable to filter messages. Routing loops could occur with catastrophic results.");
            warned = true;
        }
        return c;
    }

    /**
     * Removes the flow tag filter *for this server* from a message, if there is a filter.
     * <p>
     * Intended to modify this message so that this server's broker will OK the message.
     *
     * @return Flag indicating whether the message was modified.
     * @note This method mutates the container's contained xml.
     */
//    public boolean unfilter(CotEventContainer c) {
//        return unfilter(c, serverId);
//    }

    /**
     * Removes all of the flow tag filters from a message.
     * <p>
     * Intended to modify the message so that any federation route will be successful.
     *
     * @return The list of server Ids that were removed from the document. Empty if nothing was removed.
     * @throw ClassCastException if the message's flow tag Nodes are not all of type element
     */
//    public List<String> unfilterAll(CotEventContainer c) {
//        List<Node> attrs = c.getDocument().selectNodes(flowTagAttrXPath);
//        List<String> removed = new ArrayList<String>(attrs.size());
//
//        for (Node attr : attrs) {
//            removed.add(attr.getName());
//            attr.detach();
//        }
//
//        return removed;
//    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String id) {
        serverId = id;
    }
}
