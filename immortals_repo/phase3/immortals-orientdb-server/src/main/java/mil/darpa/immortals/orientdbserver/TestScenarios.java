package mil.darpa.immortals.orientdbserver;

import com.google.gson.*;
import mil.darpa.immortals.EnvironmentConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestScenarios {

	public enum ScenarioType {
		Scenario5bbn(5, false, true),
		Scenario5swri(5, true, false),
		Scenario6bbn(6, false, true),
		Scenario6swri(6, true, false);

		public final boolean isBbn;
		public final boolean isSwri;
		public final boolean isScenario5;
		public final boolean isScenario6;

		ScenarioType(int scenario, boolean isSwri, boolean isBbn) {
			isScenario5 = scenario == 5;
			isScenario6 = scenario == 6;
			this.isSwri = isSwri;
			this.isBbn = isBbn;

		}
	}

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public final List<TestScenario> scenarios = new LinkedList<>();

	private static final TestScenarios internal;

	static {

		ClassLoader cl = TestScenarios.class.getClassLoader();
		List<TestScenario> scenarios = new LinkedList<>();
		scenarios.addAll(initTestScenarios(cl.getResourceAsStream("s5_bbn_scenarios.json")));
		scenarios.addAll(initTestScenarios(cl.getResourceAsStream("s5_bbn_generated_scenarios.json")));
		scenarios.addAll(initTestScenarios(cl.getResourceAsStream("s5_swri_scenarios.json")));
		scenarios.addAll(initTestScenarios(cl.getResourceAsStream("s6_bbn_scenarios.json")));
		scenarios.addAll(initTestScenarios(cl.getResourceAsStream("s6_swri_scenarios.json")));
		internal = new TestScenarios(scenarios);
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

			if (!System.getenv().containsKey(envVar) || System.getenv(envVar).equals("null")) {
				return null;
			}
			envVal = System.getenv(envVar);
			output = output.replace(target, envVal);
		}
		return output;
	}

	private TestScenarios(@Nonnull List<TestScenario> testScenarios) {
		this.scenarios.addAll(testScenarios);
	}

	public TestScenarios(@Nonnull File scenarioFile) {
		try {
			this.scenarios.addAll(initTestScenarios(new FileInputStream(scenarioFile)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<TestScenario> initTestScenarios(@Nonnull InputStream inputStream) {
		LinkedList<TestScenario> scenarios = new LinkedList<>();
		JsonObject testScenariosContainer = gson.fromJson(new InputStreamReader(inputStream), JsonObject.class);

		for (JsonElement scenarioElement : testScenariosContainer.get("scenarios").getAsJsonArray()) {
			for (Map.Entry<String, JsonElement> elementAttributeEntry : scenarioElement.getAsJsonObject().entrySet()) {
				String elementAttributeKey = elementAttributeEntry.getKey();
				JsonElement elementAttributeValue = elementAttributeEntry.getValue();
				if (elementAttributeKey.equals("xmlInventoryPath") || elementAttributeKey.equals("xmlMdlrootInputPath") ||
						elementAttributeKey.equals("updatedXsdInputPath")) {
					String path = injectEnvironmentVariables(elementAttributeValue.getAsString());
					if (path != null) {
						Path p = Paths.get(path);
						if (!Files.exists(p)) {
							if (!path.startsWith("/")) {
								p = EnvironmentConfiguration.getImmortalsRoot().resolve(path);
								if (!Files.exists(p)) {
									throw new RuntimeException("Could not find directory for path '" + path + "'!");
								}
								path = p.toAbsolutePath().toString();
							}
						}
					}
					elementAttributeEntry.setValue(path == null ? JsonNull.INSTANCE : new JsonPrimitive(path));

				}
			}
			TestScenario ts = gson.fromJson(scenarioElement, TestScenario.class);
			scenarios.add(ts);
		}
		return scenarios;
	}

	public static List<TestScenario> filterScenarios(@Nullable Predicate<TestScenario> p1) {
		return filterScenarios(p1, null);
	}

	public static List<TestScenario> filterScenarios(@Nullable Predicate<TestScenario> p1, @Nullable Predicate<TestScenario> p2) {
		Stream<TestScenario> stream = internal.scenarios.stream();
		if (p1 != null) {
			stream = stream.filter(p1);
		}
		if (p2 != null) {
			stream = stream.filter(p2);
		}
		return stream.collect(Collectors.toList());
	}

	public static List<String> filterScenarioNames(@Nullable Predicate<TestScenario> p1) {
		return filterScenarioNames(p1, null);
	}

	public static List<String> filterScenarioNames(@Nullable Predicate<TestScenario> p1, @Nullable Predicate<TestScenario> p2) {
		Stream<TestScenario> stream = internal.scenarios.stream();
		if (p1 != null) {
			stream = stream.filter(p1);
		}
		if (p2 != null) {
			stream = stream.filter(p2);
		}
		return stream.map(TestScenario::getShortName).collect(Collectors.toList());
	}

	public static List<String> getBbnScenario5TestScenarioIdentifiers() {
		return filterScenarioNames(TestScenario::isBbn, TestScenario::isScenario5);
	}

	public static List<TestScenario> getBbnScenario5TestScenarios() {
		return filterScenarios(TestScenario::isBbn, TestScenario::isScenario5);
	}

	public static List<TestScenario> getSwriScenario5TestScenarios() {
		return filterScenarios(TestScenario::isSwri, TestScenario::isScenario5);
	}

	public static List<TestScenario> getAllScenario5TestScenarios() {
		return filterScenarios(TestScenario::isScenario5);
	}

	public static List<String> getAllScenario5TestScenarioIdentifiers() {
		return filterScenarioNames(TestScenario::isScenario5);
	}

	public static List<String> getSwriScenario5TestScenarioIdentifiers() {
		return filterScenarioNames(TestScenario::isSwri, TestScenario::isScenario5);
	}

	public static List<String> getBbnScenario6TestScenarioIdentifiers() {
		return filterScenarioNames(TestScenario::isBbn, TestScenario::isScenario6);
	}

	public static List<TestScenario> getBbnScenario6TestScenarios() {
		return filterScenarios(TestScenario::isBbn, TestScenario::isScenario6);
	}

	public static List<TestScenario> getSwriScenario6TestScenarios() {
		return filterScenarios(TestScenario::isSwri, TestScenario::isScenario6);
	}

	public static List<TestScenario> getAllScenario6TestScenarios() {
		return filterScenarios(TestScenario::isScenario6);
	}

	public static List<String> getAllScenario6TestScenarioIdentifiers() {
		return filterScenarioNames(TestScenario::isScenario6);
	}

	public static List<String> getSwriScenario6TestScenarioIdentifiers() {
		return filterScenarioNames(TestScenario::isSwri, TestScenario::isScenario6);
	}

	public static List<String> getAllTestScenarioIdentifiers() {
		return internal.scenarios.stream().map(TestScenario::getShortName).collect(Collectors.toList());
	}

	public static TestScenario getTestScenario(@Nonnull String shortName) {
		Optional<TestScenario> match = internal.scenarios.stream().filter(x -> x.getShortName().equals(shortName)).findFirst();
		return match.orElse(null);
	}

	public static void main(String[] args) {
		List<TestScenario> scenarios = TestScenarios.getSwriScenario6TestScenarios();
		for (TestScenario scenario : scenarios) {

		}
	}
}
