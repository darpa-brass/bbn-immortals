package mil.darpa.immortals.flitcons.datatypes.dynamic;

import com.google.gson.*;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static mil.darpa.immortals.flitcons.Utils.GLOBALLY_UNIQUE_ID;

public class DynamicObjectContainerDeserializer implements JsonDeserializer<DynamicObjectContainer> {
	@Override
	public DynamicObjectContainer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		try {
			TreeMap<String, DynamicValue> values = new TreeMap<>();

			if (json instanceof JsonObject) {
				JsonObject jo = json.getAsJsonObject();
				Set<Map.Entry<String, JsonElement>> entrySet = jo.entrySet();

				for (Map.Entry<String, JsonElement> entry : entrySet) {
					values.put(entry.getKey(), context.deserialize(entry.getValue(), DynamicValue.class));
				}

				if (values.containsKey(GLOBALLY_UNIQUE_ID)) {
					return new DynamicObjectContainer(HierarchicalIdentifier.produceTraceableNode(values.get(GLOBALLY_UNIQUE_ID).parseString(), null), values);
				} else {
					return new DynamicObjectContainer(HierarchicalIdentifier.createBlankNode(), values);
				}

			} else {
				throw AdaptationnException.internal("DynamicObjectContainer JSON root must be a JsonObject!");
			}
		} catch (DynamicValueeException e) {
			throw AdaptationnException.internal(e);
		}
	}
}
