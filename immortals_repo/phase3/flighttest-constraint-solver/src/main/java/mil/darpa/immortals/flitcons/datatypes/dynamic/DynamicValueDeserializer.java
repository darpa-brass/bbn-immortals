package mil.darpa.immortals.flitcons.datatypes.dynamic;

import com.google.gson.*;
import mil.darpa.immortals.flitcons.NestedPathException;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.HierarchicalIdentifier;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

import static mil.darpa.immortals.flitcons.Utils.GLOBALLY_UNIQUE_ID;

public class DynamicValueDeserializer implements JsonDeserializer<DynamicValue> {

	private static DynamicValue getDynamicValue(JsonElement element) throws NestedPathException {
		if (element.isJsonPrimitive()) {
			Object value = getSinglePrimitive(element);
			if (value instanceof Range) {
				return new DynamicValue((Range) value, null, null);
			} else if (value instanceof Equation) {
				return new DynamicValue(null, null, null, value);
			} else {
				return new DynamicValue(null, null, getSinglePrimitive(element));
			}

		} else if (element.isJsonObject()) {
			Object innerValue = getSinglePrimitive(element);
			if (innerValue instanceof Range) {
				return new DynamicValue((Range) innerValue, null, null);
			} else if (innerValue instanceof Equation) {
				return new DynamicValue(null, null, innerValue);
			} else {
				return new DynamicValue(null, null, innerValue);
			}

		} else if (element.isJsonArray()) {
			JsonArray ja = element.getAsJsonArray();
			int len = ja.size();
			Object[] val = new Object[len];

			for (int i = 0; i < len; i++) {
				JsonElement e = ja.get(i);
				val[i] = getSinglePrimitive(e);
			}
			return new DynamicValue(null, val, null);

		} else {
			throw AdaptationnException.internal("Unexpected Json element class '" + element.getClass().toString() + "!");
		}
	}

	private static Object getSinglePrimitive(JsonElement value) throws NestedPathException {

		if (value.isJsonObject()) {
			JsonObject jo = value.getAsJsonObject();

			if (jo.has("Min") && jo.has("Max") && jo.entrySet().size() == 2) {
				return new Range(
						getSinglePrimitive(jo.get("Min").getAsJsonPrimitive()),
						getSinglePrimitive(jo.get("Max").getAsJsonPrimitive()));

			} else if (jo.has("Equation")) {
				return new Equation(jo.get("Equation").getAsString());

			} else {
				TreeMap<String, DynamicValue> containerData = new TreeMap<>();

				for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
					containerData.put(entry.getKey(), getDynamicValue(entry.getValue()));
				}

				if (containerData.containsKey(GLOBALLY_UNIQUE_ID)) {
					return new DynamicObjectContainer(HierarchicalIdentifier.produceTraceableNode(containerData.get(GLOBALLY_UNIQUE_ID).parseString(), null), containerData);
				} else {
					return new DynamicObjectContainer(HierarchicalIdentifier.createBlankNode(), containerData);
				}
			}

		} else if (value.isJsonPrimitive()) {
			JsonPrimitive val = value.getAsJsonPrimitive();
			if (val.isNumber()) {
				Number n = value.getAsNumber();
				if (n.toString().contains(".")) {
					return value.getAsFloat();
				} else {
					return value.getAsLong();
				}

			} else if (val.isString()) {
				return value.getAsString();

			} else if (val.isBoolean()) {
				return value.getAsBoolean();

			} else {
				throw AdaptationnException.internal("Unexpected primitive type!");
			}
		} else {
			throw AdaptationnException.internal("Unexpected Json element class '" + value.getClass().toString() + "!");
		}
	}

	@Override
	public DynamicValue deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		try {
			return getDynamicValue(json);
		} catch (NestedPathException e) {
			throw AdaptationnException.internal(e);
		}
	}
}
