package mil.darpa.immortals.flitcons.datatypes.dynamic;

import com.google.gson.*;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import java.lang.reflect.Type;

public class DynamicValueSerializer implements JsonSerializer<DynamicValue> {

	private static JsonElement getSingleJsonPrimitive(Object value, JsonSerializationContext context) {
		if (value instanceof String) {
			return new JsonPrimitive((String) value);
		} else if (value instanceof Long) {
			return new JsonPrimitive((Long) value);
		} else if (value instanceof Float) {
			return new JsonPrimitive((Float) value);
		} else if (value instanceof Boolean) {
			return new JsonPrimitive((Boolean) value);
		} else if (value instanceof Enum) {
			return new JsonPrimitive(((Enum) value).name());
		} else if (value instanceof Equation) {
			JsonObject jo = new JsonObject();
			jo.addProperty("Equation", ((Equation)value).Equation);
			return jo;
		} else if (value instanceof DynamicValue) {
			return context.serialize(value);
		} else if (value instanceof DynamicObjectContainer) {
			return context.serialize(value);
		}
		throw AdaptationnException.internal("Unexpected value type of '" + value.getClass().getName() + "'!");
	}

	@Override
	public JsonElement serialize(DynamicValue src, Type typeOfSrc, JsonSerializationContext context) {
		if (src.singleValue != null) {
			return getSingleJsonPrimitive(src.singleValue, context);

		} else if (src.valueArray != null) {
			JsonArray rval = new JsonArray();
			for (Object val : src.valueArray) {
				rval.add(getSingleJsonPrimitive(val, context));
			}
			return rval;

		} else if (src.range.Min != null && src.range.Max != null) {
			JsonObject jo = new JsonObject();
			jo.add("Min", getSingleJsonPrimitive(src.range.Min, context));
			jo.add("Max", getSingleJsonPrimitive(src.range.Max, context));
			return jo;

		} else {
			throw AdaptationnException.internal("Transformation not supported!");
		}
	}
}
