package com.bbn.ataklite.service;

import mil.darpa.immortals.datatypes.Coordinates;

import javax.annotation.Nonnull;

/**
 * Interface to listen for updates
 * 
 * Created by awellman@bbn.com on 7/27/17.
 */
public interface SAIntentReceiverInterface {

    void selfLocationUpdate(@Nonnull Coordinates coordinates);
    
    void selfImageUpdate(@Nonnull Coordinates coordinates, @Nonnull String imageUrl);

    void selfDisplayMessage(@Nonnull String msg);

    void fieldLocationUpdate(@Nonnull Coordinates coordinates, @Nonnull String originIdentifier);

    void fieldImageUpdate(@Nonnull Coordinates coordinates, @Nonnull String originIdentifier,
                                 @Nonnull String imageUrl);
}
