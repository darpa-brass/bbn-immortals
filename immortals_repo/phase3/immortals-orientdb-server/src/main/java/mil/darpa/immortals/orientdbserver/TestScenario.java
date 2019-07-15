package mil.darpa.immortals.orientdbserver;

import com.google.gson.*;
import mil.darpa.immortals.EnvironmentConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

	private static String injectEnvironmentVariables(@Nonnull String input) {
		String output = input;

		int idx0;
		int idx1;
		String target;
		String envVar;
		String envVal;
		while (output.contains("${")) {
			idx0 = output.indexOf("${");
			idx1 = output.indexOf("}", idx0);
			target = output.substring(idx0, idx1 + 1);
			envVar = output.substring(idx0 + 2, idx1);

			if (System.getenv(envVar) == null) {
				throw new RuntimeException("The environment variable '" + envVar + "' must be set in order to process the scenario entries!");
			}
			envVal = System.getenv(envVar);

			output = output.replace(target, envVal);
		}
		return output;
	}

	private static TreeMap<String, TestScenario> initScenarioSet(@Nonnull String inputResource, @Nonnull Gson gson) {
		TreeMap<String, TestScenario> rval = new TreeMap<>();
		JsonObject testScenariosContainer = gson.fromJson(new InputStreamReader(TestScenario.class.getClassLoader().getResourceAsStream(inputResource)), JsonObject.class);

		for (JsonElement scenarioElement : testScenariosContainer.get("scenarios").getAsJsonArray()) {
			String scenarioType = "Scenario6";
			for (Map.Entry<String, JsonElement> elementAttributeEntry : scenarioElement.getAsJsonObject().entrySet()) {
				String elementAttributeKey = elementAttributeEntry.getKey();
				JsonElement elementAttributeValue = elementAttributeEntry.getValue();
				if (elementAttributeKey.equals("xmlInventoryPath") || elementAttributeKey.equals("xmlMdlrootInputPath")) {
					elementAttributeEntry.setValue(new JsonPrimitive(injectEnvironmentVariables(elementAttributeValue.getAsString())));
					scenarioType = "Scenario5";
				}
			}
			scenarioElement.getAsJsonObject().addProperty("scenarioType", scenarioType);
			TestScenario ts = gson.fromJson(scenarioElement, TestScenario.class);
			rval.put(ts.shortName, ts);
		}
		return rval;
	}

	private static synchronized void init() {
		if (scenario5TestScenarios == null) {
			Gson gson = new Gson();

			scenario5TestScenarios = initScenarioSet("s5_scenarios.json", gson);
			if (System.getenv().containsKey(EnvironmentConfiguration.CHALLENGE_PROBLEMS_ROOT.envVar)) {
				scenario5TestScenarios.putAll(initScenarioSet("s5_cp_scenarios.json", gson));
			}

			scenario6TestScenarios = initScenarioSet("s6_scenarios.json", gson);
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

	public static List<String> getAllTestScenarioIdentifiers() {
		init();
		LinkedList<String> rval = new LinkedList<>(scenario5TestScenarios.keySet());
		rval.addAll(scenario6TestScenarios.keySet());
		return rval;
	}

	public static TestScenario getScenario5TestScenario(@Nonnull String shortName) {
		init();
		return scenario5TestScenarios.get(shortName);
	}

	public static TestScenario getScenario6TestScenario(@Nonnull String shortName) {
		init();
		return scenario6TestScenarios.get(shortName);
	}

	public static Path getPathInParentsIfExists(@Nonnull String desiredPath) {
		Path result = null;
		Path cwd = Paths.get("").toAbsolutePath();
		while (!cwd.toString().equals("/")) {
			result = cwd.resolve(desiredPath);
			if (Files.exists(cwd.resolve("shared/tools.sh"))) {
				break;
			}
			result = null;
			cwd = cwd.getParent();
		}
		return result;
	}

	private final String shortName;
	private final String prettyName;
	private String scenarioType;
	private final int timeoutMS;
	private final String xmlInventoryPath;
	private final String xmlMdlrootInputPath;
	private final String jsonInputPath;
	private final LinkedList<String> expectedStatusSequence;
	private final JsonObject expectedJsonOutputStructure;


	public TestScenario(@Nonnull String shortName, @Nonnull String prettyName, @Nonnull String scenarioType,
	                    @Nonnull int timeoutMS, @Nullable String xmlInventoryPath, @Nullable String xmlMdlrootInputPath,
	                    @Nullable String jsonInputPath, @Nonnull List<String> expectedStatusSequence,
	                    @Nullable JsonObject expectedJsonOutputStructure) {
		this.shortName = shortName;
		this.prettyName = prettyName;
		this.scenarioType = scenarioType;
		this.timeoutMS = timeoutMS;
		this.xmlInventoryPath = xmlInventoryPath;
		this.xmlMdlrootInputPath = xmlMdlrootInputPath;
		this.jsonInputPath = jsonInputPath;
		this.expectedStatusSequence = new LinkedList<>(expectedStatusSequence);
		this.expectedJsonOutputStructure = expectedJsonOutputStructure;
	}

	public InputStream getBackupInputStream() {
		return TestScenario.class.getClassLoader().getResourceAsStream("test_databases/" + this.shortName + "-backup.zip");
	}

	public InputStream getInputJsonData() {
		InputStream inputJsonData;
		Path dataPath;
		if ((inputJsonData = TestScenario.class.getClassLoader().getResourceAsStream("inputJsonData/" + this.shortName + ".json")) != null) {
			return inputJsonData;
		} else if (getJsonInputPath() != null && (dataPath = getPathInParentsIfExists(getJsonInputPath())) != null) {
			try {
				return new FileInputStream(dataPath.toFile());
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
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

	public int getTimeoutMS() {
		return timeoutMS;
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
}
