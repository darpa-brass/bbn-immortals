package mil.darpa.immortals.das.configuration;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 6/6/16.
 */
public class GsonHelper {

    private static GsonHelper instance;

    private final Gson gson;

    private GsonHelper() {
        GsonBuilder builder = new GsonBuilder();
//        builder.registerTypeAdapter(ExecutionDataLinkedList.class, new ExecutionDataLinkedListSerializer());
//        builder.registerTypeAdapter(ExecutionDataset.class, new ExecutionDatasetSerializer());
//        builder.registerTypeAdapter(AnalysisModuleConfigurationList.class, new AnalysisModuleConfigurationListDeserializer());
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

    public synchronized <T> T fromInputStream(InputStream inputStream, Class<T> classOfT) throws IOException {
        JsonReader jr = new JsonReader(new InputStreamReader(inputStream));
        T t = gson.fromJson(jr, classOfT);
        jr.close();
        return t;
    }

    public synchronized <T> void toFile(Object src, String filepath) throws IOException {
        FileWriter fw = new FileWriter(new File(filepath));
        gson.toJson(src, fw);
        fw.flush();
        fw.close();
    }

//    public static class ExecutionDataLinkedList extends LinkedList<ExecutionData> {
//
//    }
//
//    public static class ExecutionDataLinkedListSerializer implements JsonSerializer<ExecutionDataLinkedList> {
//
//        @Override
//        public JsonElement serialize(ExecutionDataLinkedList src, Type typeOfSrc, JsonSerializationContext context) {
//            Type type = new TypeToken<LinkedList<ExecutionData>>() {
//            }.getType();
//            return context.serialize(src, type);
//        }
//    }
//
//    public static class ExecutionDataset extends HashMap<HashMap<String, String>, ExecutionData> {
//
//    }
//
//    public static class ExecutionDatasetSerializer implements JsonSerializer<ExecutionDataset> {
//
//        @Override
//        public JsonElement serialize(ExecutionDataset src, Type typeOfSrc, JsonSerializationContext context) {
//            Type type = new TypeToken<HashMap<HashMap<String, String>, ExecutionData>>() {
//            }.getType();
//            return context.serialize(src, type);
//
//        }
//    }
//
//    public static class AnalysisModuleConfigurationList extends ArrayList<AnalysisModuleConfiguration> {
//
//    }
//
//    public static class AnalysisModuleConfigurationListDeserializer implements JsonDeserializer<ArrayList<AnalysisModuleConfiguration>> {
//
//        @Override
//        public ArrayList<AnalysisModuleConfiguration> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//            Type type = new TypeToken<ArrayList<AnalysisModuleConfiguration>>() {
//            }.getType();
//
//            return context.deserialize(json, type);
//        }
//    }


    public interface Pair<LeftType, RightType> {
        public LeftType getLeft();

        public RightType getRight();
    }

    public static class StringPair implements Pair<String, String> {
        String left;
        String right;

        public String getLeft() {
            return left;
        }

        public String getRight() {
            return right;
        }
    }

    public class PairList extends LinkedList<Pair> {

    }

    public static class DfuCompositionConfigurationDeserializer implements JsonDeserializer<DfuCompositionConfiguration> {

        @Override
        public DfuCompositionConfiguration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return null;
        }
    }
}

