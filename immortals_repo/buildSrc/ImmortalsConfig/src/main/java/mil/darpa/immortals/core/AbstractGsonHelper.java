package mil.darpa.immortals.core;

import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by awellman@bbn.com on 6/18/18.
 */
public class AbstractGsonHelper {

    public static class PathDeserializer implements JsonDeserializer<Path> {
        @Override
        public Path deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Paths.get(jsonElement.getAsString());
        }
    }

    public static class FileDeserializer implements JsonDeserializer<File> {
        @Override
        public File deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return new File(jsonElement.getAsString());
        }
    }

    protected static GsonBuilder getGsonBuilder() {
        return new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(Path.class, new PathDeserializer())
                .registerTypeAdapter(File.class, new FileDeserializer());
    }
}
