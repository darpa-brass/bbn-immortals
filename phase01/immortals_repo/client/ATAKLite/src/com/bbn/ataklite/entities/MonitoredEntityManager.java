package com.bbn.ataklite.entities;

import android.location.Location;
import android.os.Environment;
import com.bbn.ataklite.ATAKLiteConfig;
import com.bbn.ataklite.GsonHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonWriter;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;
import mil.darpa.immortals.dfus.LatestSaFileByteReader;
import mil.darpa.immortals.dfus.LatestSaFileByteWriter;
import mil.darpa.immortals.dfus.compression.GzipCompressor;
import mil.darpa.immortals.dfus.crypto.jca.JcaAesEncryptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 2/4/16.
 */
public class MonitoredEntityManager {

    private transient final MonitoredEntity myself;

    private final GsonHelper.MonitoredEntityMap monitoredEntityMap = new GsonHelper.MonitoredEntityMap();

    private transient final LinkedList<MonitoredEntity> entityList = new LinkedList<>();

    private transient final HashSet<EntityChangeListener> listeners = new HashSet<>();

    private transient final Gson gson;

    private JsonWriter logWriter;

    private boolean logSelf;

    private boolean logOthers;

    public MonitoredEntityManager(@Nonnull String myCallsign, @Nullable ATAKLiteConfig config) {
        MonitoredEntity nonFinalSelf = null;

        gson = GsonHelper.createGsonInstance();

        if (config != null && config.locationLogExternalStoragePath != null) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + config.locationLogExternalStoragePath);
            try {
                if (file.exists() && file.length() > 0 && (config.loadReceivedLocationUpdatesFromLog || config.loadOwnLocationUpdatesFromLog)) {

                    String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + config.locationLogExternalStoragePath;

                    // c0ntrolp0int - LogReader
                    // A Java InputStreamReader reads a stream from the disk
                    InputStreamReader reader = new InputStreamReader(
                            // Using the DFU to read the file and determine the control point
                            new LatestSaFileByteReader(
                                    // using the specified filepath
                                    filepath
                            )

                    );

//                    // c0ntrolp0int - LogReader
//                    // A Java InputStreamReader reads a stream from the disk
//                    InputStreamReader reader = new InputStreamReader(
//                            // Using a DFU decompressor
//                            new GzipDecompressor(
//                                    // Using a decryptor
//                                    new JcaAesDecryptor(
//                                            // With the key
//                                            "fGPgAeiHCivZqzJnESrasQ==",
//                                            // And the file reader DFU
//                                            new LatestSaFileByteReader(
//                                                    // With the specified filepath
//                                                    filepath
//                                            )
//
//                                    )
//                            )
//                    );

                    GsonHelper.MonitoredEntityMap readmonitoredEntityMap = gson.fromJson(reader, GsonHelper.MonitoredEntityMap.class);
                    reader.close();


                    for (String callsign : readmonitoredEntityMap.keySet()) {
                        if (callsign.equals(myCallsign) && config.loadOwnLocationUpdatesFromLog) {
                            nonFinalSelf = readmonitoredEntityMap.get(callsign);
                            monitoredEntityMap.put(callsign, nonFinalSelf);

                        } else if (config.loadReceivedLocationUpdatesFromLog) {
                            MonitoredEntity entity = readmonitoredEntityMap.get(callsign);
                            entityList.add(entity);
                            monitoredEntityMap.put(callsign, entity);
                        }
                    }
                }
                // Ignore since that just means there is no history
            } catch (IOException | JsonParseException e) {

            }

            logSelf = config.logOwnLocationUpdates;
            logOthers = config.logReceivedLocationUpdates;

