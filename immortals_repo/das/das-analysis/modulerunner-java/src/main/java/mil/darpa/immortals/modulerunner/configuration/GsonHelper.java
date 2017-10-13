package mil.darpa.immortals.modulerunner.configuration;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import mil.darpa.immortals.modulerunner.reporting.ExecutionData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 6/6/16.
 */
public class GsonHelper {

    private static GsonHelper instance;

    private final Gson gson;

    private GsonHelper() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ExecutionDataLinkedList.class, new ExecutionDataLinkedListSerializer());
        builder.registerTypeAdapter(ExecutionDataset.class, new ExecutionDatasetSerializer());
        builder.setPrettyPrinting();
        gson = builder.create();
    }

    public static GsonHelper getInstance() {
        if (instance == null) {
            instance = new GsonHelper();
        }
        return instance;
    }

    public synchronized <T> T fromFile(String filepath, Class<T> classOfT) throws IOException {
        FileReader fr = new FileReader(new File(filepath));
        T t = gson.fromJson(fr, classOfT);
        fr.close();
        return t;
    }

    public synchronized <T> void toFile(Object src, String filepath) throws IOException {
        FileWriter fw = new FileWriter(new File(filepath));
        gson.toJson(src, fw);
        fw.flush();
        fw.close();
    }

    public static class ExecutionDataLinkedList extends LinkedList<ExecutionData> {

    }

    public static class ExecutionDataLinkedListSerializer implements JsonSerializer<ExecutionDataLinkedList> {

        @Override
        public JsonElement serialize(ExecutionDataLinkedList src, Type typeOfSrc, JsonSerializationContext context) {
            Type type = new TypeToken<LinkedList<ExecutionData>>() {
            }.getType();
            return context.serialize(src, type);
        }
    }

    public static class ExecutionDataset extends HashMap<HashMap<String, String>, ExecutionData> {

    }

    public static class ExecutionDatasetSerializer implements JsonSerializer<ExecutionDataset> {

        @Override
        public JsonElement serialize(ExecutionDataset src, Type typeOfSrc, JsonSerializationContext context) {
            Type type = new TypeToken<HashMap<HashMap<String, String>, ExecutionData>>() {
            }.getType();
            return context.serialize(src, type);

        }
    }
}

