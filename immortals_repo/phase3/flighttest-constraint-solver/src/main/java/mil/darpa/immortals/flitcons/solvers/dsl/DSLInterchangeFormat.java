package mil.darpa.immortals.flitcons.solvers.dsl;


import com.google.gson.*;
import mil.darpa.immortals.flitcons.datatypes.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.ScenarioData;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

public class DSLInterchangeFormat {

	private static final Pattern MAP_PATTERN = Pattern.compile("^(?<field>[a-zA-Z0.9]*)\\['(?<identifier>.*)'\\]$");

	private static final String ID_FIELD_NAME = "vertexId";

	/**
	 * All the DAUs that are relevant to the adaptation
	 */
	public List<DslDau> daus = new LinkedList<>();


	private static JsonArray produceJsonElement(Set<HierarchicalData> data) {
		JsonArray target = new JsonArray();
		boolean containsData = false;

		for (HierarchicalData source : data) {
			JsonElement value = produceJsonElement(source);
			if (value != null) {
				target.add(value);
				containsData = true;
			}
		}

		if (containsData) {
			return target;
		} else {
			return null;
		}
	}

	private static JsonObject produceJsonElement(HierarchicalData source) {
		JsonObject target = new JsonObject();
		boolean containsData = false;


		for (Map.Entry<String, Object> entry : source.getAttributes().entrySet()) {
			String label = entry.getKey();
			Object value = entry.getValue();

			if (value instanceof String) {
				target.addProperty(label, (String) value);
				containsData = true;

			} else if (value instanceof Number) {
				target.addProperty(label, (Number) value);
				containsData = true;

			} else if (value instanceof Boolean) {
				target.addProperty(label, (Boolean) value);
				containsData = true;
			} else {
				throw new RuntimeException("Unsupported attribute type '" + value.getClass().toString() + "'!");
			}
		}

		for (Map.Entry<String, Set<HierarchicalData>> childrenEntry : source.getChildNodeMap().entrySet()) {
			JsonElement value = produceJsonElement(childrenEntry.getValue());
			if (value != null) {
				target.add(childrenEntry.getKey(), value);
				containsData = true;
			}
		}
		if (containsData) {
			return target;
		} else {
			return null;
		}
	}


	public static DSLInterchangeFormat createFromScenarioData(ScenarioData scenarioData) {
		DSLInterchangeFormat rval;
		JsonObject target = new JsonObject();
		JsonArray daus = new JsonArray();
		target.add("daus", daus);
		for (HierarchicalData dau : scenarioData.getDauNodeData().getDauRootNodes()) {
			daus.add(produceJsonElement(dau));
		}

		Gson gson = new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(DslPort.class, new DslPort.DslPortMdlDeserializer())
				.registerTypeAdapter(DslValueObject.class, new DslValueObject.DslValueObjectDslSerializer())
				.create();

		try {
			String json = gson.toJson(target);
			FileUtils.writeStringToFile(new File("IntermediateDslInput.json"), json, Charset.defaultCharset());
			rval = gson.fromJson(json, DSLInterchangeFormat.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return rval;

	}

	public void writeToDslInputFile(@Nonnull File targetFilepath) {
		Gson gson = new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(DslPort.class, new DslPort.DslPortMdlDeserializer())
				.registerTypeAdapter(DslValueObject.class, new DslValueObject.DslValueObjectDslSerializer())
				.create();

		try {
			FileUtils.writeStringToFile(targetFilepath, gson.toJson(this), Charset.defaultCharset());

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
