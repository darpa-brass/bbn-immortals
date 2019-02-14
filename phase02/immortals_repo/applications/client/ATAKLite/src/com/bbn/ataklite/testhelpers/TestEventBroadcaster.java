package com.bbn.ataklite.testhelpers;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import com.bbn.ataklite.service.SACommunicationService;
import mil.darpa.immortals.datatypes.Coordinates;
import mil.darpa.immortals.javatypeconverters.CoordinateLocationConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by awellman@bbn.com on 2/1/16.
 */
public class TestEventBroadcaster {

    public enum EventType {
        IMAGE
    }

    private Coordinates currentLocation = null;

    private final Timer timer = new Timer();

    private final Map<EventType, Set<TimerTask>> eventTypeTaskMap = new HashMap<>();

    public TestEventBroadcaster() {
    }

    public synchronized void startBroadcastingImage(final Context context, int intervalMS, int startDelayMS) {

        writeSampleImageFileIfNecessary(context);

        File imagePath = new File("/sdcard/ataklite/sample_image.jpg");

        if (!imagePath.exists()) {
            imagePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "sample_image.jpg").getAbsoluteFile();
        }
        final String sampleImagePath = imagePath.getAbsolutePath();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (currentLocation != null) {

                    Location location = CoordinateLocationConverter.toLocation(currentLocation);
                    SACommunicationService.startActionSendImage(context, location, sampleImagePath);
                }
            }
        };

        Set<TimerTask> taskSet = eventTypeTaskMap.get(EventType.IMAGE);

        if (taskSet == null) {
            taskSet = new HashSet<>();
            eventTypeTaskMap.put(EventType.IMAGE, taskSet);
        }

        taskSet.add(task);

        timer.schedule(task, startDelayMS, intervalMS);
    }

    public synchronized void setCurrentLocation(Coordinates coordinates) {
        currentLocation = coordinates;
    }

    public synchronized void stopAllTasksOfType(EventType eventType) {
        Set<TimerTask> taskSet = eventTypeTaskMap.get(eventType);

        if (taskSet != null) {
            for (TimerTask task : taskSet) {
                task.cancel();
            }
            taskSet.clear();
        }
    }

    private void writeSampleImageFileIfNecessary(Context context) {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File f = new File(path, "sample_image.jpg");

        if (!f.exists()) {
            try {
                InputStream fis = context.getAssets().open("test/sample_image.jpg");
                byte[] fileBytes = new byte[fis.available()];
                fis.read(fileBytes);
                fis.close();

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(fileBytes);
                fos.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
