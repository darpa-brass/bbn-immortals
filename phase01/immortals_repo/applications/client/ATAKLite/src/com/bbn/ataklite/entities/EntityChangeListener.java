package com.bbn.ataklite.entities;

import android.location.Location;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 2/8/16.
 */
public interface EntityChangeListener {
    public void onMyLocationChanged(@Nonnull Location newLocation);

    public void onExternalEntityLocationAddedOrChanged(@Nonnull String identifier, @Nonnull Location newLocation);

    public void onExternalEntityImageAdded(@Nonnull String identifier, @Nonnull Location imageLocation, @Nonnull String imageUrl);
}
