package mil.darpa.immortals.core.api.ll.phase2.result;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Directly using a "LinkedList<TestDetails>" object type as the body in REST bodies has issues.
 * This also adds a timestamp
 */
public class AdaptationDetailsList extends LinkedList<AdaptationDetails> {
    
    public int sequence = -1;

    
    public AdaptationDetailsList() {
        super();
    }

    public AdaptationDetailsList(Collection<AdaptationDetails> adaptationDetails) {
        super(adaptationDetails);
    }

    private AdaptationDetailsList(int sequence, Collection<AdaptationDetails> adaptationDetails) {
        super(adaptationDetails);
        this.sequence = sequence;
    }

    public static class AdaptationDetailsListDeserializer implements JsonDeserializer<AdaptationDetailsList> {

        @Override
        public AdaptationDetailsList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jo = (JsonObject) json;
            Type type = new TypeToken<LinkedList<AdaptationDetails>>() {
            }.getType();
            return new AdaptationDetailsList(
                    jo.get("sequence").getAsInt(),
                    context.deserialize(jo.getAsJsonArray("values"), type));
        }
    }


    public static class AdaptationDetailsListSerializer implements JsonSerializer<AdaptationDetailsList> {

        @Override
        public JsonElement serialize(AdaptationDetailsList src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject rval = new JsonObject();
            rval.addProperty("sequence", src.sequence);
            JsonArray ja = new JsonArray();

            for (AdaptationDetails td : src) {
                ja.add(context.serialize(td));
            }
            rval.add("values", ja);
            return rval;
        }
    }
}
