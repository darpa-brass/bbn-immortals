package mil.darpa.immortals.flitcons.mdl.validation;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.DuplicateInterface;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DevicePort implements DuplicateInterface<DevicePort> {

	private static final Map<String, Set<String>> validDevicePortMappings = new HashMap<>();

	static {
		// TODO: Unspecified?
		Set<String> inputValues = new HashSet<>();
		inputValues.add("Output");
		inputValues.add("Bidirectional");
		validDevicePortMappings.put("Input", inputValues);

		Set<String> outputValues = new HashSet<>();
		outputValues.add("Input");
		outputValues.add("Bidirectional");
		validDevicePortMappings.put("Output", outputValues);

		Set<String> bidirectionalValues = new HashSet<>();
		bidirectionalValues.add("Bidirectional");
		validDevicePortMappings.put("Bidirectional", bidirectionalValues);
	}


	public final String id;
	public String excitationSource;
	public String direction;

	public DevicePort(@Nonnull String id) {
		this.id = id;
	}

	public void fits(@Nonnull DauPort dauPort, @Nonnull DisplayablePortMapping displayablePortMapping) {
		if (excitationSource == null || excitationSource.equals("Internal")) {
			displayablePortMapping.setExcitationResult("N/A", true);

		} else {
			if (dauPort.excitationPortIsPresent) {
				displayablePortMapping.setExcitationResult("Device --> Port", true);
			} else {
				displayablePortMapping.setExcitationResult("Device -/-> Port", false);
			}
		}

		if (validDevicePortMappings.containsKey(direction)) {
			Set<String> validValues = validDevicePortMappings.get(direction);
			if (validValues.contains(dauPort.direction)) {
				displayablePortMapping.setDirectionResult(direction + " --> " + dauPort.direction, true);

			} else {
				displayablePortMapping.setDirectionResult(direction + " -/-> " + dauPort.direction, false);
			}

		} else {
			throw AdaptationnException.internal("Unexpected direction \"" + direction + "\"!");
		}

	}

	public String toString() {
		return "{" +
				"\"id\": \"" + id + "\", " +
				"\"excitationSource\": " +
				(excitationSource == null ? "null" : ("\"" + excitationSource + "\"")) +
				", " +
				"\"direction\": " +
				(direction == null ? "null" : ("\"" + direction + "\"")) +
				"}";
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DevicePort)) {
			return false;
		} else {
			return toString().equals(o.toString());
		}
	}

	@Override
	public DevicePort duplicate() {
		DevicePort rval = new DevicePort(id);
		rval.excitationSource = excitationSource;
		rval.direction = direction;
		return rval;
	}
}
