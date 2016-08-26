package com.bbn.ataklite.testhelpers;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import com.bbn.ataklite.service.SACommunicationService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by awellman@bbn.com on 2/1/16.
 */
public class TestEventBroadcaster {

    public enum EventType {
        IMAGE
    }

    // Rough mostly-continental chunk of the US
    public static final double MAX_RANDOM_LAT = 48.562068;
    public static final double MIN_RANDOM_LAT = 30.755641;
    public static final double MAX_RANDOM_LON = -81.490127;
    public static final double MIN_RANDOM_LON = -122.387100;

    private final Timer timer = new Timer();

    private final Map<EventType, Set<TimerTask>> eventTypeTaskMap = new HashMap<>();

    public TestEventBroadcaster() {
    }

    public synchronized void startBroadcastingImage(final Context context, int intervalMS, int startDelayMS) {

        writeSampleImageFileIfNecessary(context);

        final String sampleImagePath = (new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "sample_image.jpg")).getAbsolutePath();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Location location = new Location("zyx");

                location.setLatitude(ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LAT, MAX_RANDOM_LAT));
                location.setLongitude(ThreadLocalRandom.current().nextDouble(MIN_RANDOM_LON, MAX_RANDOM_LON));

                SACommunicationService.startActionSendImage(context, location, sampleImagePath);
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
