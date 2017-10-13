package com.bbn.ataklite.service;

import mil.darpa.immortals.datatypes.Coordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Displays activity on the CLI
 * <p>
 * Created by awellman@bbn.com on 7/27/17.
 */
public class SAIntentCLIReceiver implements SAIntentReceiverInterface {
    
    Logger logger = LoggerFactory.getLogger(SAIntentCLIReceiver.class);
    
    @Override
    public void selfLocationUpdate(@Nonnull Coordinates coordinates) {
        logger.info("Sending location: (" + coordinates.getLatitude() + ", " + coordinates.getLongitude() + ")");
    }

    @Override
    public void selfImageUpdate(@Nonnull Coordinates coordinates, @Nonnull String imageUrl) {
        String[] filepath = imageUrl.split("/");
        logger.info("Sending Image " + filepath[filepath.length - 1] +
                " with location (" + coordinates.getLatitude() + ", " + coordinates.getLongitude() + ")");

    }

    @Override
    public void selfDisplayMessage(@Nonnull String msg) {
        System.out.println(msg);
    }

    @Override
    public void fieldLocationUpdate(@Nonnull Coordinates coordinates, @Nonnull String originIdentifier) {
        logger.info("Received " + originIdentifier + "'s location (" +
                coordinates.getLatitude() + ", " + coordinates.getLongitude() + ")");
    }

    @Override
    public void fieldImageUpdate(@Nonnull Coordinates coordinates, @Nonnull String originIdentifier, @Nonnull String imageUrl) {
        String[] filepath = imageUrl.split("/");

        logger.info("Received " + originIdentifier + "'s image " + filepath[filepath.length - 1] +
                " with location (" + coordinates.getLatitude() + ", " + coordinates.getLongitude() + ")");
    }
}
