package mil.darpa.immortals.flitcons.datatypes.dynamic;

import com.google.gson.*;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DynamicObjectContainerDeserializer implements JsonDeserializer<DynamicObjectContainer> {
	@Override
	public DynamicObjectContainer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		TreeMap<String, DynamicValue> values = new TreeMap<>();

		if (json instanceof JsonObject) {
			JsonObject jo = json.getAsJsonObject();
			Set<Map.Entry<String, JsonElement>> entrySet = jo.entrySet();

			for (Map.Entry<String, JsonElement> entry : entrySet) {
				values.put(entry.getKey(), context.deserialize(entry.getValue(), DynamicValue.class));
			}

			return new DynamicObjectContainer(HierarchicalIdentifier.UNDEFINED, values);

		} else {
			throw new RuntimeException("DynamicObjectContainer JSON root must be a JsonObject!");
		}
	}
}
