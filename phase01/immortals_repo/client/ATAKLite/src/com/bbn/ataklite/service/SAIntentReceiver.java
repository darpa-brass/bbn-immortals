package com.bbn.ataklite.service;

import android.location.Location;
import android.os.Bundle;
import com.bbn.ataklite.GsonHelper;
import com.google.gson.Gson;

/**
 * Created by awellman@bbn.com on 2/15/16.
 */
public class SAIntentReceiver {

    public static final String SELF_LOCATION_UPDATE = "com.bbn.ataklite.action.SELF_LOCATION_UPDATE";
    //    public static final String EXTRA_COORDINATES = "com.bbn.ataklite.extra.COORDINATES";
    public static final String FIELD_LOCATION_UPDATE = "com.bbn.ataklite.action.FIELD_LOCATION_UPDATE";
    public static final String FIELD_IMAGE_UPDATE = "com.bbn.ataklite.action.IMAGE_UPDATE";
    public static final String DISPLAY_MESSAGE = "com.bbn.ataklite.action.DISPLAY_MESSAGE";
    public static final String EXTRA_TEXT = "com.bbn.ataklite.extra.EXTRA_TEXT";
    public static final String EXTRA_ORIGIN_ID = "com.bbn.ataklite.extra.LOCATION_ID";
    public static final String EXTRA_LOCATION = "com.bbn.ataklite.extra.LOCATION";
    public static final String EXTRA_IMAGE_URL = "com.bbn.ataklite.extra.IMAGE";

    private static Gson gson = GsonHelper.createGsonInstance();

    public Location parseLocation(Bundle intentExtras) {
        return (Location) intentExtras.get(EXTRA_LOCATION);
    }

    public String parseOriginIdentifier(Bundle intentExtras) {
        return intentExtras.getString(EXTRA_ORIGIN_ID);
    }

    public String parseImageUrl(Bundle intentExtras) {
        return intentExtras.getString(EXTRA_IMAGE_URL);
    }
}