package mil.darpa.immortals.flitcons.solvers.dsl;

import com.google.gson.*;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DslPort {

	public final String GloballyUniqueId;

	/**
	 * The direction of the data flow in the port
	 */
	public final DslPortDirection PortDirection;

	public final String SupersededPortId;

	public final String Extension;

	public final String BBNPortFunctionality;

	/**
	 * The port type
	 */
	public final DslPortType PortType;

	public final DslValueObject SampleRate;

	public final DslValueObject DataLength;

	public final DslValueObject DataRate;

	DslPort(String globallyUniqueId, String bbnPortFunctionality, DslPortDirection portDirection, String supersededPortId,
			String extension, DslPortType portType, DslValueObject sampleRate, DslValueObject dataLength,
			DslValueObject dataRate) {
		this.GloballyUniqueId = globallyUniqueId;
		this.PortDirection = portDirection;
		this.SupersededPortId = supersededPortId;
		this.Extension = extension;
		this.BBNPortFunctionality = bbnPortFunctionality;
		this.PortType = portType;
		this.SampleRate = sampleRate;
		this.DataLength = dataLength;
		this.DataRate = dataRate;
	}

	public static class DslPortMdlDeserializer implements JsonDeserializer<DslPort> {

		private static DslValueObject getDslValueObject(@Nonnull JsonObject parentObject, @Nonnull String rootValue) {
			Integer max = parentObject.has("BBNMax" + rootValue) ? parentObject.get("BBNMax" + rootValue).getAsInt() : null;
			Integer min = parentObject.has("BBNMin" + rootValue) ? parentObject.get("BBNMin" + rootValue).getAsInt() : null;

			Integer value = null;
			Integer[] valueArray = null;

			if (parentObject.has(rootValue)) {
				JsonElement e = parentObject.get(rootValue);
				if (e.isJsonPrimitive()) {
					JsonPrimitive ePrimative = e.getAsJsonPrimitive();

					if (!ePrimative.isNumber()) {
						throw new RuntimeException("Non-number primitives not yet supported!");
					}
					value = ePrimative.getAsInt();

				} else if (e.isJsonArray()) {
					List<Integer> valueList = new LinkedList<>();

					Iterator<JsonElement> iter = e.getAsJsonArray().iterator();

					while (iter.hasNext()) {
						JsonPrimitive ePrimative = iter.next().getAsJsonPrimitive();

						if (!ePrimative.isNumber()) {
							throw new RuntimeException("Non-number primitives not yet supported!");
						}

						valueList.add(ePrimative.getAsInt());
					}
					valueArray = (Integer[]) valueList.toArray();
				}
			}

			DslValueObject rval = new DslValueObject(min, max, valueArray, value);
			parentObject.remove(rootValue);
			return rval;
		}

		@Override
		public DslPort deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject in = (JsonObject) json;

			String portFunctionality = in.get("BBNPortFunctionality").getAsString();
			DslValueObject sampleRate = getDslValueObject(in, "SampleRate");
			DslValueObject dataLength = getDslValueObject(in, "DataLength");
			DslValueObject dataRate = getDslValueObject(in, "DataRate");

			DslPortType pt;

			if (in.has("PortType")) {
				pt = DslPortType.valueOf(in.get("PortType").getAsString());
			} else if (in.has("Thermocouple")) {
				pt = DslPortType.Thermocouple;
			} else {
				throw new RuntimeException("Cannot determine port type for node with identifier '" + in.get("GloballyUniqueId") + "'!");
			}

			return new DslPort(
					in.get("GloballyUniqueId").getAsString(),
					portFunctionality,
					DslPortDirection.valueOf(in.get("PortDirection").getAsString()),
					null,
					in.has("Thermocouple") ? in.get("Thermocouple").getAsString() : null,
					pt,
					sampleRate,
					dataLength,
					dataRate
			);
		}
	}
}
