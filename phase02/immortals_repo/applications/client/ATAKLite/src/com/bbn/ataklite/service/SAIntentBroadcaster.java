package com.bbn.ataklite.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import mil.darpa.immortals.datatypes.Coordinates;
import mil.darpa.immortals.javatypeconverters.CoordinateLocationConverter;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 2/15/16.
 */
class SAIntentBroadcaster {

    private final LocalBroadcastManager localBroadcastManager;

    public SAIntentBroadcaster(@Nonnull Context context) {
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void broadcastSelfLocationUpdate(Coordinates coordinates) {
        Intent i = new Intent(SAIntentReceiver.SELF_LOCATION_UPDATE);
        i.putExtra(SAIntentReceiver.EXTRA_LOCATION, CoordinateLocationConverter.toLocation(coordinates));
        localBroadcastManager.sendBroadcast(i);
    }

    public void displayMessage(@Nonnull String message) {
        Intent i = new Intent(SAIntentReceiver.DISPLAY_MESSAGE);
        i.putExtra(SAIntentReceiver.EXTRA_TEXT, message);
        localBroadcastManager.sendBroadcast(i);
    }

    public void broadcastFieldUpdate(@Nonnull Coordinates coordinates, @Nonnull String originIdentifier) {
        Intent i = new Intent(SAIntentReceiver.FIELD_LOCATION_UPDATE);
        i.putExtra(SAIntentReceiver.EXTRA_LOCATION, CoordinateLocationConverter.toLocation(coordinates));
        i.putExtra(SAIntentReceiver.EXTRA_ORIGIN_ID, originIdentifier);
        localBroadcastManager.sendBroadcast(i);
    }

    public void broadcastImageUpdate(@Nonnull Coordinates coordinates, @Nonnull String originIdentifier, @Nonnull String imageUrl) {
        Intent i = new Intent(SAIntentReceiver.FIELD_IMAGE_UPDATE);
        i.putExtra(SAIntentReceiver.EXTRA_LOCATION, CoordinateLocationConverter.toLocation(coordinates));
        i.putExtra(SAIntentReceiver.EXTRA_ORIGIN_ID, originIdentifier);
        i.putExtra(SAIntentReceiver.EXTRA_IMAGE_URL, imageUrl);
        localBroadcastManager.sendBroadcast(i);
    }
}
