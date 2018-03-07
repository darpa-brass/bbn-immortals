package mil.darpa.immortals.das.configuration;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import mil.darpa.immortals.das.sourcecomposer.configuration.paradigms.GenericParameterConfiguration;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by awellman@bbn.com on 6/6/16.
 */
public class GsonHelper {

    private static GsonHelper instance;

    private final Gson gson;

    private final Gson plainGson;

    private GsonHelper() {
        GsonBuilder builder = new GsonBuilder();
//        builder.registerTypeAdapter(ExecutionDataLinkedList.class, new ExecutionDataLinkedListSerializer());
//        builder.registerTypeAdapter(ExecutionDataset.class, new ExecutionDatasetSerializer());
//        builder.registerTypeAdapter(AnalysisModuleConfigurationList.class, new AnalysisModuleConfigurationListDeserializer());
        builder.registerTypeAdapter(GenericParameterConfiguration.class, new GenericParameterConfigurationDeserializer());
        builder.setPrettyPrinting();
        gson = builder.create();
        plainGson = new Gson();
    }

    public static GsonHelper getInstance() {
        if (instance == null) {
            instance = new GsonHelper();
        }
        return instance;
    }

    public synchronized <T> T fromFilepath(String filepath, Class<T> classOfT) throws IOException {
        FileReader fr = new FileReader(new File(filepath));

        T t = gson.fromJson(fr, classOfT);
        fr.close();
        return t;
    }

    public synchronized <T> T fromResourceIdentifier(String resourceIdentifier, Class<T> classOfT) {
        InputStream is = GsonHelper.class.getResourceAsStream(resourceIdentifier);
        InputStreamReader isr = new InputStreamReader(is);
        return gson.fromJson(isr, classOfT);
    }

    public synchronized <T> List<T> listFromResourceIdentifier(String resourceIdentifier, Class<T> classOfT) {
        List<T> returnList = new LinkedList<>();

        InputStream is = GsonHelper.class.getResourceAsStream(resourceIdentifier);
        InputStreamReader isr = new InputStreamReader(is);

        JsonArray ja = gson.fromJson(isr, JsonArray.class);
        for (JsonElement je : ja) {
            T object = gson.fromJson(je, classOfT);
            returnList.add(object);
        }
        return returnList;
    }
    
    public synchronized <T> T fromJsonObject(JsonObject jsonObject, Class<T> classOfT) {
        return gson.fromJson(jsonObject, classOfT);
    }

    public synchronized <T> T fromInputStream(InputStream inputStream, Class<T> classOfT) throws IOException {
        return fromInputStream(inputStream, classOfT, null);
    }

    public synchronized <T> T fromInputStream(InputStream inputStream, Class<T> classOfT, @Nullable Map<String, Object> replacementMap) throws IOException {
        JsonReader jr = new JsonReader(new InputStreamReader(inputStream));
        T t;

        if (replacementMap == null) {
            t = gson.fromJson(jr, classOfT);
        } else {
            JsonElement je = gson.fromJson(jr, JsonElement.class);
            JsonObject jo = je.getAsJsonObject();

            for (String key : replacementMap.keySet()) {
                String[] keyPath = key.split("\\.");
                Iterator<String> it = Arrays.asList(keyPath).iterator();

                JsonElement currentElement = jo;


                do {
                    String str = it.next();


                    if (currentElement.isJsonArray()) {
                        currentElement = ((JsonArray) currentElement).get(Integer.parseInt(str));

                    } else if (currentElement.isJsonObject()) {
                        if (it.hasNext()) {
                            currentElement = ((JsonObject) currentElement).get(str);

                        } else if (replacementMap.get(key) instanceof String) {
                            ((JsonObject) currentElement).addProperty(str, (String) replacementMap.get(key));

                        } else {
                            throw new RuntimeException("Only strings supported currently!");
                        }
                    }
                } while (it.hasNext());
            }

            t = gson.fromJson(je, classOfT);

        }

        jr.close();

        return t;
    }

    public synchronized JsonElement toJsonElement(Object src) {
        return gson.toJsonTree(src);
    }

    public synchronized void toFile(Object src, String filepath) throws IOException {
        FileWriter fw = new FileWriter(new File(filepath));
        gson.toJson(src, fw);
        fw.flush();
        fw.close();
    }

    public static class GenericParameterConfigurationDeserializer implements JsonDeserializer<GenericParameterConfiguration> {

        @Override
        public GenericParameterConfiguration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jo = (JsonObject) json;
            JsonElement valueElement = jo.get("value");
            if (valueElement != null && valueElement.isJsonPrimitive()) {
                JsonArray ja = new JsonArray();
                ja.add(valueElement);
                jo.add("value", ja);
            }
            return GsonHelper.getInstance().plainGson.fromJson(json, GenericParameterConfiguration.class);
        }
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
//
//
//    public interface Pair<LeftType, RightType> {
//        public LeftType getLeft();
//
//        public RightType getRight();
//    }
//
//    public static class StringPair implements Pair<String, String> {
//        String left;
//        String right;
//
//        public String getLeft() {
//            return left;
//        }
//
//        public String getRight() {
//            return right;
//        }
//    }
//
//    public class PairList extends LinkedList<Pair> {
//
//    }
//
//    public static class DfuCompositionConfigurationDeserializer implements JsonDeserializer<DfuCompositionConfiguration> {
//
//        @Override
//        public DfuCompositionConfiguration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//            return null;
//        }
//    }
}

