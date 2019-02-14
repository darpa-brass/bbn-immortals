package com.bbn.ataklite;

import com.bbn.ataklite.entities.MonitoredEntity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

/**
 * Created by awellman@bbn.com on 2/9/16.
 */
public class GsonHelper {

    public static Gson createGsonInstance() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(MonitoredEntityMap.class, new MonitoredEntity.MonitoredEntityMapSerializer());
        builder.registerTypeAdapter(MonitoredEntityMap.class, new MonitoredEntity.MonitoredEntityMapDeserializer());
        builder.registerTypeAdapter(MonitoredEntity.class, new MonitoredEntity.MonitoredEntityMapSerializer());
        builder.registerTypeAdapter(MonitoredEntity.class, new MonitoredEntity.MonitoredEntityMapDeserializer());
        return builder.create();
    }

    // The Serializer and Deserializer for this are within the MonitoredEntity class for access to protected fields
    public static class MonitoredEntityMap extends HashMap<String, MonitoredEntity> {
    }
}
