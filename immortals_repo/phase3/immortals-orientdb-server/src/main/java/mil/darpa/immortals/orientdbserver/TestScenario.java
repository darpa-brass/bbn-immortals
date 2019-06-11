package mil.darpa.immortals.orientdbserver;

import com.google.gson.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class TestScenario {

	private final static String DB_NAME_PREFIX = "IMMORTALS_";


	private static TreeMap<String, TestScenario> scenario5TestScenarios;

	private static TreeMap<String, TestScenario> scenario6TestScenarios;

	private static class TestScenarios {
		public final Set<TestScenario> scenarios;

		public TestScenarios(@Nonnull Set<TestScenario> scenarios) {
			this.scenarios = scenarios;
		}
	}

	private static synchronized void init() {
		if (scenario5TestScenarios == null) {
			TestScenarios s5 = new Gson().fromJson(new InputStreamReader(TestScenario.class.getClassLoader().getResourceAsStream("s5_scenarios.json")), TestScenarios.class);
			scenario5TestScenarios = new TreeMap<>(s5.scenarios.stream().peek(x -> x.scenarioType = "Scenario5").collect(Collectors.toMap(x -> x.shortName, x -> x)));
			TestScenarios s6 = new Gson().fromJson(new InputStreamReader(TestScenario.class.getClassLoader().getResourceAsStream("s6_scenarios.json")), TestScenarios.class);
			scenario6TestScenarios = new TreeMap<>(s6.scenarios.stream().peek(x -> x.scenarioType = "Scenario6").collect(Collectors.toMap(x -> x.shortName, x -> x)));
		}
	}

	public static List<String> getScenario5TestScenarioIdentifiers() {
		init();
		return new LinkedList<>(scenario5TestScenarios.keySet());
	}

	public static List<String> getScenario6TestScenarioIdentifiers() {
		init();
		return new LinkedList<>(scenario6TestScenarios.keySet());
	}

	public static TestScenario getScenario5TestScenario(@Nonnull String shortName) {
		init();
		return scenario5TestScenarios.get(shortName);
	}

	public static TestScenario getScenario6TestScenario(@Nonnull String shortName) {
		init();
		return scenario6TestScenarios.get(shortName);
	}

	private final String shortName;
	private final String prettyName;
	private String scenarioType;
	private final String xmlInventoryPath;
	private final String xmlMdlrootInputPath;
	private final String jsonInputPath;
	private final LinkedList<String> expectedStatusSequence;
	private final JsonObject expectedJsonOutputStructure;


	public TestScenario(@Nonnull String shortName, @Nonnull String prettyName, @Nonnull String scenarioType,
	                    @Nullable String xmlInventoryPath, @Nullable String xmlMdlrootInputPath,
	                    @Nullable String jsonInputPath, @Nonnull List<String> expectedStatusSequence,
	                    @Nullable JsonObject expectedJsonOutputStructure) {
		this.shortName = shortName;
		this.prettyName = prettyName;
		this.scenarioType = scenarioType;
		this.xmlInventoryPath = xmlInventoryPath;
		this.xmlMdlrootInputPath = xmlMdlrootInputPath;
		this.jsonInputPath = jsonInputPath;
		this.expectedStatusSequence = new LinkedList<>(expectedStatusSequence);
		this.expectedJsonOutputStructure = expectedJsonOutputStructure;
	}

	public File getBackupFile() {
		return ProvidedTestingData.getTestDatabaseDirectory().resolve(this.shortName + "-backup.zip").toFile();
	}

	public String getDbName() {
		return DB_NAME_PREFIX + shortName;
	}

	public String getShortName() {
		return shortName;
	}

	public String getPrettyName() {
		return prettyName;
	}

	public String getScenarioType() {
		return scenarioType;
	}

	public String getXmlInventoryPath() {
		return xmlInventoryPath;
	}

	public String getXmlMdlrootInputPath() {
		return xmlMdlrootInputPath;
	}

	public String getJsonInputPath() {
		return jsonInputPath;
	}

	public List<String> getExpectedStatusSequence() {
		return expectedStatusSequence;
	}


	private static void recursivelyCompareStructure(@Nonnull JsonElement expectedJsonElement, @Nonnull JsonElement actualJsonElement) throws NestedException {
		if (expectedJsonElement.isJsonObject() && actualJsonElement.isJsonObject()) {
			JsonObject expectedObject = expectedJsonElement.getAsJsonObject();
			JsonObject actualObject = actualJsonElement.getAsJsonObject();

			for (Map.Entry<String, JsonElement> entry : expectedObject.entrySet()) {
				String key = entry.getKey();
				if (!actualObject.has(key)) {
					throw new NestedException("", "Does not contain expected key value '" + key + "'!");
				} else {
					System.out.println(key + "==" + key);
				}
				try {
					JsonElement expectedEntryValue = entry.getValue();
					JsonElement actualEntryValue = actualObject.get(key);
					recursivelyCompareStructure(expectedEntryValue, actualEntryValue);
				} catch (NestedException e) {
					e.addPathParent(key);
					throw e;
				}
			}

		} else if (expectedJsonElement.isJsonPrimitive() && actualJsonElement.isJsonPrimitive()) {
			JsonPrimitive expectedPrimitive = expectedJsonElement.getAsJsonPrimitive();
			JsonPrimitive actualPrimitive = actualJsonElement.getAsJsonPrimitive();

			if (expectedPrimitive.isString() && actualPrimitive.isString()) {
				String expectedString = expectedPrimitive.getAsString();
				String actualString = actualPrimitive.getAsString();
				if (!expectedString.equals(actualString)) {
					throw new NestedException("", "Actual value '" + actualString + "' does not equal expected value '" + expectedString + "'!");
				}

			} else if (expectedPrimitive.isNumber() && actualPrimitive.isNumber()) {
				if (!expectedPrimitive.equals(actualPrimitive)) {
					throw new NestedException("", "Actual value '" + actualPrimitive.getAsNumber() + "' does not equal expected value '" + expectedJsonElement.getAsNumber() + "'!");
				}

			} else if (expectedPrimitive.isBoolean() && actualPrimitive.isBoolean()) {
				if (expectedPrimitive.getAsBoolean() != actualPrimitive.getAsBoolean()) {
					throw new NestedException("", "Actual value '" + actualPrimitive.getAsBoolean() + "' does not equal expected value '" + expectedPrimitive.getAsBoolean() + "'!");
				}

			} else {
				throw new NestedException("", "Primitive values are not of the same type!");
			}


		} else if (expectedJsonElement.isJsonArray() && actualJsonElement.isJsonArray()) {
			JsonArray expectedArray = expectedJsonElement.getAsJsonArray();
			JsonArray actualArray = actualJsonElement.getAsJsonArray();
			if (expectedArray.size() != actualArray.size()) {
				throw new RuntimeException("Actual array size of '" + actualArray.size() + "' is not equal to expected array size of '" + expectedArray.size() + "'!");
			}

		} else if (!(expectedJsonElement.isJsonNull() && actualJsonElement.isJsonNull())) {
			throw new NestedException("", "Json types do not match!");
		}
	}

	public void validateJsonOutputStructure(@Nonnull JsonObject actualJsonOutput) throws NestedException {
		if (expectedJsonOutputStructure != null) {
			recursivelyCompareStructure(expectedJsonOutputStructure, actualJsonOutput);
		}
	}

//	public static void main(String[] args) {
//		try {
//			Gson gson = new Gson();
//
//			JsonReader expectedReader = new JsonReader(new InputStreamReader(TestScenario.class.getClassLoader().getResourceAsStream("json_validation_sample_expected.json")));
//			JsonReader validReader = new JsonReader(new InputStreamReader(TestScenario.class.getClassLoader().getResourceAsStream("json_validation_sample_valid.json")));
//			JsonReader invalidReader = new JsonReader(new InputStreamReader(TestScenario.class.getClassLoader().getResourceAsStream("json_validation_sample_invalid.json")));
//			JsonObject expected = gson.fromJson(expectedReader, JsonObject.class);
//			JsonObject valid = gson.fromJson(validReader, JsonObject.class);
//			JsonObject invalid = gson.fromJson(invalidReader, JsonObject.class);
//			recursivelyCompareStructure(expected, invalid);
//
//			System.out.println("MEH");
//		} catch (NestedException e) {
//			throw new RuntimeException(e);
//		}
//  }
}
