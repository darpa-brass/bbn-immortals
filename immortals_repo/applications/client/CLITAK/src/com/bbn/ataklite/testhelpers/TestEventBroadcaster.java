package com.bbn.ataklite.testhelpers;

import com.bbn.ataklite.ATAKLiteConfig;
import com.bbn.ataklite.service.SACommunicationService;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.datatypes.Coordinates;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by awellman@bbn.com on 2/1/16.
 */
public class TestEventBroadcaster {

    private Coordinates currentLocation = null;

    private final SACommunicationService saCommunicationService;
    private final Path storageDirectory;

    public TestEventBroadcaster(SACommunicationService saCommunicationService) {
        this.saCommunicationService = saCommunicationService;
        this.storageDirectory = Paths.get(saCommunicationService.getConfig().storageDirectory);
    }

    public synchronized void startBroadcastingImage(int intervalMS, int startDelayMS) {

        File imagePath = storageDirectory.resolve("sample_image.jpg").toFile();

        if (!imagePath.exists()) {
            throw new RuntimeException("Image path " + imagePath.getAbsolutePath() + " does not exist!");
        }
        final String sampleImagePath = imagePath.getAbsolutePath();

        Thread t = new Thread() {
            public void run() {
                if (currentLocation != null) {
                    saCommunicationService.handleActionSendImage(sampleImagePath, currentLocation);
                }

                try {
                    Thread.sleep(startDelayMS);

                    while (true) {
                        if (currentLocation != null) {
                            saCommunicationService.handleActionSendImage(sampleImagePath, currentLocation);
                        }
                        Thread.sleep(intervalMS);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        };

        Analytics.registerThread(t);
        t.start();
    }

    public synchronized void setCurrentLocation(Coordinates coordinates) {
        currentLocation = coordinates;
    }

}
