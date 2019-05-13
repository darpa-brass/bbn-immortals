package mil.darpa.immortals.schemaevolution;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class BBNEvaluationData {

	private static final String BBNEvaluationDataLabel = "BBNEvaluationData";
	private static final String inputJsonDataLabel = "inputJsonData";
	private static final String outputJsonDataLabel = "outputJsonData";
	private static final String currentStateLabel = "currentState";
	private static final String currentStateInfoLabel = "currentStateInfo";

	private String inputJsonData;
	private String outputJsonData;
	private String currentState;
	private String currentStateInfo;

	public String getInputJsonData() {
		return inputJsonData;
	}

	public String getOutputJsonData() {
		return outputJsonData;
	}

	public String getCurrentState() {
		return currentState;
	}

	public String getCurrentStateInfo() {
		return currentStateInfo;
	}

	private String genEntry(@Nonnull String label, @Nullable String value) {
		return "\"" + label + "\": \"" + (value == null ? "null" : value) + "\"";

	}

	public String toJsonString() {
		return "{\n" +
				genEntry("inputJsonData", inputJsonData) + ",\n" +
				genEntry("outputJsonData", outputJsonData) + ",\n" +
				genEntry("currentState", currentState) + ",\n" +
				genEntry("currentStateInfo", currentState) + "\n" +
				"}";
	}

	public static BBNEvaluationData fromFieldMap(@Nonnull Map<String, String> fieldMap) {
		BBNEvaluationData data = new BBNEvaluationData();
		data.inputJsonData = fieldMap.get(inputJsonDataLabel);
		data.outputJsonData = fieldMap.get(outputJsonDataLabel);
		data.currentState = fieldMap.get(currentStateLabel);
		data.currentStateInfo = fieldMap.get(currentStateInfoLabel);
		return data;
	}
}
