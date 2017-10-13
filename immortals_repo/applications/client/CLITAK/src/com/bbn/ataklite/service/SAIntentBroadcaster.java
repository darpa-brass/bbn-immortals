package com.bbn.ataklite.service;

import mil.darpa.immortals.datatypes.Coordinates;

import javax.annotation.Nonnull;
import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 2/15/16.
 */
class SAIntentBroadcaster {

    private LinkedList<SAIntentReceiverInterface> listeners = new LinkedList<>();

    SAIntentBroadcaster() {
    }

    void addListener(@Nonnull SAIntentReceiverInterface listener) {
        this.listeners.add(listener);
    }

    void removeListener(@Nonnull SAIntentReceiverInterface listener) {
        this.listeners.remove(listener);
    }

    void broadcastSelfLocationUpdate(@Nonnull Coordinates coordinates) {
        for (SAIntentReceiverInterface l : listeners) {
            l.selfLocationUpdate(coordinates);
        }
    }

    void broadcastSelfImageUpdate(@Nonnull Coordinates coordinates, @Nonnull String imageUrl) {
        for (SAIntentReceiverInterface l : listeners) {
            l.selfImageUpdate(coordinates, imageUrl);
        }
    }

    void displayMessage(@Nonnull String message) {
        for (SAIntentReceiverInterface l : listeners) {
            l.selfDisplayMessage(message);
        }
    }

    void broadcastFieldUpdate(@Nonnull Coordinates coordinates, @Nonnull String originIdentifier) {
        for (SAIntentReceiverInterface l : listeners) {
            l.fieldLocationUpdate(coordinates, originIdentifier);
        }
    }

    void broadcastImageUpdate(@Nonnull Coordinates coordinates, @Nonnull String originIdentifier, @Nonnull String imageUrl) {
        for (SAIntentReceiverInterface l : listeners) {
            l.fieldImageUpdate(coordinates, originIdentifier, imageUrl);
        }
    }
}
