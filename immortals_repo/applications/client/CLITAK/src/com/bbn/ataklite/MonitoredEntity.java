package com.bbn.ataklite;

import com.bbn.ataklite.GsonHelper.MonitoredEntityMap;
import com.google.gson.*;
import com.google.gson.annotations.Expose;
import mil.darpa.immortals.datatypes.Coordinates;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by awellman@bbn.com on 2/4/16.
 */
public class MonitoredEntity {
    public static final String KEY_CALLSIGN = "callsign";
    protected static final String KEY_LOCATION = "location";
    protected static final String KEY_IMAGE_URL = "imageUrl";

    @Expose
    protected final LinkedList<Coordinates> userTracks;

    @Expose
    protected final TreeMap<String, Coordinates> userLocationImages;

    @Expose
    private final String callsign;

    public MonitoredEntity(String callsign) {
        this.callsign = callsign;
        userTracks = new LinkedList<>();
        userLocationImages = new TreeMap<>();
    }

    public String getCallsign() {
        return callsign;
    }

    public int getPictureUploadCount() {
        return userLocationImages.size();
    }

    public int getLocationTrackCount() {
        return userTracks.size();
    }

    public synchronized void addImageUrl(@Nonnull String imageUrl, @Nonnull Coordinates location) {
        userLocationImages.put(imageUrl, location);
    }

    public synchronized void updateLocation(@Nonnull Coordinates coordinates) {
        userTracks.add(coordinates);
    }

    public static class MonitoredEntitySerializer implements JsonSerializer<MonitoredEntity> {

        @Override
        public JsonElement serialize(MonitoredEntity src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray dataArray = new JsonArray();

            String callsign = src.callsign;
            LinkedList<Coordinates> locationLinkedList = src.userTracks;
            TreeMap<String, Coordinates> locationImageMap = src.userLocationImages;

            for (Coordinates location : locationLinkedList) {
                JsonObject obj = new JsonObject();
                obj.addProperty(KEY_CALLSIGN, callsign);
                obj.add(KEY_LOCATION, context.serialize(location, Coordinates.class));
                dataArray.add(obj);
            }

            for (String imageUrl : locationImageMap.keySet()) {
                Coordinates location = locationImageMap.get(imageUrl);
                JsonObject obj = new JsonObject();
                obj.addProperty(KEY_CALLSIGN, callsign);
                obj.add(KEY_LOCATION, context.serialize(location, Coordinates.class));
                obj.addProperty(KEY_IMAGE_URL, imageUrl);
                dataArray.add(obj);
            }

            return dataArray;
        }
    }

    public static class MonitoredEntityDeserializer implements JsonDeserializer<MonitoredEntity> {

        @Override
        public MonitoredEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            throw new RuntimeException("Cannot deserialize a single MonitoredEntity! Please deserialize a MonitoredEntityMap instead!");
        }
    }

    public static class MonitoredEntityMapSerializer implements JsonSerializer<MonitoredEntityMap> {

        @Override
        public JsonElement serialize(MonitoredEntityMap src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray dataArray = new JsonArray();

            Set<String> callsigns = src.keySet();

            for (String callsign : callsigns) {
                MonitoredEntity entity = src.get(callsign);

                JsonArray monitoredEntityJson = (JsonArray) context.serialize(entity, MonitoredEntity.class);
                dataArray.addAll(monitoredEntityJson);
            }
            return dataArray;
        }
    }

    public static class MonitoredEntityMapDeserializer implements JsonDeserializer<MonitoredEntityMap> {

        @Override
        public MonitoredEntityMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            MonitoredEntityMap entityMap = new MonitoredEntityMap();

            if (json instanceof JsonArray) {

                JsonArray jsonArray = (JsonArray) json;
                for (JsonElement element : jsonArray) {

                    if (element instanceof JsonObject) {
                        JsonObject obj = (JsonObject) element;

                        String callsign = obj.has(KEY_CALLSIGN) ? obj.get(KEY_CALLSIGN).getAsString() : null;
                        String imageUrl = obj.has(KEY_IMAGE_URL) ? obj.get(KEY_IMAGE_URL).getAsString() : null;
                        Coordinates location = obj.has(KEY_LOCATION) ? (Coordinates) context.deserialize(obj.get(KEY_LOCATION), Coordinates.class) : null;

                        if (callsign != null) {
                            MonitoredEntity entity;
                            if (entityMap.containsKey(callsign)) {
                                entity = entityMap.get(callsign);

                            } else {
                                entity = new MonitoredEntity(callsign);
                                entityMap.put(callsign, entity);
                            }


                            if (location != null) {
                                if (imageUrl != null) {
                                    entity.userLocationImages.put(imageUrl, location);

                                } else {
                                    entity.userTracks.add(location);
                                }

                            } else {
                                throw new JsonParseException("A MonitoredEntity entry must contain a location!");
                            }


                        } else {
                            throw new JsonParseException("A MonitoredEntity entry must contain a callsign!");
                        }


                    } else {
                        throw new JsonParseException("A MonitoredEntity array only contains objects!");
                    }
                }

            } else {
                throw new JsonParseException("A MonitoredEntity file must contain an array of entries!");
            }

            return entityMap;
        }
    }
}
