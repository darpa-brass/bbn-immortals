package mil.darpa.immortals.orientdbserver;

import com.google.gson.*;
import org.apache.tools.ant.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class TestScenario {

	private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private final static String DB_NAME_PREFIX = "IMMORTALS_";

	private final static Logger logger = LoggerFactory.getLogger(TestScenario.class);

	public static class Scenario6InputData {
		public final String initialMdlVersion;
		public final String updatedMdlVersion;
		public final String updatedMdlSchema;

		public Scenario6InputData(@Nonnull String initialMdlVersion, @Nullable String updatedMdlVersion, @Nullable String updatedMdlSchema) {
			this.initialMdlVersion = initialMdlVersion;
			this.updatedMdlVersion = updatedMdlVersion;
			this.updatedMdlSchema = updatedMdlSchema;
		}
	}

	private final String shortName;
	private final String prettyName;
	private final String scenarioType;
	private final int timeoutMS;
	private final String xmlInventoryPath;
	private final String xmlMdlrootInputPath;
	private final String ingestedXmlInventoryHash;
	private final String ingestedXmlMdlrootInputHash;
	private final String initialXsdVersion;
	private final String updatedXsdVersion;
	private final String updatedXsdInputPath;
	private final String updatedXsdInputPathHash;
	private final LinkedList<String> expectedStatusSequence;
	private final JsonObject expectedJsonOutputStructure;
	private final List<Set<String>> expectedDauSelections;

	private transient TestScenarios.ScenarioType _scenarioType;


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

	public TestScenario(@Nonnull String shortName, @Nonnull String prettyName, @Nonnull String scenarioType,
	                    @Nonnull int timeoutMS, @Nullable String xmlInventoryPath, @Nullable String xmlMdlrootInputPath,
	                    @Nonnull String ingestedXmlInventoryHash, @Nonnull String ingestedXmlMdlrootInputHash,
	                    @Nonnull String initialXsdVersion, @Nonnull String updatedXsdVersion,
	                    @Nonnull String updatedXsdInputPath, @Nonnull String updatedXsdInputPathHash,
	                    @Nonnull List<String> expectedStatusSequence, @Nullable JsonObject expectedJsonOutputStructure,
	                    @Nullable List<Set<String>> expectedDauSelections) {
		this.shortName = shortName;
		this.scenarioType = scenarioType;
		this.prettyName = prettyName;
		this.timeoutMS = timeoutMS;
		this.xmlInventoryPath = xmlInventoryPath;
		this.xmlMdlrootInputPath = xmlMdlrootInputPath;
		this.ingestedXmlInventoryHash = ingestedXmlInventoryHash;
		this.ingestedXmlMdlrootInputHash = ingestedXmlMdlrootInputHash;
		this.initialXsdVersion = initialXsdVersion;
		this.updatedXsdVersion = updatedXsdVersion;
		this.updatedXsdInputPath = updatedXsdInputPath;
		this.updatedXsdInputPathHash = updatedXsdInputPathHash;
		this.expectedStatusSequence = new LinkedList<>(expectedStatusSequence);
		this.expectedJsonOutputStructure = expectedJsonOutputStructure;
		this.expectedDauSelections = expectedDauSelections;
	}

	public synchronized TestScenarios.ScenarioType getScenarioType() {
		if (_scenarioType == null) {
			_scenarioType = TestScenarios.ScenarioType.valueOf(scenarioType);
		}
		return _scenarioType;
	}

	public boolean isSwri() {
		return getScenarioType().isSwri;
	}

	public boolean isBbn() {
		return getScenarioType().isBbn;
	}

	public boolean isScenario5() {
		return getScenarioType().isScenario5;
	}

	public boolean isScenario6() {
		return getScenarioType().isScenario6;
	}

	public InputStream getBackupInputStream() {
		InputStream option1 = TestScenarios.class.getClassLoader().getResourceAsStream("test_databases/generated/" + this.shortName + "-backup.zip");
		if (option1 != null) {
			return option1;
		}
		return TestScenarios.class.getClassLoader().getResourceAsStream("test_databases/" + this.shortName + "-backup.zip");
	}

	public boolean hasBackup() {
		return getBackupInputStream() != null;
	}

	public String getInputJsonDataString() {
		try {
			Scenario6InputData data = new Scenario6InputData(
					initialXsdVersion,
					updatedXsdVersion,
					FileUtils.readFully(new InputStreamReader(new FileInputStream(updatedXsdInputPath)))
			);
			return gson.toJson(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

	public int getTimeoutMS() {
		return timeoutMS;
	}

	public File getXmlInventoryPath() {
		if (xmlInventoryPath == null) {
			throw new RuntimeException("XML Inventory is null!");
		}
		File f = new File(xmlInventoryPath);
		if (!f.exists()) {
			throw new RuntimeException("The XML Inventory file '" + f.toString() + "' does not exist!");
		}
		return f;
	}

	public File getXmlMdlrootInputPath() {
		if (xmlMdlrootInputPath == null) {
			throw new RuntimeException("XML MDLRoot is null!");
		}
		File f = new File(xmlMdlrootInputPath);
		if (!f.exists()) {
			throw new RuntimeException("The XML MDLRoot file '" + f.toString() + "' does not exist!");
		}
		return f;
	}

	public File getUpdatedXsdInputPath() {
		if (updatedXsdInputPath == null) {
			throw new RuntimeException("Updated XSD is null!");
		}
		File f = new File(updatedXsdInputPath);
		if (!f.exists()) {
			throw new RuntimeException("The Updated XSD file '" + f.toString() + "' does not exist!");
		}
		return f;
	}

	public boolean hasXmlInventoryInput() {
		return xmlInventoryPath != null;
	}

	public boolean hasUpdatedXsdInputPath() {
		return updatedXsdInputPath != null;
	}

	public boolean hasXmlMdlrootInput() {
		return xmlMdlrootInputPath != null;

	}

//	public String getJsonInputPath() {
//		return jsonInputPath;
//	}

	public List<String> getExpectedStatusSequence() {
		return expectedStatusSequence;
	}

	private static boolean fileIsOutdated(@Nonnull File file, @Nonnull String knownHash) {
		try {
			String fileData = FileUtils.readFully(new FileReader(file));

			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(fileData.getBytes(StandardCharsets.UTF_8));

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			String currentHash = hexString.toString();

			return !currentHash.equals(knownHash);
		} catch (IOException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean backupIsOutdated() {
		if (!(hasXmlMdlrootInput() || hasXmlInventoryInput() || hasUpdatedXsdInputPath())) {
			throw new RuntimeException("Cannot validate backup since no source files can be found!");
		}

		return ((hasXmlMdlrootInput() && fileIsOutdated(getXmlMdlrootInputPath(), ingestedXmlMdlrootInputHash)) ||
				(hasXmlInventoryInput() && fileIsOutdated(getXmlInventoryPath(), ingestedXmlInventoryHash)) ||
				(hasUpdatedXsdInputPath() && fileIsOutdated(getUpdatedXsdInputPath(), updatedXsdInputPathHash)) ||
				(!hasBackup()));
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
					logger.info(key + "==" + key);
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
