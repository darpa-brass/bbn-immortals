package mil.darpa.immortals.flitcons.solvers.dsl;


import com.google.gson.*;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class DslValueObject {

	DslValueObject(@Nullable Integer Min, @Nullable Integer Max, @Nullable Integer[] valueList, @Nullable Integer value) {
		this.Min = Min;
		this.Max = Max;
		this.valueList = valueList;
		this.value = value;
		String err = validateAndReturnError();
		if (err != null) {
			throw new RuntimeException(err);
		}
	}


	public Integer Min;
	public Integer Max;
	public Integer[] valueList;
	public Integer value;


	@Nullable
	private String validateAndReturnError() {

		if (Min != null || Max != null) {
			if (Min == null) {
				return "Value found with Max defined but not Min!";

			} else if (Max == null) {
				return "Value found with Min defined but not Max!";

			} else if (valueList != null) {
				return ("Value found with Min/Max defined and valueList defined!");

			} else if (value != null) {
				return ("Value found with Min/Max defined and value defined!");

			} else {
				return null;
			}

		} else if (valueList != null) {
			if (value != null) {
				return ("Value found with valueList and value defined!");
			} else {
				return null;
			}
		} else {
			return null;
		}

	}

//	public static class DslValueObjectMdlSerializer implements JsonSerializer<DslValueObject> {
//		@Override
//		public JsonElement serialize(DslValueObject src, Type typeOfSrc, JsonSerializationContext context) {
//			String err = src.validateAndReturnError();
//			if (err != null) {
//				throw new RuntimeException(err);
//			}
//
//			if (src.value != null) {
//				return new JsonPrimitive(src.value);
//			} else if (src.valueList != null) {
//				JsonArray rval = new JsonArray();
//				for (Number val : src.valueList) {
//					rval.add(val);
//				}
//				return rval;
//			} else {
//				throw new RuntimeException("Transformation not supported!");
//			}
//		}
//	}

//	public static class DslValueObjectMdlDeserializer implements JsonDeserializer<DslValueObject> {
//		@Override
//		public DslValueObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//
//			return null;
//		}
//	}

	public static class DslValueObjectDslSerializer implements JsonSerializer<DslValueObject> {
		@Override
		public JsonElement serialize(DslValueObject src, Type typeOfSrc, JsonSerializationContext context) {
			String err = src.validateAndReturnError();
			if (err != null) {
				throw new RuntimeException(err);
			}

			if (src.value != null) {
				return new JsonPrimitive(src.value);

			} else if (src.valueList != null) {
				JsonArray rval = new JsonArray();
				for (Number val : src.valueList) {
					rval.add(val);
				}
				return rval;

			} else if (src.Min != null || src.Max != null) {
				JsonObject rval = new JsonObject();
				rval.addProperty("Min", src.Min);
				rval.addProperty("Max", src.Max);
				return rval;

			} else {
				throw new RuntimeException("Transformation not supported!");
			}
		}
	}

//	public static class DslValueObjectDslDeserializer implements JsonDeserializer<DslValueObject> {
//		@Override
//		public DslValueObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//			return null;
//		}
//	}
}

