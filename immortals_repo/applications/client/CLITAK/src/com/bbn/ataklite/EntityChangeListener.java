package com.bbn.ataklite;

import mil.darpa.immortals.datatypes.Coordinates;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 2/8/16.
 */
public interface EntityChangeListener {
    public void onMyLocationChanged(@Nonnull Coordinates newLocation);

    public void onExternalEntityLocationAddedOrChanged(@Nonnull String identifier, @Nonnull Coordinates newLocation);

    public void onExternalEntityImageAdded(@Nonnull String identifier, @Nonnull Coordinates imageLocation, @Nonnull String imageUrl);
}