            try {
                // TODO: Don't clobber existing file, Add existing data to new file?
                if (logSelf || logOthers) {

                    String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + config.locationLogExternalStoragePath;

                    // c0ntrolp0int - LogWriter
                    // The JsonWriter
                    logWriter = new JsonWriter(
                            // Takes an OutputStreamWriter
                            new OutputStreamWriter(
                                    // That uses a DFU to write the data
                                    new LatestSaFileByteWriter(
                                            // To the specified filepath
                                            filepath
                                    )
                            )
                    );

//                    // c0ntrolp0int - LogWriter
//                    // The JsonWriter
//                    logWriter = new JsonWriter(
//                            // Takes an OutputStreamWriter
//                            new OutputStreamWriter(
//                                    // Connected to a Compressor
//                                    new GzipCompressor(
//                                            //  Connected to an Encryptor
//                                            new JcaAesEncryptor(
//                                                    // With the specified key
//                                                    "fGPgAeiHCivZqzJnESrasQ==",
//                                                    // That uses a DFU to write the data
//                                                    new LatestSaFileByteWriter(
//                                                            // With the specified filepath
//                                                            filepath
//                                                    )
//
//                                            )
//                                    )
//                            )
//                    );

                    logWriter.beginArray();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (nonFinalSelf == null) {
            myself = new MonitoredEntity(myCallsign);
        } else {
            myself = nonFinalSelf;
        }
    }


    public synchronized void addOrUpdateExternalEntityLocation(@Nonnull String identifier, @Nonnull Location newLocation) {
        MonitoredEntity entity = monitoredEntityMap.get(identifier);

        if (entity == null) {
            entity = new MonitoredEntity(identifier);
            monitoredEntityMap.put(identifier, entity);
            entityList.add(entity);
        }

        entity.updateLocation(newLocation);

        Analytics.log(Analytics.newEvent(AnalyticsEventType.FieldLocationUpdated, identifier, newLocation));

        for (EntityChangeListener listener : listeners) {
            listener.onExternalEntityLocationAddedOrChanged(identifier, newLocation);
        }

        if (logOthers) {
            JsonObject obj = new JsonObject();
            obj.addProperty(MonitoredEntity.KEY_CALLSIGN, identifier);
            obj.add(MonitoredEntity.KEY_LOCATION, gson.toJsonTree(newLocation, Location.class));
            gson.toJson(obj, logWriter);
        }
    }


    public synchronized void addOrUpdateExternalEntityImages(@Nonnull String identifier, @Nonnull Location newLocation, @Nonnull String imageUrl) {
        MonitoredEntity entity = monitoredEntityMap.get(identifier);

        if (entity == null) {
            entity = new MonitoredEntity(identifier);
            monitoredEntityMap.put(identifier, entity);
            entityList.add(entity);

        }

        entity.addImageUrl(imageUrl, newLocation);

        Analytics.log((Analytics.newEvent(AnalyticsEventType.ImageReceived, identifier, newLocation)));


        for (EntityChangeListener listener : listeners) {
            listener.onExternalEntityImageAdded(identifier, newLocation, imageUrl);
        }

        if (logOthers) {
            JsonObject obj = new JsonObject();
            obj.addProperty(MonitoredEntity.KEY_CALLSIGN, identifier);
            obj.addProperty(MonitoredEntity.KEY_IMAGE_URL, imageUrl);
            obj.add(MonitoredEntity.KEY_LOCATION, gson.toJsonTree(newLocation, Location.class));
            gson.toJson(obj, logWriter);
        }
    }

    public synchronized void updateMyLocation(@Nonnull Location newLocation) {
        myself.updateLocation(newLocation);

        Analytics.log(Analytics.newEvent(AnalyticsEventType.MyLocationUpdated, Analytics.getOwnSourceIdentifier(), newLocation));

        for (EntityChangeListener listener : listeners) {
            listener.onMyLocationChanged(newLocation);
        }

        if (logSelf) {
            JsonObject obj = new JsonObject();
            obj.addProperty(MonitoredEntity.KEY_CALLSIGN, myself.getCallsign());
            obj.add(MonitoredEntity.KEY_LOCATION, gson.toJsonTree(newLocation, Location.class));
            gson.toJson(obj, logWriter);
        }
    }

    public void addEntityChangeListener(EntityChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeEntityChangeListener(EntityChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public LinkedList<MonitoredEntity> getEntityList() {
        return entityList;
    }

    public void shutdown() {
        // Shutting down, so probably shouldn't bomb out if this fails...
        try {
            logWriter.endArray();
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
