package com.bbn.marti.service;

import com.bbn.marti.net.CotEventSender;
import com.bbn.marti.remote.RemoteSubscription;
import org.apache.log4j.Logger;

/**
 * Created by awellman@bbn.com on 12/9/15.
 */
class Subscription extends RemoteSubscription {
    private static final long serialVersionUID = -4032164972566035627L;
    private static Logger log = Logger.getLogger(Subscription.class);
    public CotEventSender sender = null;
    public boolean proxy = false;

    public Subscription() {
        if (CoreConfig.getInstance().getAttributeBoolean("filter.thumbnail.enable") == true) {
            imgPref = ImagePref.THUMBNAIL;
        } else {
            imgPref = ImagePref.FULL_IMAGE;
        }
        //log.info("Added subscription with img pref: " + imgPref.toString());
    }

    // TODO: simon -- enum.valueOf throws exception if modeName doesn't exist, doesn't return null.
    synchronized public void setImgMode(String modeName) {
        imgPref = ImagePref.valueOf(modeName);
        if (imgPref == null) {
            Subscription.log.error("Invalid Image Mode selected: " + modeName);
            imgPref = ImagePref.THUMBNAIL; // default to thumbnail
        }
    }
}
